import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class Probe {
	
	public String exec(String commande) throws IOException, InterruptedException{
		Process proc = Runtime.getRuntime().exec(commande);
		proc.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = reader.readLine();
		reader.close();
		return line;
	}
	
	public double exploit(String trucmuche, int nbProc){
		return Double.parseDouble(trucmuche.substring(0, 4))/nbProc;
		
	}
}
