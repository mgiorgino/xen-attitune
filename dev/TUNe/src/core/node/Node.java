package core.node;

import java.rmi.Naming;
import java.util.Hashtable;

import core.interfaces.LauncherItf;
import core.main.Generator;
import core.util.StreamGlobbler;
import core.util.TuneException;

public class Node 
{
	private String nodeName;
	private String dirLocal;
	
	private String user;
	private String passwd;
	
	private String rmi_port;
	
	private String javaHome;
	
	private LauncherItf rl;
	
	private boolean isDeployed;
	
	private Hashtable legacyDeploy;
	
	private String protocole_copy;
	private String protocole_rsh;
	
	private String rep;
	
	public Node(String nodeName, String javaHome, String user, String dirLocal, String protocole, String rep)
	{
		this.nodeName=nodeName;
		this.javaHome=javaHome;
		this.user=user;
		this.dirLocal=dirLocal;
		this.rep=rep;
		isDeployed=false;
		legacyDeploy=new Hashtable();
		
		if(protocole.equals("ssh"))
		{
			protocole_copy="scp";
			protocole_rsh="ssh";
		}
		else if(protocole.equals("oarsh"))
		{
			protocole_copy="oarcp";
			protocole_rsh="oarsh";
		}
		
	}
	
	public synchronized void shutdownNode()
	{
		if(isDeployed)
		{
			try
			{
				Generator.logger.info("Stopping remote launcher on node "+nodeName);
				rl.shutdown();
				
				Process remote_process;
				Generator.logger.info("Cleaning dirLocal on node "+nodeName);
				String make_rep=getProtocoleRsh()+" -q -q "+getUser()+(getUser().equals("")?"":"@")+getNodeName()+" -- rm -rf "+getDirLocal();
				// on envoie le runtime Jade. Cela devra être fait lors su startFc du node associé (bis)
				remote_process=Runtime.getRuntime().exec(make_rep);
				new StreamGlobbler(remote_process.getInputStream(),"NodeAllocator-RemoveLocalDir");
				new StreamGlobbler(remote_process.getErrorStream(),"NodeAllocator-RemoveLocalDir");
				remote_process.waitFor();
				
			}
			catch(Exception e)
			{
				Generator.logger.error("Exception",e);
			}
		}
		
	}
	
	public synchronized void deploy()
	{
		if(!isDeployed())
		{
			setRmi_port("1099");
			Process remote_process;

			try
			{
				Generator.logger.info("Deploying on node "+getNodeName());

				String make_rep=getProtocoleRsh()+" -q -q "+getUser()+(getUser().equals("")?"":"@")+getNodeName()+" -- rm -rf "+getDirLocal()+" && mkdir "+getDirLocal();
				// on envoie le runtime Jade. Cela devra être fait lors su startFc du node associé (bis)
				remote_process=Runtime.getRuntime().exec(make_rep);
				new StreamGlobbler(remote_process.getInputStream(),"NodeAllocator-MakeLocalDir");
				new StreamGlobbler(remote_process.getErrorStream(),"NodeAllocator-MakeLocalDir");
				remote_process.waitFor();


				String send_rtm=getProtocoleCopy()+" -q -q "+rep+"TUNe.jar "+getUser()+(getUser().equals("")?"":"@")+getNodeName()+":"+getDirLocal()+"/";
				// on envoie le runtime Jade. Cela devra être fait lors su startFc du node associé (bis)
				remote_process=Runtime.getRuntime().exec(send_rtm);
				new StreamGlobbler(remote_process.getInputStream(),"NodeAllocator-DeployRuntime");
				new StreamGlobbler(remote_process.getErrorStream(),"NodeAllocator-DeployRuntime");
				remote_process.waitFor();
				
				// on démarre le launcher
				String stri=getProtocoleRsh()+" -q -q "+getUser()+(getUser().equals("")?"":"@")+getNodeName()+" -- "+getJavaHome()+"/bin/java -cp "+getClasspath()+" core.wrapper.remote.RemoteLauncher "+getRmi_port();
				remote_process=(Runtime.getRuntime().exec(stri));
				new StreamGlobbler(remote_process.getInputStream(),"");
				new StreamGlobbler(remote_process.getErrorStream(),"");
				boolean running=false;
				LauncherItf li;
				
				for(int j=0;j<Generator.retries&&!running;j++)
				{
					try
					{
						li=	(LauncherItf)Naming.lookup("rmi://"+getNodeName()+":"+getRmi_port()+"/launcher");
						running=li.isDeployed();
						setRl(li);
					}
					catch(Exception e)
					{
						// peut être que le ssh n'a pas eu le temps de se faire...
						running=false;
					}

					if(!running)
						Thread.sleep(1000);
				}
				if(!running)
					throw new TuneException("Unable to start application");
				
				setDeployed(true);
				
			}
			catch(Exception e)
			{
				Generator.logger.error("Exception",e);
			}

		
		
		}

	}
	
	
	public String getProtocoleCopy()
	{
		return protocole_copy;
	}
	
	public String getProtocoleRsh()
	{
		return protocole_rsh;
	}
	
	public synchronized boolean isLegacyDeployed(String legacyName)
	{
		return legacyDeploy.get(legacyName)!=null;
	}
	
	public synchronized void addALegacy(String legacyName)
	{
		legacyDeploy.put(legacyName, "deploye");
	}
	
	public boolean isDeployed() 
	{
		return isDeployed;
	}
	public void setDeployed(boolean isDeployed) 
	{
		this.isDeployed = isDeployed;
	}
	public synchronized LauncherItf getRl()
	{
		return rl;
	}
	public synchronized void setRl(LauncherItf rl) 
	{
		this.rl = rl;
	}
	public String getJavaHome() 
	{
		return javaHome;
	}
	public void setJavaPath(String javaPath) 
	{
		this.javaHome = javaPath;
	}
	public String getRmi_port() 
	{
		return rmi_port;
	}
	public void setRmi_port(String rmi_port) 
	{
		this.rmi_port = rmi_port;
	}
	public String getClasspath() 
	{
		return dirLocal+"/TUNe.jar";
	}

	public String getPasswd() 
	{
		return passwd;
	}
	public void setPasswd(String passwd) 
	{
		this.passwd = passwd;
	}
	public String getUser() 
	{
		return user;
	}
	public void setUser(String user) 
	{
		this.user = user;
	}
	public String getDirLocal() 
	{
		return dirLocal;
	}
	public void setDirLocal(String dirLocal) 
	{
		this.dirLocal = dirLocal;
	}
	
	public String getNodeName() 
	{
		return nodeName;
	}
	public void setNodeName(String nodeName) 
	{
		this.nodeName = nodeName;
	}

	
}
