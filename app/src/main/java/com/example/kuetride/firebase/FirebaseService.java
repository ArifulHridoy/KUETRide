package com.example.kuetride.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class FirebaseService {
    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) instance = new FirebaseService();
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }

    public CollectionReference users() { return db.collection("users"); }

    public Task<Void> createStudentUser(String uid, String email, String username, String rollNumber, String department, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("username", username);
        user.put("role", "student");
        user.put("email", email);
        user.put("rollNumber", rollNumber);
        user.put("department", department);
        user.put("phone", phone);
        user.put("assignedRoute", "");
        user.put("assignedBus", "");
        return users().document(rollNumber).set(user);
    }

    public interface RoleCallback { void onResult(String role); void onError(Exception e); }

    public void fetchRole(String uid, RoleCallback cb) {
        users().whereEqualTo("uid", uid).limit(1).get().addOnSuccessListener(snap -> {
            if (!snap.isEmpty()) {
                String role = snap.getDocuments().get(0).getString("role");
                cb.onResult(role != null ? role : "student");
            } else {
                // Check if the user is the admin
                if ("admin@kuetride.com".equals(auth.getCurrentUser().getEmail())) {
                    cb.onResult("admin");
                } else {
                    cb.onResult("student"); // Default to student if not found
                }
            }
        }).addOnFailureListener(cb::onError);
    }
}
