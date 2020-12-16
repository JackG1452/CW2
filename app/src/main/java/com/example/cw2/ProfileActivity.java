package com.example.cw2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ProfileActivity extends AppCompatActivity implements SensorEventListener {

    private Uri selectedImage;
    private Bitmap bitmap;
    private static int RESULT_LOAD_IMAGE = 1;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String UID;
    private TextView textView;
    private SparseArray checker;
    private String textViewText;
    private TextView textViewTitle;
    private String link;
    private ImageView imageView;
    private Button ProcessButton;
    private Button chooseButton;

    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private DatabaseReference textRef;

    private SensorManager sensorManager;
    private View view;
    private long lastUpdateTime;
    private static float SHAKE_THRESHOLD_GRAVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Home");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.recentImage:
                        startActivity(new Intent(ProfileActivity.this, GalleryActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.textGallery:
                        startActivity(new Intent(ProfileActivity.this, TextActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.logOut:
                        firebaseAuth.signOut();
                        finish();
                        Toast.makeText(ProfileActivity.this, "Logout Successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));

                }
                return false;
            }
        });

        ProcessButton = (Button) findViewById(R.id.buttonProcess);
        chooseButton = (Button) findViewById(R.id.buttonLoadPicture);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        lastUpdateTime=System.currentTimeMillis();

        imageView = findViewById(R.id.imgView);
        textViewTitle = findViewById(R.id.Pro_Title);
        textView = findViewById(R.id.textView5);
        if (isNetworkConnected() == true) {
            if (isConecctedToInternet() == true) {

                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                TextView textViewUserEmail = (TextView) findViewById(R.id.Pro_Title);
                textViewUserEmail.setText("Welcome " + currentUser.getEmail());
                UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference();

                mDatabase = FirebaseDatabase.getInstance();
                myRef = mDatabase.getReference(UID).child("images");
                textRef = mDatabase.getReference(UID).child("textValues");
            }
            else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                GlideApp.with(this)
                        .asBitmap()
                        .load(R.drawable.without_internet)
                        .into(imageView);
                chooseButton.setVisibility(View.GONE);
                ProcessButton.setVisibility(View.GONE);
                textViewTitle.setText("Error with connection to Firebase");
                textView.setText("No Internet Connection, Please check your connection and reload page to view home");
            }
        }
        else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            GlideApp.with(this)
                    .asBitmap()
                    .load(R.drawable.without_internet)
                    .into(imageView);
            chooseButton.setVisibility(View.GONE);
            ProcessButton.setVisibility(View.GONE);
            textViewTitle.setText("Error with connection to Firebase");
            textView.setText("No Internet Connection, Please check your connection and reload page to view home");
        }

        final Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                imageView = (ImageView) findViewById(R.id.imgView);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(this, "Image Selected", Toast.LENGTH_LONG).show();
                Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
                buttonLoadImage.setText("Choose New Image");
                textView = findViewById(R.id.textView);
                textView.setText("");
                textView = findViewById(R.id.textView);
                processText(textView);
                String check = textView.getText().toString();
                Log.d("ValueMessage", "" + check);

            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Failure to Select Image", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void uploadImageAndText(View view) {
        if (isNetworkConnected() == true) {
            if (isConecctedToInternet() == true) {
                if (selectedImage != null && checker.size() != 0) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    int random = ThreadLocalRandom.current().nextInt();
                    String imageName = "image" + random;
                    final StorageReference ref = storageReference.child(UID + "/images/" + imageName);
                    UploadTask uploadTask = ref.putFile(selectedImage);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                int random = ThreadLocalRandom.current().nextInt();
                                String rando = "image" + random;

                                Uri downloadUri = task.getResult();
                                Log.d("Checking Value", "DownloadLink:" + downloadUri);
                                link = downloadUri.toString();
                                writeToTempFile(downloadUri.toString(), UID+"_temp_Details.txt");
                                writeToTempFile(textViewText, UID+"_temp_Write.txt");

                                HashMap<String, Object> result = new HashMap<>();
                                result.put("link", link);
                                result.put("text", textViewText);

                                FirebaseDatabase.getInstance().getReference(UID).child("ImageDetails").child(rando).updateChildren(result);

                                progressDialog.dismiss();
                                textView = findViewById(R.id.textView);
                                textView.setText("");
                                Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                    imageView = (ImageView) findViewById(R.id.imgView);
                    imageView.setImageResource(R.drawable.default_image);
                    Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
                    buttonLoadImage.setText("Choose Image");
                } else {
                    Toast.makeText(ProfileActivity.this, "Error: No text to upload, please choose image with text", Toast.LENGTH_SHORT).show();
                    ProcessButton.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                GlideApp.with(this)
                        .asBitmap()
                        .load(R.drawable.without_internet)
                        .into(imageView);
                chooseButton.setVisibility(View.GONE);
                ProcessButton.setVisibility(View.GONE);
                textViewTitle.setText("Error with connection to Firebase");
                textView.setText("No Internet Connection, Please check your connection and reload page to view home");
            }
        }
        else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            GlideApp.with(this)
                    .asBitmap()
                    .load(R.drawable.without_internet)
                    .into(imageView);
            chooseButton.setVisibility(View.GONE);
            ProcessButton.setVisibility(View.GONE);
            textViewTitle.setText("Error with connection to Firebase");
            textView.setText("No Internet Connection, Please check your connection and reload page to view home");
        }
    }

    public void processText(View view){
        textView = findViewById(R.id.textView);
        TextRecognizer textDetector = new TextRecognizer.Builder(getApplicationContext()).build();
        if(!textDetector.isOperational()){
            textView.setText("Could not set up the detector!");
            return;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray items = textDetector.detect(frame);
        checker = items;
        if(items.size()==0){
            Toast.makeText(this,"Failure, No text detected", Toast.LENGTH_LONG).show();
        }else{
            StringBuilder stringBuilder = new StringBuilder();
            for(int i =0; i <items.size(); i++){
                TextBlock item = (TextBlock) items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append("\n");
                for(Text line : item.getComponents()){
                    Log.v("lines", line.getValue());
                    for(Text element : line.getComponents()){
                        Log.v("element", element.getValue());
                    }
                }
            }
            textView.setText(stringBuilder.toString());
            textView.setMovementMethod(new ScrollingMovementMethod());
            textViewText = stringBuilder.toString();

        }
    }

    public void writeToTempFile(String data, String textFileName) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(textFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event){
        float[] values = event.values;
        //Movement
        float x =values[0];
        float y =values[1];
        float z =values[2];

        float gx = x / SensorManager.GRAVITY_EARTH;
        float gy = y / SensorManager.GRAVITY_EARTH;
        float gz = z / SensorManager.GRAVITY_EARTH;

        //gForce will be close to 1 when there is no movement.
        float gForce = (float)Math.sqrt(gx * gx + gy * gy + gz * gz );

        long currentTime = System.currentTimeMillis();
        if(gForce >= SHAKE_THRESHOLD_GRAVITY)
        {
            if(currentTime - lastUpdateTime <200){
                return;
            }
            lastUpdateTime=currentTime;
            //Toast.makeText(this, "Device was shaken", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(ProfileActivity.this, GalleryActivity.class));
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //Register this class as a listener for the orientation and
        //accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        //unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
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