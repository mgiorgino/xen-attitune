package core.node;

import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.util.Vector;

import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import core.interfaces.LauncherItf;
import core.interfaces.NodeAllocatorItf;
import core.main.Generator;
import core.util.StreamGlobbler;
import core.util.TuneException;
import core.wrapper.local.GenericWrapper;

public class GenericNode extends GenericWrapper implements NodeAllocatorItf
{
	
	private boolean isStarted, isDeploy;
	
	private String name;
	
	Vector nodes;
	
	// on fait du round robin dans notre liste de noeud
	private int current_node;
	
	private String rep;
	
	
	public GenericNode(String name, String nodeFile, String javaHome, String user, String dirLocal, String protocole, String rep)
	{
		isStarted=false;
		isDeploy=false;
		
		this.name=name;
		current_node=0;
		
		this.rep=rep;
		
		Generator.logger.info("NodeFile: "+nodeFile);
		
		setAttribute("name",name);
		
		// on crée la liste de noeuds associés
		RandomAccessFile nodeFileFDW;
		nodes=new Vector();
		try 
		{
			nodeFileFDW = new RandomAccessFile(rep+nodeFile, "r");

			String tmp=nodeFileFDW.readLine();
			while (tmp != null)
			{
				nodes.add(new Node(tmp,javaHome,user,dirLocal, protocole,rep));
				tmp=nodeFileFDW.readLine();
			}

			nodeFileFDW.close();
		}

		catch (Exception e) 
		{
			Generator.logger.error("getNodes() : Exception - 2 - ",e);
		}

	}
	
	public String getName()
	{
		return name;
	}
	
	public void shutdownNodes()
	{
		Node n;
		for(int i=0;i<nodes.size();i++)
		{
			n=(Node)nodes.elementAt(current_node);
			n.shutdownNode();
		}
	}
	
	public Node getNode()
	{

		// un node est juste une structure dont le generic wrapper se sert par la suite
		Node n=(Node)nodes.elementAt(current_node);
		current_node = (current_node + 1) % nodes.size();
		
		n.deploy();

		return n;
	}
	
	// LifeCycleController methods
	public String getFcState()
	{
		return (isStarted?LifeCycleController.STARTED:LifeCycleController.STOPPED);
	}

	public void startFc() throws IllegalLifeCycleException 
	{
		isStarted=true;
	}

	public void stopFc() throws IllegalLifeCycleException 
	{
		// lors du stop, on va détruire les autres noeuds
		isStarted=false;
	}

	public void deploy() 
	{
		isDeploy=true;
	}

	public boolean isDeployed() 
	{
		return isDeploy;
	}

	public void undeploy() 
	{
		shutdownNodes();
		isDeploy=false;
	}
	
}
