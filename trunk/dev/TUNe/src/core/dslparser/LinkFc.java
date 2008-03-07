package core.dslparser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import core.main.Generator;
import core.util.AttributeUtil;
import core.util.TuneException;
import core.wrapper.local.GenericWrapper;



public class LinkFc 
{
	protected String className;
	protected String methName;
	protected ArrayList args;
	protected ArrayList links;
	protected ArrayList linkfc;
	protected GenericWrapper gw;
	protected String name;
	protected ArrayList args_resolus;
	private boolean call_meth;
	private boolean multiple;
	
	public LinkFc(GenericWrapper gw)
	{
		args=new ArrayList();
		links=new ArrayList();
		linkfc=new ArrayList();
		this.gw=gw;
		this.call_meth=false;
		this.multiple=false;
	}
	
	public LinkFc(GenericWrapper gw, boolean call_meth)
	{
		if(call_meth)
		{
			// cas d'une méthode
			args=new ArrayList();
			links=new ArrayList();
			linkfc=new ArrayList();
			this.gw=gw;
			this.call_meth=true;
			this.multiple=false;
		}
		else
		{
			// cas d'un linkfc-multiple
			args=new ArrayList();
			links=new ArrayList();
			linkfc=new ArrayList();
			this.gw=gw;
			this.call_meth=false;
			this.multiple=true;
		}
		
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setMethod(String methName)
	{
		this.methName=methName;
	}
	
	public void setClass(String className)
	{
		this.className=className;
	}
	
	public boolean isMeth()
	{
		return call_meth;
	}
	
	public void addAttribute(String value, GenericWrapper wg)
	{
		args.add(value);	
		
	}
	
	public String apply() throws Exception
	{
		if(!multiple)
			return apply_aux(null);
		else
		{
			// on se fait renvoyer tous les fcname qui sont rattachés à notre nom
			Collection values=gw.getFcData().values();
			Iterator ite=values.iterator();
			InterfaceCliente fc;
			while(ite.hasNext())
			{
				fc=(InterfaceCliente)ite.next();
				if(fc.itfGroup.equals(className))
					apply_aux(fc.itfValue);
			}
			
			return "";
			
		}
	}
	
	public Object[] resoud_arg() throws Exception
	{
		try
		{
			args_resolus=new ArrayList();
			// on résoud les liens
			for(int i=0;i<args.size();i++)
				args_resolus.add(AttributeUtil.solveAtt((String)args.get(i),gw));
			
			return args_resolus.toArray();
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
			return null;
		}
	}
	
	public String getMethodName()
	{
		return this.methName;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	public String apply_aux(Object oo) throws Exception
	{
		// on récupère la classe associée à la clé
		Object o=null;
		Class cl=null;
		
		if(oo==null)
		{
			if(!call_meth)
				// linkfc/linkfc-controler
				o=gw.lookupFc(className);
			else
			{
				// method
				cl=Class.forName(className);
				
				try
				{
					// si le champs __wv existe, on le set
					Field __wv=cl.getDeclaredField("__wv");
					__wv.set(null, gw);
				}
				catch(Exception e)
				{
					// oki, il n'existe pas
				}
				
			}
		}
		else
			// linkfc-multiple
			o=oo;
		
		Method m=null;
		
		// on résoud les liens
		resoud_arg();
		
		try
		{
			Class [] c=new Class[args.size()];
			for(int i=0;i<args.size();i++)
				c[i]=String.class;
			if(!call_meth)
				m=o.getClass().getMethod(methName, c);
			else
				m=cl.getMethod(methName, c);

		}
		catch(NoSuchMethodException e)
		{
			throw new TuneException("Runtime - LinkFc.apply() - NoSuchMethod: "+methName);
		}
		
		try
		{
			return ((String)m.invoke(o,args_resolus.toArray()));
		}
		catch(Exception e)
		{
			throw new TuneException("Runtime - LinkFc.apply() - InvokeError");
		}

	}
}
