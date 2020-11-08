package com.hassanassowe.raspberrycam.Classes;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/*
The SSH class handles ALL SSH processes
-Connect
-Disconnect
-Command Line
 */
public class SSH {
    private String username;
    private String password;
    private String hostname;
    private int port;

    JSch ssh;
    Session session;

    public SSH(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            ssh = new JSch();
            session = ssh.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");

        } catch (JSchException e) {
            Log.i("SSH", "Connection FATAL error");
            e.printStackTrace();
        }
        class connectAsyncTask extends AsyncTask<Void, Void, Boolean> {

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    session.connect(30000);
                } catch (JSchException e) {
                    Log.i("SSH", "Connection FATAL error");
                    e.printStackTrace();
                    return false;
                }
                return null;
            }
        }
        new connectAsyncTask().execute();
    }

    private void disconnect() {
        try {
            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            Log.i("SSH", "Connected: " + session.isConnected());
            return session.isConnected();
        } catch (Exception e) {
            Log.i("SSH", "Connected FATAL: " + e.toString());
            return false;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public JSch getSsh() {
        return ssh;
    }

    public Session getSession() {
        return session;
    }
}
