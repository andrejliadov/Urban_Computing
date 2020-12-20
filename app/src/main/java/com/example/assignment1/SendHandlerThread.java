package com.example.assignment1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;

import java.io.IOException;

public class SendHandlerThread extends HandlerThread {

    public SendHandlerThread(String name, int priority) throws IOException {
        super("SendHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
    }
}
