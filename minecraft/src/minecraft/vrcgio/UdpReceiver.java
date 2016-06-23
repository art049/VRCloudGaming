package vrcgio;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpReceiver implements Runnable {
	private Thread t;
	private String threadName = new String("receive");
	byte[] buf;
	private DatagramSocket socket;
	private int pitchValue;
	private int yawValue;


	public void startThreadReceiver() {
		if (t == null) {
			t = new Thread(this, "receive");
			t.start();
		}
	}

	public UdpReceiver() {
		buf = new byte[5];
		try {
			socket = new DatagramSocket(8345);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void run() {
		while (true) {
			try {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				synchronized(this){
					int a ;//Store pitch and yaw
				}

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public synchronized void getPitch(){
		
	}

}

