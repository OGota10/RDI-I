package cenarioIII;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientApp extends Thread {

	public static void main(String[] args) {
		System.out.println(args[0]);
		final double ARRIVAL_RATE = Double.parseDouble(args[0]);
		
        // Local SAP - lo; SAP destino . 
		// Local socket - localSocket
		InetSocketAddress lo = null;
		mySocketClient localSocket = null;
		
        try {
        	// Identificação do SAP local
        	InetAddress loIP = InetAddress.getByName("127.0.0.1");
        	lo = new InetSocketAddress(loIP, 20000);

        	// Criação e binding do soket 
			localSocket = new mySocketClient(lo);
			System.out.println("LocalIP: "+localSocket.getLocalAddress().toString());
			System.out.println("LocalPort: "+localSocket.getLocalPort());
	    } catch (UnknownHostException e) {	
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
        
		// Envio de 25 mensagens ('A' a 'Y')- com um tamanho de 100 bytes cada
		for (int i=0; i<25; i++) {
			byte[] data = new byte[100];
			for (int j=0; j<100; j++) {
				data[j]=(byte)(i+'A');
			}
			try {
	        	// Identificação do SAP destino
	        	InetAddress IPdestino = InetAddress.getByName("127.0.0.2");
	        	InetSocketAddress SAPdestino = new InetSocketAddress(IPdestino, 30000);    
	        	// Criação do datagrama UDP
				DatagramPacket p = new DatagramPacket(data, data.length, SAPdestino);

               	long beginTime = System.currentTimeMillis();
				// Envio do datagrama UDP
				localSocket.send(p);
        		long endTime = System.currentTimeMillis();        		
				
				System.out.println("Colocados no SAP "+p.getLength()+" bytes com destino a "
						           +p.getAddress().toString()+":"+p.getPort() + " em "+
						           (endTime-beginTime) + " miliseg");

				double interval;
			    // Tempo de espera entre gerações de pacotes - D (intervalo fixo)
			    interval = (1.0/ARRIVAL_RATE);
			    // Tempo de espera entre gerações de pacotes - M (intervalo exponencial negativo)
				//interval = -1.0/ARRIVAL_RATE*Math.log(Math.random());
				sleep((long)(interval*1000.0)); // Conversão para miliseg.

			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
