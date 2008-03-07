package extension;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;



public class GenericConfigureXML 
{
	private Document xmlDoc;
	private Element root;
	private String separator;
	private String affectator;
	
	public void configure(String file, String[] args)
	{
		System.out.println("configuring XML file: "+file);

		try
		{
			// on lit le fichier XML
			SAXBuilder builder = new SAXBuilder();
			xmlDoc = builder.build(new File(file));

			root=xmlDoc.getRootElement();
			
			String cle, value;
			String []path;
			separator=args[0];
			affectator=args[1];

			System.out.println("-> separator: "+separator);
			System.out.println("-> affectator: "+affectator);

			for(int i=2;i<args.length;i++)
			{
			    cle=args[i].split(affectator,2)[0];
			    value=args[i].split(affectator,2)[1];
			    
			    // on décompose la clé en fonction des '/'
			    path=cle.split("\\"+separator);
			    
			    searchElem(path,value);
			    

			}
			
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(root, new FileOutputStream(file));
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void searchElem(String[] path, String value)
	{
		searchElemAux(path, 0, root, value);
	}
	
	private boolean match(Element e, String value)
	{
		// la chaine value est composée ainsi: nom[(att1=val1,att2=val2,...)]
		String name;
		String param;
		String []params;
		
		name=value.split("\\(")[0];

		try
		{
			boolean allMatch=true;
			
			param=value.split("\\(")[1];
			param=param.substring(0, param.length()-1);
			
			params=param.split(",");
			
			Attribute a;
			String key,valeur;
			
			for(int i=0;i<params.length;i++)
			{
				key=params[i].split("=")[0];
				valeur=params[i].split("=")[1];
				a=e.getAttribute(key);
				allMatch&=a.getValue().equals(valeur);
			}
			
			if(!allMatch)
				return false;
			
		}
		catch(Exception ex)
		{
			param="";
		}

		return name.equals(e.getName());
	}

	private void searchElemAux(String[] path, int level, Element current, String value)
	{
		// on allons rechercher l'élement qui va bien
		try
		{
			if(level==0)
			{
				// cas particulier de la racine
				if(match(current, path[0]))
					level++;
				else
					// en première approche
					return ;
			}
			
			List l=current.getChildren();
			Element e=null;
			
			for(int i=0;i<l.size();i++)
			{
				e=(Element)l.get(i);
				if(match(e,path[level]))
					searchElemAux(path,level+1,e,value);
			}
			
			// si on arrive ici, on a un problème.
			// soit l'élément n'existe pas, soit il faut le chercher à ce niveau (mais alors, level=path.length-1)
			if(level==path.length-1)
			{
				boolean found=false;
				// oki, on cherche l'élément parmis les attributs ou parmis le texte enbalisé
				l=current.getAttributes();
				Attribute a;
				for(int i=0;i<l.size();i++)
				{
					a=(Attribute)l.get(i);
					if(a.getName().equals(path[level]))
					{
						found=true;
						a.setValue(value);
					}
				}
				
				if(!found && ("@"+e.getName()).equals(path[level]))
					e.setText(value);
				
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String args[])
	{
		(new GenericConfigureXML()).configure("/home/cedric/tmp/fic.composite", args);
	}

}
