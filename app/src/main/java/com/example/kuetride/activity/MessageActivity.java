package com.example.kuetride.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
import com.example.kuetride.model.Message;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private final List<Message> items = new ArrayList<>();
    private MAdapter adapter; private FirebaseFirestore db;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        db = FirebaseFirestore.getInstance();
        RecyclerView rv = findViewById(R.id.rvMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MAdapter(items);
        rv.setAdapter(adapter);
        EditText msgEt = findViewById(R.id.etMessage);
        MaterialButton sendBtn = findViewById(R.id.btnSend);
        sendBtn.setOnClickListener(v -> {
            String msg = msgEt.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) { Toast.makeText(this, "Type a message", Toast.LENGTH_SHORT).show(); return; }
            Map<String,Object> data = new HashMap<>();
            String uid = FirebaseAuth.getInstance().getUid();
            data.put("userId", uid);
            data.put("message", msg);
            data.put("timestamp", System.currentTimeMillis());
            db.collection("messages").add(data).addOnSuccessListener(r -> { msgEt.setText(""); load(); })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
        });
        load();
    }
    private void load() {
        db.collection("messages").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(snap -> {
            items.clear();
            for (QueryDocumentSnapshot d : snap) {
                items.add(new Message(d.getId(), d.getString("userId"), d.getString("message"), d.getLong("timestamp")));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show());
    }
    static class MAdapter extends RecyclerView.Adapter<MVH> {
        private final List<Message> items; MAdapter(List<Message> items) { this.items = items; }
        @NonNull
        @Override public MVH onCreateViewHolder(ViewGroup parent, int vt) {
            android.view.View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_admin, parent, false);
            return new MVH(v);
        }
        @Override public void onBindViewHolder(@NonNull MVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }
    static class MVH extends RecyclerView.ViewHolder {
        private final TextView tvMessage, tvTimestamp;
        MVH(android.view.View itemView) { 
            super(itemView); 
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
        void bind(Message m) { 
            tvMessage.setText(m.getMessage());
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(m.getTimestamp());
            String date = DateFormat.format("dd-MM-yyyy hh:mm a", cal).toString();
            tvTimestamp.setText(date);
        }
    }
}
