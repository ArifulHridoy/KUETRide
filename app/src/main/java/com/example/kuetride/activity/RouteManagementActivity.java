package com.example.kuetride.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.example.kuetride.R;
import com.example.kuetride.model.Route;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteManagementActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private List<Route> routes = new ArrayList<>();
    private List<Route> filtered = new ArrayList<>();
    private RouteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_management);
        db = FirebaseFirestore.getInstance();

        RecyclerView rv = findViewById(R.id.rvRoutes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteAdapter(filtered, this::deleteRoute);
        rv.setAdapter(adapter);

        EditText routeIdEt = findViewById(R.id.etRouteId);
        EditText nameEt = findViewById(R.id.etName);
        EditText startEt = findViewById(R.id.etStart);
        EditText endEt = findViewById(R.id.etEnd);
        MaterialButton addBtn = findViewById(R.id.btnAdd);
        EditText searchEt = findViewById(R.id.etSearch);

        addBtn.setOnClickListener(v -> {
            String routeId = routeIdEt.getText().toString().trim();
            String name = nameEt.getText().toString().trim();
            String start = startEt.getText().toString().trim();
            String end = endEt.getText().toString().trim();
            if (TextUtils.isEmpty(routeId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("start", start);
            data.put("end", end);
            db.collection("routes").document(routeId).set(data).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Route added", Toast.LENGTH_SHORT).show();
                routeIdEt.setText("");
                nameEt.setText("");
                startEt.setText("");
                endEt.setText("");
                loadRoutes();
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
        });

        searchEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        loadRoutes();
    }

    private void loadRoutes() {
        db.collection("routes").get().addOnSuccessListener(snap -> {
            routes.clear();
            for (QueryDocumentSnapshot d : snap) {
                Route r = new Route(d.getId(), d.getString("name"), d.getString("start"), d.getString("end"));
                routes.add(r);
            }
            applyFilter(null);
        });
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.toLowerCase();
        filtered.clear();
        for (Route r : routes) {
            String text = (r.getName() + " " + r.getStart() + " " + r.getEnd()).toLowerCase();
            if (text.contains(q)) filtered.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteRoute(Route r) {
        db.collection("routes").document(r.getId()).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            loadRoutes();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }

    static class RouteAdapter extends RecyclerView.Adapter<RouteViewHolder> {
        interface OnDelete { void onDelete(Route r); }
        private final List<Route> items;
        private final OnDelete onDelete;
        RouteAdapter(List<Route> items, OnDelete onDelete) { this.items = items; this.onDelete = onDelete; }
        @Override public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
            return new RouteViewHolder(v);
        }
        @Override public void onBindViewHolder(RouteViewHolder h, int pos) { h.bind(items.get(pos), onDelete); }
        @Override public int getItemCount() { return items.size(); }
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final MaterialButton deleteBtn;
        RouteViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
        void bind(Route r, RouteAdapter.OnDelete onDelete) {
            title.setText(r.getName() + " (" + r.getStart() + " → " + r.getEnd() + ")");
            deleteBtn.setOnClickListener(v -> onDelete.onDelete(r));
        }
    }
}
