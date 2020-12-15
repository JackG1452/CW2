package com.example.cw2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class GalleryActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String currentUserEmail;
    private String UID;

    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    private String lastSavedImage;
    private ImageView imageView;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getSupportActionBar().setTitle("Recently Uploaded");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.recentImage);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.recentImage:
                        startActivity(new Intent(GalleryActivity.this, GalleryActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(GalleryActivity.this, ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.textGallery:
                        startActivity(new Intent(GalleryActivity.this, TextActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.logOut:
                        firebaseAuth.signOut();
                        finish();
                        Toast.makeText(GalleryActivity.this, "Logout Successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(GalleryActivity.this, LoginActivity.class));
                }
                return false;
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView textViewUserEmail = (TextView) findViewById(R.id.textView);
        textViewUserEmail.setText("Welcome " + currentUser.getEmail());

        TextView textView = (TextView) findViewById(R.id.textView2);

        try{
            lastSavedImage = readFromFile("temp_Details.txt");
            text = readFromFile("temp_Write.txt");
        }
        catch (IOError error){

        }


        imageView = findViewById(R.id.imageView);

        if(isNetworkConnected() == true){
            try {
                if(isConnected() ==true){
                    //If readFromFile for recentImageUrl is empty or null
                    if(lastSavedImage == null || lastSavedImage.isEmpty()){
                        GlideApp.with(this)
                                .asBitmap()
                                .load(R.drawable.noimageuploaded)
                                .into(imageView);
                        textView.setText("No Image Uploaded, Please try uploading an image to see this page loaded with most recent uploaded image");
                    }
                    //Else readFromFile for recentImageUrl should have value to loadImage
                    else{
                        Log.d("checkingValue",""+lastSavedImage);
                        String reference = lastSavedImage;
                        Log.d("checkingValue","" + reference);
                        loadImage(reference);
                        textView.setText(text);
                        textView.setMovementMethod(new ScrollingMovementMethod());
                    }
                }
                else{
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                    GlideApp.with(this)
                            .asBitmap()
                            .load(R.drawable.without_internet)
                            .into(imageView);
                    textView.setText("No Internet Connection, Please check your connection and reload page to view most recent image");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            GlideApp.with(this)
                    .asBitmap()
                    .load(R.drawable.without_internet)
                    .into(imageView);
            textView.setText("No Internet Connection, Please check your connection and reload page to view most recent image");
        }

    }

    public void loadImage(String reference){
        String str = reference.trim();
        URI uri = null;
        try {
            Uri myUri = Uri.parse(str);
            uri = new URI(myUri.toString());
            Log.d("URI","string converted to Uri");

            URL url = uri.toURL();
            Log.d("URL","uri converted to url");


            GlideApp.with(this)
                    .asBitmap()
                    .load(url)
                    .into(imageView);

        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String readFromFile(String fileName) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("Profile activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Profile activity", "Can not read file: " + e.toString());
        }

        return ret;
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
            Process ipProcess = runtime.exec("ping -c 1 console.firebase.google.com");
            int     exitValue = ipProcess.waitFor();
            return true;
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}