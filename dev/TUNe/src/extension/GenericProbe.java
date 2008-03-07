package extension;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.rmi.RemoteException;

public class GenericProbe implements Runnable
{
	
	private String pid_to_check;
	private String probedEntity;
	private String tubeAddr;
	private String notificationName;

	public static void main(String args[])
	{
		new GenericProbe(args[0], args[1], args[2], args[3]);
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				if(!isAlive())
				{
					// envoie de la notification au pipe
					try
					{
						// nom de la notif;element sur lequel le state chart doit être appliqué;argument
						new PrintStream(new FileOutputStream(tubeAddr)).println(notificationName+";this;"+probedEntity);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				Thread.sleep(1000);
				
			}
		}
		catch(Exception e)
		{

		}
		
	}
	
	public GenericProbe(String pid_to_check, String tubeAddr, String probedEntity, String notificationName)
	{
		this.pid_to_check=pid_to_check;
		this.tubeAddr=tubeAddr;
		this.probedEntity=probedEntity;
		this.notificationName=notificationName;
		
		new Thread(this).start();
	}
	
	public boolean isAlive() throws RemoteException
	{
		String commande="ps -p "+pid_to_check+" -o command=";
		String str = "";
		try
		{
			BufferedReader bf = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(commande).getInputStream()));
			try
			{
				String tmp = bf.readLine();
				while (!tmp.equals(null))
				{
					str=str+tmp;
					tmp = bf.readLine();
				}
			}
			catch (Exception e)
			{
				// pass through here whenever the end of bf is reached
			}

			if(str.equals(""))
				return false;
			else
				return true;
		}
		catch(Exception e)
		{
			return false;
		}

	}
	
	

	

}
