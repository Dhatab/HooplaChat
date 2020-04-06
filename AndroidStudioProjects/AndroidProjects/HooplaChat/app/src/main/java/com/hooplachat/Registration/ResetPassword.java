package com.hooplachat.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.hooplachat.R;

public class ResetPassword extends AppCompatActivity {
    private EditText reset_email_text;
    private Button reset_email_btn;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reset_email_text = findViewById(R.id.reset_email_text);
        reset_email_btn = findViewById(R.id.reset_email_btn);

        firebaseAuth = FirebaseAuth.getInstance();

        reset_email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = reset_email_text.getText().toString();

                if(email.equals("")){
                    Toast.makeText(ResetPassword.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPassword.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassword.this, LoginActvity.class));
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetPassword.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
