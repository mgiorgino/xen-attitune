package core.dslparser;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.fractal.api.control.BindingController;

import core.interfaces.AttributeControllerItf;

public class AttFcImpl implements AttributeControllerItf, BindingController 
{

	private Hashtable attributs;
	private Hashtable fc_data;
	
	public AttFcImpl()
	{
		attributs=new Hashtable();
		fc_data=new Hashtable();

	}
	
	/******************** Attributs *******************/
	
	public String getAttribute(String name) 
	{
		return (String)attributs.get(name);
	}
	
	public void setAttribute(String name, String value)
	{
		attributs.put(name,value);
	}
	
	public String[] listFcAtt()
	{
		int size=attributs.size();
		int i=0;
		String [] atts=new String[size];
		Enumeration e=attributs.keys();
		while(e.hasMoreElements())
			atts[i++]=(String)e.nextElement();
		return atts;
	}
	
	public Hashtable getAttributes()
	{
		return attributs;
	}

	/******************** Interface clientes Fc *******************/
	
	public void bindFc(final String itfName, final Object itfValue) 
	{	
		fc_data.put(itfName, new InterfaceCliente(itfName,"equals",itfValue));
	}
	
	public String[] listFc()
	{
		int size=fc_data.size();
		int i=0;
		String [] atts=new String[size];
		Enumeration e=fc_data.keys();
		while(e.hasMoreElements())
			atts[i++]=(String)e.nextElement();
		
		return atts;
		
	}
	
	public void setMySelf(Object ct)
	{
		fc_data.put("this", new InterfaceCliente("this","equals", ct));
	}
	
	public Object lookupFc(String itfName)
	{
		try
		{
			return ((InterfaceCliente)fc_data.get(itfName)).itfValue;
		}
		catch(Exception e)
		{
			// on n'a pas trouvé l'interface avec le nom extact. Peut être que cette interface possède un suffixe
			// on va balayer pour trouver la première
			Enumeration enu=fc_data.keys();
			String cle;
			String prefixe;
			while(enu.hasMoreElements())
			{
				cle=(String)enu.nextElement();
				prefixe=cle.split("\\_")[0];
				if(prefixe.equals(itfName))
					return ((InterfaceCliente)fc_data.get(cle)).itfValue;
				
			}
		}
		
		return null;
	}
	
	public Vector getAllFc(String itfName)
	{
		Enumeration enu=fc_data.keys();
		String cle;
		Vector retour=new Vector();
		while(enu.hasMoreElements())
		{
			cle=(String)enu.nextElement();
			if(cle.startsWith(itfName))
				retour.add(((InterfaceCliente)fc_data.get(cle)).itfValue);
			
		}
		
		return retour;
		
	}
	
	public Vector getAllFcName(String itfName)
	{
		Enumeration enu=fc_data.keys();
		String cle;
		Vector retour=new Vector();
		while(enu.hasMoreElements())
		{
			cle=(String)enu.nextElement();
			if(cle.startsWith(itfName))
				retour.add(cle);
			
		}
		
		return retour;
		
	}
	
	public void unbindFc(String itfName)
	{
		
		fc_data.remove(itfName);
		
		
	}
	
	public Hashtable getFcData()
	{
		return fc_data;
	}

}
