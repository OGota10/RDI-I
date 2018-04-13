package cenarioIII;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Graph {
	static private BufferedWriter fd = null;
	static private BufferedWriter fg = null;
	static private BufferedReader fr = null;
	static private BufferedReader fs = null;
	
	static private double maxt = 0;
	static private long startTime = 0;
	
	static HashMap<String, Integer>layers;
	
	public static void main(String[] args) throws IOException {
		fg = new BufferedWriter (new FileWriter("./r.gpl"));
		fd = new BufferedWriter (new FileWriter("./r.data"));
		
		fr = new BufferedReader(new FileReader("./receiver.data"));
		fs = new BufferedReader(new FileReader("./sender.data"));
		
		layers = new HashMap<String,Integer>();
		layers.put("APP", 2);
		layers.put("MS", 1);
		layers.put("UDP", 0);
	
		preamble();
		firstLines();
		String line = fr.readLine();
		while (line != null) {
			arrow(line);
			data(line);
			line = fr.readLine();
		}
		line = fs.readLine();
		while (line != null) {
			arrow(line);
			data(line);
			line = fs.readLine();
		}		
		closing();
		fg.flush();
		fg.close();
		fd.flush();
		fd.close();
		Process p = Runtime.getRuntime().exec("gnuplot r.gpl");
		boolean end = true;
		while(end) {};
	}

	static public void firstLines() throws IOException {
		String receiverLine1 = fr.readLine();
		String senderLine1 = fs.readLine();
		
		String[] params;
		params = receiverLine1.split(" ");
		startTime = Long.parseLong(params[0]);
		params = senderLine1.split(" ");
		long startTime2 = Long.parseLong(params[0]);
		maxt = startTime2-startTime;
		
		if (startTime2 < startTime) {
			maxt=-maxt;
			startTime = startTime2;	
		}
		
		data(receiverLine1);
		arrow(receiverLine1);
		data(senderLine1);
		arrow(senderLine1);
	}
	
	static public void arrow(String line) throws IOException {
		String[] params = line.split(" ");
		String type = params[4];
		long time = Long.parseLong(params[0])-startTime;
		String timeS = (new Long(time)).toString();
		String nSeq = params[5];
		int from = layers.get(params[2]);
		int to = layers.get(params[3]);
		String fromS = Integer.toString(layers.get(params[2]));
		String toS = Integer.toString(layers.get(params[3]));
		
		if (time > maxt) maxt = time;
		if (from != to)
			fg.write("set arrow from " +timeS+","+fromS+" to "+timeS+","+toS+" arrowstyle "+style(line)+"\n");
	    if ((from == 1 && to == 0) && type.equals("R"))
	    	fg.write("set label \""+nSeq+"\" at "+timeS+",0.4 textcolor rgb \"red\"\n");
	    if ((from == 0 && to == 1) && type.equals("R"))
	    	fg.write("set label \""+nSeq+"\" at "+timeS+",0.6 textcolor rgb \"red\"\n");
	    if ((from == 1 && to == 0) && type.equals("R"))
	    	fg.write("set label \""+nSeq+"\" at "+timeS+",0.2 textcolor rgb \"blue\"\n");
	    if ((from == 0 && to == 1) && type.equals("R"))
	    	fg.write("set label \""+nSeq+"\" at "+timeS+",0.8 textcolor rgb \"green\"\n");
	}

	static public void data(String line) throws IOException {
		String[] params = line.split(" ");
		if (params[4].equals("R")) return;
		long time = Long.parseLong(params[0]) - startTime;
		String timeS = (new Long(time)).toString();
		
		if (params[4].equals("T")) {
			fd.write(timeS+"\tNaN\tNaN\t1\n");
			return;
		}
		
		if (layers.get(params[2]) > layers.get(params[3])) {
			fd.write(timeS+"\t"+ params[1] +"\tNaN\tNaN\n");
				return;
		}

		if (layers.get(params[2]) < layers.get(params[3])) {
			fd.write(timeS+"\tNaN\t"+ params[1] +"\tNaN\n");
				return;
		}
	}
	

	static public	String style (String line) {
		String params[] = line.split(" ");
		String ret;
		ret = "-1";
		if (params[4].equals("I")) {
			if (layers.get(params[2]) < layers.get(params[3])) ret = "1";
			if (layers.get(params[2]) > layers.get(params[3])) ret = "2";			
		}
		if (params[4].equals("R")) ret = "3";
		return ret;
	}
	
	static public void preamble() throws IOException {
		fg.write("reset\n");
		fg.write("set style arrow 1 linecolor rgb \"blue\"\n");
		fg.write("set style arrow 2 linecolor rgb \"green\"\n");
		fg.write("set style arrow 3 linecolor rgb \"red\"\n");
		fg.write("set ytics (\"\" -2, \"APP\" 2, \"MS\" 1, \"UDP\" 0)\n"); 
		fg.write("set y2tics 0, 1\n");
		fg.write("set my2tics 2\n");
		fg.write("set xlabel \"t (mseg)\"\n");
		fg.write("set y2label \"queue size\"\n"); 
		fg.write("set yrange [-.7:2.2]\n");
		fg.write("set y2range [0:*]\n");	
		fg.write("set xrange [-0.1:*]\n");
	}
	
	static public void closing() throws IOException {
		String maxtS, maxtS4, maxtS35;
		maxtS = (new Double(maxt)).toString();
		maxtS4 = (new Double(maxt/4.0)).toString();
		maxtS35 = (new Double(maxt/3.5)).toString();
		
		fg.write("set arrow from 0,-0.1 to "+ maxtS4 +",-0.1 linecolor rgb \"blue\"\n");
		fg.write("set label \"Receive\" at "+ maxtS35 +",-0.1 textcolor rgb \"blue\"\n");
		fg.write("set arrow from 0,-0.3 to "+ maxtS4 +",-0.3 linecolor rgb \"green\"\n");
		fg.write("set label \"Send\" at "+ maxtS35 +",-0.3 textcolor rgb \"green\"\n");
		fg.write("set arrow from 0,-0.5 to "+ maxtS4 +",-0.5 linecolor rgb \"red\"\n");
		fg.write("set label \"Report\" at "+ maxtS35 +",-0.5 textcolor rgb \"red\"\n");
		fg.write("set key at "+ maxtS +",-0.1\n");
		fg.write("plot \"r.data\" u 1:2 axes x1y2 t \"Sender mq\" linecolor rgb \"green\" \n");
		fg.write("replot \"r.data\" u 1:3 axes x1y2 t \"Receiver mq\" linecolor rgb \"blue\" \n");
		fg.write("replot \"r.data\" u 1:4 axes x1y1 t \"TimeOut\" linecolor rgb \"red\" \n");
		fg.write("pause -1\n");
	}
}
