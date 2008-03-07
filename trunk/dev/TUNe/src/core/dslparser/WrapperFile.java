package core.dslparser;

import java.util.ArrayList;

import core.util.TuneException;
import core.wrapper.local.GenericWrapper;


public class WrapperFile 
{
	int src;
	String resname;
	
	String name;
	boolean erase;
	
	boolean dir;
	
	boolean copy_all;
	
	private GenericWrapper gw;
	
	String linkfc=null;
	String linkattribute=null;
	
	
	ArrayList content_list;
	WrapperFile owned_by;
	
	// Content
	public WrapperFile(GenericWrapper gw, String resname, String type, WrapperFile owned_by, String linkfc, String linkattribute) throws Exception
	{
		dir=false;
		
		this.gw=gw;
		this.resname=resname;
		this.owned_by=owned_by;
		this.linkattribute=linkattribute;
		this.linkfc=linkfc;
		
		copy_all=type.equals("dir");
		
	}
	
	// Directory locale
	public WrapperFile(GenericWrapper gw, String name, String erase, String linkfc, String linkattribute) throws Exception
	{
		
		dir=true;
		
		this.gw=gw;
		
		if(erase.equalsIgnoreCase("false"))
			this.erase=false;
		else
			this.erase=true;
		
		this.name=name;
		this.linkfc=linkfc;
		this.linkattribute=linkattribute;
		content_list=new ArrayList();
		
	}
	
	public void addContent(WrapperFile wf)
	{
		content_list.add(wf);
	}
	
	public String resoud() throws Exception
	{
		// on résoud le nom de la directory
		if(linkfc!=null)
		{
			LinkFc lkfc=(LinkFc)gw.linkfc.get(linkfc);
			if(lkfc==null)
				throw new TuneException("Runtime - WrapperFile.resoud() - NoSuchLinkFc");
			
			return lkfc.apply();
		}
		if(linkattribute!=null)
			return gw.getAttribute(linkattribute);
		
		if(dir)
			return name;
		else
			return resname;
	}
	

}
