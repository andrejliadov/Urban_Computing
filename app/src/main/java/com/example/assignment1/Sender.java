package com.example.assignment1;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorderConfig;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

class Sender extends Thread {
    private int sampleRateInHz = 41000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private AudioRecord audioRecord;
    private short[] buffer;

    Context applicationContext;

    private boolean isRecording;

    Sender(Context context) {
        int audioSource = MediaRecorder.AudioSource.MIC;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        buffer = new short[1024];
        applicationContext = context;
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

    String shortArrayToString(short [] input, int readLength)
    {
        String string = "";

        for(int index = 0; index < readLength; ++index)
        {
            string += Short.toString(input[index]) + "\n";
        }

        return string;
    }



    public void run() {
        int readSize = 0;
        int count = 0;
        Data data = new Data(buffer, readSize);
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
        while (true) {
            try {
                if (isRecording) {
                    readSize = audioRecord.read(buffer, 0, buffer.length);
                    count += 1;
                    firehoseRecorder.saveRecord("AudioReading: " + Integer.toString(count) + "\n", "NoiseStream");
                    firehoseRecorder.saveRecord(shortArrayToString(buffer, readSize) + "\n", "NoiseStream");
                    firehoseRecorder.submitAllRecords();

                    TimeUnit.MILLISECONDS.sleep(10);
                }
                else {
                    TimeUnit.MILLISECONDS.sleep(10);
                    count = 0;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

