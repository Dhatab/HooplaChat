package com.hooplachat.Adapter;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.hooplachat.MenuItems.UsersAccount;
import com.hooplachat.Messages.MessageActivity;
import com.hooplachat.Model.StatusPost;
import com.hooplachat.Model.User;
import com.hooplachat.R;
import com.hooplachat.TimeStamp.PostDate;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends  RecyclerView.Adapter<StatusAdapter.ViewHlder>{
    public Context mContext;
    public List<StatusPost> statusPosts;
    private boolean isChat;

    private FirebaseUser firebaseUser;

    public StatusAdapter(Context mContext, List<StatusPost> statusPosts, boolean isChat) {
        this.mContext = mContext;
        this.statusPosts = statusPosts;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_friends_design, parent, false);
        return new StatusAdapter.ViewHlder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHlder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final StatusPost post = statusPosts.get(position);

        holder.activity_rv_status_message.setText(post.getMessage());

        PostDate getPostDate = new PostDate();
        String postDate = getPostDate.getPostDate(post.getCurrentDate(), post.getCurrentTime(), mContext);
        holder.activity_rv_date.setText(postDate);

        friendInfo(holder.activity_rv_profile_image, holder.activity_rv_displayName, holder.activity_rv_userName, post.getUserID(), holder.activity_rv_status);

        holder.rel_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Click Event for each item.
                        if(i == 0){
                            Intent profileIntent = new Intent(mContext, UsersAccount.class);
                            profileIntent.putExtra("userid", post.getUserID());
                            mContext.startActivity(profileIntent);
                        }

                        if(i == 1){
                            Intent chatIntent = new Intent(mContext, MessageActivity.class);
                            chatIntent.putExtra("userid", post.getUserID());
                            mContext.startActivity(chatIntent);
                        }
                    }
                });

                builder.show();


            }
        });


    }

    @Override
    public int getItemCount() {
        return statusPosts.size();
    }

    public class ViewHlder extends RecyclerView.ViewHolder{

        public TextView activity_rv_displayName, activity_rv_userName, activity_rv_status_message, activity_rv_date;
        public CircleImageView activity_rv_status;
        public RelativeLayout rel_layout;
        public ImageView activity_rv_profile_image;

        public ViewHlder(@NonNull View itemView) {
            super(itemView);

            activity_rv_displayName = itemView.findViewById(R.id.activity_rv_displayName);
            activity_rv_userName = itemView.findViewById(R.id.activity_rv_userName);
            activity_rv_status_message = itemView.findViewById(R.id.activity_rv_status_message);
            activity_rv_profile_image = itemView.findViewById(R.id.activity_rv_profile_image);
            activity_rv_status = itemView.findViewById(R.id.activity_rv_status);
            activity_rv_date = itemView.findViewById(R.id.activity_rv_date);
            rel_layout = itemView.findViewById(R.id.rel_layout);
        }
    }

    private void friendInfo(final ImageView profile_image, final TextView displayname, final TextView username, final String userid, final CircleImageView status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                displayname.setText(user.getDisplayName());
                username.setText(user.getUsername());

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(user.getDisplayName());
                String userChar = user.getDisplayName().substring(0,1);
                TextDrawable drawable = TextDrawable.builder().buildRound(userChar.toUpperCase(), color);

                if (!user.getImageURL().equals("default")){
                    Glide.with(mContext).load(user.getImageURL()).apply(RequestOptions.circleCropTransform()).into(profile_image);
                } else{
                    profile_image.setImageDrawable(drawable);
                }

                if (isChat){
                    if (user.getStatus().equals("online")){
                        status.setVisibility(View.VISIBLE);
                    } else{
                        status.setVisibility(View.GONE);
                    }
                } else {
                    status.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
