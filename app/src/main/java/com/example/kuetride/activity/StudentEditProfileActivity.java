package com.example.kuetride.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentEditProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String studentDocId;
    private EditText etName, etDepartment, etRollNumber, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_edit_profile);
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etDepartment = findViewById(R.id.etDepartment);
        etRollNumber = findViewById(R.id.etRollNumber);
        etPhone = findViewById(R.id.etPhone);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").whereEqualTo("uid", uid).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        studentDocId = doc.getId();
                        etName.setText(doc.getString("username"));
                        etDepartment.setText(doc.getString("department"));
                        etRollNumber.setText(doc.getString("rollNumber"));
                        etPhone.setText(doc.getString("phone"));
                    }
                });
    }

    private void saveProfile() {
        if (studentDocId == null) {
            Toast.makeText(this, "Could not save profile. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String roll = etRollNumber.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("department", department);
        updates.put("rollNumber", roll);
        updates.put("phone", phone);

        db.collection("users").document(studentDocId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show());
    }
}
