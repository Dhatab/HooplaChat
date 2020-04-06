package com.hooplachat.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hooplachat.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

public class CustomActivityDialog extends AppCompatDialogFragment {
    private MaterialEditText dialog_message;
    private TextView dialog_char_count;
    private FirebaseUser firebaseUser;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        dialog_message = view.findViewById(R.id.dialog_message);
        dialog_char_count = view.findViewById(R.id.dialog_char_count);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        builder.setView(view)
                .setTitle("Status Post")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = dialog_message.getText().toString();
                        if (message.equals("")){
                            Toast.makeText(getContext(), "Status post can't be empty", Toast.LENGTH_SHORT).show();
                        } else{
                            updateStatus(message);
                        }
                    }
                });
        return builder.create();
    }

    private void updateStatus(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Status_Post");

        String postID = databaseReference.push().getKey();
        String currentDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());



        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("postID", postID);
        hashMap.put("message", message);
        hashMap.put("currentDate", currentDate);
        hashMap.put("currentTime", currentTime);

        databaseReference.child(postID).setValue(hashMap);
        Toast.makeText(getContext(), "Status post updated!", Toast.LENGTH_SHORT).show();

    }
}
