package com.example.assignment1;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Send extends AsyncTask<Data, Void, Void> {
    public Socket socket;
    PrintWriter writer;

    @Override
    protected Void doInBackground(Data... voids) {

        Data data = voids[0];

        try {
            socket = new Socket("37.228.224.150", 10203);
            writer = new PrintWriter(socket.getOutputStream());
            for(int i = 0; i < data.readLength; i++) {
                writer.write(data.audioData[i]);
            }
            writer.flush();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
