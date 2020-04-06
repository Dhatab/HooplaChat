package com.hooplachat.Messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hooplachat.Adapter.MessageAdapter;
import com.hooplachat.MainActivity;
import com.hooplachat.Model.Chat;
import com.hooplachat.Model.User;
import com.hooplachat.Notifications.Data;
import com.hooplachat.Notifications.Sender;
import com.hooplachat.Notifications.Token;
import com.hooplachat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private ImageView message_profile_image;
    private TextView message_username;
    private ImageView message_videocall;
    private ImageButton message_send_btn;
    private EditText message_edittext;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private MessageAdapter messageAdapter;
    private List<Chat> chatList;
    private RecyclerView message_recycler_view;
    private Intent intent;
    private ValueEventListener text_readListener;
    private String contactUID;
    private RequestQueue requestQueue;
    private boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        message_username = findViewById(R.id.message_username);
        message_profile_image = findViewById(R.id.message_profile_image);
        message_send_btn = findViewById(R.id.message_send_btn);
        message_edittext = findViewById(R.id.message_edittext);
        message_videocall = findViewById(R.id.message_videocall);

        requestQueue = Volley.newRequestQueue(getApplicationContext());



        message_recycler_view = findViewById(R.id.message_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        message_recycler_view.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        contactUID = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(contactUID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                message_username.setText(user.getDisplayName());

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(user.getDisplayName());
                String userChar = user.getDisplayName().substring(0,1);
                TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

                if (!user.getImageURL().equals("default")){
                    Glide.with(getApplicationContext()).load(user.getImageURL()).apply(RequestOptions.circleCropTransform()).into(message_profile_image);
                } else{
                    message_profile_image.setImageDrawable(drawable);
                }
                readMessages(firebaseUser.getUid(), contactUID, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        message_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = message_edittext.getText().toString();
                if (!message.equals("")){
                    sendMessage(firebaseUser.getUid(), contactUID, message);
                } else {
                    Toast.makeText(MessageActivity.this, "Message can't be empty.", Toast.LENGTH_SHORT).show();
                }
                message_edittext.setText("");
            }
        });

        readMessage(contactUID);
    }

    private void readMessage(final String userID){
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        text_readListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("read_text", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, final String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("currentDate", currentDate);
        hashMap.put("currentTime", currentTime);
        hashMap.put("read_text", false);

        reference.child("Chats").push().setValue(hashMap);

        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify){
                    sendNotification(contactUID, user.getDisplayName(), message);
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid()).child(contactUID);
        final DatabaseReference contactchatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(contactUID).child(firebaseUser.getUid());


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(contactUID);
                    contactchatRef.child("id").setValue(firebaseUser.getUid());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendNotification(final String contactUID, final String displayName, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(contactUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Token token = dataSnapshot1.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), displayName+":"+message, "New Message", contactUID, R.mipmap.ic_launcher);
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAA4G65pZE:APA91bGZ9m0_6Sl725D8aQUQGbAmfQKh9HNR8YpuP7IQJEwzS-1X9ICIOTOVLOI4ipQTPgR1qkvPDSK5IkL7ZJoOvC46AfJ94tYZEaW5MK148599w12TnBFmk-5fcUS7rpYRzdduYGAh");

                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myID, final String contactID, final String imageurl){
        chatList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat.getReceiver().equals(myID) && chat.getSender().equals(contactID) ||
                        chat.getReceiver().equals(contactID) && chat.getSender().equals(myID)){
                    chatList.add(chat);
                }
                messageAdapter = new MessageAdapter(MessageActivity.this, chatList, imageurl);
                message_recycler_view.setAdapter(messageAdapter);
                message_recycler_view.scrollToPosition(chatList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(text_readListener);
    }
}
