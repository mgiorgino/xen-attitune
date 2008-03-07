package core.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Tune
{
	
	public final int QUIT=0;
	public final int DEPLOY=1;
	public final int UNDEPLOY=2;
	public final int LIST=3;
	public final int HELP=4;
	public final int UNKNOW=99;
	public final int FOO=98;
	
	public String arg;
	
	public Vector app_name, app_tune;
	
	public static final int INTERACTIVE=0;
	public static final int PIPE=1;
	public static final int TELNET=2;
	
	public int launchMode;
	
	public static void main(String [] args)
	{
		if(args.length<1)
			new Tune(INTERACTIVE);
		else if(args.length==1)
		{
			if(args[0].equalsIgnoreCase("interactive"))
				new Tune(INTERACTIVE);
			else if(args[0].equalsIgnoreCase("pipe"))
				new Tune(PIPE);
			else if(args[0].equalsIgnoreCase("telnet"))
				new Tune(TELNET);
			else
				usage();
		}
		else
			usage();
	}
	
	public static void usage()
	{
		System.out.println("usage: java core.main.Tune [interactive|pipe|telnet (port:8023)]");
	}


	public Tune(int launchMode)
	{
		this.launchMode=launchMode;
		
		app_name=new Vector();
		app_tune=new Vector();

		if(launchMode==INTERACTIVE)
			interactive(new BufferedReader(new InputStreamReader(System.in)), System.out);
		else if(launchMode==PIPE)
			pipe();
		else if(launchMode==TELNET)
			telnet();
		
	}
	
	public void telnet()
	{
		try
		{
			Socket s;
			boolean sortie=false;
			ServerSocket ss=new ServerSocket(8023);
			while(!sortie)
			{
				s=ss.accept();
				new HandleTelnet(this,s); 
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
		
	public void pipe()
	{
		try
		{
			boolean sortie=false;
			String line;
			Runtime.getRuntime().exec("mkfifo tune-pipe").waitFor();
			File f=new File("tune-pipe");
			FileInputStream fis=new FileInputStream(f);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			
			File devnull=new File("/dev/null");
			
			while(!sortie)
			{
				line=br.readLine();
				if(line!=null && !line.equals(""))
					sortie=handleLine(line, new PrintStream(new FileOutputStream(devnull)));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public boolean handleLine(String line, PrintStream ps)
	{
		int commande;
		boolean sortie=false;
		commande=analyseCmd(line);

		if(commande==QUIT)
		{
			quit(ps);
			sortie=true;
		}
		else if(commande==UNKNOW)
			unknow(ps);
		else if(commande==DEPLOY)
			deploy(ps);
		else if(commande==HELP)
			help(ps);
		else if(commande==LIST)
			list(ps);
		else if(commande==UNDEPLOY)
			undeploy(ps);
		else if(commande==FOO)
			return false;
		
		return sortie;
		
	}
	
	public void interactive(BufferedReader br, PrintStream ps)
	{
		
		boolean sortie=false;
		
		ps.println("Type help to get help");
		ps.println();
		
		
		while(!sortie)
		{
			ps.println("Please enter your commande");
			
			try
			{
				sortie=handleLine(br.readLine(),ps);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}
	
	public void undeploy(PrintStream ps)
	{
		try
		{
			int no_app=Integer.parseInt(arg);
			Generator g=(Generator)app_tune.get(no_app);
			g.undeploy();
			app_tune.set(no_app, null);
			app_name.set(no_app, "");
		}
		catch(Exception e)
		{
			ps.println("No such application: "+arg);
		}
	}
	
	public void help(PrintStream ps)
	{
		ps.println(" -> deploy path uml-file => Deploy a Tune application");
		ps.println(" -> undeploy #appli      => Undeploy a Tune application");
		ps.println(" -> list                 => List all of deployed Tune application");
		ps.println(" -> quit                 => Undeploy all applications and quit");
	}
	
	public void list(PrintStream ps)
	{
		for(int i=0;i<app_name.size();i++)
		{
			if(!app_name.get(i).equals(""))
				ps.println(" -> #"+i+" "+app_name.get(i));
		}
	}
	
	public void deploy(PrintStream ps)
	{
		ps.println("Deploying "+arg+" ...");
		
		try
		{
			// on extrait le fichier uml du chemin
			String rep=arg.split(" ")[0];
			String umlFile=arg.split(" ")[1];
		
			Generator g=new Generator(umlFile,"-vv", rep);
			
			app_name.add(arg);
			app_tune.add(g);
			
			ps.println("Deployed appli #"+(app_name.size()-1));
		}
		catch(Exception e)
		{
			ps.println("Bad syntax");
		}
	}
	
	public void quit(PrintStream ps)
	{
		for(int i=0;i<app_name.size();i++)
		{
			try
			{
				if(!app_name.get(i).equals(""))
				{
					ps.println(" Shutdown appli #"+i+" "+app_name.get(i));
					((Generator)app_tune.get(i)).undeploy();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		ps.println("Exiting...");
	}
	
	public void unknow(PrintStream ps)
	{
		ps.println("Unknow command");
	}
	
	
	public int analyseCmd(String str)
	{
		try
		{
			String cmd=null;
			String arg=null;
			
			try
			{
				cmd=str.split(" ")[0];
				arg=str.substring(cmd.length()+1);
			}
			catch(Exception e)
			{
				arg=null;
			}
			
			this.arg=arg;
			
			if(cmd==null)
				cmd=str;
			
			if(cmd.equals("quit"))
				return QUIT;
			else if(cmd.equals("deploy"))
				return DEPLOY;
			else if(cmd.equals("undeploy"))
				return UNDEPLOY;
			else if(cmd.equals("help"))
				return HELP;
			else if(cmd.equals("list"))
				return LIST;
			else
				return UNKNOW;
		}
		catch(Exception e)
		{
			return FOO;
		}
	}
	
}

class HandleTelnet implements Runnable
{
	private Socket s;
	private Tune t;
	
	public HandleTelnet(Tune t, Socket s)
	{
		this.s=s;
		this.t=t;
		new Thread(this).start();
	}
	
	public void run()
	{
		try
		{
			t.interactive(new BufferedReader(new InputStreamReader(s.getInputStream())),new PrintStream(s.getOutputStream()));
		}
		catch(Exception e)
		{}
		
	}
	
	
	
}
