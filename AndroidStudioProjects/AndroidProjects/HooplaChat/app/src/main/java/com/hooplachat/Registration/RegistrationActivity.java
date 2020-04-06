package com.hooplachat.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hooplachat.MainActivity;
import com.hooplachat.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    private MaterialEditText register_username, register_email, register_displayName, register_password;
    private TextView register_login;
    private Button register_button;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupUI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        firebaseAuth = FirebaseAuth.getInstance();

        register_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActvity.class));
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_username = register_username.getText().toString().trim();
                String text_displayName = register_displayName.getText().toString().trim();
                String text_email = register_email.getText().toString().trim();
                String text_password = register_password.getText().toString();

                if (TextUtils.isEmpty(text_username) || TextUtils.isEmpty(text_displayName) || TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)){
                    Toast.makeText(RegistrationActivity.this, "Please make sure all fields are filled!", Toast.LENGTH_SHORT).show();
                } else if (text_password.length() <= 6){
                    Toast.makeText(RegistrationActivity.this, "Password must be at more then 6 characters!", Toast.LENGTH_SHORT).show();
                } else {
                    register(text_username,text_email, text_displayName, text_password);
                    Toast.makeText(RegistrationActivity.this, "Account Registered!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void register(final String username, final String email, final String displayName, String password){
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String userID = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userID);
                    hashMap.put("displayName", displayName);
                    hashMap.put("username", "@"+username);
                    hashMap.put("imageURL", "default");
                    hashMap.put("search", username.toLowerCase());

                    updateStatus(firebaseUser);

                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegistrationActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setupUI(){
        register_username = findViewById(R.id.register_username);
        register_email = findViewById(R.id.register_email);
        register_displayName = findViewById(R.id.register_displayName);
        register_password = findViewById(R.id.register_password);
        register_button = findViewById(R.id.register_button);
        register_login = findViewById(R.id.register_login);
    }

    private void updateStatus(FirebaseUser firebaseUser) {
        firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Status_Post");

        String postID = databaseReference.push().getKey();
        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());



        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("postID", postID);
        hashMap.put("message", "Hello World! I am new to Hoopla Chat!");
        hashMap.put("currentDate", currentDate);
        hashMap.put("currentTime", currentTime);

        databaseReference.child(postID).setValue(hashMap);
    }
}
