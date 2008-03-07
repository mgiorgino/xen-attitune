package core.wrapper.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import core.dslparser.WrapperVariable;
import core.interfaces.NotifyItf;
import core.interfaces.WrapperItf;
import core.wrapper.local.GenericWrapper;

public class RemoteWrapper extends UnicastRemoteObject implements WrapperItf,WrapperVariable, NotifyItf, Runnable
{
	
	private String wrapperName;
	private String node;
	
	private Hashtable wrapper_variable;
	
	private NotifyItf notifyItf;
	
	public int rmi_port;
	
	public String tubeAddr;
	
	private Thread checkTube;
	
	private boolean alive;



	/* fourni par le system representation. Contient aussi tous les linkfc et assimilés résolus */
	public Hashtable attributes;
	
	public RemoteWrapper(String wrapperName, String srmi_port, String tubeAddr) throws Exception
	{
		this.wrapperName=wrapperName;
		node=InetAddress.getLocalHost().getHostName();
		wrapper_variable=new Hashtable();
		
		rmi_port=Integer.parseInt(srmi_port);
		
		alive=true;
		
		this.tubeAddr=tubeAddr;
		
		startRMI();
		
		
		System.out.println("Remote wrapper "+wrapperName+" is deploying on node "+node);
	}
	
	private void startRMI() throws Exception
	{
		try
		{
			java.rmi.registry.LocateRegistry.createRegistry(rmi_port);
		}
		catch(Exception e)
		{
			// le serveur est sûrement déjà démarré sur ce noeud
		}
		
		// on s'enregistre sur le serveur de nom local RMI
		Naming.rebind("rmi://"+node+"/"+wrapperName,this);
	

	}
	
	
	public boolean isDeployed() throws RemoteException
	{
		// bah si on reçoit l'appel, c'est bien que nous sommes déployés !
		return true;
	}
	
 	public String getName() throws RemoteException
	{
		return wrapperName;
	}

	public void setName(String name, int numero) throws RemoteException
	{
		
	}
	
	public void killRemoteWrapper() throws RemoteException
	{
		alive=false;
		new File(tubeAddr).delete();
	}
	
	public void run()
	{
		try
		{
			String str;
			String [] str_split;
			BufferedReader d;
			while(alive)
			{
				try
				{
					d=new BufferedReader(new InputStreamReader(new FileInputStream(tubeAddr)));
					str=d.readLine();
					str_split=str.split(";");
					notify(str_split[0],str_split[1],str_split[2]);
				}
				catch(Exception e)
				{
					Thread.sleep(1000);
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setNotifyNode(String node) throws RemoteException
	{
		try
		{
			// on va chercher le stub sur le node du SR
			System.out.println("RMI notification binding on "+node);			
			notifyItf=(NotifyItf)Naming.lookup(node);
			
			// on crée le tube pour la communication avec le legacy
			Runtime.getRuntime().exec("mkfifo "+tubeAddr).waitFor();
			
			// on démarre le thread de check
			checkTube=new Thread(this);
			checkTube.start();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public void notify(String notification, String component, String arg) throws RemoteException 
	{
		notifyItf.notify(notification,component,arg);
		
	}
	

	/******************** Facility methods ***********************/
	public void setVar(String name, Object value)
	{
		wrapper_variable.put(name, value);
	}
	
	public Object getVar(String name)
	{
		return wrapper_variable.get(name);
	}
	
	public GenericWrapper getGw()
	{
		return null;
	}
	
	
	public Object getObject(String className)
	{
   	   	Object o=null;
   	   	try
    	{
    		Class cl=Class.forName(className);
    		o=cl.newInstance();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return o;
	}
	
    public void meth(String className, String methName, Object[] args) throws RemoteException
    {
    	System.out.println("Running "+className+"."+methName);
    	try
    	{
       		Class cl=Class.forName(className);
       		Object o=getObject(className);
    		
	  		try
			{
				// si le champs __wv existe, on le set
				Field __wv=cl.getDeclaredField("__wv");
				__wv.set(o, this);
				
				// si la champs __notifyitf existe, on le set
				Field __notifyitf=cl.getDeclaredField("__notifyitf");
				__notifyitf.set(o, this);
				
			}
			catch(Exception e)
			{
				// oki, il n'existe pas
			}
			
			Method m=null;
			
			try
			{
				// pour ne pas à devoir balancer un pavé de 50 lignes d'introspection de signateure de méthode, on essaye en ventilant et si ça marche pas, on essaye en balancant le tableau et si ça marche pas, on lache l'affaire
				
				
				// ici, on cherche une méthode qui a autant d'argument que ce qui est passé dans les param du DSL
				Class [] c=new Class[args.length];
				for(int i=0;i<args.length;i++)
					c[i]=String.class;
				m=cl.getMethod(methName, c);
			
			
				m.invoke(o,args);
			}
			catch(NoSuchMethodException  e)
			{
				
				try
				{
					// ici, on cherche une méthode qui prendrait une String puis un tableau de String en paramètre
					Class []c=new Class[2];
					Object []args_bis=new Object[2];
	
					c[0]=String.class;
					c[1]=String[].class;
					m=cl.getMethod(methName, c);
	
					args_bis[0]=(String)args[0];
					args_bis[1]=new String[args.length-1];
	
					for(int i=1;i<args.length;i++)
						((Object[])args_bis[1])[i-1]=args[i];
	
					m.invoke(o,args_bis);
				}
				catch(NoSuchMethodException ex)
				{
					// ici, on cherche juste une méthode qui prend un tableau de String
					
					// ici, on cherche une méthode qui prendrait une String puis un tableau de String en paramètre
					Class []c=new Class[1];
					Object []args_bis=new Object[1];
	
					c[0]=String[].class;
					m=cl.getMethod(methName, c);
	
					args_bis[0]=new String[args.length];
					
					for(int i=0;i<args.length;i++)
						((Object[])args_bis[0])[i]=args[i];
					
	
					m.invoke(o,args_bis);
					
					
				}

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
   	
    }
    
 	
}
