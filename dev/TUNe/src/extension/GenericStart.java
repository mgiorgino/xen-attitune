
package extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import core.dslparser.WrapperVariable;
import core.interfaces.NotifyItf;
import core.util.StreamGlobbler;


public class GenericStart
{

	public WrapperVariable __wv;
	public NotifyItf __notifyitf;

	private Process agent_pid=null;
	private String pid;
	private static int probeInterval;

	public void start_with_pid_linux_wait_file(String cmd, String [] args)
	{
		String waitFilename = args[0];
		try
		{
			File waitFile_tmp = new File(waitFilename);
			if (waitFile_tmp.exists())
				waitFile_tmp.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String [] evt = new String[args.length - 1];
		for (int i = 1; i < args.length; i++)
			evt[i-1] = args[i];
		
		start_with_pid_linux_aux(cmd, evt);

		File waitFile = new File(waitFilename);
		System.out.println("Wait file:"+waitFilename);

		try
		{
			while (!waitFile.exists())
				Thread.sleep(probeInterval);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void start_with_pid_linux_aux(String cmd, String [] evt)
	{
		String[] p = {"bash","-c", cmd+" & echo __TUNE-$!"};

		String aff="";
		for(int i=0;i<p.length;i++)
			aff=aff+" "+p[i];

		System.out.println("Execute (and get PID - Bash Only) ( "+aff+" ) in evt:");
		for(int i=0;i<evt.length;i++)
			System.out.println(" -> "+evt[i]);

		try 
		{
			try 
			{
				agent_pid = Runtime.getRuntime().exec(p, evt);
				agent_pid.waitFor();


			}
			catch(InterruptedException e)
			{}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			InputStream is=agent_pid.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			
			// on attends le PID
			pid=null;
			String ligne;
			while(pid==null)
			{
				try
				{
					ligne=br.readLine();
					if(ligne.split("\\-")[0].equals("__TUNE"))
						pid=ligne.split("\\-")[1];
					else
						System.out.println("[ CMD ] "+cmd+" => "+ligne);
				}
				catch(Exception e)
				{}
			}


			new StreamGlobbler(is,cmd,false);
			new StreamGlobbler(agent_pid.getErrorStream(),cmd,false);


			if (agent_pid == null)
				System.out.println("WARN: null PID");

			if (__wv == null)
				System.out.println ("__pid null");

			__wv.setVar("pid",agent_pid );
			__notifyitf.notify("setPID", "this",pid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public void start_with_pid_linux(String cmd, String [] evt)
	{
		
		start_with_pid_linux_aux(cmd,evt);
		try
		{
			Thread.sleep(1500);
		}
		catch(InterruptedException e)
		{
			
		}

	}

	public void start(String cmd, String []evt)
	{
		System.out.println("Execute "+cmd+ " in evt:");
		for(int i=0;i<evt.length;i++)
			System.out.println(" -> "+evt[i]);

		try 
		{
			try 
			{
				agent_pid = Runtime.getRuntime().exec(cmd, evt);
				Thread.sleep(2000);
			}
			catch(InterruptedException e)
			{}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (agent_pid == null)
				System.out.println("WARN: null PID");

			if (__wv ==null)
				System.out.println ("__pid null");

			__wv.setVar("pid",agent_pid );

		} 
		catch (Exception e) 
		{

			e.printStackTrace();
		}

	}


	public void start(String cmd)
	{
		System.out.println("Execute "+cmd);
		try 
		{
			try 
			{
				agent_pid = Runtime.getRuntime().exec(cmd);
				Thread.sleep(2000);
			}
			catch(InterruptedException e)
			{}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (agent_pid == null)
				System.out.println("WARN: null PID");

			if (__wv ==null)
				System.out.println ("__pid null");

			__wv.setVar("pid",agent_pid );


		} 
		catch (Exception e) 
		{

			e.printStackTrace();
		}

		
	}
	

}
