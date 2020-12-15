package com.example.cw2;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class ReadWriteFiles extends AppCompatActivity {

//    public static void createTempFile() {
//        try {
//            File myObj = new File("temp_text.txt");
//            if (myObj.createNewFile())
//            {
//            }
//            else
//                {
//            }
//        } catch (IOException e) {
//            Log.e("Profile activity", "Can not create file: " + e.toString());
//        }
//    }
//    public static void writeToTempFile(String data, String outputFile) {
//        createTempFile();
//        try {
//            FileWriter writer = new FileWriter(outputFile);
//            writer.append(data);
//            //writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            Log.e("Profile activity", "Can not read file: " + e.toString());
//        }
//
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("temp_Details.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }

//    public static String readFromFile() {

//        String ret = "";

//        try {
//            File myObj = new File("temp_text.txt");
//            Scanner myReader = new Scanner(myObj);
//            while (myReader.hasNextLine()) {
//                ret = myReader.nextLine();
//            }
//            myReader.close();
//        } catch (FileNotFoundException e) {
//            Log.e("Profile activity", "File not found: " + e.toString());
//        } catch (IOException e) {
//            Log.e("Profile activity", "Can not read file: " + e.toString());
//        }

//
//        try {
//            InputStream inputStream = openFileInput("temp_Details.txt");
//
//            if ( inputStream != null ) {
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                String receiveString = "";
//                StringBuilder stringBuilder = new StringBuilder();
//
//                while ( (receiveString = bufferedReader.readLine()) != null ) {
//                    stringBuilder.append("\n").append(receiveString);
//                }
//
//                inputStream.close();
//                ret = stringBuilder.toString();
//            }
//        }
//        catch (FileNotFoundException e) {
//            Log.e("Profile activity", "File not found: " + e.toString());
//        } catch (IOException e) {
//            Log.e("Profile activity", "Can not read file: " + e.toString());
//        }
//
//        return ret;
//    }
}
