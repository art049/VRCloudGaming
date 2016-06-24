package paf.pafvr;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by arthur on 6/24/16.
 */
public class GyroThread extends Thread {
    private float yaw=0,pitch=0;
    private Context ctx;
    public GyroThread(Context ctx){
        super();
        this.ctx = ctx;
    }
    public void setYawPitch(float yaw,float pitch){
        this.yaw = yaw;
        this.pitch = pitch;
    }
    @Override
    public void run(){
        final String server_ip = "192.168.42.141";
        final int server_port = 8345;
        String messageStr;
        DatagramSocket s;
        try {
            InetAddress local;
            s = new DatagramSocket();
            while(true) {
                try {

                    local = InetAddress.getByName(server_ip);
                    messageStr = yaw + " " + pitch;
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
                SystemClock.sleep(50);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
}
