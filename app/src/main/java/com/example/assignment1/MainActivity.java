package com.example.assignment1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

import java.io.IOException;
import java.sql.Time;

public class MainActivity extends AppCompatActivity {

    private SendHandlerThread sendHandlerThread;
    private Handler sendHandler;

    private Button start, stop;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendHandlerThread.quit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initialiseAWS();
        } catch (AmplifyException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        start = findViewById(R.id.startButton);
        stop = findViewById(R.id.stopButton);
        start.setEnabled(true);
        stop.setEnabled(false);

        getPermissions();

       /* //Record the audio
        audioHandlerThread = new AudioHandlerThread("AudioHandlerThread", Process.THREAD_PRIORITY_AUDIO);
        audioHandlerThread.start();
        audioHandler = new Handler(audioHandlerThread.getLooper());*/

        //Get the location
        /*locationHandlerThread = new LocationHandlerThread("LocationHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
        locationHandlerThread.start();
        locationHandler = new Handler(locationHandlerThread.getLooper());*/

    }

    /*public void sendMessage() throws IOException {
        AudioRunnable record = new AudioRunnable(sendHandler);
        LocationRunnable locate = new LocationRunnable(this, sendHandler);

        audioHandler.post(record);
        locationHandler.post(locate);
    }*/

    public void onClickStart(View v) throws InterruptedException {
        //This thread starts to record audio(producer)
        start.setEnabled(false);
        stop.setEnabled(true);

        //Send the data
        try { sendHandlerThread = new SendHandlerThread("SendHandlerThread", Process.THREAD_PRIORITY_BACKGROUND); } catch (IOException e) { e.printStackTrace(); }
        sendHandlerThread.start();
        sendHandler = new Handler(sendHandlerThread.getLooper());

        try {
            SendRunnable send = new SendRunnable(this);
            while(true) {
                sendHandler.post(send);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickStop(View v){
        //sender.setIsRecording(false);

        start.setEnabled(true);
        stop.setEnabled(false);

        sendHandlerThread.quit();
    }

    private void getPermissions(){
        //Add permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 2);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 3);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        }
    }

    private void initialiseAWS() throws AmplifyException {
        Amplify.addPlugin(new AWSCognitoAuthPlugin());
        Amplify.configure(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case 10:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }
            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }
            case 3:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }
        }
    }

}


