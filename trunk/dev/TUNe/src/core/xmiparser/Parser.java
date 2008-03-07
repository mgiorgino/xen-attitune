package core.xmiparser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import core.main.ComponentFactory;
import core.main.Generator;


/** Parse un fichier XMI pour en extraire la structure et générer un système FRACTAL.
 * 
 * Le système ainsi généré sera intégré à un composite Deployer
 *
 */


public class Parser extends DefaultHandler
{
	private String uml;
	private XMLReader saxReader;
	
	private boolean association_evt;
	private boolean diagramme_evt;
	private boolean class_evt;
	private OneBinding ob;
	private OneLegacy ol;
	
	private Hashtable ht;
	private Hashtable to_fractal;
	
	private Hashtable state;
	private Vector state_binding;
	
	private Hashtable return_c;
	
	private boolean statechart_evt;
	private String no_collection;
	
	private OneChartState first,last;
	
	private int diag_type;
	private final int CLASS_TYPE=0;
	private final int STATEMACHINE_TYPE=1;
	private final int ASSOC_TYPE=2;
	private final int PACKAGE_TYPE=3;
	private final int UNKNOW_TYPE=99;
	
	private boolean attribute_evt;
	private String attribute_name;
	
	private boolean card_evt;
	private boolean firstEndPoint;
	
	private boolean comment_evt, comment_body_evt;
	private String comment;
	
	private ComponentFactory mcf;
	
	public Parser(String uml, ComponentFactory mcf) throws Exception
	{
		this.uml=uml;
		this.mcf=mcf;

		saxReader = XMLReaderFactory.createXMLReader();
		saxReader.setContentHandler(this);
		
		return_c=new Hashtable();
		
	}
	
	public Hashtable parse() throws Exception
	{
		Generator.logger.info("Parsing XMI file ("+uml+")...");
		
		ht=new Hashtable();
		to_fractal=new Hashtable();
		association_evt=false;
		diagramme_evt=false;
		class_evt=false;
		statechart_evt=false;
		attribute_evt=false;
		card_evt=false;
		comment_evt=false;
		comment_body_evt=false;

		saxReader.parse(new InputSource(new FileInputStream(uml)));

		return return_c;


	}
	
	public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributs) throws SAXException 
	{
		
		// récupération des classes et des attributs
		if(localName.equals("packagedElement"))
		{
			String name=null, id=null;
			diag_type=UNKNOW_TYPE;
			
			// ici, ça regroupe un peu tout :(
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("type") && attributs.getValue(index).equals("uml:Class"))
					diag_type=CLASS_TYPE;
				else if (attributs.getLocalName(index).equals("type") && attributs.getValue(index).equals("uml:StateMachine"))
					diag_type=STATEMACHINE_TYPE;
				else if (attributs.getLocalName(index).equals("type") && attributs.getValue(index).equals("uml:Association"))
					diag_type=ASSOC_TYPE;
				else if (attributs.getLocalName(index).equals("type") && attributs.getValue(index).equals("uml:Package"))
					diag_type=PACKAGE_TYPE;
				else if(attributs.getLocalName(index).equals("name"))
					name=attributs.getValue(index);
				else if(attributs.getLocalName(index).equals("id"))
					id=attributs.getValue(index);

				
			}
			
			if(diag_type==CLASS_TYPE)
			{
				ol=new OneLegacy(name,id);
				ht.put(id, ol);
				class_evt=true;
				
				// cette hastable est là pour séparer les classes déclarées des classes utilisées.
				// Avec TopCased, on met dans des packages différents on en a donc plus besoin.
				to_fractal.put(id, ht.get(id));
				
			}
			else if(diag_type==STATEMACHINE_TYPE)
			{
				if(name.equals("startchart") || name.equals("stopchart") || name.startsWith("chart-"))
				{
					statechart_evt=true;
					state=new Hashtable();
					state_binding=new Vector();
					no_collection=name;
				}
			}
			else if(diag_type==ASSOC_TYPE)
			{
				association_evt=true;
				ob=new OneBinding(id);
				ob.setName(name);
				ht.put(id, ob);
				
				// cette hastable est là pour séparer les classes déclarées des classes utilisées.
				// Avec TopCased, on met dans des packages différents on en a donc plus besoin.
				to_fractal.put(id, ht.get(id));
				
			}
			else if(diag_type==PACKAGE_TYPE)
			{
				ht=new Hashtable();
				to_fractal=new Hashtable();
				
				diagramme_evt=true;
				no_collection=name;

			}

			
		}
		
		if(localName.equals("ownedAttribute")&&class_evt)
		{
			String name="",id="";
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("name"))
					attribute_name=attributs.getValue(index);
				
			}
			
			attribute_evt=true;				
			
			
		}
		
		if(localName.equals("defaultValue")&&attribute_evt)
		{
			String id="";
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("value"))
					id=attributs.getValue(index);
				
			}

			// pour les applicatifs
			if(attribute_name.equals("wrapper"))
				ol.setWrapperFile(id);
			else if(attribute_name.equals("initial"))
				ol.setNbCurrent(id);
			else if(attribute_name.equals("host-family"))
				ol.setHostFamily(id);
			
			// pour les noeuds
			else if(attribute_name.equals("nodefile"))
				ol.setNodeFile(id);
			else if(attribute_name.equals("javahome"))
				ol.setJavaHome(id);
			else if(attribute_name.equals("user"))
				ol.setUser(id);
			else if(attribute_name.equals("dirlocal"))
				ol.setDirLocal(id);
			else if(attribute_name.equals("protocole"))
				ol.setProtocole(id);

			
			ol.addAttributes(attribute_name,id);
			
		}
		
		if(localName.equals("ownedEnd") && association_evt)
		{
			String id=null;
			
			card_evt=true;
			
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("type"))
					id=attributs.getValue(index);
			}

			if(!ob.isFromSetted())
			{
				ob.setFromXMI(id);
				firstEndPoint=true;
			}
			else
			{
				ob.setToXMI(id);
				firstEndPoint=false;
			}
			
		}
		
		if(localName.equals("upperValue") && card_evt)
		{
			String multiplicity="1";
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("value"))
					multiplicity=attributs.getValue(index);
			}
			if(firstEndPoint)
				ob.setMultiplicityFrom(multiplicity);
			else
				ob.setMultiplicityTo(multiplicity);
				
			
		}
		
		if(localName.equals("lowerValue") && card_evt)
		{
			String multiplicity="1";
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("value"))
					multiplicity=attributs.getValue(index);
			}
			if(firstEndPoint)
				ob.setMultiplicityFromLower(multiplicity);
			else
				ob.setMultiplicityToLower(multiplicity);
				
			
		}
		
		
		// ok, on passe aux statecharts
		if(localName.equals("subvertex")&&statechart_evt)
		{
			String name=null, id=null, type=null, kind="";
			OneChartState tmp;

			// oki, on a détecté un état. Cela signifie que c'est un legacy
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("id"))
					id=attributs.getValue(index);
				if(attributs.getLocalName(index).equals("name"))
					name=attributs.getValue(index);
				if(attributs.getLocalName(index).equals("type"))
					type=attributs.getValue(index);
				if(attributs.getLocalName(index).equals("kind"))
					kind=attributs.getValue(index);
				
			}
			
			// on analyse le type de l'état
			if(type.equals("uml:State"))
				tmp=new OneChartState(id, OneChartState.BETWEEN,name,mcf);
			else if(type.equals("uml:FinalState"))
				tmp=new OneChartState(id, OneChartState.FINAL,name,mcf);
			else if(type.equals("uml:Pseudostate") && (kind.equals("fork")||kind.equals("join")))
				tmp=new OneChartState(id,OneChartState.FORKJOIN,"",mcf);		
			else
				tmp=new OneChartState(id,OneChartState.INITIAL,"",mcf);		
			
			state.put(id,tmp);
			
			if(tmp.isInitial())
			{
				first=tmp;
				first.setName(no_collection);
				first.setTrigger(comment);
			}
			
			if(tmp.isFinal())
				last=tmp;
			
		}
		
		if(localName.equals("transition")&&statechart_evt)
		{
			String xmi_from=null, xmi_to=null, id=null;

			// oki, on a détecté un état. Cela signifie que c'est un legacy
			for (int index = 0; index < attributs.getLength(); index++) 
			{ 
				// on parcourt la liste des attributs
				if(attributs.getLocalName(index).equals("target"))
					xmi_to=attributs.getValue(index);
				if(attributs.getLocalName(index).equals("source"))
					xmi_from=attributs.getValue(index);
				if(attributs.getLocalName(index).equals("id"))
					id=attributs.getValue(index);
				
				
			}
			
			state_binding.add(new OneChartBinding(xmi_from,xmi_to,id));
			
		}
				
		if(localName.equals("ownedComment")&&statechart_evt)
			comment_evt=true;
		
		if(localName.equals("body")&&comment_evt)
			comment_body_evt=true;
		
			
			

			
	}
	
	public void characters(char[] ch, int start, int end) throws SAXException 
	{
		if(comment_body_evt)
		{
			comment=new String(ch, start, end);
			if(first!=null)
				first.setTrigger(comment);
		}
	}

	public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException 
	{
		
		if(localName.equals("packagedElement")&&class_evt)
			class_evt=false;
		
		else if (localName.equals("packagedElement")&&association_evt)
			association_evt=false;

		else if (localName.equals("packagedElement")&&statechart_evt)
		{
			return_c.put(no_collection,makeList());
			statechart_evt=false;
			first=null;
			last=null;
			comment=null;

		}
		
		else if(localName.equals("packagedElement")&&diagramme_evt)
		{
			if(diagramme_evt)
			{
				diagramme_evt=false;
				
				// on résoud les dépendances xmi-id -> java ref dans les bindings
				Collection c=to_fractal.values();
				Iterator it=c.iterator();
				Object o;

				while(it.hasNext())
				{
					o=it.next();
					if( ((OneEntity)o).isBinding())
					{
						ob=(OneBinding)o;
						try
						{	
							ob.setFrom((OneLegacy)to_fractal.get(ob.getFromXMI()));
							ob.setTo((OneLegacy)to_fractal.get(ob.getToXMI()));
						}
						catch(Exception e)
						{
						
							// il peut y avoir des relations qui pointent sur rien (merci Umbrello).
							// on supprime donc les bindings sans queue ni tête
							to_fractal.remove(ob.getXmiId());
						}
						
					}
				}

				ArrayList ar=new ArrayList();
				Enumeration e=to_fractal.elements();
				while(e.hasMoreElements())
					ar.add(e.nextElement());
				
				return_c.put(no_collection, ar);
				
				
			}
			
		}
		
		else if (localName.equals("ownedAttribute"))
			attribute_evt=false;
		
		else if(localName.equals("ownedEnd"))
			card_evt=false;
		
		else if(localName.equals("ownedComment"))
			comment_evt=false;
		
		else if(localName.endsWith("body"))
			comment_body_evt=false;
			
	}
	
	public ArrayList makeList()
	{
		ArrayList bidon;
		
		// on génère la liste chainée associée au statechart qu'on nous passe en paramètre
		bidon=new ArrayList();
		bidon.add(first);
		bidon.add(last);
		bidon.add(no_collection);
		bidon.add(state_binding);
		bidon.add(state);
		
		return bidon;
		
	}
	
}