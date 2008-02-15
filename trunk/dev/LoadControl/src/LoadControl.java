import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;


public class LoadControl {

	static Vector<Loader> threads = new Vector<Loader>();
	
	
	/**
	 * @param args arguments of command line 
	 * 	First argument stands for the target load
	 * 	Second argument stands for the number of CPU available on the host
	 * 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
//		double target = Double.parseDouble(args[0]);
//		int nbProc = Integer.parseInt(args[1]);
		
		double target = 2;
		int nbProc = 2;
		long initTime = System.currentTimeMillis();
		long currenttTime = System.currentTimeMillis();
		
		Probe Sonde = new Probe();
		
		try {
			double load = Sonde.exploit(Sonde.exec("cat /proc/loadavg"), nbProc);
			while (true){
				if (System.currentTimeMillis() - currenttTime > 1000){
					currenttTime = System.currentTimeMillis();
					System.out.println(currenttTime + " " + load);
				}
				if (load - target < 0 && threads.size() < (int) target*nbProc ){
//					System.out.println("++" + load);
					Loader l = new Loader();
//					System.out.println("Créé " + l);
					threads.add(l);
					l.start();
					temporize(500);
				} else if (load - target > 0) {
//					System.out.println("--" + load);
					if(!threads.isEmpty()){
						threads.firstElement().stop();
//						System.out.println("Interrompu " + threads.firstElement());
						threads.remove(0);
					}
				}
				load = Sonde.exploit(Sonde.exec("cat /proc/loadavg"), 2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void temporize(long t) throws InterruptedException{
		Thread.sleep(t);
	}
	
	
}
