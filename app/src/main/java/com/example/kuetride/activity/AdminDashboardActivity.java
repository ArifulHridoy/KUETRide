package com.example.kuetride.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Button Listeners
        findViewById(R.id.btnManageStudents).setOnClickListener(v -> startActivity(new Intent(this, StudentManagementActivity.class)));
        findViewById(R.id.btnGenerateSchedule).setOnClickListener(v -> startActivity(new Intent(this, ScheduleGenerationActivity.class)));
        findViewById(R.id.btnManageRoutes).setOnClickListener(v -> startActivity(new Intent(this, RouteManagementActivity.class)));
        findViewById(R.id.btnManageBuses).setOnClickListener(v -> startActivity(new Intent(this, BusManagementActivity.class)));
        findViewById(R.id.btnViewRequests).setOnClickListener(v -> startActivity(new Intent(this, RequestsActivity.class)));
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> startActivity(new Intent(this, MessageActivity.class)));
        findViewById(R.id.btnViewSchedules).setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));

        loadCounts();
    }

    private void loadCounts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView tvStudents = findViewById(R.id.tvStudents);
        TextView tvBuses = findViewById(R.id.tvBuses);
        TextView tvRoutes = findViewById(R.id.tvRoutes);
        TextView tvRequests = findViewById(R.id.tvRequests);

        db.collection("users").whereEqualTo("role", "student").get().addOnSuccessListener(snap -> tvStudents.setText(String.valueOf(snap.size())));
        db.collection("buses").get().addOnSuccessListener(snap -> tvBuses.setText(String.valueOf(snap.size())));
        db.collection("routes").get().addOnSuccessListener(snap -> tvRoutes.setText(String.valueOf(snap.size())));
        // Correctly count only PENDING requests
        db.collection("requests").whereEqualTo("status", "PENDING").get().addOnSuccessListener(snap -> tvRequests.setText(String.valueOf(snap.size())));
    }
}
