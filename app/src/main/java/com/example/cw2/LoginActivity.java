package com.example.cw2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(View view){
        if(isNetworkConnected() == true){
            if(isConecctedToInternet() ==true){
                //Get params
                email = ((EditText) findViewById(R.id.Log_Email)).getText().toString();
                String password = ((EditText) findViewById(R.id.Log_Password)).getText().toString();
                //verify
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(this,"Please enter email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(this,"Please enter password", Toast.LENGTH_LONG).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Display message
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                            finish();
                        }
                        else{
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            //Display message
                            Toast.makeText(LoginActivity.this, "Login Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else{
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }
    public void goRegister(View view){
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -i 5 -c 1 console.firebase.google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    public boolean isConecctedToInternet() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            Log.d(" ExitValue ",""+exitValue);
            if(exitValue==0){
                return true;
            }else{
                return false;
            }
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

}