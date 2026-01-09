package com.example.kuetride.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.example.kuetride.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEt, passwordEt, usernameEt, rollNumberEt, departmentEt, phoneEt;
    private Button registerBtn;
    private ProgressBar progress;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        usernameEt = findViewById(R.id.etUsername);
        rollNumberEt = findViewById(R.id.etRollNumber);
        departmentEt = findViewById(R.id.etDepartment);
        phoneEt = findViewById(R.id.etPhone);
        registerBtn = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        registerBtn.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String email = emailEt.getText().toString().trim();
        String pass = passwordEt.getText().toString();
        String username = usernameEt.getText().toString().trim();
        String rollNumber = rollNumberEt.getText().toString().trim();
        String department = departmentEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(username) || TextUtils.isEmpty(rollNumber) || TextUtils.isEmpty(department) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                FirebaseService.getInstance().createStudentUser(uid, email, username, rollNumber, department, phone)
                        .addOnSuccessListener(aVoid -> {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show();
                        });
            } else {
                progress.setVisibility(View.GONE);
                Toast.makeText(this, String.valueOf(task.getException().getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }
}
