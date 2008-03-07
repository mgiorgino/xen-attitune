package core.xmiparser;

import java.util.Vector;

import org.objectweb.fractal.api.Component;

import core.wrapper.local.GenericWrapper;

public class OneLegacy extends OneEntity
{
	private String sr_name;
	private Vector c;
	private String wrapperFile, hostFamily;
	private int nb_current;
	private String nodeFile,javaHome,user, dirLocal, protocole;
	
	private Vector attributes;
	
	private int numero;
	
	public OneLegacy(String sr_name, String xmi_id)
	{
		super(xmi_id,OneEntity.LEGACY);
		this.sr_name=sr_name;
		c=new Vector();
		attributes=new Vector();
		nb_current=1;
		numero=0;
		wrapperFile="";
	}
	
	public int getNumber()
	{
		return numero++;
	}

	
	public String getProtocole() 
	{
		return protocole;
	}

	public void setProtocole(String protocole) 
	{
		this.protocole = protocole;
	}


	public String getDirLocal() 
	{
		return dirLocal;
	}

	public void setDirLocal(String dirLocal) 
	{
		this.dirLocal = dirLocal;
	}

	public String getHostFamily() 
	{
		return hostFamily;
	}

	public void setHostFamily(String hostFamily) 
	{
		this.hostFamily = hostFamily;
	}

	public String getNodeFile() 
	{
		return nodeFile;
	}

	public void setNodeFile(String nodeFile) 
	{
		this.nodeFile = nodeFile;
	}

	public String getJavaHome() 
	{
		return javaHome;
	}

	public void setJavaHome(String javaHome) 
	{
		this.javaHome = javaHome;
	}

	public String getUser() 
	{
		return user;
	}

	public void setUser(String user) 
	{
		this.user = user;
	}

	public String getSr_name()
	{
		return sr_name;
	}
	
	public void addAttributes(String att, String value)
	{
		String tab[]={att,value};
		attributes.add(tab);
	}
	
	public Vector getAttributes()
	{
		return attributes;
	}
	
	public void setComponent(Component c, GenericWrapper gw)
	{
		Object stockage[]=new Object[2];
		stockage[0]=c;
		stockage[1]=gw;
		this.c.add(stockage);
	}
	
	public Component getComponent(int no)
	{
		return (Component)(((Object[])c.get(no))[0]);
	}
	
	public GenericWrapper getGw(int no)
	{
		return (GenericWrapper)(((Object[])c.get(no))[1]);
	}
	
	public int getNbComponent()
	{
		return c.size();
	}
	
	
	public void setWrapperFile(String wrapperFile)
	{
		this.wrapperFile=wrapperFile;
	}
	
	public String getWrapperFile()
	{
		return wrapperFile;
	}
	
	public void setNbCurrent(String nb)
	{
		nb_current=Integer.parseInt(nb);
	}
	
	public void setNbCurrent(int nb)
	{
		nb_current=nb;
	}
	
	public int getNbCurrent()
	{
		return nb_current;
	}
	
	
	
}
