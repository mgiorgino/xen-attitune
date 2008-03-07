package core.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;

import core.wrapper.local.GenericWrapper;
import core.xmiparser.OneChartBinding;
import core.xmiparser.OneChartState;

public class ComponentRunning
{
	Component appliComponent;
	
	public ComponentRunning(Component appliComponent)
	{
		this.appliComponent=appliComponent;
	}
	
	public OneChartState[] duplicateChart(Collection c)
	{
		OneChartState [] ret=new OneChartState[2];
		Vector state_binding=(Vector)((ArrayList)c).get(3);
		Hashtable state=(Hashtable)((ArrayList)c).get(4);
		Hashtable new_hash=new Hashtable();
		OneChartState first=null;
		OneChartState last=null;
		
		// on clone la hastable et ses états
		Enumeration e=state.keys();
		Object o;
		OneChartState clone;
		while(e.hasMoreElements())
		{
			o=e.nextElement();
			clone=((OneChartState)state.get(o)).simplyClone();
			new_hash.put(o, clone);
			if(clone.isInitial())
				first=clone;
			if(clone.isFinal())
				last=clone;
		}
		
		state=new_hash;
		
		// on génère la liste chainée associée au statechart qu'on vient de dupliquer
		OneChartBinding ocb=null;
		for(int i=0;i<state_binding.size();i++)
		{
			try
			{
				// oki, un binding, on l'associe aux deux éléments des bouts
				ocb=(OneChartBinding)state_binding.get(i);
				((OneChartState)state.get(ocb.getXmiFrom())).addNextState((OneChartState)state.get(ocb.getXmiTo()));
				((OneChartState)state.get(ocb.getXmiTo())).addPreviousState((OneChartState)state.get(ocb.getXmiFrom()));
			}
			catch(NullPointerException exc)
			{
				Generator.logger.warn("No object bound to the "+ocb.getXmiId());
			}
		}
		
		ret[0]=first;
		ret[1]=last;

		return ret;
	}

	public void run(Collection c)
	{
		try
		{
			OneChartState ret[]=duplicateChart(c);
			OneChartState first=ret[0];
			OneChartState last=ret[1];
			
			// le premier état est par définition l'état initial
			// on démarre le parcours !
			
			first.startStateChart(((ContentController)appliComponent.getFcInterface("content-controller")).getFcSubComponents());
			
			synchronized(last)
			{
				last.wait();
			}
			
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
		}
	}
	
	public void run(Collection c, String failedItem, String argComponent, String argVar, GenericWrapper gw)
	{
		try
		{
			OneChartState ret[]=duplicateChart(c);
			OneChartState first=ret[0];
			OneChartState last=ret[1];
			
			first.setArgVar(argVar);
			first.setGw(gw);
			first.setComponentArg(argComponent);
			
			// le premier état est par définition l'état initial
			// on démarre le parcours !
			Generator.logger.info("Running the "+first.getName()+" state chart");
			first.setFailedComponent(failedItem);
			first.startStateChart(((ContentController)appliComponent.getFcInterface("content-controller")).getFcSubComponents());
			
			synchronized(last)
			{
				last.wait();
			}
			
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
		}
	}
		
	
}
