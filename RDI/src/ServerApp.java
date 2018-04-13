
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ServerApp {

	public static void main(String[] args) {
		
        // Local SAP - lo
		// Local socket - localSocket
		InetSocketAddress lo = null;
		mySocketServer localSocket = null;
        try {
        	// Identificação do SAP local
        	InetAddress loIP = InetAddress.getByName("127.0.0.2");
        	lo = new InetSocketAddress(loIP, 30000);
        	
			// Criação e binding do soket 
			localSocket = new mySocketServer(lo);
			System.out.println("LocalIP: "+localSocket.getLocalAddress().toString());
			System.out.println("LocalPort: "+localSocket.getLocalPort());
	    } catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Receção de pacotes com tamanho até 10000 bytes 
		while (true) {
				byte[] data = new byte[10000];
				// Instaciaçáo de um objecto datagrama UDP
				DatagramPacket p = new DatagramPacket(data, data.length);

				// Receção de um datagrama
				localSocket.receive(p); 
				System.out.println("Recebidos do SAP "+p.getLength()+" bytes com origem em "
						                       +p.getSocketAddress().toString());			
		}
	}
}