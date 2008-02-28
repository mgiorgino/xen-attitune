/**
 * 
 */
package xen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;




/**
 * @author adminxen
 *
 */
public class Probe {



	public void bidon(String Tuyal, String Machine) throws InterruptedException, FileNotFoundException{
		System.out.println("Xen Fake Probe started");
		Thread.sleep(14000);
		System.out.println("Xen Fake Probe sending migration");
		new PrintStream(new FileOutputStream(Tuyal)).println("Migrate;this;"+Machine);
		System.out.println("Xen Fake Probe sleeping");
//		Thread.sleep(20000);
//		System.out.println("Xen Fake Probe sending shutdown");
//		new PrintStream(new FileOutputStream(Tuyal)).println("Shutdown;this;"+Machine);
	}
	
	public void baril(String Tuyal, String Machine) throws FileNotFoundException, InterruptedException{
		Thread.sleep(20000);
		System.out.println("Xen Fake Probe sending shutdown");
		new PrintStream(new FileOutputStream(Tuyal)).println("Shutdown;this;"+Machine);
	}
	
	public static void main(String args[]) throws InterruptedException, FileNotFoundException
	{
		new PrintStream(new FileOutputStream(args[0])).println("fixXen;this;"+args[1]);
	}
	

}
