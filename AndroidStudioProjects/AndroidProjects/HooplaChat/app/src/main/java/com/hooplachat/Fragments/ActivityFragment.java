package com.hooplachat.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hooplachat.Adapter.StatusAdapter;
import com.hooplachat.Adapter.UsersAdapter;
import com.hooplachat.MenuItems.UsersAccount;
import com.hooplachat.Messages.MessageActivity;
import com.hooplachat.Model.Contacts;
import com.hooplachat.Model.StatusPost;
import com.hooplachat.Model.User;
import com.hooplachat.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private ImageView activity_profile_image;
    private TextView activity_displayName, activity_status_message, activity_userName;
    private RelativeLayout rel_layout;
    private ProgressBar progressBar;


    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference, statusDBref;

    private StatusAdapter statusAdapter;
    private List<StatusPost> statusPostList;
    private List<String> friendsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        floatingActionButton = view.findViewById(R.id.activity_FAB);
        activity_profile_image = view.findViewById(R.id.activity_profile_image);
        activity_displayName = view.findViewById(R.id.activity_displayName);
        activity_userName = view.findViewById(R.id.activity_userName);
        activity_status_message = view.findViewById(R.id.activity_status_message);
        rel_layout = view.findViewById(R.id.rel_layout);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.recyclerview_activityfrag);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        statusPostList = new ArrayList<>();
        statusAdapter = new StatusAdapter(getContext(), statusPostList, true);
        recyclerView.setAdapter(statusAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        statusDBref = FirebaseDatabase.getInstance().getReference("Status_Post");
        databaseReference.keepSynced(true);
        statusDBref.keepSynced(true);

        statusDBref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StatusPost statusPost = snapshot.getValue(StatusPost.class);
                    if (statusPost.getUserID().equals(firebaseUser.getUid())){
                        activity_status_message.setText(statusPost.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rel_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options();
            }
        });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        loadMyPost();
        checkFriends();
        return view;
    }
    private void checkFriends(){
        friendsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Friends")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    friendsList.add(snapshot.getKey());
                }
                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Status_Post");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                statusPostList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StatusPost statusPost = snapshot.getValue(StatusPost.class);
                    for (String id : friendsList){
                        if (statusPost.getUserID().equals(id)){
                            statusPostList.add(statusPost);
                        }
                    }
                }
                statusAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyPost() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                activity_displayName.setText(user.getDisplayName());
                activity_userName.setText(user.getUsername());

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(user.getDisplayName());
                String userChar = user.getDisplayName().substring(0,1);
                TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

                if (!user.getImageURL().equals("default")){
                    Glide.with(getContext()).load(user.getImageURL()).apply(RequestOptions.circleCropTransform()).into(activity_profile_image);
                } else {
                    activity_profile_image.setImageDrawable(drawable);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void options(){
        CharSequence options[] = new CharSequence[]{"Open Profile", "Edit Status"};

        final androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Select Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Click Event for each item.
                if(i == 0){

                    Intent profileIntent = new Intent(getContext(), UsersAccount.class);
                    profileIntent.putExtra("userid", firebaseUser.getUid());
                    startActivity(profileIntent);

                }

                if(i == 1){
                    openDialog();
                }

            }
        });

        builder.show();

    }

    private void openDialog() {
        CustomActivityDialog activityDialog = new CustomActivityDialog();
        activityDialog.show(getFragmentManager(), "Status Activity");
    }
}
