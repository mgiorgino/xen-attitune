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


	// Core methods
	/**
	 * Starts a virtual machine (VM)
	 * 
	 * @param VMname
	 *            the VM name given by TUNe
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void start(String VMname) throws InterruptedException,
			IOException {
		Process VM;
		/*
		 * The VM name given by TUNe contains identifier TUNe needs. We first
		 * get ride of this identifier before creating the VM. Then we catch the
		 * VM networks interfaces during the start up.
		 */
		
		//Invoke the create method with the correct VM name
		VM = create(VMname.split("_")[0]);
		Hashtable<String, String> networkInterfaces;
		
		// Invokes the getInterface method to get network interfaces
		networkInterfaces = getInterfaces(VM);

		// The network interfaces are printed in standard output
		for (String s : networkInterfaces.keySet()) {
			System.out.println(s + " " + networkInterfaces.get(s));
		}

		// Wait for the end of VM startup before terminating the method
		VM.waitFor();
	}

	/**
	 * Creates a Virtual Machine (VM) The VM to create is supposed : - existing -
	 * its configuration file is located on /xen.group1/conf - the configuration
	 * file is name <VMname>.cfg
	 * 
	 * @param VMname
	 *            the VM to start
	 * @return
	 * @throws IOException
	 */
	public static Process create(String VMname) throws IOException {
		/*
		 * Creating a VM by using command xm create -c <path to config file>
		 */
		String[] cmdarray = { "xm", "create", "-c",
				"/xen/group1/conf/" + VMname + ".cfg" };
		return Runtime.getRuntime().exec(cmdarray);
	}

	/**
	 * Migrates a Virtual Machine (VM)
	 * 
	 * @param VMname
	 *            the VM to migrate. Its name is given directly by TUNe
	 * @param Dom0
	 *            the destination Dom0
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void migrate(String VMname, String Dom0) throws IOException,
			InterruptedException {
		/*
		 * Initiating live migration by suing command xm migrate --live <VirtualMachine> <destination>
		 */
		String[] cmdarray = { "xm", "migrate", "--live", VMname.split("_")[0],
				Dom0 };
		Runtime.getRuntime().exec(cmdarray).waitFor();
	}

	/**
	 * Shuts down a Virtual Machine (VM)
	 * 
	 * @param VMname
	 *            the VM to stop. This parameter is given by TUNe
	 * @throws IOException
	 */
	public static void shutdown(String VMname) throws IOException {
		/*
		 * Shutting down VM by using command xm shutdown <VirtualMachine>
		 */
		String[] cmdarray = { "xm", "shutdown", VMname.split("_")[0] };
		Process proc = Runtime.getRuntime().exec(
				"xm console " + VMname.split("_")[0]);
		Runtime.getRuntime().exec(cmdarray);
		/*
		 * The VM console output is caught and redirected to standard output
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc
				.getInputStream()));
		String line = "";
		do {
			line = reader.readLine();
			System.out.println(line);
		} while (line != null);
		reader.close();
	}

	/**
	 * Gets the network interfaces of the Virtual Machine. This method must be
	 * called during the VM startup
	 * 
	 * @param VM
	 *            the process of console output of a starting up VM
	 * @return a hastable containing all configured network interfaces of the VM
	 *         and the corresponding IP address
	 * @throws IOException
	 */

	//TODO methode à revoir
	public static Hashtable<String, String> getInterfaces(Process VM)
			throws IOException {
		/*
		 * reader contains the output terminal of a starting up VM
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(VM
				.getInputStream()));
		Hashtable<String, String> Interfaces = new Hashtable<String, String>();
		String line;
		boolean complete = false;
		
		do {
			line = reader.readLine();
			if (line.contains("device=")) {
				String[] elements = line.split(",");
				String dev = "", ip = "";
				for (String s : elements) {
					String[] tokens = s.split("=");
					if (tokens[0].contains("device")) {
						dev = tokens[1];
					}
					if (tokens[0].contains("addr")) {
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
	 * Entry point of the program for testing purpose
	 * 
	 * @param args
	 *            line command parameters
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
			Process proc = create("/xen/group1/conf/etch-1-1.cfg");
			Hashtable<String, String> networkInterfaces = getInterfaces(proc);
			for (String s : networkInterfaces.keySet()) {
				System.out.println(s + " " + networkInterfaces.get(s));
			}

	}

}
