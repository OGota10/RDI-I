package cenarioIV;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class myPDU {
	// Enumerado para os diferentes tipos de myPDU
	public enum Type {
		I(0), R(1);
		public int value;
		private Type(int value) {
			this.value = value;
		}
		public int getValue() {return value; };
	};
	
	// PCI da myPDU (tipo, n.o sequencia, marca de tempo)
	private Type type;
	private int nSeq;
	private long timeStamp;
	// IDU que será fornecida pelo utilizador
	private DatagramPacket userIDU;

    public myPDU(Type t, int seq) {
    	type = t;
    	nSeq = seq;
    	timeStamp = System.currentTimeMillis();
    	userIDU = null;
    }
	
	public myPDU(DatagramPacket idu, Type t, int seq) { 
		// Criaçao de uma myPDU a partir da IDU do utilizador
		type = t;
		nSeq=seq;
		timeStamp = System.currentTimeMillis();
		userIDU = idu;
	}

	public myPDU(DatagramPacket udp) {
		// Criaçao de uma myPDU a partir da IDU recebida via socket.
		// A myPDU vai ser extraida dos dados recebidos via UDP - udp
		byte[] myPDUbytes = udp.getData();
		byte[] data = new byte[udp.getData().length - (4+4+8)];
		
		try {
			// Copiar os 4 bytes de type do myPCI (bytes 0 a 3 da myPDU)
			int type_value = 0;
			for (int i=0; i<4; i++) { 
				type_value = (type_value << 8);
				type_value = type_value+((myPDUbytes[i]) & 0xFF);
			}
			if (type_value == Type.I.value) type = Type.I;
			if (type_value == Type.R.value) type = Type.R;
			
			// Copiar os 4 bytes de nSeq do myPCI (bytes 4 a 7 da myPDU)
			nSeq = 0;
			for (int i=4; i<8; i++) { 
				nSeq = (nSeq << 8);
				nSeq = nSeq+((myPDUbytes[i]) & 0xFF);
			}
			// Copiar os 8 bytes de timeStamp do myPCI (bytes 8 a 15 da myPDU)
			timeStamp = 0;
			for (int i=8; i<16; i++) {
				timeStamp = timeStamp << 8;
				timeStamp = timeStamp+((myPDUbytes[i]) & 0xFF);
			}
			// Copiar os dados (bytes de 16 em diante da myPDU)
			for (int i=16; i<myPDUbytes.length; i++) 
				data[i-16] = myPDUbytes[i];
			
			// Construir a IDU
			userIDU = new DatagramPacket(data, data.length, udp.getSocketAddress());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] toBytes() {
		int PDUsize;
		if (userIDU == null) {
			PDUsize = 4+4+8; // Tamanho do type + nSeq + long
		} else {
			PDUsize = 4+4+8+userIDU.getData().length; // Tamanho do type + nSeq + long + dadosUser
		}
		byte[] myPDUbytes = new byte[PDUsize];

		// Construir o myPCI
		// Copiar os 4 bytes de type para myPCI
		for (int i=0; i<4; i++) 
			myPDUbytes[3-i] = (byte)(type.getValue() >>> (i*8));
		// Copiar os 4 bytes de nSeq para myPCI
		for (int i=0; i<4; i++) 
			myPDUbytes[7-i] = (byte)(nSeq >>> (i*8));		
		// Copiar os 8 bytes de timeStamp para myPCI
		for (int i=0; i<8; i++) 
			myPDUbytes[15-i] = (byte)(timeStamp >>> (i*8));		

		// Copiar os dados da IDU do utilizador para os dados da myPDU
		// começam a partir do byte 16 da myPDU
		if (userIDU != null) {
			for (int i=0; i<userIDU.getData().length; i++)
	 	    	{ myPDUbytes[i+16]=(userIDU.getData())[i]; };
		};
		
		return myPDUbytes;
	}
	
	public int getSeq() { return nSeq; }
	public long getTimestamp() { return timeStamp; }
	public DatagramPacket getIDU() { return userIDU; }
	public Type getType() { return type; }
}
