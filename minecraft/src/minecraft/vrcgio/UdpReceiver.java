package vrcgio;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpReceiver extends Thread {
	private Thread t;
	private String threadName = new String("receive");
	private byte[] buf;
	private DatagramSocket socket;
	private float pitchValue;
	private float yawValue;


	public UdpReceiver(String name) {
		super(name);
		buf = new byte[100];
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
				//System.out.println("Received "+packet.toString());
				String str = new String(buf, "utf-8");
				String[] splitStr = str.split(" ");
				String yawStr = splitStr[0];
				String pitchStr = splitStr[1];
				
				synchronized(this){
					try {
						pitchValue = (float) (Float.parseFloat(pitchStr)*180/Math.PI);
						yawValue = (float) (Float.parseFloat(yawStr)*180/Math.PI);
					}
					catch (Exception e){}
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
	
	public synchronized float getPitch(){
		
		return  pitchValue; // TODO HANDLE Converting
	}
	public synchronized float getYaw(){
		return  yawValue;
		
	}

}

