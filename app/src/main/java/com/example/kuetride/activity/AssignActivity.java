package com.example.kuetride.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.example.kuetride.model.Bus;
import com.example.kuetride.model.Route;
import com.example.kuetride.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AssignActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private final List<User> students = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private final List<Bus> buses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign);
        db = FirebaseFirestore.getInstance();

        Spinner studentSpinner = findViewById(R.id.spnStudent);
        Spinner routeSpinner = findViewById(R.id.spnRoute);
        Spinner busSpinner = findViewById(R.id.spnBus);
        MaterialButton assignBtn = findViewById(R.id.btnAssign);

        loadStudents(studentSpinner);
        loadRoutes(routeSpinner);
        loadBuses(busSpinner);

        assignBtn.setOnClickListener(v -> {
            User selectedStudent = students.get(studentSpinner.getSelectedItemPosition());
            Route selectedRoute = routes.get(routeSpinner.getSelectedItemPosition());
            Bus selectedBus = buses.get(busSpinner.getSelectedItemPosition());

            db.collection("users").document(selectedStudent.getId())
                    .update("assignedRoute", selectedRoute.getName(), "assignedBus", selectedBus.getNumber())
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Assigned", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
        });
    }

    private void loadStudents(Spinner spinner) {
        db.collection("users").whereEqualTo("role", "student").get().addOnSuccessListener(snap -> {
            students.clear();
            List<String> studentNames = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                User u = new User(d.getId(), d.getString("username"), d.getString("email"), d.getString("department"), d.getString("assignedRoute"), d.getString("assignedBus"));
                students.add(u);
                studentNames.add(u.getUsername());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        });
    }

    private void loadRoutes(Spinner spinner) {
        db.collection("routes").get().addOnSuccessListener(snap -> {
            routes.clear();
            List<String> routeNames = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Route r = new Route(d.getId(), d.getString("name"), d.getString("start"), d.getString("end"));
                routes.add(r);
                routeNames.add(r.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        });
    }

    private void loadBuses(Spinner spinner) {
        db.collection("buses").get().addOnSuccessListener(snap -> {
            buses.clear();
            List<String> busNumbers = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Bus b = new Bus(d.getId(), d.getString("number"), d.getLong("capacity").intValue());
                buses.add(b);
                busNumbers.add(b.getNumber());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, busNumbers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        });
    }
}
