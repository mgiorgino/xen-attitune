import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Ping {

	private XYSeriesCollection dataset = new XYSeriesCollection();
	private XYSeries series = new XYSeries("pings");

	synchronized public void addToSeries(double x, double y){
		series.add(x, y);
	}

	public XYSeriesCollection startOn(InetAddress address, int delay, int size) throws RMIException{

		IRMIServer irmi = getServer(address.getCanonicalHostName());
		
		Timer timer = new Timer();
		timer.schedule(new PingTask(irmi, this, size), 1000, delay);
		dataset.addSeries(series);

		return dataset;
	}

	public IRMIServer getServer(String host) throws RMIException {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		String name = "//" + host + "/RMIServer";
		IRMIServer rmi = null;
		try {
			rmi = (IRMIServer) Naming.lookup(name);
		} catch (MalformedURLException e) {
			exception(e);
		} catch (RemoteException e) {
			exception(e);
		} catch (NotBoundException e) {
			exception(e);
		}
		return rmi;
	}
	
	public static void help(){
		System.out.println("usage : RMIPing [< address > [ < delay > [ < size > ]]]");
	}
	
	static String[] memUnits = {"o", "Ko", "Mo", "Go", "To"};
	
	public static void main(String[] args) throws IOException, RMIException {
		String host = "localhost";
		int delay = 100;
		int size = 100000;

		if(args.length > 0){
			if(args[0].contains("help")){
				help();
				System.exit(0);
			}else{
				host = args[0];
				if(args.length > 1){
					delay = Integer.parseInt(args[1]);
				}
				if(args.length > 2){
					size = Integer.parseInt(args[2]);
				}
				if(args.length > 3){
					help();
					System.exit(1);
				}
			}
		}else{
			host = JOptionPane.showInputDialog("Host address", "192.168.2.");
		}

		System.out.println("RMIPing de "+ host);

		InetAddress address = Inet4Address.getByName(host);

		IntervalXYDataset dataset = new Ping().startOn(address, delay, size);
		float fsize = size;
		int unit = 0;
		while(fsize > 1024. && unit < memUnits.length){
			fsize /= 1024.;
			unit++;
		}
		JFreeChart chart = ChartFactory.createXYLineChart(
			"RMIPing " +
				"of ~"+Math.round(fsize)+memUnits[unit]+" " +
				"of \""+host+"\" "
				+"each "+delay+" ms ",
			"Time (s)",
			"Response time (ms)",
			dataset,
			PlotOrientation.VERTICAL,
			true,
			true,
			true);
		JFrame frame = new JFrame();
		frame.add(new ChartPanel(chart));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	static public void exception(Exception e) throws RMIException{
		throw new RMIException(e);
	}

}

class UpdateChart extends TimerTask{

	JFreeChart chart;

	public UpdateChart(JFreeChart chart){
		this.chart = chart;
	}

	@Override
	public void run() {
		chart.setNotify(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chart.setNotify(false);
	}
}

class PingTask extends TimerTask{

	private IRMIServer irmi;
	private Ping ping;
	private SizeableObject o;
	private long initialTime = System.currentTimeMillis();

	public PingTask(IRMIServer irmi, Ping ping, int size){
		this.irmi = irmi;
		this.ping = ping;
		this.o = new SizeableObject(size);
	}

	@Override
	public void run() {
		new RMIPinger().start();
		new Thread(){
			public void run(){
			}
		}.start();
	}

	public class RMIPinger extends Thread{
		@Override
		public void run() {
			long begin = System.currentTimeMillis();
			try {
				irmi.pingPong(o);
				long end = System.currentTimeMillis();
				ping.addToSeries((begin - initialTime)/1000., end-begin);
			} catch (RemoteException e) {
				e.printStackTrace();
				ping.addToSeries((begin - initialTime)/1000., 20);
			}
		}
	}
}
