package com.example.kuetride.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Student;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends AppCompatActivity {
    private final List<Student> allStudents = new ArrayList<>();
    private final List<Student> filteredStudents = new ArrayList<>();
    private StudentListAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        EditText searchEt = findViewById(R.id.etSearch);
        RecyclerView rvStudents = findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentListAdapter(filteredStudents, student -> {
            Intent intent = new Intent(this, StudentDetailActivity.class);
            intent.putExtra("studentId", student.getUserId());
            intent.putExtra("rollNumber", student.getRollNumber());
            intent.putExtra("studentName", student.getName());
            startActivityForResult(intent, 100);
        });
        rvStudents.setAdapter(adapter);

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadStudents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh student list after assignment
            loadStudents();
            Toast.makeText(this, "Student assigned successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudents() {
        db.collection("users").whereEqualTo("role", "student").get().addOnSuccessListener(snap -> {
            allStudents.clear();
            for (QueryDocumentSnapshot d : snap) {
                Student s = new Student();
                s.setUserId(d.getId());
                s.setName(d.getString("username"));
                s.setDepartment(d.getString("department"));
                s.setRollNumber(d.getString("rollNumber"));
                s.setPhone(d.getString("phone"));
                s.setAssignedRoute(d.getString("assignedRoute"));
                s.setAssignedBus(d.getString("assignedBus"));
                allStudents.add(s);
            }
            filterStudents("");
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load students: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterStudents(String query) {
        filteredStudents.clear();
        if (query.isEmpty()) {
            filteredStudents.addAll(allStudents);
        } else {
            String q = query.toLowerCase();
            for (Student s : allStudents) {
                if ((s.getName() != null && s.getName().toLowerCase().contains(q)) ||
                    (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(q)) ||
                    (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q))) {
                    filteredStudents.add(s);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    static class StudentListAdapter extends RecyclerView.Adapter<StudentViewHolder> {
        interface OnStudentClick { void onClick(Student student); }
        private final List<Student> items;
        private final OnStudentClick listener;

        StudentListAdapter(List<Student> items, OnStudentClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StudentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvRoll, tvDept, tvRoute, tvBus;
        private final MaterialCardView card;

        StudentViewHolder(android.view.View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRoll = itemView.findViewById(R.id.tvRoll);
            tvDept = itemView.findViewById(R.id.tvDept);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvBus = itemView.findViewById(R.id.tvBus);
            card = itemView.findViewById(R.id.card);
        }

        void bind(Student s, StudentListAdapter.OnStudentClick listener) {
            tvName.setText(s.getName() != null ? s.getName() : "N/A");
            tvRoll.setText("Roll: " + (s.getRollNumber() != null ? s.getRollNumber() : "N/A"));
            tvDept.setText("Dept: " + (s.getDepartment() != null ? s.getDepartment() : "N/A"));
            tvRoute.setText("Route: " + (s.getAssignedRoute() != null && !s.getAssignedRoute().isEmpty() ? s.getAssignedRoute() : "Not assigned"));
            tvBus.setText("Bus: " + (s.getAssignedBus() != null && !s.getAssignedBus().isEmpty() ? s.getAssignedBus() : "Not assigned"));
            card.setOnClickListener(v -> listener.onClick(s));
        }
    }
}
