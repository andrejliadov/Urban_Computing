package com.example.assignment1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorderConfig;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static androidx.core.content.ContextCompat.startActivity;

class Sender extends Thread {
    //Audio reader variables
    private int sampleRateInHz = 41000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private AudioRecord audioRecord;
    private short[] buffer;

    private Context applicationContext;
    private Location location;
//    private LocationThread locationProcess;

    private boolean isRecording;

    Sender(Context context) {
        int audioSource = MediaRecorder.AudioSource.MIC;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        buffer = new short[1024];
        applicationContext = context;
        /*locationProcess = new LocationThread(applicationContext);
        location = locationProcess.getReadLocation();*/
    }

    private void send(Data data, PrintWriter writer){
        for(int i = 0; i < data.readLength; i++) {
            writer.write(data.audioData[i]);
        }
        writer.flush();
    }

    public void setIsRecording(boolean val){
        isRecording = val;
    }

    public File getAudioStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_MUSIC), albumName);
        if (!file.mkdirs()) {
            Log.e("FILE", "Directory not created");
        }
        return file;
    }

    private String shortArrayToString(short [] input, int readLength)
    {
        String string = "";

        for(int index = 0; index < readLength; ++index)
        {
            string += Short.toString(input[index]) + "\n";
        }

        return string;
    }

    private int averageArray(short[] array, int readSize){
        int result = 0;
        int sum = 0;

        for (int i = 0; i < readSize; i++){
            sum += array[i];
        }

        result = sum/readSize;
        return result;
    }

    public void run() {
        int readSize = 0;
        int avg = 0;
        try {
            File.createTempFile("kinesisCache", null, applicationContext.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }

        File cacheFile = new File(applicationContext.getCacheDir(), "kinesisCache");

        KinesisFirehoseRecorder firehoseRecorder = new KinesisFirehoseRecorder(cacheFile, Regions.EU_WEST_1, AWSMobileClient.getInstance());

        KinesisRecorderConfig kinesisRecorderConfig = firehoseRecorder.getKinesisRecorderConfig();
        Long maxStorageSize = kinesisRecorderConfig.getMaxStorageSize();

        audioRecord.startRecording();
       /* while (true) {
            try {
                if (isRecording) {
                    location = locationProcess.getReadLocation();
                    readSize = audioRecord.read(buffer, 0, buffer.length);
                    avg = averageArray(buffer, readSize);
                    firehoseRecorder.saveRecord(Integer.toString(avg) +", " + location.getLatitude() + ", " + location.getLongitude() + "\n", "NoiseStream");
                    firehoseRecorder.submitAllRecords();

                    TimeUnit.MILLISECONDS.sleep(10);
                }
                else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}

/*class LocationThread{
    private Location readLocation;
    private MyLocation.LocationResult result;
    private MyLocation location;
    private Context mContext;

    LocationThread(Context applicationContext) {
        mContext = applicationContext;
        //Setup location service
        result = new MyLocation.LocationResult() {
            public Location readLoc;

            @Override
            public void gotLocation(Location location) {
                setReadLoc(location);
                readLoc = location;
            }
        };
        location = new MyLocation();

        //Get initial reading
        if (location.getLocation(applicationContext, result)) {
            SystemClock.sleep(2500);
            readLocation = result.readLoc;
        }
    }

    public Location getReadLocation(){return readLocation;}
}*/
