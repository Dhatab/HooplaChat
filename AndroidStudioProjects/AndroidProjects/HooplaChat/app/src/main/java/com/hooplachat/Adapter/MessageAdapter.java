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
import com.hooplachat.Messages.MessageActivity;
import com.hooplachat.Model.Chat;
import com.hooplachat.Model.User;
import com.hooplachat.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<com.hooplachat.Adapter.MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> chatList;
    private String imageURL;

    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> chatList, String imageURL) {
        this.context = context;
        this.chatList = chatList;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public com.hooplachat.Adapter.MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new com.hooplachat.Adapter.MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new com.hooplachat.Adapter.MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
       final Chat chat = chatList.get(position);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(chat.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(user.getDisplayName());
                String userChar = user.getDisplayName().substring(0,1);
                TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

                if (!imageURL.equals("default")){
                    Glide.with(context).load(imageURL).apply(RequestOptions.circleCropTransform()).into(holder.chat_message_profile_image);
                } else{
                    holder.chat_message_profile_image.setImageDrawable(drawable);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       holder.chat_message_text.setText(chat.getMessage());
       if (position == chatList.size() - 1){
           if (chat.isRead_text()){
               holder.chat_read_text.setText("Read");
           } else {
               holder.chat_read_text.setText("Delivered");
           }
       } else {
           holder.chat_read_text.setVisibility(View.GONE);
       }
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView chat_message_text, chat_read_text;
        public ImageView chat_message_profile_image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chat_message_text = itemView.findViewById(R.id.chat_message_text);
            chat_read_text = itemView.findViewById(R.id.chat_read_text);
            chat_message_profile_image = itemView.findViewById(R.id.chat_message_profile_image);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
      if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
          return MSG_TYPE_RIGHT;
      }else{
          return MSG_TYPE_LEFT;
      }
    }
}

