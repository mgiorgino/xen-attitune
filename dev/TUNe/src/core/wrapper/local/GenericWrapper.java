package core.wrapper.local;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Hashtable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import core.dslparser.AttFcImpl;
import core.dslparser.LinkFc;
import core.dslparser.SimpleContentHandler;
import core.interfaces.DeployControllerItf;
import core.interfaces.NodeAllocatorItf;
import core.interfaces.WrapperItf;
import core.main.Generator;
import core.node.Node;
import core.util.StreamGlobbler;


public class GenericWrapper extends AttFcImpl implements  WrapperItf, BindingController, LifeCycleController, AttributeController, DeployControllerItf
{
	
	
	// Appartient au wrapper générique
	private String wrapperName;
	
	private int number;
	
	private Process remote_process;
	private WrapperItf remoteWrapper;
	private boolean deployed;
	private boolean isStarted;
	private Thread deployThread;
	
	public Hashtable linkfc;
	
	private int retries=Generator.retries;
	
	private Node n;
	
	private NotifyClass nc;
	
	private Hashtable fixDiagram;
	private Component appliComponent;
	private Component contentsOf;
	
	private Hashtable nbBinding;
	
	private String rep;

	public GenericWrapper()
	{
		// juste pour les noeuds, pour ne pas que l'exception 
		// disant qu'il n'y a pas de XML associé apparaisse
		
		super();
		isStarted=false;
		deployed=false;
		linkfc=new Hashtable();
		nbBinding=new Hashtable();
	}
	
	public GenericWrapper(String rep, String descriptionFile)
	{
		super();
		
		this.rep=rep;
		
		deployed=false;
		linkfc=new Hashtable();
		nbBinding=new Hashtable();
		
		try 
		{
			XMLReader saxReader = XMLReaderFactory.createXMLReader();
			saxReader.setContentHandler(new SimpleContentHandler(this));
			saxReader.parse(new InputSource(new FileInputStream(rep+descriptionFile)));
		}
		catch (Throwable t) 
		{
			Generator.logger.error("Exception",t);
		}
	}
	
	public void setContentsOf(Component contentsOf)
	{
		this.contentsOf=contentsOf;
	}
	
	public Component getContentsOf()
	{
		return contentsOf;
	}
	
	public int getNbBindingForLink(Object fromWhat) 
	{
		Integer i=(Integer)nbBinding.get(fromWhat);
		if(i==null)
			return 0;
		else
			return i.intValue();
	}



	public void setNbBindingForLink(int nbBinding, Object fromWhat) 
	{
		this.nbBinding.put(fromWhat, new Integer(nbBinding));
	}
	
	public String getWrapperName()
	{
		return wrapperName+"_"+number;
	}
	
	public String getName() throws RemoteException
	{
		return getWrapperName();
	}
	
	public void setName(String name, int numero) throws RemoteException
	{
		this.number=numero;
		wrapperName=name;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public void setFixDiagram(Hashtable fix)
	{
		fixDiagram=fix;
	}
	
	public void setAppliComponent(Component appli)
	{
		appliComponent=appli;
	}
	
	public void setComponentThis(Object ct)
	{
		setMySelf(ct);
	}
	
	public GenericWrapper getGw()
	{
		return this;
	}
	
	
	
	/******************** Start & Stop *******************/
	
	public Thread getDeploy()
	{
		return deployThread;
	}
	
	public boolean isDeployed()
	{
		// si on a une référence sur notre alter ego distant, on lui demande juste si il est déployé !
		// on fait un ping quoi.
		boolean retour=false;
		try
		{
			deployThread.join();
			retour=deployed && remoteWrapper.isDeployed();
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception", e);
		}
		return retour;
	}
	
	
	private void startLocalRMIStub() throws Exception
	{
		try
		{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
		}
		catch(Exception e)
		{
			// le serveur est sûrement déjà démarré sur ce noeud
		}
		// on s'enregistre sur le serveur local RMI
		nc=new NotifyClass(fixDiagram, appliComponent,this);
		Naming.rebind("rmi://"+InetAddress.getLocalHost().getHostName()+"/notify/"+getWrapperName(),nc);

	}
	

	// LifeCycleController methods
	public void startFc() throws IllegalLifeCycleException 
	{
		isStarted=true;
	}
	
	public void stopFc() throws IllegalLifeCycleException 
	{
		isStarted=false;
	}
	
	public String getFcState()
	{
		return (isStarted?LifeCycleController.STARTED:LifeCycleController.STOPPED);
	}
	

	// Méthodes du déployer
	public void deploy() 
	{
		(deployThread=new Thread()
		{
			
			public void run()
			{
		
				Generator.logger.info("-> Deploying "+getWrapperName());
				n=((NodeAllocatorItf)lookupFc("node")).getNode();
		
				String tubeAddr=n.getDirLocal()+"/"+getWrapperName();
		
				
				// on sette les attributs
				setAttribute("dirLocal",n.getDirLocal());
				setAttribute("nodeName",n.getNodeName());
				setAttribute("tubeAddr",tubeAddr);
		
		

				try
				{
					
					// on démarre le démon RMI pour les notifications
					startLocalRMIStub();
		
					
					// on envoie le legacy
					String legacy=getAttribute("legacyFile");
					if(legacy!=null)
					{
						if(!n.isLegacyDeployed(legacy))
						{
						
							Generator.logger.info("Now deploying legacy file "+legacy+" on node "+n.getNodeName());
							String legacyToDeploy=n.getProtocoleCopy()+" -q -q "+rep+legacy+" "+n.getUser()+(n.getUser().equals("")?"":"@")+n.getNodeName()+":"+n.getDirLocal()+"/";
							String deploy=n.getProtocoleRsh()+" -q -q "+n.getUser()+(n.getUser().equals("")?"":"@")+n.getNodeName()+" -- cd "+n.getDirLocal()+" ; tar -zxf "+n.getDirLocal()+"/"+legacy;
							remote_process=Runtime.getRuntime().exec(legacyToDeploy);
							new StreamGlobbler(remote_process.getInputStream(),"PushLegacy");
							new StreamGlobbler(remote_process.getErrorStream(),"PushLegacy");
							remote_process.waitFor();
							
							// on déploie le legacy
							remote_process=Runtime.getRuntime().exec(deploy);
							new StreamGlobbler(remote_process.getInputStream(),"DeployLegacy");
							new StreamGlobbler(remote_process.getErrorStream(),"DeployLegacy");
							remote_process.waitFor();
							
							n.addALegacy(legacy);
							
						}
						else
							Generator.logger.info("File "+legacy+" already deploy on node "+n.getNodeName());
												
					}
					else
						Generator.logger.warn("No legacy file defined for component "+getWrapperName());
					
					// oki, maintenant, on démarre le wrapper distant
					n.getRl().deployARemoteWrapper(getWrapperName(), n.getRmi_port(), tubeAddr);

					for(int i=0;i<retries&&!deployed;i++)
					{
						try
						{
							remoteWrapper=(WrapperItf)Naming.lookup("rmi://"+n.getNodeName()+":"+n.getRmi_port()+"/"+getWrapperName());
							remoteWrapper.setNotifyNode("rmi://"+InetAddress.getLocalHost().getHostName()+"/notify/"+getWrapperName());
							deployed=true;
						}
						catch(Exception e)
						{
							if(i==retries)
								deployed=false;		// inutile, c'est déjà le cas à priori
							else
							{
								// on attends un peu et on recommence
								try
								{
									Thread.sleep(1500);
								}
								catch(InterruptedException ex)
								{}
							}
						}
					}
				}
			
				catch(Exception e)
				{
					Generator.logger.error("Exception",e);
				}
			}
		}).start();

	}


	public void undeploy()
	{
		// on tue notre RemoteWrapper associé, par contre, on ne touche pas aux fichiers de déploiement car on est peut être pas le seul
		//  élément de cette classe de composant
		try
		{
			remoteWrapper.killRemoteWrapper();
		}
		catch(Exception e)
		{
			// au pire on prendra une exception ici car on aura éteint le serveur RMI.
			// Mais on est sûr que le remoteWrapper existe, sinon le composant n'aurait pas été lancé
		}
	}

	
	/***** Interfaces WrapperItf ****/	
    public void meth(String className, String methName, Object[] args) throws RemoteException
    {
    	try
    	{
    		LinkFc lkfc=((LinkFc)linkfc.get(className));
    		if(lkfc.isMeth())
    		{
    			Generator.logger.info("Running meth "+lkfc.getClassName()+"."+lkfc.getMethodName()+" on "+getWrapperName());
    			
    			Object []argsR=lkfc.resoud_arg();
    			String argsRS="";
    			for(int i=0;i<argsR.length;i++)
    				argsRS=argsRS+(String)argsR[i]+" ";
    			Generator.logger.info("   with args "+argsRS);
    			
    			remoteWrapper.meth(lkfc.getClassName(), lkfc.getMethodName(), lkfc.resoud_arg());
    		}
    		else
    			lkfc.apply();
    	}
    	catch(Exception e)
    	{
    		Generator.logger.error("Exception - ", e);
    	}
    	
    
    }

	
	public void setNotifyNode(String node) throws RemoteException
	{
		// ici, on ne fait rien, c'est une méthode pour RemoteWrapper ça
	}
	
	public void killRemoteWrapper() throws RemoteException
	{
		// ici, on ne fait rien, c'est une méthode pour RemoteWrapper ça
	}

	
	
	
	
	
}
