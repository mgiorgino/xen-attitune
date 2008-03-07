package core.wrapper.local;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.objectweb.fractal.api.Component;

import core.interfaces.NotifyItf;
import core.main.ComponentRunning;
import core.main.Generator;
import core.util.AttributeUtil;
import core.xmiparser.OneChartState;

public class NotifyClass extends UnicastRemoteObject implements NotifyItf 
{	
	private ComponentRunning mcr;
	private Collection diags;
	private GenericWrapper gw;

	public NotifyClass(Hashtable fixDiagram, Component appliComponent, GenericWrapper gw) throws RemoteException
	{
		mcr=new ComponentRunning(appliComponent);
		diags=fixDiagram.values();
		this.gw=gw;
		
	}
	public void notify(String notification, String component, String arg) throws RemoteException
	{
		String comp_resolv=AttributeUtil.findComponent(component, gw).getWrapperName();
		Generator.logger.info("Notification => "+ notification+" on "+component+" ( "+comp_resolv+" ) with "+arg);

		// on cherche le premier statechart à répondre à notre notification
		Iterator it=diags.iterator();
		String argComponentWrapper;
		while(it.hasNext())
		{
			try
			{
				ArrayList c=(ArrayList)it.next();
				
				OneChartState first=(OneChartState)c.get(0);
				
				if(first.isActivable(notification))
				{
					// on va chercher le composant associé à arg et on le "set" dans les state chart
					GenericWrapper argComponent=AttributeUtil.findComponent(arg, gw);
					
					argComponentWrapper=null;
					if(argComponent!=null)
						argComponentWrapper=argComponent.getWrapperName();
					mcr.run(c, comp_resolv, argComponentWrapper, AttributeUtil.solveAtt(arg, gw), gw);
				}
			}
			catch(ClassCastException e)
			{
				// à priori, pb de cast, on doit être sur le diagramme de déploiement
			}
			catch(Exception e)
			{
				Generator.logger.error("exception",e);
			}
		}
	}

}
