package com.example.osmz;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class RequestSender extends Thread {

    Socket s;
    Semaphore lock;
    Handler messageHandler;
    String pathName;

    public RequestSender(Socket s, Semaphore lock, Handler handler, String pathName) {
        this.s = s;
        this.lock = lock;
        this.messageHandler = handler;
        this.pathName = pathName;
    }

    @Override
    public void run() {
        Log.d("SERVER", "Socket Accepted");
        try {
            messageHandler.sendEmptyMessage(1); //socket count
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String tmp = in.readLine();
            String filename = this.pathName;
            if (tmp != null) {

                try {
                    //Log.d("SERVER","Trying to open file " + path+filename);
                    File f = new File(pathName);
                    Log.d("something", pathName);

                    FileInputStream fileIS = new FileInputStream(f);

                    //Log.d("SERVER","HTTP/1.0 200 OK");
                    out.write("HTTP/1.0 200 OK\n");

                    if ((filename.endsWith(".htm") || filename.endsWith(".html")))
                        out.write("Content-Type: text/html\n");
                    if ((filename.endsWith(".jpg") || filename.endsWith(".jpeg")))
                        out.write("Content-Type: image/jpeg\n");
                    if ((filename.endsWith(".png"))) out.write("Content-Type: image/png\n");

                    out.write("Content-Length: " + String.valueOf(f.length()) + "\n");
                    out.write("\n");
                    out.flush();

                    int c;
                    byte[] buffer = new byte[1024];

                    while ((c = fileIS.read(buffer)) != -1) {
                        o.write(buffer, 0, c);
                    }

                    fileIS.close();
                    s.close();
                    Log.d("SERVER", "Socket Closed");
                } catch (FileNotFoundException e) {
                    Log.d("SERVER", "HTTP/1.0 404 Not Found");
                    out.write("HTTP/1.0 404 Not Found\n\n");
                    out.write("Page not found");
                    out.flush();
                }
            }
        } catch (IOException e) {

        } finally {
            lock.release();
            messageHandler.sendEmptyMessage(-1);
            Log.d("THREADS", "Free lock " + lock.availablePermits());

        }
    }
}
