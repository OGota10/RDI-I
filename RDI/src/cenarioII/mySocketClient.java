package cenarioII;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class mySocketClient extends mySocket {
	static private int counter = 0;
	final private int WAIT_R = 2000; // Espera pela PDU R - em miliseg.
	static private boolean idle = true;
	protected HashMap<DatagramPacket, Long> generationTimes; // Hash para guardar tempos de geracao
	
	public mySocketClient(InetSocketAddress localAddress) {
		super(localAddress, "./sender.data");
		generationTimes = new HashMap<DatagramPacket, Long>();
	}
	
	public void run() {
		idle = false;
		DatagramPacket userIDU;
		while(!idle) {
			try {
				// Receber a IDU vinda do utilizador através da fila - queue
				userIDU = queue.take();
				// Enviar a SDU do utilizador através no mySocket
				sendToUDP(userIDU);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		return;
	}
	
	public void sendToUDP(DatagramPacket myIDU) {
		// Construir a myPDU - tipo I, nSeq dado por counter
		myPDU p = new myPDU(myIDU, myPDU.Type.I, counter);
		// Verficar quanto tempo na fila - queue
		Long genTime = generationTimes.get(myIDU);
		generationTimes.remove(myIDU);
		System.out.println("Tempo na fila : "+
				            Long.toString(System.currentTimeMillis()-genTime.longValue())+
				            " miliseg.");
		
		try {
			// Construir o datagrama UDP que vai transportar a myPDU
			// A myPDU é a SDU do protocolo UDP que a vai transportar
			byte[] myPDUbytes = p.toBytes();
			DatagramPacket iduUDP = new DatagramPacket (myPDUbytes, myPDUbytes.length,
					                                    myIDU.getSocketAddress());
			// Pedido para enviar IDU do protocolo UDP
			local.send(iduUDP);
			waitForResponse(iduUDP);
			// Escreve log
			//log("MS", "UDP", (myPDU.Type.I).toString(), p.getSeq());			
		
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void waitForResponse(DatagramPacket myIDU) {
	try {
		byte[] rdata = new byte[4+4+8];
		DatagramPacket rUDP = new DatagramPacket(rdata, rdata.length);
		local.setSoTimeout(WAIT_R); // timeout para recepcao, se comentado bloqueia à espera
		local.receive(rUDP);
		myPDU rPDU = new myPDU(rUDP);
		if(rPDU.getSeq() != counter) 
			changeCounter();
		else
			sendToUDP(myIDU);
		//log("UDP", "MS", (myPDU.Type.R).toString(), rPDU.getSeq());
		
		} catch (SocketTimeoutException e) {
			sendToUDP(myIDU);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	public int changeCounter() {
		if(counter == 0) 
			counter = 1;
		else 
			counter = 0;
		return counter;
	}
	
	public void send(DatagramPacket p) {
		try {
			// Colocar no HashMap de tempos de geraçao
			generationTimes.put(p, Long.valueOf(System.currentTimeMillis()));
			// Colocar na fila para emissao pelo protocolo mySocket
			queue.put(p);
			// Criar o log
			log("APP","MS",(myPDU.Type.I).toString(), counter);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}