package com.example.assignment1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.IOException;

public class SendRunnable implements Runnable {

    private Context mContext;
    private String mData;
    private Locator locator;
    private Audio audio;

    SendRunnable(Context context) throws IOException {
        mContext = context;
        File.createTempFile("kinesisCache", null, mContext.getCacheDir());
        mData = "";
        locator = new Locator(mContext);
        audio = new Audio();
    }

    public void setData(){
        Location l = locator.gotLocation();
        int avg = audio.getAudio();

        mData = Integer.toString(avg) + ", " + Double.toString(l.getLatitude()) + ", " + Double.toString(l.getLongitude());
    }

    @Override
    public void run() {
        File cacheFile = new File(mContext.getCacheDir(), "kinesisCache");
        KinesisFirehoseRecorder firehoseRecorder = new KinesisFirehoseRecorder(cacheFile, Regions.EU_WEST_1, AWSMobileClient.getInstance());

        setData();
        firehoseRecorder.saveRecord(mData + "\n", "NoiseStream");
        firehoseRecorder.submitAllRecords();
        SystemClock.sleep(1000);
    }
}

class Locator {
    private LocationManager lm;
    private android.location.Location locationResult;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private Context mContext;
    private Handler sendHandler;

    public android.location.Location returnLocation(){return locationResult;}


    @SuppressLint("MissingPermission")
    Locator(Context context){
        mContext = context;
        if(lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        if(gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if(network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(android.location.Location location) {
            locationResult = location;
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(android.location.Location location) {
            locationResult = location;
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    @SuppressLint("MissingPermission")
    public android.location.Location gotLocation() {
        lm.removeUpdates(locationListenerGps);
        lm.removeUpdates(locationListenerNetwork);

        android.location.Location net_loc=null, gps_loc=null;
        if(gps_enabled)
            gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(network_enabled)
            net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(gps_loc!=null){
            locationResult = gps_loc;
        }
        else if(net_loc!=null){
            locationResult = net_loc;
        }
        else {
            locationResult = null;
        }
        return locationResult;
    }
}

class Audio {
    //Audio reader variables
    private int sampleRateInHz = 41000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private AudioRecord audioRecord;
    private short[] buffer;
    private int readSize;
    private int avg;

    Audio(){
        int audioSource = MediaRecorder.AudioSource.MIC;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        buffer = new short[1024];
        audioRecord.startRecording();
    }

    private int averageArray(short[] array, int readSize){
        int result = 0;
        int sum = 0;

        for (int i = 0; i < readSize; i++){
            sum += array[i];
        }

        result = sum/(readSize+1);
        return result;
    }

    public int getAudio() {
        readSize = audioRecord.read(buffer, 0, buffer.length);
        avg = averageArray(buffer, readSize);
        readSize = audioRecord.read(buffer, 0, buffer.length);
        avg = averageArray(buffer, readSize);
        return avg;
    }
}

