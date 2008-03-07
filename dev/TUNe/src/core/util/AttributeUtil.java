package core.util;

import java.util.Vector;

import org.objectweb.fractal.api.control.BindingController;

import core.interfaces.AttributeControllerItf;
import core.interfaces.NodeAllocatorItf;
import core.interfaces.WrapperItf;
import core.wrapper.local.GenericWrapper;

public class AttributeUtil 
{
	
	/** 
	 * Cette méthode décompose les chaîne de type "$var1.var11/$var2 -cp $var3/toto" et remplace chaque
	 * $var par sa valeur résolue 
	 */ 
	
	public static String solveAtt(String att, GenericWrapper gw)
	{
// System.out.println("[ solveAtt ] appelé avec: "+att);		
		String retour="";
		if(att!=null && att.contains("$"))
		{
			String str[]=att.split("\\ ");
//System.out.println("[ solveAtt ] taille: "+str.length);			
			for(int i=0;i<str.length;i++)
			{			
// System.out.println("[ solveAtt ] string i: "+str[i]);			

			    if(str[i].length()>1 && str[i].contains("$"))
				{
					// on cherche la ou les variables
					String []multi_split=str[i].split("[^a-zA-Z$\\.]");
					String retour_split;
					for(int j=0;j<multi_split.length;j++)
					{
//					    System.out.println("[ solveAtt ] multi_split => "+multi_split[j]);
						retour_split=solveVar(multi_split[j],gw,false,null);
//System.out.println("[ solveAtt ] remplacement dans "+str[i]+" de "+multi_split[j]+" par "+retour_split);						
						str[i]=str[i].replace(multi_split[j], retour_split);
					}
					
				}
				retour=retour+" "+str[i];
			}
//System.out.println("[ solveAtt ] sort avec: "+retour);			
			return retour.trim();
		}
		else
		{
		    //rien à déférencer, on renvoie att
		    //System.out.println("[ solveAtt ] sort avec: "+att);			
			return att;
		}
	}
	
	/**
	 * Cette méthode permet de résoudre une expression de type var=$toto ou var.this.sonde=$toto, var=4...
	 */
	
	public static void evaluateExpr(String expr, GenericWrapper gw) throws TuneException
	{
		String []operande=expr.split("\\=");
//System.out.println("[ evaluateExpr ] appelé avec: "+expr);			
		
		if(operande[0].length() > 1 && operande[0].charAt(0)!='$')
		{
			// on résoud à droite
			String resultat=solveVar(operande[1].trim(),gw,false,null);
			
			// on résoud à gauche et on assigne
			solveVar("$"+operande[0].trim(),gw,true,resultat);
			
//System.out.println("[ evaluateExpr ] réalise l'ope suivante: "+operande[0].trim()+" <- "+resultat);			
			
		}
		else
			throw new TuneException("Invalid lvalue");
	}
	
	public static GenericWrapper findComponent(String var, GenericWrapper gw)
	{
		
//System.out.println("[ findComponent ] appelé avec "+var+" gw: "+gw);		
		
		Vector retour=findComponents(var,gw);
		
//System.out.println("[ findComponent ] nb gw trouve: "+retour.size());		
		
		if(retour!=null && retour.size()>0)
			return (GenericWrapper)retour.get(0);
		else
			return null;
	}
	
	/**
	 * Cette méthode permet de rechercher le GenericWrapper qui se trouve en bout de chaîne.
	 * Ainsi, si on a une expression de type $var.this.component_suivant.var3, cette méthode renverra
	 * le GenericWrapper associé à component_suivant. 
	 */
	
	public static Vector findComponents(String var, GenericWrapper gw)
	{
//System.out.println("[ findComponents ] appelé avec "+var);		
			
		Vector retour=new Vector();

		if(var.equals(""))
		{
			// oki, c'est fini, on renvoie le GenericWrapper
//System.out.println("[ findComponents ]  sorti avec le gw qui wrappe: "+gw.getWrapperName());
			retour.add(gw);
			return retour;
		}

		
		String str[]=var.split("\\.");
		
		// on calcule l'index de la chaine suivante.
		int nextIndex;

		if(str.length>1)
			nextIndex=str[0].length()+1;
		else
			nextIndex=str[0].length();
		
//System.out.println("[ findComponents ] nextIndex: "+nextIndex);
		

		// on analyse str[0]
		if(str[0].equals("this"))
			// on se renvoie la suite
			return findComponents(var.substring(nextIndex),gw);
		else if(str[0].equals("node"))
		{
//System.out.println("[ findComponents ] appelé avec node");			
			try
			{
				// on renvoie l'interface sur le noeud
				return findComponents(var.substring(nextIndex),((NodeAllocatorItf)((BindingController)gw).lookupFc("node")).getGw());
			}
			catch(Exception e)
			{
				return null;
			}
		}
		else
		{
			// on déférence pour aller taper le lien suivant.
			// c'est ici qu'il faut traiter les liens multiples
			// pour le moment, on considère les liens simples
			try
			{
				
				// si le nom du composant se termine déjà par _qq-chose, on va le chercher directement
				String str_decomp[]=str[0].split("\\_");
				
				// on regarde si notre dernier champs est un entier
				try
				{
					Integer.parseInt(str_decomp[str_decomp.length-1]);

//System.out.println("[ findComponents ] on va chercher "+("wrapperitf-client_"+str[0])+" => "+((BindingController)gw).lookupFc("wrapperitf-client_"+str[0]));	
					return findComponents(var.substring(nextIndex),((WrapperItf)((BindingController)gw).lookupFc("wrapperitf-client_"+str[0])).getGw());
				}
				catch(Exception e)
				{
					// oki, soit le dernier champs n'est pas un chiffre soit le split à renvoyer une chaine
					// de longueur 1 car il n'y a pas de split soit le findComponent précédent s'est planté
//System.out.println("[ findComponents ] on récupère toutes les interfaces "+("wrapperitf-client_"+str[0])+" de "+gw);	
					
					Vector itf=gw.getAllFc("wrapperitf-client_"+str[0]);
//System.out.println("[ findComponents ] nb itf multiples: "+itf.size());	
					
					for(int i=0;i<itf.size();i++)
					{
//System.out.println("[ findComponents ] on va chercher "+((WrapperItf)itf.get(i)).getGw().getName());	
						retour.addAll(findComponents(var.substring(nextIndex),((WrapperItf)itf.get(i)).getGw()));
					}
					return retour;
					
				}
			}
			catch(Exception e)
			{
//System.out.println("[ findComponents ] impossible de déférencer "+("wrapperitf-client_"+str[0]+"_0"));
				return null;
			}
		}
		
	}
	
	/**
	 * Cette méthode permet de renvoyer la valeur d'un attribut d'un composant quelconque,
	 * toujours avec notre notation pointée.
	 * Ainsi, si on a une expression de type $var.this.component_suivant.var3, cette méthode renverra
	 * la valeur de var3 du composant component_suivant.
	 * Elle peut en outre assigner une valeur value en positionnant assign à true
	 */
	
	private static String solveVar(String var, GenericWrapper gw, boolean assign, String value)
	{
		if(var.length()>1 && var.charAt(0)=='$')
		{
			String a_renvoyer="";
			
			// on récupère la variable proprement dite
			String []decoupe=var.split("\\.");
			String var_finale=decoupe[decoupe.length-1];
			
			// dans le cas où la variable est seule: (exemple: $PID)
			if(var_finale.charAt(0)=='$')
				var_finale=var_finale.substring(1);
			
			// on recrée le chemin
			var="";
			for(int i=0;i<decoupe.length-1;i++)
				var=var+decoupe[i]+".";
			
			
			if(var.length()>1)
			{
				// on enlève le premier '$'
				var=var.substring(1);

				// on enlève le dernier '.'
				var=var.substring(0, var.length()-1);
			}
			
//System.out.println("[ solveVar ] Appel de findComponent avec la chaine "+var);			
			//GenericWrapper gwf=findComponent(var,gw);
			Vector retour=findComponents(var,gw);
			
			// on boucle pour les assignations
			if(assign)
			{
				if(retour!=null)
				{
					for(int i=0;i<retour.size();i++)
						((AttributeControllerItf)retour.get(i)).setAttribute(var_finale, value);
				}
			}
			
			// on construit notre chaine de renvoi, séparé par des ';'
			if(retour!=null)
			{
				for(int i=0;i<retour.size();i++)
					a_renvoyer=a_renvoyer+(i>0?";":"")+((AttributeControllerItf)retour.get(i)).getAttribute(var_finale);
			}

			return (a_renvoyer.equals("")?null:a_renvoyer);
//System.out.println("[ solveVar ] Récupération de la variable "+var_finale+" avec la valeur "+((AttributeController)gwf).getAttribute(var_finale));		
		}
		else
			return var;
		
	}

	

}
