package com.hooplachat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hooplachat.Adapter.UsersAdapter;
import com.hooplachat.Model.Chatlist;
import com.hooplachat.Model.User;
import com.hooplachat.R;
import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> mUsers;
    private List<Chatlist> userList;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerview_chatsfrag);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    userList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void chatList() {
                mUsers = new ArrayList<>();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            User user = snapshot.getValue(User.class);
                            for (Chatlist chatlist : userList){
                                if (user.getId().equals(chatlist.getId())){
                                    mUsers.add(user);
                                }
                            }
                        }
                        usersAdapter = new UsersAdapter(getContext(), mUsers, true);
                        recyclerView.setAdapter(usersAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
