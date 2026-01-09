package com.example.kuetride.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kuetride.R;
import com.example.kuetride.model.Message;
import com.example.kuetride.model.Request;
import com.example.kuetride.model.Schedule;
import com.example.kuetride.model.Student;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {
    private final List<Schedule> schedules = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    private MessageAdapter messageAdapter;
    private FrameLayout container;
    private FirebaseFirestore db;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        container = findViewById(R.id.container);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                showProfileFragment();
            } else if (itemId == R.id.nav_schedule) {
                showScheduleFragment();
            } else if (itemId == R.id.nav_request) {
                showRequestFragment();
            } else if (itemId == R.id.nav_messages) {
                showMessagesFragment();
            }
            return true;
        });

        // Show profile by default
        showProfileFragment();
        loadStudentInfo();
    }

    private void loadStudentInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return; // Not logged in

        db.collection("users").whereEqualTo("uid", uid).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot snap = queryDocumentSnapshots.getDocuments().get(0);
                        currentStudent = new Student(
                                snap.getId(),
                                snap.getString("username"),
                                snap.getString("email"),
                                snap.getString("department"),
                                snap.getString("rollNumber"),
                                snap.getString("phone"),
                                snap.getString("assignedRoute"),
                                snap.getString("assignedBus")
                        );
                        showProfileFragment();
                        loadSchedules(); // Load schedules after student info is loaded
                    }
                });
    }

    private void showProfileFragment() {
        container.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_student_profile, container, false);
        container.addView(view);

        if (currentStudent != null) {
            TextView tvStudentId = view.findViewById(R.id.tvStudentId);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvDepartment = view.findViewById(R.id.tvDepartment);
            TextView tvRoll = view.findViewById(R.id.tvRoll);
            TextView tvPhone = view.findViewById(R.id.tvPhone);
            TextView tvAssignedRoute = view.findViewById(R.id.tvAssignedRoute);
            TextView tvAssignedBus = view.findViewById(R.id.tvAssignedBus);

            tvStudentId.setText("Student ID: " + currentStudent.getRollNumber());
            tvName.setText("Name: " + currentStudent.getName());
            tvDepartment.setText("Department: " + currentStudent.getDepartment());
            tvRoll.setText("Roll: " + currentStudent.getRollNumber());
            tvPhone.setText("Phone: " + currentStudent.getPhone());
            tvAssignedRoute.setText("Route: " + currentStudent.getAssignedRoute());
            tvAssignedBus.setText("Bus: " + currentStudent.getAssignedBus());
        }

        MaterialButton btnEdit = view.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentEditProfileActivity.class);
            startActivity(intent);
        });

        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showScheduleFragment() {
        container.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_student_schedule, container, false);
        container.addView(view);

        RecyclerView rv = view.findViewById(R.id.rvSchedules);
        rv.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(schedules, db);
        rv.setAdapter(scheduleAdapter);

        if (currentStudent != null) {
            loadSchedules();
        }
    }

    private void showRequestFragment() {
        container.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_student_request, container, false);
        container.addView(view);

        MaterialButton btnSendRequest = view.findViewById(R.id.btnSendRequest);
        RecyclerView rvRequests = view.findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        btnSendRequest.setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestChangeActivity.class);
            startActivity(intent);
        });

        loadRequests(rvRequests);
    }

    private void showMessagesFragment() {
        container.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_student_messages, container, false);
        container.addView(view);

        RecyclerView rv = view.findViewById(R.id.rvMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messages);
        rv.setAdapter(messageAdapter);

        loadMessages();
    }

    private void loadSchedules() {
        if (currentStudent == null || currentStudent.getAssignedRoute() == null) return;

        db.collection("schedules")
                .whereEqualTo("routeId", currentStudent.getAssignedRoute())
                .addSnapshotListener((snap, e) -> {
            if (snap != null) {
                schedules.clear();
                for (QueryDocumentSnapshot d : snap) {
                    Schedule s = new Schedule(d.getId(), d.getString("routeId"), d.getString("busId"), d.getString("date"), d.getString("time"));
                    schedules.add(s);
                }
                if (scheduleAdapter != null) scheduleAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadRequests(RecyclerView rv) {
        if (currentStudent == null) return;
        db.collection("requests").whereEqualTo("studentId", currentStudent.getRollNumber())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
            if (snap != null) {
                List<Request> requests = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Request r = new Request(d.getId(), d.getString("studentId"), d.getString("type"), d.getString("description"), d.getString("status"));
                    requests.add(r);
                }
                RequestAdapter adapter = new RequestAdapter(requests);
                rv.setAdapter(adapter);
            }
        });
    }

    private void loadMessages() {
        db.collection("messages").addSnapshotListener((snap, e) -> {
            if (snap != null) {
                messages.clear();
                for (QueryDocumentSnapshot d : snap) {
                    Message m = new Message(d.getId(), d.getString("userId"), d.getString("message"), d.getLong("timestamp"));
                    messages.add(m);
                }
                if (messageAdapter != null) messageAdapter.notifyDataSetChanged();
            }
        });
    }

    // Adapters and ViewHolders remain the same...

    private static class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {
        private final List<Schedule> schedules;
        private final FirebaseFirestore db;

        public ScheduleAdapter(List<Schedule> schedules, FirebaseFirestore db) {
            this.schedules = schedules;
            this.db = db;
        }

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ScheduleViewHolder(view, db);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            holder.bind(schedules.get(position));
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }
    }

    private static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvBus, tvDateTime;
        private final FirebaseFirestore db;

        public ScheduleViewHolder(@NonNull View itemView, FirebaseFirestore db) {
            super(itemView);
            this.db = db;
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvBus = itemView.findViewById(R.id.tvBus);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
        }

        public void bind(Schedule schedule) {
            db.collection("routes").document(schedule.getRouteId()).get().addOnSuccessListener(routeSnap -> {
                if (routeSnap.exists()) {
                    tvRoute.setText("Route: " + routeSnap.getString("name") + " (" + routeSnap.getString("start") + " - " + routeSnap.getString("end") + ")");
                }
            });

            db.collection("buses").document(schedule.getBusId()).get().addOnSuccessListener(busSnap -> {
                if (busSnap.exists()) {
                    tvBus.setText("Bus: " + busSnap.getString("number") + " (Cap: " + busSnap.getLong("capacity") + ")");
                }
            });

            tvDateTime.setText("Time: " + schedule.getDate() + " " + schedule.getTime());
        }
    }

    private static class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private final List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            holder.bind(messages.get(position));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage, tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getMessage());
            // You can format the timestamp here as you wish
            tvTimestamp.setText(String.valueOf(message.getTimestamp()));
        }
    }

    private static class RequestAdapter extends RecyclerView.Adapter<RequestViewHolder> {
        private final List<Request> requests;

        public RequestAdapter(List<Request> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_request, parent, false);
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
        private final TextView tvType, tvDescription, tvStatus;

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
