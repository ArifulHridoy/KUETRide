package com.example.kuetride.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Schedule;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private final List<Schedule> schedules = new ArrayList<>();
    private ScheduleAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        db = FirebaseFirestore.getInstance();

        RecyclerView rvSchedules = findViewById(R.id.rvSchedules);
        rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter(schedules, this::deleteSchedule);
        rvSchedules.setAdapter(adapter);

        loadSchedules();
    }

    private void loadSchedules() {
        db.collection("schedules").get().addOnSuccessListener(snap -> {
            schedules.clear();
            for (QueryDocumentSnapshot d : snap) {
                Schedule s = new Schedule(d.getId(), d.getString("routeId"), d.getString("busId"), d.getString("date"), d.getString("time"));
                schedules.add(s);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void deleteSchedule(Schedule schedule) {
        db.collection("schedules").document(schedule.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                    loadSchedules();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete schedule", Toast.LENGTH_SHORT).show());
    }

    private static class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {
        private final List<Schedule> schedules;
        private final OnDelete onDelete;

        public ScheduleAdapter(List<Schedule> schedules, OnDelete onDelete) {
            this.schedules = schedules;
            this.onDelete = onDelete;
        }

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_admin, parent, false);
            return new ScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            holder.bind(schedules.get(position), onDelete);
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }
    }

    private static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvBus, tvDateTime;
        private final MaterialButton btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvBus = itemView.findViewById(R.id.tvBus);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Schedule schedule, OnDelete onDelete) {
            tvRoute.setText("Route: " + schedule.getRouteId());
            tvBus.setText("Bus: " + schedule.getBusId());
            tvDateTime.setText("Time: " + schedule.getDate() + " " + schedule.getTime());
            btnDelete.setOnClickListener(v -> onDelete.onDelete(schedule));
        }
    }

    interface OnDelete {
        void onDelete(Schedule schedule);
    }
}
