package com.hooplachat.MenuItems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hooplachat.MainActivity;
import com.hooplachat.Model.User;
import com.hooplachat.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UsersAccount extends AppCompatActivity {

    private TextView contact_username, contact_displayname, contact_bio , contact_email, contact_phone, contact_location;
    private ImageButton friend_button, accept_friend_button, contact_msg_btn;
    private ImageView contact_profile_image;

    private Intent intent;
    String contactUID;

    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    private DatabaseReference friendReqDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference mRootRef;

    private Timestamp timestamp;
    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_account);
        setupUI();

        intent = getIntent();
        contactUID = intent.getStringExtra("userid");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UsersAccount.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        mRootRef = FirebaseDatabase.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(contactUID);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference("Friend_Request");
        friendDatabase = FirebaseDatabase.getInstance().getReference("Friends");

        mCurrent_state = "not_friends";


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //loading user data into UI
                User user = dataSnapshot.getValue(User.class);
                contact_displayname.setText(user.getDisplayName());
                contact_username.setText(user.getUsername());

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(user.getDisplayName());
                String userChar = user.getDisplayName().substring(0,1);
                TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

                if (!user.getImageURL().equals("default")){
                    Glide.with(getApplicationContext()).load(user.getImageURL()).apply(RequestOptions.circleCropTransform()).into(contact_profile_image);
                } else{
                    contact_profile_image.setImageDrawable(drawable);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //checking if friends already
        friendReqDatabase.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if request is sent
                if (dataSnapshot.hasChild(contactUID)) {
                    String req_type = dataSnapshot.child(contactUID).child("request_type").getValue().toString();
                    if (req_type.equals("received")) {
                        mCurrent_state = "req_received";

                        friend_button.setBackgroundResource(R.drawable.circle_button_decline_friend);
                        friend_button.setImageResource(R.drawable.contact_close);

                        accept_friend_button.setVisibility(View.VISIBLE);
                        accept_friend_button.setEnabled(true);

                    } else if (req_type.equals("sent")) {
                        mCurrent_state = "req_sent";
                        friend_button.setBackgroundResource(R.drawable.circle_button_decline_friend);
                        friend_button.setImageResource(R.drawable.contact_close);

                    }
                } else {
                    //if we are friends
                    friendDatabase.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(contactUID)) {
                                mCurrent_state = "friends";
                                friend_button.setBackgroundResource(R.drawable.circle_button_decline_friend);
                                friend_button.setImageResource(R.drawable.contact_remove);
                                accept_friend_button.setVisibility(View.GONE);
                                accept_friend_button.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        friend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friend_button.setEnabled(false);
                // NOT FRIENDS STATE -- send request
                if (mCurrent_state.equals("not_friends")) {
                    sendRequest();
                }
                // cancel request state
                if (mCurrent_state.equals("req_sent")) {
                    cancelRequest();
                }
                //friend request received
                if (mCurrent_state.equals("req_received")) {
                    acceptFriend();
                }
                ///remove Friend
                if (mCurrent_state.equals("friends")) {
                    removeFriend();
                }
            }
        });

        accept_friend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriend();
            }
        });

    }

    private void removeFriend() {
        Map unfriendMap = new HashMap();
        unfriendMap.put("Friends/" + firebaseUser.getUid() + "/" + contactUID, null);
        unfriendMap.put("Friends/" + contactUID + "/" + firebaseUser.getUid(), null);

        mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {

                    mCurrent_state = "not_friends";
                    friend_button.setBackgroundResource(R.drawable.circle_button_add_friend);
                    friend_button.setImageResource(R.drawable.contact_request);
                    accept_friend_button.setEnabled(false);
                    accept_friend_button.setVisibility(View.GONE);
                    Toast.makeText(UsersAccount.this, "Friend Removed", Toast.LENGTH_SHORT).show();

                } else {
                    String error = databaseError.getMessage();
                    Toast.makeText(UsersAccount.this, error, Toast.LENGTH_SHORT).show();


                }

                friend_button.setEnabled(true);

            }
        });
    }

    private void acceptFriend() {

        final String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("Friends/" + firebaseUser.getUid() + "/" + contactUID + "/date", currentDate);
        friendsMap.put("Friends/" + contactUID + "/" + firebaseUser.getUid() + "/date", currentDate);


        friendsMap.put("Friend_Request/" + firebaseUser.getUid() + "/" + contactUID, null);
        friendsMap.put("Friend_Request/" + contactUID + "/" + firebaseUser.getUid(), null);


        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mCurrent_state = "friends";
                    Toast.makeText(UsersAccount.this, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                    friend_button.setEnabled(true);
                    friend_button.setBackgroundResource(R.drawable.circle_button_decline_friend);
                    friend_button.setImageResource(R.drawable.contact_remove);

                    accept_friend_button.setVisibility(View.GONE);
                    accept_friend_button.setEnabled(false);

                } else {
                    String error = databaseError.getMessage();
                    Toast.makeText(UsersAccount.this, error, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void cancelRequest() {
        friendReqDatabase.child(firebaseUser.getUid()).child(contactUID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                friendReqDatabase.child(contactUID).child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mCurrent_state = "not_friends";
                        friend_button.setBackgroundResource(R.drawable.circle_button_add_friend);
                        friend_button.setImageResource(R.drawable.contact_request);
                        friend_button.setEnabled(true);
                        Toast.makeText(UsersAccount.this, "Friend Request Canceled", Toast.LENGTH_SHORT).show();
                        accept_friend_button.setVisibility(View.GONE);
                        accept_friend_button.setEnabled(false);

                    }
                });

            }

        });

    }

    private void sendRequest() {
        Map requestMap = new HashMap();
        requestMap.put("Friend_Request/" + firebaseUser.getUid() + "/" + contactUID + "/request_type", "sent");
        requestMap.put("Friend_Request/" + contactUID + "/" + firebaseUser.getUid() + "/request_type", "received");

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {
                    Toast.makeText(UsersAccount.this, "Something went wrong!", Toast.LENGTH_SHORT).show();

                } else {
                    mCurrent_state = "req_sent";
                    Toast.makeText(UsersAccount.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                    friend_button.setBackgroundResource(R.drawable.circle_button_decline_friend);
                    friend_button.setImageResource(R.drawable.contact_close);
                    friend_button.setEnabled(true);
                }
            }
        });
    }

    private void setupUI() {
        contact_username = findViewById(R.id.contact_username);
        contact_displayname = findViewById(R.id.contact_displayname);
        contact_bio = findViewById(R.id.contact_bio);
        contact_bio = findViewById(R.id.contact_bio);
        contact_email = findViewById(R.id.contact_email);
        contact_phone = findViewById(R.id.contact_phone);
        contact_location = findViewById(R.id.contact_location);
        friend_button = findViewById(R.id.friend_button);
        accept_friend_button = findViewById(R.id.accept_friend_button);
        contact_msg_btn = findViewById(R.id.contact_msg_btn);
        contact_profile_image = findViewById(R.id.contact_profile_image);
    }
}
