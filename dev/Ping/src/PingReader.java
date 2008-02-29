import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class PingReader extends Thread {

	private XYSeriesCollection dataset = new XYSeriesCollection();
	private XYSeries series = new XYSeries("pings");
	private InetAddress address;

	public PingReader(InetAddress address){
		this.address = address;
	}
	
	synchronized public void addToSeries(double x, double y){
		series.add(x, y);
	}
	
	public XYSeriesCollection getDataSet(){
		dataset.addSeries(series);
		return dataset;
	}
	
	@Override
	public void run(){
		try {
			readPing(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Pattern pattern = Pattern.compile(
			// group 1
			"(\\d*) bytes " +
			// group 2
			"from (.*): " +
			// group 3
			"icmp_seq=(\\d*) " +
			// group 4
			"ttl=(\\d*) " +
			// group 5
	"time=([\\d\\.]*) (.*)");

	public static void main(String[] args) throws IOException {

		String host;

		if(args.length == 1){
			host = args[0];
		}else{
			host = JOptionPane.showInputDialog("Host address", "192.168.2.");
		}

		System.out.println("Ping de "+ host);

		PingReader pr = new PingReader(Inet4Address.getByName(host));
		
		IntervalXYDataset dataset = pr.getDataSet();
		
		pr.start();
		
		JFreeChart chart = ChartFactory.createXYLineChart("Ping of \""+host+"\"", "Time (s)", "Response time (ms)", dataset, PlotOrientation.VERTICAL, true, true, true);

		JFrame frame = new JFrame();
		frame.add(new ChartPanel(chart));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
	
	public void readPing(InetAddress address) throws IOException {
				
		long initialTime = System.currentTimeMillis();
		
		Process p = Runtime.getRuntime().exec("gksudo ping -c 1000 -i 0.001 -W 60 "+address.getCanonicalHostName());
		
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		InputStreamReader errorStreamReader = new InputStreamReader(p.getErrorStream());
		int c;
		while((c = errorStreamReader.read()) == -1){
			System.out.print((char)c);
			System.out.println("rtiiothjqg");
		}
		
		Double time = null;
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String s;
		while((s = br.readLine())!= null){
			System.out.println("yoyoyo");
			Matcher m = pattern.matcher(s);
			if(m.find()){
				time = (m.group(6).equals("ms")?1:1000)*Double.parseDouble(m.group(5));
				long begin = System.currentTimeMillis();
				addToSeries((begin - initialTime)/1000., time);
			}
		}
	}
}

//class PingTask extends TimerTask{
//
//	private InetAddress address;
//	private PingReader ping;
//
//	public PingTask(InetAddress address, PingReader ping){
//		this.address = address;
//		this.ping = ping;
//	}
//
//	private int nbLost = 0;
//
//	private double test(InetAddress address) throws IOException{
//		//return address.isReachable(50000);
//	}
//
//	@Override
//	public void run() {
//		new Thread(){
//			public void run(){
//				try {
//					long begin = System.currentTimeMillis();
//					if(address.isReachable(50000)){
//						long end = System.currentTimeMillis();
//						ping.addToSeries((begin - initialTime)/1000., test(address));
//					//	nbLost = 0;
//					}else{
//						throw new IOException("Host unreachable");
//					}
//				} catch (IOException e) {
//					//nbLost ++;
//					//			try {
//					//				Thread.sleep(60000);
//					//			} catch (InterruptedException e1) {
//					e.printStackTrace();
//					//			}
//					//System.out.println("Perdus : "+ nbLost);
//				}
//			}
//		}.start();
//	}
//
//}
