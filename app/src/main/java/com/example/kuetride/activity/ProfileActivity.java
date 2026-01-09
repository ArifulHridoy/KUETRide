package com.example.kuetride.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetride.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView usernameTv = findViewById(R.id.tvUsername);
        TextView emailTv = findViewById(R.id.tvEmail);
        TextView roleTv = findViewById(R.id.tvRole);
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(snap -> {
            String username = snap.getString("username");
            String role = snap.getString("role");
            String email = snap.getString("email");
            usernameTv.setText(username);
            emailTv.setText(email);
            roleTv.setText(role);
        });
    }
}
