import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class mySocketServer extends mySocket {
	static private int counter = 1;
	
	public mySocketServer(InetSocketAddress localAddress) {
		super(localAddress, "./receiver.data");
	}
	
	public void run() {
		while(true) {
			try {
				// Ler a myPDU via UDP
				myPDU p = receiveFromUDP();
				// Escreve log
				//log("UDP", "MS", (myPDU.Type.I).toString(), p.getSeq());
				System.out.println("Recebida PDU I, tempo transferência: "+
						           Long.toString(System.currentTimeMillis()-p.getTimestamp())+
						            " miliseg.");
				
				// Coloca a IDU do utilizador na fila
				queue.put(p.getIDU());
				if(p.getSeq() == counter) {		
				//	local.send(iduUDP);
					changeCounter();}
//				}else {
//					local.send(iduUDP);
//				}
				// Implementacao da parte Report do protocolo
					// Criar uma PDU Report
					// com o n.o sequencia da ultima PDU recebida, nao tem dados
				myPDU r = new myPDU(myPDU.Type.R, counter);
				DatagramPacket iduUDP = new DatagramPacket(r.toBytes(), r.toBytes().length,
												           p.getIDU().getSocketAddress());	
				
					local.send(iduUDP);
					changeCounter();
					// Escreve log para a PDU R
					//log("MS", "UDP", (myPDU.Type.R).toString(), p.getSeq());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SocketException e) {
			    e.printStackTrace();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private myPDU receiveFromUDP() {
		byte[] pdu = new byte[MAXDATA+(4+4+8)];
		DatagramPacket iduUDP = new DatagramPacket(pdu, pdu.length);
		try {
			// Recebe a IDU vinda do socket UDP.
			local.receive(iduUDP);
			// Da IDU recebida constroi a myPDU que foi enviada
			return new myPDU(iduUDP);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int changeCounter() {
		if(counter == 0) 
			counter = 1;
		else 
			counter = 0;
		return counter;
	}
	
	public void receive(DatagramPacket myIDU) {
		if (this.isInterrupted()) {this.start();}
		try {
			// Recolhe da fila (bloqueante) a IDU recebida para dar ao user
			DatagramPacket receivedIDU = queue.take();
			myIDU.setData(receivedIDU.getData());
			myIDU.setAddress(receivedIDU.getAddress());
			myIDU.setPort(receivedIDU.getPort());
			log("MS", "APP", (myPDU.Type.I).toString(), counter);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}