package xen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;




/**
 * @author adminxen
 *
 */
public class Probe extends Thread{
	
	int NbOfCPU;
	double threshold;
	String Pipe;
	String Monitored;
	
	
	/**
	 * This method reads from /proc/loadavg the average load of the system
	 * during the one last minute
	 * 
	 * @return the average load
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public double getCPULoad() throws InterruptedException, IOException{
		/*
		 * The CPU load we are interested in the first word of /proc/loadavg. We'll read the first line
		 * of /proc/loadavg then we split the line to get its first word.
		 */
		Process proc = Runtime.getRuntime().exec("cat /proc/loadavg");
		proc.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		double load = Double.parseDouble(reader.readLine().split(" ")[0])/NbOfCPU;
		reader.close();
		return load;
	}
	
	/**
	 * Reads from the /proc/stat the number of the CPU on the machine
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void initializeNbOfCPU() throws IOException, InterruptedException{
		/*
		 * this method reads from /proc/stat the number of CPU. /proc/stat contains global statistics
		 * of global CPU and statistics of each CPU. The number of lines started by "cpu" corresponds to
		 * the effective number of CPU on the computer plus 1.
		 */
		int n = 0;
		//Read /proc/stat
		Process proc = Runtime.getRuntime().exec("cat /proc/stat");
		proc.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = reader.readLine();
		//Count the number of line started with "cpu"
		while(line.startsWith("cpu")){
			n++;
			line = reader.readLine();
		}
		//Adjust the number of CPU and update the NbOfCPU field
		this.NbOfCPU = n-1;
	}

	/**
	 * Watches the CPU overload and writes a message in the pipe when threshold is exeeded
	 * This version of the monitor detects CPU overload only once
	 * 
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void monitorCPUOverload() throws FileNotFoundException, InterruptedException, IOException{
		boolean overloaded = false;
		/*
		 * the overload condition is checked every 500ms
		 */
		while(!overloaded){
			overloaded = threshold-getCPULoad() < 0;
			Thread.sleep(500);
		}
		/*
		 * An event is raised into the Pipe when CPU overload is detected
		 */
		new PrintStream(new FileOutputStream(this.Pipe)).println("Migrate;this;"+this.Monitored);
	}
	
	/**
	 * Creates and start a new CPU load based Probe
	 * 
	 * @param PipeAdress the Pipe in which events must be written
	 * @param Name	the name of probed component
	 * @param Limit	the threshold CPU load
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void create(String PipeAdress, String Name, String Limit) throws IOException, InterruptedException{
		/*
		 * this method reads from /proc the number of CPU and initializes attributes of the class
		 * with given parameters before starting run() method
		 */
		initializeNbOfCPU();
		threshold = Double.parseDouble(Limit);
		Pipe = PipeAdress;
		Monitored = Name;
		System.out.println("Initialization done " + threshold + " - " + Pipe + " - " + Monitored);
		this.start();
	}
	
	/**
	 * Entry point of the class for testing purpose
	 * @param args
	 */
	public static void main(String args[]){
		try {
			Probe p = new Probe();
			p.create(args[0],args[1] ,args[2]);
			System.out.println(p.NbOfCPU + "-" + p.Monitored + "-" + p.Pipe + "-" + p.threshold);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	
	public void finalize() throws FileNotFoundException, InterruptedException{
		System.out.println("Xen Fake Probe sending shutdown");
		new PrintStream(new FileOutputStream(Pipe)).println("Shutdown;this;"+Monitored);
	}
	
	@Override
	public void run() {
		try {
			monitorCPUOverload();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found Exception raised");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Interrupted Exception raised");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O Exception raised");
			e.printStackTrace();
		}
	}
		
}
