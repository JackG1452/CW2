package com.example.cw2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Registration");

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar_reg);
        progressBar.setVisibility(View.INVISIBLE);

    }

    public void registerUser(View view) {
        if (isNetworkConnected() == true) {
            if (isConecctedToInternet() == true) {
                //Get Params
                String email = ((EditText) findViewById(R.id.Email)).getText().toString();
                String password = ((EditText) findViewById(R.id.Password)).getText().toString();

                //verify
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
                    return;
                }
                //if empty
                progressBar.setVisibility(View.VISIBLE);
                //Create User
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Checking success
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            //Display message
                            Toast.makeText(MainActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            //Display message
                            Toast.makeText(MainActivity.this, "Failed Registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    public void goLogin(View view){
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 console.firebase.google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    public boolean isConecctedToInternet() {
        Runtime runtime = Runtime.getRuntime();
        try {
            //If running on normal/real devices Ping Google
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            Log.d(" ExitValue 1st Cond",""+exitValue);
            if(exitValue==0){
                return true;
            }
            else{
                //for emulator will ping localhost
                ipProcess = runtime.exec("/system/bin/ping -c 1 127.0.0.1");
                exitValue = ipProcess.waitFor();
                Log.d(" ExitValue 2nd Cond",""+exitValue);
                if(exitValue==0) {
                    return true;
                }
                else{
                    return false;
                }
            }
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

}