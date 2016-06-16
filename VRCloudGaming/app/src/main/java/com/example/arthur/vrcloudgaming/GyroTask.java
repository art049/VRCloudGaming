package com.example.arthur.vrcloudgaming;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by arthur on 6/16/16.
 */
public class GyroTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        String messageStr = params[0];
        int server_port = 1234;

        DatagramSocket s;
        try {
            s = new DatagramSocket();
            InetAddress local;
            try {
                local = InetAddress.getByName("192.168.1.2");
                int msg_length = messageStr.length();
                byte[] message = messageStr.getBytes();
                DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
                try {
                    s.send(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }
}
