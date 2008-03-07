package core.dslparser;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;

import core.interfaces.AttributeControllerItf;
import core.util.TuneException;
import core.wrapper.local.GenericWrapper;




public class LinkFcControler extends LinkFc
{
	private String interfaceName;
	
	public LinkFcControler(GenericWrapper gw)
	{
		super(gw);
		args=new ArrayList();
		links=new ArrayList();
		linkfc=new ArrayList();
		this.gw=gw;
	}
	
	
	public void setInterface(String interfaceName)
	{
		this.interfaceName=interfaceName;
	}
	
	
	public String apply() throws Exception
	{
		// on récupère la classe associée à la clé
		Object o=gw.lookupFc(className);
		
		Method m=null;
		AttributeControllerItf ma;
		
		if(o==null)
		{
			throw new TuneException("Runtime - LinkFcControler.apply() - NoSuchInterface: "+methName);	
		}
		
		
		
		// on résoud les liens
		resoud_arg();
		
		try
		{
			Interface t = (Interface)o;
			Component comp = (t).getFcItfOwner();
			
			ma = (AttributeControllerItf)(comp.getFcInterface(interfaceName));
			
			Class [] c=new Class[args.size()];
			for(int i=0;i<args.size();i++)
				c[i]=String.class;
			m=ma.getClass().getMethod(methName, c);
		}
		catch(NoSuchMethodException e)
		{
			
			throw new TuneException("Runtime - LinkFcControler.apply() - NoSuchMethod: "+methName);
		}
		
		try
		{
			return ((String)m.invoke(ma,args_resolus.toArray()));
		}
		catch(Exception e)
		{
			throw new TuneException("Runtime - LinkFcControler.apply() - InvokeError");
		}

	}
}
