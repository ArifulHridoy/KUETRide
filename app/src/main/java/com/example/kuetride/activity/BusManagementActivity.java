package com.example.kuetride.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Bus;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusManagementActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private final List<Bus> buses = new ArrayList<>();
    private final List<Bus> filtered = new ArrayList<>();
    private BusAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_management);
        db = FirebaseFirestore.getInstance();

        RecyclerView rv = findViewById(R.id.rvBuses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusAdapter(filtered, this::deleteBus);
        rv.setAdapter(adapter);

        EditText busIdEt = findViewById(R.id.etBusId);
        EditText numberEt = findViewById(R.id.etNumber);
        EditText capacityEt = findViewById(R.id.etCapacity);
        MaterialButton addBtn = findViewById(R.id.btnAdd);

        addBtn.setOnClickListener(v -> {
            String busId = busIdEt.getText().toString().trim();
            String number = numberEt.getText().toString().trim();
            String capStr = capacityEt.getText().toString().trim();
            if (TextUtils.isEmpty(busId) || TextUtils.isEmpty(number) || TextUtils.isEmpty(capStr)) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            int capacity = Integer.parseInt(capStr);
            Map<String, Object> data = new HashMap<>();
            data.put("number", number);
            data.put("capacity", capacity);
            db.collection("buses").document(busId).set(data).addOnSuccessListener(r -> {
                Toast.makeText(this, "Bus added", Toast.LENGTH_SHORT).show();
                busIdEt.setText("");
                numberEt.setText("");
                capacityEt.setText("");
                loadBuses();
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
        });

        loadBuses();
    }

    private void loadBuses() {
        db.collection("buses").get().addOnSuccessListener(snap -> {
            buses.clear();
            for (QueryDocumentSnapshot d : snap) {
                Bus b = new Bus(d.getId(), d.getString("number"), d.getLong("capacity").intValue());
                buses.add(b);
            }
            filtered.clear();
            filtered.addAll(buses);
            adapter.notifyDataSetChanged();
        });
    }

    private void deleteBus(Bus b) {
        db.collection("buses").document(b.getId()).delete().addOnSuccessListener(a -> {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            loadBuses();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }

    static class BusAdapter extends RecyclerView.Adapter<BusViewHolder> {
        interface OnDelete { void onDelete(Bus b); }
        private final List<Bus> items;
        private final OnDelete cb;
        BusAdapter(List<Bus> items, OnDelete cb) { this.items = items; this.cb = cb; }
        @Override public BusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            android.view.View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus, parent, false);
            return new BusViewHolder(v);
        }
        @Override public void onBindViewHolder(BusViewHolder h, int pos) { h.bind(items.get(pos), cb); }
        @Override public int getItemCount() { return items.size(); }
    }

    static class BusViewHolder extends RecyclerView.ViewHolder {
        private final TextView busNumber, capacity;
        private final Button del;
        BusViewHolder(android.view.View itemView) { 
            super(itemView); 
            busNumber = itemView.findViewById(R.id.tvBusNumber); 
            capacity = itemView.findViewById(R.id.tvCapacity);
            del = itemView.findViewById(R.id.btnDelete); 
        }
        void bind(Bus b, BusAdapter.OnDelete cb) {
            busNumber.setText("Bus " + b.getNumber());
            capacity.setText("Capacity: " + b.getCapacity());
            del.setOnClickListener(v -> cb.onDelete(b));
        }
    }
}
