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
import com.hooplachat.MainActivity;
import com.hooplachat.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActvity extends AppCompatActivity {

    private MaterialEditText login_email, login_password;
    private Button login_button;
    private TextView login_register, login_forgot_password;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //checking if the user is already logged in
        if (firebaseUser != null){
            Intent intent = new Intent(LoginActvity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_actvity);
        setupUI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hoopla Chat");

        firebaseAuth = FirebaseAuth.getInstance();


        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text_email = login_email.getText().toString();
                final String text_password = login_password.getText().toString();

                if(TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)){
                    Toast.makeText(LoginActvity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                } else{
                    firebaseAuth.signInWithEmailAndPassword(text_email,text_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActvity.this, "Log In Successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActvity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActvity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        login_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActvity.this, ResetPassword.class));
            }
        });

        login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActvity.this, RegistrationActivity.class));
            }
        });
    }

    private void setupUI(){
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        login_register = findViewById(R.id.login_register);
        login_forgot_password = findViewById(R.id.login_forgot_password);

    }
}
