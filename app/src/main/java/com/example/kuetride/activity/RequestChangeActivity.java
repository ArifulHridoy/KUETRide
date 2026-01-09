package com.example.kuetride.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Request;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestChangeActivity extends AppCompatActivity {
    private final List<Request> requests = new ArrayList<>();
    private RequestAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_change);
        db = FirebaseFirestore.getInstance();

        Spinner spnType = findViewById(R.id.spnType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);

        EditText descEt = findViewById(R.id.etDescription);
        MaterialButton submit = findViewById(R.id.btnSubmit);
        submit.setOnClickListener(v -> {
            String type = spnType.getSelectedItem().toString();
            String desc = descEt.getText().toString().trim();
            if (TextUtils.isEmpty(desc)) {
                Toast.makeText(this, "Description required", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;

            // Get student's roll number
            db.collection("users").whereEqualTo("uid", uid).limit(1).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String rollNumber = queryDocumentSnapshots.getDocuments().get(0).getString("rollNumber");

                            Map<String, Object> data = new HashMap<>();
                            data.put("studentId", rollNumber);
                            data.put("type", type);
                            data.put("description", desc);
                            data.put("status", "PENDING");
                            data.put("timestamp", System.currentTimeMillis());

                            db.collection("requests").add(data).addOnSuccessListener(r -> {
                                Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                                descEt.setText("");
                                loadRequests(); // Refresh the list
                            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to submit request.", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        RecyclerView rvRequests = findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new RequestAdapter(requests);
        rvRequests.setAdapter(this.adapter);

        loadRequests();
    }

    private void loadRequests() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").whereEqualTo("uid", uid).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String rollNumber = queryDocumentSnapshots.getDocuments().get(0).getString("rollNumber");
                        db.collection("requests").whereEqualTo("studentId", rollNumber)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(snap -> {
                                    requests.clear();
                                    for (QueryDocumentSnapshot d : snap) {
                                        requests.add(new Request(d.getId(), d.getString("studentId"), d.getString("type"), d.getString("description"), d.getString("status")));
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private static class RequestAdapter extends RecyclerView.Adapter<RequestViewHolder> {
        private final List<Request> requests;

        public RequestAdapter(List<Request> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_change, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            holder.bind(requests.get(position));
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }
    }

    private static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvType;
        private final TextView tvDescription;
        private final TextView tvStatus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Request request) {
            tvType.setText(request.getType());
            tvDescription.setText(request.getDescription());
            tvStatus.setText(request.getStatus());
        }
    }
}
