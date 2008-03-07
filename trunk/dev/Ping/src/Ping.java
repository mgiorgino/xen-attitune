import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Ping {

	static private XYSeriesCollection dataset = new XYSeriesCollection();
	static private XYSeries series = new XYSeries("pings");
	static private Timer timer;
	static private InetAddress address;
	static private JButton pause;
	static private JButton clear;
	static private long initTime;
	static private int delay;
	static private int timeout;

	synchronized public void addToSeries(double x, double y){
		series.add(x, y);
	}

	public XYSeriesCollection startOn(InetAddress address, long initTime, int delay, int timeout){
		timer = new Timer();
		timer.schedule(new PingTask(address, this,  initTime, timeout), 0, delay);
		return dataset;
	}

	public static void help(){
		System.out.println("usage : Ping [< address > [ < delay > [ < timeout > ]]]");
	}
	
	public static void main(String[] args) throws IOException {

		String host = "localhost";
		delay = 10;
		timeout = 2000;

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
					timeout = Integer.parseInt(args[2]);
				}
				if(args.length > 3){
					help();
					System.exit(1);
				}
			}
		}else{
			host = JOptionPane.showInputDialog("Host address", "192.168.2.");
		}
		
		if(host == null){
			host = "localhost";
		}

		System.out.println("Ping de "+ host);

		address = Inet4Address.getByName(host);
		
		initTime = System.currentTimeMillis();
		dataset = new Ping().startOn(address, initTime, delay, timeout);
		dataset.addSeries(series);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Ping of \""+host+"\" each "+delay+" ms with a timeout of "+timeout+" ms",
				"Time (s)",
				"Response time (ms)",
				dataset,
				PlotOrientation.VERTICAL,
				true, true, true);
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(new ChartPanel(chart), BorderLayout.CENTER);
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.SOUTH);
		pause = new JButton("Pause");
		pause.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(timer == null){
					new Ping().startOn(address, initTime, delay, timeout);
					pause.setText("Pause");
				}else{
					timer.cancel();
					timer = null;
					pause.setText("Start");
				}
			}
			
		});
		panel.add(pause);
		clear = new JButton("Clear");
		clear.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(timer != null) timer.cancel();
				initTime = System.currentTimeMillis();
				new Ping().startOn(address, initTime, delay, timeout);
				series.clear();
			}
			
		});
		panel.add(clear);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
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

	private InetAddress address;
	private Ping ping;
	private long initialTime;

	public PingTask(InetAddress address, Ping ping, long initialTime, int timeout){
		this.address = address;
		this.ping = ping;
		this.initialTime = initialTime;
	}

	@Override
	public void run() {
		new Thread(){
			public void run(){
				try {
					int timeout = 2000;
					long begin = System.currentTimeMillis();
					if(address.isReachable(timeout)){
						long end = System.currentTimeMillis();
						ping.addToSeries((begin - initialTime)/1000., end-begin);
					}else{
						ping.addToSeries((begin - initialTime)/1000., timeout);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
