package com.example.osmz;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketServer extends Thread {

	ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;
    Handler messageHandler;
    String pathName;

    private Semaphore lock = new Semaphore(10);

    public SocketServer(Handler messageHandler, String pathName) {
        this.messageHandler = messageHandler;
        this.pathName = pathName;
    }

    public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}


	public void run() {
        try {
        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                try{
                    lock.acquire();
                    Log.d("THREADS", "Get LOCK " + lock.availablePermits());
                    RequestSender rs = new RequestSender(s,  lock, messageHandler, this.pathName);

                    rs.start();
                }catch(InterruptedException e){
                    Log.d("SERVER", "No free slots");
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

}
