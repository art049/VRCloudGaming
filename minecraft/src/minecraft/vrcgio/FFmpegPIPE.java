package vrcgio;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuAbstractProcessHandler; 

import net.minecraft.client.shader.Framebuffer;



public class FFmpegPIPE extends Thread {
	private BufferedImage current;
	private OutputStream pipestream;
	private NuProcess process;
	private Boolean changed = false;
	private int i=0;
	public FFmpegPIPE(){
		//ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-f", "image2pipe", "-r", "1", "-vcodec", "png", "-i", "-", "-vcodec", "libx264", "out.mp4");
		//String command = "ffmpeg -i pipe:0 -vsync 2 -s 480x360 -c:v libx264 -preset veryfast -tune zerolatency -pix_fmt yuv444p -x264opts crf=20:vbv-maxrate=3000:vbv-bufsize=50:intra-refresh=1:slice-max-size=1500:keyint=25:ref=1:bframes=0:b-adapt=1 -f mpegts -f mpegts -fflags nobuffer -threads 4 udp://127.0.0.1:1234";
		//List<String> cmdlist = Arrays.asList(command.split(" "));
		/*ProcessBuilder builder = new ProcessBuilder(cmdlist);
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);*/
		//NuProcessBuilder pb = new NuProcessBuilder(cmdlist);
		//ProcessHandler handler = new ProcessHandler();
		//pb.setProcessListener(handler);
		
		//LottaProcessHandler handler = new LottaProcessHandler();
		//pb.setProcessListener(handler);
		//process = pb.start();

		/*try {
			this.process = builder.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			process = null;
			e.printStackTrace();
		}*/
		//this.pipestream = process.getOutputStream();
		
	}
	public void pushImage(BufferedImage p){	
		this.current = p;
		if(!this.isAlive())this.start();
		/*ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		try{
			ImageIO.write(p, "png", pipestream);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//System.out.println(i);
		/*if(i++> 500){
			process.destroy();
		}*/
		//saveScreenshot(new File("./"), "./", "a"+i++, int width, int height, Framebuffer buffer)
		//pipestream.read(tmp.toByteArray());*/
	}
	
	public void run(){
		while(true){
			try {
					ImageIO.write(this.current, "png", new File("/home/arthur/virtueldd/"+i++ +"mine.png") );
					/*ByteArrayOutputStream s = new ByteArrayOutputStream();
					
					byte [] barray = s.toByteArray();
					ByteBuffer bbuffer = ByteBuffer.allocate(barray.length);
					bbuffer.put(barray);
					bbuffer.flip();
					process.writeStdin(bbuffer);*/
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
	}
	
	class ProcessHandler extends NuAbstractProcessHandler {
		   private NuProcess nuProcess;

		   @Override
		   public void onStart(NuProcess nuProcess) {
		      this.nuProcess = nuProcess;
		   }

		   @Override
		   public void onStdout(ByteBuffer buffer, boolean closed) {
		      byte[] bytes = new byte[buffer.remaining()];
		      buffer.get(bytes);
		      System.out.println(new String(bytes));
		   }

	        @Override
	        public void onStderr(ByteBuffer buffer, boolean closed)
	        {
	            this.onStdout(buffer, false);
	        }
    }
}
