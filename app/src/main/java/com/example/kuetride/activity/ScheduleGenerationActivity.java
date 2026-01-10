package com.example.kuetride.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleGenerationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Spinner spinnerRoute, spinnerBus;
    private EditText etDate, etTime;
    private List<String> routeIds = new ArrayList<>();
    private List<String> busIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_generation);
        db = FirebaseFirestore.getInstance();

        spinnerRoute = findViewById(R.id.spinnerRoute);
        spinnerBus = findViewById(R.id.spinnerBus);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        MaterialButton btnGenerate = findViewById(R.id.btnGenerate);

        loadRoutes();
        loadBuses();

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnGenerate.setOnClickListener(v -> generateSchedule());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            etDate.setText(String.format("%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth));
        }, year, month, day).show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            etTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
        }, hour, minute, false).show();
    }

    private void loadRoutes() {
        db.collection("routes").get().addOnSuccessListener(snap -> {
            routeIds.clear();
            List<String> routeNames = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                routeIds.add(d.getId());
                routeNames.add(d.getString("name"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRoute.setAdapter(adapter);
        });
    }

    private void loadBuses() {
        db.collection("buses").get().addOnSuccessListener(snap -> {
            busIds.clear();
            List<String> busNumbers = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                busIds.add(d.getId());
                busNumbers.add(d.getString("number"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, busNumbers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBus.setAdapter(adapter);
        });
    }

    private void generateSchedule() {
        int routePos = spinnerRoute.getSelectedItemPosition();
        int busPos = spinnerBus.getSelectedItemPosition();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();

        if (routePos < 0 || busPos < 0 || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String routeId = routeIds.get(routePos);
        String busId = busIds.get(busPos);

        db.collection("users")
                .whereEqualTo("assignedRoute", routeId)
                .whereEqualTo("assignedBus", busId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> studentIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        studentIds.add(document.getId());
                    }

                    Map<String, Object> schedule = new HashMap<>();
                    schedule.put("routeId", routeId);
                    schedule.put("busId", busId);
                    schedule.put("date", date);
                    schedule.put("time", time);
                    schedule.put("students", studentIds);

                    db.collection("schedules").add(schedule)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(ScheduleGenerationActivity.this, "Schedule generated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ScheduleGenerationActivity.this, "Failed to generate schedule", Toast.LENGTH_SHORT).show());
                });
    }
}
