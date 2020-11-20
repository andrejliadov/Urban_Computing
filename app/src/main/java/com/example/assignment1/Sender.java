package com.example.assignment1;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class Sender extends Thread {
    private int sampleRateInHz = 41000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private AudioRecord audioRecord;
    private short[] buffer;

    private boolean isRecording;

    Sender() {
        int audioSource = MediaRecorder.AudioSource.MIC;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        buffer = new short[1024];
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

    public void run() {
        Socket socket = null;
        PrintWriter writer = null;

        int readSize = 0;
        Data data = new Data(buffer, readSize);

        try {
            socket = new Socket("192.168.0.153", 10203);
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioRecord.startRecording();
        while (true) {
            try {
                if (isRecording) {
                    readSize = audioRecord.read(buffer, 0, buffer.length);
                    data.audioData = buffer;
                    data.readLength = readSize;
                    if(writer != null) {
                        send(data, writer);
                    }
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

