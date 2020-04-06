package com.hooplachat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hooplachat.Fragments.ChatsFragment;
import com.hooplachat.MenuItems.UsersAccount;
import com.hooplachat.Messages.MessageActivity;
import com.hooplachat.Model.Chat;
import com.hooplachat.Model.User;
import com.hooplachat.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private boolean isChat;

    String lastMessage;
    int unread = 0;

    public UsersAdapter(Context context, List<User> userList, boolean isChat) {
        this.context = context;
        this.userList = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_rv_design, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.user_rv_displayName.setText(user.getDisplayName());


        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(user.getDisplayName());
        String userChar = user.getDisplayName().substring(0,1);
        TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

        if (!user.getImageURL().equals("default")){
            Glide.with(context).load(user.getImageURL()).apply(RequestOptions.circleCropTransform()).into(holder.user_rv_profile_image);
        } else{
            holder.user_rv_profile_image.setImageDrawable(drawable);
        }

        checkUnreadMessage(holder.user_rv_unread, user.getId());

        if (isChat) {
            checkLastMessage(user.getId(), holder.user_rv_last_message);
        } else {
            holder.user_rv_last_message.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.user_rv_status.setVisibility(View.VISIBLE);
            } else {
                holder.user_rv_status.setVisibility(View.GONE);
            }
        } else {
            holder.user_rv_status.setVisibility(View.GONE);
        }

        holder.user_rv_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UsersAccount.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView user_rv_displayName, user_rv_last_message, user_rv_unread;
        public CircleImageView user_rv_status;
        public ImageView user_rv_profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_rv_displayName = itemView.findViewById(R.id.user_rv_displayName);
            user_rv_profile_image = itemView.findViewById(R.id.user_rv_profile_image);
            user_rv_unread = itemView.findViewById(R.id.user_rv_unread);
            user_rv_status = itemView.findViewById(R.id.user_rv_status);
            user_rv_last_message = itemView.findViewById(R.id.user_rv_last_message);

        }
    }

    private void checkUnreadMessage(final TextView user_rv_unread, final String userID) {
        unread = 0;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID) && !chat.isRead_text()) {
                        unread++;
                    }
                }

                switch (unread) {
                    case 0:
                        user_rv_unread.setText("");
                        user_rv_unread.setVisibility(View.GONE);
                        break;
                    default:
                        user_rv_unread.setText(String.valueOf(unread));
                        user_rv_unread.setVisibility(View.VISIBLE);
                        break;
                }
                unread = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkLastMessage(final String userID, final TextView last_message) {
        lastMessage = "default";


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID) && chat.getSender().equals(firebaseUser.getUid())) {

                        lastMessage = chat.getMessage();
                    }
                }
                switch (lastMessage) {
                    case "default":
                        last_message.setText("");
                        break;
                    default:
                        last_message.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
