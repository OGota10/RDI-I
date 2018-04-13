package cenarioIII;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class mySocket extends Thread {
	static public int MAXDATA=10000;       // Tamnho maximo para a SDU
	static public int REPORT_INTERVAL = 4; // N.o de PDUs I para a producao de um relatorio R
	
	protected LinkedBlockingQueue<DatagramPacket> queue;     // Fila para guardar IDUs do/para user
	protected DatagramSocket local;
	protected FileWriter fd;
	
	public mySocket (InetSocketAddress localAddress, String file) {
		queue = new LinkedBlockingQueue<DatagramPacket>();
		try {
			local = new DatagramSocket(localAddress);
			fd = new FileWriter(file);
			this.start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getLocalAddress() {
		return local.getLocalAddress();
	}
	
	public int getLocalPort() {
		return local.getLocalPort();
	}
	
	public void log(String from, String to, String type, int seq) {
		String time = Long.toString(System.currentTimeMillis());
		String qsize = Integer.toString(queue.size());
		String nseq = Integer.toString(seq);
		try {
			fd.write(time+" "+qsize+" "+from+" "+to+" "+type+" "+nseq+"\n");
			fd.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
