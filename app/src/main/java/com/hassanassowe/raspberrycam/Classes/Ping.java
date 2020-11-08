package com.hassanassowe.raspberrycam.Classes;

import android.util.Log;

import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

//Thie Ping class is designed to check if a camera connection is active and available.
public class Ping {
    protected Boolean ping = false;


    public Boolean Ping(RaspberryPi instance, int timeout) throws IOException {
        Socket socket = new Socket();
        try {
            Log.i("Ping", "Attempting ping.");
            socket.connect(new InetSocketAddress(instance.getAddress(), instance.getPort()), timeout);
            if (socket.isConnected()) {
                Log.i("Ping", "Ping Sucessful");
                socket.close(); //CLOSE THREAD ALWAYS
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close(); //CLOSE THREAD ALWAYS
        return false;
    }
}


