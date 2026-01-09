package com.example.kuetride.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.example.kuetride.firebase.FirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEt, passwordEt;
    private MaterialButton loginBtn;
    private TextView registerLink;
    private ProgressBar progress;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.btnGoRegister);
        progress = findViewById(R.id.progress);

        if (auth.getCurrentUser() != null) {
            navigateByRole();
        }

        loginBtn.setOnClickListener(v -> doLogin());
        registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = emailEt.getText().toString().trim();
        String pass = passwordEt.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            progress.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                navigateByRole();
            } else {
                Toast.makeText(this, String.valueOf(task.getException().getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateByRole() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        FirebaseService.getInstance().fetchRole(uid, new FirebaseService.RoleCallback() {
            @Override public void onResult(String role) {
                Intent intent = "admin".equals(role) ? new Intent(LoginActivity.this, AdminDashboardActivity.class)
                        : new Intent(LoginActivity.this, StudentDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Failed to fetch role", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
