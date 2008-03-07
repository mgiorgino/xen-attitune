package core.wrapper.remote;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import core.interfaces.LauncherItf;

public class RemoteLauncher extends UnicastRemoteObject implements LauncherItf
{

	public void deployARemoteWrapper(String wrapperName, String srmi_port, String tubeAddr) throws RemoteException
	{
		try
		{
			new RemoteWrapper(wrapperName, srmi_port,tubeAddr);
			System.out.println("Successfully create new RemoteWrapper with [ "+wrapperName+" , "+srmi_port+" , "+tubeAddr+" ] parameters");
		}
		catch(Exception e)
		{
			throw new RemoteException("Unable to start RemoteWrapper with [ "+wrapperName+" , "+srmi_port+" , "+tubeAddr+" ] parameters");
		}
	}
	
	public RemoteLauncher(String srmi_port) throws RemoteException
	{
		int rmi_port=Integer.parseInt(srmi_port);
		
		
		try
		{
			System.out.println("Launching new RemoteLauncher on node "+InetAddress.getLocalHost().getHostName()+" and RMI port "+rmi_port);
			startRMI(rmi_port,InetAddress.getLocalHost().getHostName());
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
	}
	
	public boolean isDeployed()
	{
		return true;
	}
	
	public void shutdown() throws RemoteException
	{
		try
		{
			System.out.println("Stopping RemoteLauncher on node "+InetAddress.getLocalHost().getHostName()+" in 5s to avoir RMI fault...");
			
			(new Thread()
			{
				public void run()
				{
					try
					{
						Thread.sleep(5000);
					}
					catch(InterruptedException e){}
					System.exit(0);
				}
			}).start();
			
		}
		catch(Exception e)
		{
			throw new RemoteException(e.getMessage());
		}
		
	}
	
	private void startRMI(int rmi_port, String node) throws Exception
	{
		try
		{
			java.rmi.registry.LocateRegistry.createRegistry(rmi_port);
		}
		catch(Exception e)
		{
			// le serveur est sûrement déjà démarré sur ce noeud
		}
		// on s'enregistre sur le serveur local RMI
		Naming.rebind("rmi://"+node+"/launcher",this);
	

	}

	
	public static void main(String args[])
	{
		try
		{
			new RemoteLauncher(args[0]);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
