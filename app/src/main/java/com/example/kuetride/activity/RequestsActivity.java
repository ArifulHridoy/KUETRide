package com.example.kuetride.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Request;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsActivity extends AppCompatActivity {
    private final List<Request> items = new ArrayList<>();
    private FirebaseFirestore db;
    private RAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        db = FirebaseFirestore.getInstance();
        RecyclerView rv = findViewById(R.id.rvRequests);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RAdapter(items, this::updateStatus);
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        db.collection("requests").whereEqualTo("status", "PENDING").get().addOnSuccessListener(snap -> {
            items.clear();
            for (QueryDocumentSnapshot d : snap) {
                items.add(new Request(d.getId(), d.getString("studentId"), d.getString("type"), d.getString("description"), d.getString("status")));
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void updateStatus(Request r, String status) {
        Map<String,Object> data = new HashMap<>();
        data.put("status", status);
        db.collection("requests").document(r.getId()).update(data).addOnSuccessListener(a -> { Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show(); load(); })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }

    static class RAdapter extends RecyclerView.Adapter<RViewHolder> {
        interface OnAction { void change(Request r, String status); }
        private final List<Request> items; private final OnAction action;
        RAdapter(List<Request> items, OnAction action) { this.items = items; this.action = action; }
        @NonNull
        @Override public RViewHolder onCreateViewHolder(ViewGroup parent, int vt) {
            android.view.View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_admin, parent, false);
            return new RViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull RViewHolder h, int pos) { h.bind(items.get(pos), action); }
        @Override public int getItemCount() { return items.size(); }
    }
    static class RViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvType, tvDescription, tvStatus;
        private final MaterialButton approve, reject;
        RViewHolder(android.view.View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            approve = itemView.findViewById(R.id.btnApprove);
            reject = itemView.findViewById(R.id.btnReject);
        }
        void bind(Request r, RAdapter.OnAction action) {
            tvStudentId.setText("Student ID: " + r.getStudentId());
            tvType.setText("Type: " + r.getType());
            tvDescription.setText("Description: " + r.getDescription());
            tvStatus.setText("Status: " + r.getStatus());
            approve.setOnClickListener(v -> action.change(r, "APPROVED"));
            reject.setOnClickListener(v -> action.change(r, "REJECTED"));
        }
    }
}
