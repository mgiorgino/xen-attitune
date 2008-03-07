package core.dslparser;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import core.wrapper.local.GenericWrapper;

public class SimpleContentHandler extends DefaultHandler
{

	private GenericWrapper wg;
	
	private LinkFc lkfc;
	
	private String config_attribute_value;
	

	public SimpleContentHandler(GenericWrapper wg) 
	{
		super();
		lkfc=null;
		this.wg=wg;
	}



	public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributs) throws SAXException 
	{
		
		if(localName.equals("method"))
		{
			lkfc=new LinkFc(wg,true);
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
			
				if(attributs.getLocalName(index).equals("key"))
					lkfc.setClass(attributs.getValue(index));
				if(attributs.getLocalName(index).equals("method"))
					lkfc.setMethod(attributs.getValue(index));
				if(attributs.getLocalName(index).equals("name"))
					lkfc.setName(attributs.getValue(index));
			}
			
		}

		
		else if(localName.equals("param"))
		{
			config_attribute_value=null;
			
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				if (attributs.getLocalName(index).equals("value"))
					config_attribute_value=attributs.getValue(index);
			}
			lkfc.addAttribute(config_attribute_value,wg);
			
		}
		
	}

	public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException 
	{
		if(localName.equals("method"))
		{
			wg.linkfc.put(lkfc.getName(), lkfc);
			lkfc=null;
		}
		
	}


}


