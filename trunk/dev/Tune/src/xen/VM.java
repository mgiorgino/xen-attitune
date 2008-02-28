package xen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * @author adminxen
 *
 */
public class VM {

	public VM(){
		super();
	}
	public static void start(String VMname) throws InterruptedException {
		Process VM;
		try {
			VM = create(VMname.split("_")[0]);
			Hashtable<String, String> networkInterfaces;
			networkInterfaces = getInterfaces(VM);

			for (String s: networkInterfaces.keySet()){
				System.out.println(s + " " + networkInterfaces.get(s));
			}
			VM.waitFor();
		} catch (IOException e) {
			System.err.println("IO Exception 2");
			e.printStackTrace();
		}
	}
	public static Process create(String VMname) throws IOException{
		String[] cmdarray = {"xm","create","-c","/xen/group1/conf/"+VMname+".cfg"};
		System.out.println(cmdarray[cmdarray.length-1]);
		return Runtime.getRuntime().exec(cmdarray);
	}

	public static void migrate(String VMname, String Dom0) throws IOException, InterruptedException{
		System.out.println("Migrating " + VMname.split("_")[0] + " to " + Dom0 );
		String[] cmdarray = {"xm","migrate","--live",VMname.split("_")[0], Dom0};
		Runtime.getRuntime().exec(cmdarray).waitFor();
	}
	
	public static void shutdown(String VMname) throws IOException{
		System.out.println("Shuting down" + VMname.split("_")[0]);
		String[] cmdarray = {"xm","shutdown",VMname.split("_")[0]};
		Process proc = Runtime.getRuntime().exec("xm console "+VMname.split("_")[0]);
		Runtime.getRuntime().exec(cmdarray);
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = "";
		do{
			line = reader.readLine();
			System.out.println(line);
		} while (line != null);
		reader.close();
	}

	public static Hashtable<String, String> getInterfaces(Process VM) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(VM.getInputStream()));
		Hashtable<String, String> Interfaces = new Hashtable<String, String>();
		String line;
		boolean complete = false;
		do{
			line = reader.readLine();
			if (line.contains("device=")){
				String[] elements = line.split(",");
				String dev = "", ip = "";
				for(String s : elements){
					String[] tokens = s.split("=");
					if (tokens[0].contains("device")){
						dev = tokens[1];
					}
					if (tokens[0].contains("addr")){
						ip = tokens[1];
					}
				}
				Interfaces.put(dev, ip);
				complete = true;
			}
		} while (!complete);
		reader.close();
		return Interfaces;
	}
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		try {
			Process proc = create("/xen/group1/conf/etch-1-1.cfg");
			Hashtable<String, String> networkInterfaces = getInterfaces(proc);
			for (String s: networkInterfaces.keySet()){
				System.out.println(s + " " + networkInterfaces.get(s));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IO exception");
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
