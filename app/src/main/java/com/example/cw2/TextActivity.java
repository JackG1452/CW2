package com.example.cw2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

public class TextActivity extends AppCompatActivity {

    private ListView listView;
    private String UID;
    private TextView textView;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__text);
        getSupportActionBar().setTitle("Text Gallery");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.textGallery);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.recentImage:
                        startActivity(new Intent(TextActivity.this, GalleryActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(TextActivity.this, ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.textGallery:
                        startActivity(new Intent(TextActivity.this, TextActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.logOut:
                        firebaseAuth.signOut();
                        finish();
                        Toast.makeText(TextActivity.this, "Logout Successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(TextActivity.this, LoginActivity.class));
                }
                return false;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listView = findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.textView);




        if (isNetworkConnected() == true) {
            try {
                if (isConnected() == true) {

                    textView.setVisibility(View.GONE);
                    final ArrayList<String> list = new ArrayList<>();
                    final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_item, list);
                    listView.setAdapter(adapter);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(UID).child("ImageDetails");
                    reference.addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                /*Might Be useful for deleting, dont remove
                                Log.d("Checking Value",""+snapshot.getKey().toString());*/
                                Information info = snapshot.getValue(Information.class);
                                //String txt = info.getLink() + " : " + info.getText();
                                String txt = info.getText();
                                list.add(txt);
                            }
                            adapter.notifyDataSetChanged();

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                else{
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                    textView.setText("No Internet Connection, Please check your connection and reload page to view most recent image");
                    listView.setVisibility(View.GONE);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            textView.setText("No Internet Connection, Please check your connection and reload page to view most recent image");
            listView.setVisibility(View.GONE);
        }


//        listView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("", listView.getText());
//                clipboard.setPrimaryClip(clip);
//                Toast.makeText(TextActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 console.firebase.google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

}