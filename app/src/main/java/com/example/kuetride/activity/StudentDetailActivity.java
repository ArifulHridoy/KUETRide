package com.example.kuetride.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String studentId, studentName;
    private Spinner spinnerRoute, spinnerBus;
    private List<Map<String, String>> routes = new ArrayList<>();
    private List<Map<String, String>> buses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        studentId = getIntent().getStringExtra("studentId");
        String rollNumber = getIntent().getStringExtra("rollNumber");
        studentName = getIntent().getStringExtra("studentName");

        TextView tvRoll = findViewById(R.id.tvRollNumber);
        tvRoll.setText("Student: " + studentName + " (Roll: " + rollNumber + ")");

        spinnerRoute = findViewById(R.id.spinnerRoute);
        spinnerBus = findViewById(R.id.spinnerBus);
        MaterialButton btnAssign = findViewById(R.id.btnAssign);

        loadRoutes();
        loadBuses();

        btnAssign.setOnClickListener(v -> assignStudent());
    }

    private void loadRoutes() {
        db.collection("routes").get().addOnSuccessListener(snap -> {
            routes.clear();
            List<String> routeNames = new ArrayList<>();
            routeNames.add("Select Route");
            routes.add(new HashMap<>()); // Empty entry for "Select Route"
            
            for (QueryDocumentSnapshot d : snap) {
                Map<String, String> routeMap = new HashMap<>();
                routeMap.put("id", d.getId());
                routeMap.put("name", d.getString("name"));
                routes.add(routeMap);
                routeNames.add(d.getString("name") + " (" + d.getString("start") + "-" + d.getString("end") + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRoute.setAdapter(adapter);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load routes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadBuses() {
        db.collection("buses").get().addOnSuccessListener(snap -> {
            buses.clear();
            List<String> busNames = new ArrayList<>();
            busNames.add("Select Bus");
            buses.add(new HashMap<>()); // Empty entry for "Select Bus"
            
            for (QueryDocumentSnapshot d : snap) {
                Map<String, String> busMap = new HashMap<>();
                busMap.put("id", d.getId());
                busMap.put("number", d.getString("number"));
                buses.add(busMap);
                busNames.add(d.getString("number") + " (Cap: " + d.getLong("capacity") + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, busNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBus.setAdapter(adapter);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load buses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void assignStudent() {
        int routePos = spinnerRoute.getSelectedItemPosition();
        int busPos = spinnerBus.getSelectedItemPosition();

        if (routePos <= 0 || busPos <= 0) {
            Toast.makeText(this, "Select both route and bus", Toast.LENGTH_SHORT).show();
            return;
        }

        String routeId = routes.get(routePos).get("id");
        String busId = buses.get(busPos).get("id");

        Map<String, Object> data = new HashMap<>();
        data.put("assignedRoute", routeId);
        data.put("assignedBus", busId);

        db.collection("users").document(studentId).update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student assigned successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to assign: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
