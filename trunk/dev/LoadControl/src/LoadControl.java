import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeMap;
import java.util.Vector;

public class LoadControl {

	static Vector<Loader> threads = new Vector<Loader>();

	/**
	 * @param args
	 *            arguments of command line First argument stands for the target
	 *            load Second argument stands for the number of CPU available on
	 *            the host third argument stands for the duration of CPU loading
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		if (args.length != 3) {
			System.out.println("Usage : \n\t java -jar LoadControl <target> <number of CPU> <duration (in seconds)>");
		} else {
			System.out.println("Initializing CPU Loader using " + args[1] + " CPU. Target load is " + args[0] + " for " + args[2] + " seconds");
			double target = Double.parseDouble(args[0]);
			int nbProc = Integer.parseInt(args[1]);
			int duration = Integer.parseInt(args[2]) * 1000;
			long initTime = System.currentTimeMillis();
			long currenttTime = System.currentTimeMillis();
			TreeMap<Long, Double> stats = new TreeMap<Long, Double>();
			Probe Sonde = new Probe();
			try {
				double load = Sonde.exploit(Sonde.exec("cat /proc/loadavg"),
						nbProc);
				long time = 0;
				System.out.println("CPU loader initialized, performing now...");
				while (time < duration) {
					if (System.currentTimeMillis() - currenttTime > 1000) {
						currenttTime = System.currentTimeMillis();
						time = currenttTime - initTime;
						stats.put(time / 1000, load);
					}
					if (load - target < 0
							&& threads.size() < (int) target * nbProc) {
						Loader l = new Loader();
						threads.add(l);
						l.start();
						temporize(500);
					} else if (load - target > 0) {
						if (!threads.isEmpty()) {
							threads.firstElement().stop();
							threads.remove(0);
						}
					}
					load = Sonde.exploit(Sonde.exec("cat /proc/loadavg"), 2);
				}
			} catch (IOException e) {
				System.err.println("IOException occured :");
				e.printStackTrace();
			}
			System.out.println("Terminating CPU loader, killing active threads now.");
			while (!threads.isEmpty()) {
				threads.firstElement().stop();
				threads.remove(0);
			}
			plot p = new plot("CPU Loader Graph - Target = " + target, stats);
			p.pack();
			p.setVisible(true);
		}
	}

	public static void temporize(long t) throws InterruptedException {
		Thread.sleep(t);
	}

}
