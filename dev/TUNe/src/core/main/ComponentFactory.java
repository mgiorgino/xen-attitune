package core.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import core.interfaces.AttributeControllerItf;
import core.interfaces.DeployControllerItf;
import core.interfaces.WrapperItf;
import core.node.GenericNode;
import core.util.TuneException;
import core.wrapper.local.GenericWrapper;
import core.xmiparser.OneBinding;
import core.xmiparser.OneEntity;
import core.xmiparser.OneLegacy;

public class ComponentFactory 
{
	private Component boot;
	private TypeFactory tf;
	private GenericFactory cf;
	
	private Component mainComponent;
	private ComponentType mainComponentType;
	private ContentController mainComponentContentController;
	
	private Component appliComponent;
	private ComponentType appliComponentType;
	private ContentController appliComponentContentController;
	
	private Component gridComponent;
	private ComponentType gridComponentType;
	private ContentController gridComponentContentController;
	
	private String rep;
	
	private Hashtable fixthem;
	
	
	private Component nodeAllocator;
	
	public void runComponentFactory(String rep, Hashtable fixthem) throws Exception
	{
		Generator.logger.info("Getting fractal...");
  		boot = Fractal.getBootstrapComponent();
  		
  		this.rep=rep;
  		this.fixthem=fixthem;

		tf = (TypeFactory)boot.getFcInterface ("type-factory");
		cf = (GenericFactory)boot.getFcInterface ("generic-factory");
			

	}
	
	public void addAppliComponent() throws Exception
	{
		mainComponentContentController.addFcSubComponent(appliComponent);
		Generator.logger.info("Adding appli component to main component");
	}
	
	public void addGridComponent() throws Exception
	{
		mainComponentContentController.addFcSubComponent(gridComponent);
		Generator.logger.info("Adding grid component to main component");
	}
	
	public void generateGridComponent() throws Exception
	{
		Generator.logger.info("Creating grid component...");

		Collection c=(Collection)fixthem.get("grid");
		
		gridComponentType=tf.createFcType(new InterfaceType[] {
				tf.createFcItfType("node", "core.interfaces.NodeAllocatorItf", true, false, true)
		});
		
		gridComponent=cf.newFcInstance(gridComponentType, "composite", null);
		gridComponentContentController=(ContentController)gridComponent.getFcInterface("content-controller");
		
		// on parcours maintenant les components legacy de notre appli
		addGridComponents(c, fixthem);
		
		// on les relie entre eux dans le composite quand on aura une sémantique
		//makeAppliBindings(c);
		
		((LifeCycleController)gridComponent.getFcInterface("lifecycle-controller")).startFc();
		
		
	}
	
	
	public void addGridComponents(Collection c, Hashtable fixthem) throws Exception
	{
		Iterator it=c.iterator();
		OneLegacy ol;
		Object o;
		Object retour[];
		
		
		Component node;
		while(it.hasNext())
		{
			o=it.next();
			if( ((OneEntity)o).isLegacy())
			{
				ol=((OneLegacy)o);
				
				for(int i=0;i<ol.getNbCurrent();i++)
				{
					GenericNode gn;
					Generator.logger.info("Creating "+ol.getSr_name()+" node");
					retour=createNode(ol,c,i,fixthem);
					node=(Component)retour[0];
					gridComponentContentController.addFcSubComponent(node);
					
					// pour pouvoir lier les noeuds aux applis
					mainComponentContentController.addFcSubComponent(node);
					gn=(GenericNode)retour[1];
//					ol.setComponent(node, gn);
					Generator.logger.info("Adding the node "+gn.getName()+" to grid component");
				}
			}
			
		}
		
	}
	
	public Object[] createNode(OneLegacy o, Collection c, int numero, Hashtable fixthem) throws Exception
	{
		
		GenericNode gn=null;
		AttributeControllerItf ac;
		InterfaceType [] itt;
		
		Object retour[]=new Object[2];
		
		itt=new InterfaceType[3];
	
		
		itt[0]=tf.createFcItfType("attribute-controller", "core.interfaces.AttributeControllerItf", false, false, false);
		itt[1]=tf.createFcItfType("node", "core.interfaces.NodeAllocatorItf", false, false, true);
		itt[2]=tf.createFcItfType("deployer", "core.interfaces.DeployControllerItf", false, false, false);
		
		// Création du primitif du composant applicatif
		gn=new GenericNode(o.getSr_name(),o.getNodeFile(), o.getJavaHome(), o.getUser(), o.getDirLocal(), o.getProtocole(),rep);
		ac=(AttributeControllerItf)gn;
		
		
		// on sette les attributs
		Vector att=o.getAttributes();
		String tab[];
		for(int i=0;i<att.size();i++)
		{
			tab=(String[])att.elementAt(i);
			ac.setAttribute(tab[0],tab[1]);
		}
		
		ac.setAttribute("srname", gn.getName());
		
		Component ct=cf.newFcInstance(tf.createFcType(itt), "primitive", gn);
		
		retour[0]=ct;
		retour[1]=gn;
		return retour;

	}
	
	public void stopGridComponent()
	{
		try
		{
			
			Component []gn=gridComponentContentController.getFcSubComponents();
			for(int j=0;j<gn.length;j++)
			{
				((DeployControllerItf)gn[j].getFcInterface("deployer")).undeploy();
			}
			((LifeCycleController)gridComponent.getFcInterface("lifecycle-controller")).stopFc();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void generateMainComponent() throws Exception
	{
		Generator.logger.info("Generating main component...");
		
		mainComponentType = tf.createFcType(new InterfaceType[] {
			});
		mainComponent = cf.newFcInstance(mainComponentType, "composite", null);
		mainComponentContentController=(ContentController)mainComponent.getFcInterface("content-controller");
		
	}
	
	public Component getMainComponent()
	{
		return mainComponent;
	}
	
	public Component getAppliComponent()
	{
		return appliComponent;
	}
	
	public void generateAppliComponent() throws Exception
	{

		Generator.logger.info("Creating appli component...");

		Collection c=(Collection)fixthem.get("deploiement");

		appliComponentType=tf.createFcType(new InterfaceType[] {
				tf.createFcItfType("node", "core.interfaces.NodeAllocatorItf", true, false, true)
		});
		
		
		appliComponent=cf.newFcInstance(appliComponentType, "composite", null);
		appliComponentContentController=(ContentController)appliComponent.getFcInterface("content-controller");
		
		// on parcours maintenant les components legacy de notre appli
		addAppliLegacyComponents(c, fixthem,false);
		
		// on les relie entre eux dans le composite
		makeAppliBindings(c);
	}
	
	public Object[] addAppliLegacyComponents(Collection c, Hashtable fixthem, boolean isReconf) throws Exception
	{

		Iterator it=c.iterator();
		OneLegacy ol;
		Object o;
		Object retour[]=null;
		
		Component legacyComponent;
		while(it.hasNext())
		{
			o=it.next();
			if( ((OneEntity)o).isLegacy())
			{
				ol=((OneLegacy)o);
				
				for(int i=0;i<(!isReconf?ol.getNbCurrent():1);i++)
				{
					int j;
					AttributeControllerItf aci=null;
					
					GenericWrapper gw;
					Generator.logger.info("Creating "+ol.getSr_name()+" component");
					retour=createComponent(ol,c,ol.getNumber(),fixthem);
					legacyComponent=(Component)retour[0];
					appliComponentContentController.addFcSubComponent(legacyComponent);

					// pour pouvoir lier avec les noeud
					mainComponentContentController.addFcSubComponent(legacyComponent);

					// on lie le composant à son GenericNode grâce à l'attribut host-family
					Component []gn=gridComponentContentController.getFcSubComponents();
					for(j=0;j<gn.length;j++)
					{
						aci=(AttributeControllerItf)gn[j].getFcInterface("attribute-controller");
						if(aci.getAttribute("name").equals(ol.getHostFamily()))
						{
							((BindingController)legacyComponent.getFcInterface("binding-controller")).bindFc("node", gn[j].getFcInterface("node"));
							
							// inutile de continuer, on a trouvé
							break;
						}
					}

					gw=(GenericWrapper)retour[1];
					if(j<gn.length)
					{
						// on a trouvé un GenericNode qui correspond au bon host-family
						ol.setComponent(legacyComponent, gw);
						Generator.logger.info("Adding "+gw.getWrapperName()+" to appli component - bound to "+aci.getAttribute("name")+" node family");
					}
					else
						Generator.logger.info("Adding "+gw.getWrapperName()+" to appli component - !!! Not bound to any grid component !!!");
						
					
				}
			}
			
		}
		
		if(retour!=null)
			return retour;
		else
			return null;

	}
	
	public Object[] createComponent(OneLegacy o, Collection c, int numero, Hashtable fixthem) throws Exception
	{
		
		GenericWrapper gw=null;
		AttributeControllerItf ac;
		InterfaceType [] itt;
		WrapperItf wi;
		
		Object retour[]=new Object[2];
		
		itt=new InterfaceType[5];
	
		// tous les composants applicatifs auront une interface serveur wrapper
		Generator.logger.info("-> adding the client wrapper interface");
		itt[0]=tf.createFcItfType("wrapperitf-client", "core.interfaces.WrapperItf", true, true,true);
		
		Generator.logger.info("-> adding the client node interface");
		itt[1]=tf.createFcItfType("node", "core.interfaces.NodeAllocatorItf", true, false, false);

		Generator.logger.info("-> adding the server wrapper interface");
		itt[2]=tf.createFcItfType("wrapperitf", "core.interfaces.WrapperItf", false, true, false);
		
		itt[3]=tf.createFcItfType("attribute-controller", "core.interfaces.AttributeControllerItf", false, false, false);
		itt[4]=tf.createFcItfType("deployer", "core.interfaces.DeployControllerItf", false, false, false);
		
		// Création du primitif du composant applicatif
		gw=new GenericWrapper(rep,o.getWrapperFile());
		gw.setName(o.getSr_name(),numero);
		wi=(WrapperItf)gw;
		ac=(AttributeControllerItf)gw;
		
		gw.setFixDiagram(fixthem);
		gw.setAppliComponent(appliComponent);
		
		// on sette les attributs
		Vector att=o.getAttributes();
		String tab[];
		for(int i=0;i<att.size();i++)
		{
			tab=(String[])att.elementAt(i);
			ac.setAttribute(tab[0],tab[1]);
		}
		
		ac.setAttribute("srname", wi.getName());
		
		Component ct=cf.newFcInstance(tf.createFcType(itt), "primitive", gw);
		gw.setComponentThis(ct.getFcInterface("wrapperitf"));
		gw.setContentsOf(ct);
		
		retour[0]=ct;
		retour[1]=gw;
		return retour;

	}
	
	public void removeComponent(Object []newComponent, String name)
	{
		// Cette méthode est là dans le cadre de la reconfiguration où on ajoute un élément 
		try
		{
			// on supprime le component qu'on vient d'ajouter
			appliComponentContentController.removeFcSubComponent((Component)newComponent[0]);
			((BindingController)((Component)newComponent[0]).getFcInterface("binding-controller")).unbindFc("node");
			mainComponentContentController.removeFcSubComponent((Component)newComponent[0]);
			Generator.logger.error("Fail to add new "+name);
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
		}
	}
	
	public boolean removeComponent(ArrayList listOfBindings, Component componentToRemove, GenericWrapper gwToRemove)
	{
		// Cette méthode est là dans le cadre de la reconfiguration où on supprime un composant
		// Quand on arrive dans cette méthode, on a déjà validé que le composant peut être enlevé tout en respectant l'architecture
		try
		{
			OneBinding ob;
			Vector targetBinding;
			String linkName, myName, complementaryName;
			WrapperItf wrapperItf;
			Component complementary;
			GenericWrapper complementaryGw;

			myName=((AttributeControllerItf)componentToRemove.getFcInterface("attribute-controller")).getAttribute("srname");

//System.out.println("removeComponent: "+myName+" relié aux autres par "+listOfBindings.size()+" classe de liaison");
			
			// On parcourt la liste des types de liaisons pour les casser une par une
			for(int i=0;i<listOfBindings.size();i++)
			{
				ob=(OneBinding)listOfBindings.get(i);
				targetBinding=gwToRemove.getAllFcName("wrapperitf-client_"+ob.getFrom().getSr_name());
				
//System.out.println("-> on s'occupe de la classe de liaison "+ob.getName());				
				
				for(int k=0;k<targetBinding.size();k++)
				{
					
//System.out.println("--> dans cette classe, on s'occupe de la "+k+"-ieme liaison multiple");					
					// il y avait des liaisons multiples, on les démonte une par une
					linkName=(String)targetBinding.get(k);
					
					// on va chercher le composant complémentaire
					wrapperItf=(WrapperItf)((BindingController)componentToRemove.getFcInterface("binding-controller")).lookupFc(linkName);
					complementaryGw=wrapperItf.getGw();
					complementary=complementaryGw.getContentsOf();
					complementaryName=((AttributeControllerItf)complementary.getFcInterface("attribute-controller")).getAttribute("srname");
					
					// on arrête le SR
					((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).stopFc();
					
					// on débinde la liaison dans l'autre sens
					((BindingController)complementary.getFcInterface("binding-controller")).unbindFc("wrapperitf-client_"+myName);

					// on débinde la liaison
					((BindingController)componentToRemove.getFcInterface("binding-controller")).unbindFc(linkName);
					
					// on débinde les liaisons nommées si cette liaison est nommée
					if(ob.isNamed())
					{
						((BindingController)componentToRemove.getFcInterface("binding-controller")).unbindFc("wrapperitf-client_"+ob.getNameForBind(complementaryName));
						
						// pas besoin de rechercher le complémentaire vu qu'à la construction, si la liaison est nommé, on refait un bind
						//   qui porte un nom différent mais entre les mêmes composants
						((BindingController)complementary.getFcInterface("binding-controller")).unbindFc("wrapperitf-client_"+ob.getNameForBind(myName));							
					}

					// on décrémente le nombre de composants liés sur celui qui reste bien évidemment...
					complementaryGw.setNbBindingForLink(complementaryGw.getNbBindingForLink(ob.getXmiId())-1,ob.getXmiId());
					
					// on redémarre le SR
					((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();
					
				}
				
			}
			
			// ok, ici, on a viré toutes les liaisons, on peut enlever le composant du SR, bon vent!
			//  dans le cas où on aurait oublié une liaison en route, on prendra une exception... utile pour le debuggage ! :D
			((DeployControllerItf)componentToRemove.getFcInterface("deployer")).undeploy();
			((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).stopFc();
			((BindingController)componentToRemove.getFcInterface("binding-controller")).unbindFc("node");
			appliComponentContentController.removeFcSubComponent(componentToRemove);
			mainComponentContentController.removeFcSubComponent(componentToRemove);
			((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();
		
			return true;
			
			
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
		}

		try
		{
			// on redémarre le SR au cas où on ait pris une exception en cours de route
			((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();
		}
		catch(Exception e)
		{}
		
		
		return false;
		
	}
	
	public GenericWrapper launchComponent(Object []newComponent, OneLegacy ol, String name)
	{
		
		GenericWrapper newElt=null;
		
		try
		{
			newElt=(GenericWrapper)newComponent[1];
			
			// on le démarre fractalement parlant
			((LifeCycleController)(((Component)newComponent[0]).getFcInterface("lifecycle-controller"))).startFc();
			
			// on déploie le composant
			((DeployControllerItf)(((Component)newComponent[0]).getFcInterface("deployer"))).deploy();
			
			// On redémarre le SR, obligé pour pouvoir accéder au nodeAllocator.
			// De toutes façons, à cet endroit, on a terminé de travailler sur le SR, tous les bindings on été fait
			((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();							
			
			// on attends que le nouveau composant soit déployé
			boolean running=false;
			for(int j=0;j<Generator.retries&&!running;j++)
			{
				running=((WrapperItf)((Component)newComponent[0]).getFcInterface("wrapperitf")).isDeployed();
	
				if(!running)
					Thread.sleep(500);
			}
			if(!running)
			{
				// Echec au lancement du nouveau composant
				Generator.logger.error("Unable to deploy new component "+name);
				removeComponent(newComponent,name);
				newElt=null;
			}
			else
				// tout est bon, on peut rajouter ce composant dans les nb_initial
				ol.setNbCurrent(ol.getNbCurrent()+1);
		}
		catch(Exception e)
		{
			Generator.logger.error("Exception",e);
		}
		
		return newElt;
		
	}
	
	
	public GenericWrapper reconf(boolean add, String name, boolean isClassVar, GenericWrapper toWorkOnIt)
	{
		// TODO faire l'insertion automatique de composants manquants
		
		GenericWrapper newElt=null;
		
		try
		{
			
			if(add)
			{
				// lors de l'ajour, on ajoute un composant de la classe "name" qui nous ait passé.
				//  on ne peut pas cibler de composant spéciaux tels qu'avec -- et this ou arg...
				Generator.logger.info("Try to add new "+name);
				
				// on va récupérer le nom du composant dans la liste des composants uml
				Iterator it=((Collection)fixthem.get("deploiement")).iterator();
				Object o;
				OneLegacy ol;
				ArrayList al=new ArrayList();
				Object[] newComponent;
				
				while(it.hasNext())
				{
					o=it.next();
					if(((OneEntity)o).isLegacy())
					{
						ol=(OneLegacy)o;
						
						// on compare les noms
						if(ol.getSr_name().equals(name))
						{
							al.add(ol);
							try
							{
								newComponent=addAppliLegacyComponents(al,fixthem,true);
								
								// On arrête le SR					
								((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).stopFc();							
								
								if(makeAppliBindingsReconf(ol,(Component)newComponent[0], (GenericWrapper)newComponent[1]))
									newElt=launchComponent(newComponent,ol,name);
								else
									// on supprime le component qu'on vient d'ajouter
									removeComponent(newComponent,name);
								
								// On redémarre le SR au cas où on aurait eu un échec
								((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();							
								
								
							}
							catch(Exception e)
							{
								Generator.logger.error("Exception", e);
							}
							break;
						}
					}
				}
			}
			else
			{
				if(isClassVar)
					Generator.logger.info("Try to remove a "+name);
				else
					Generator.logger.info("Try to remove the "+toWorkOnIt.getWrapperName());
					
				// on va récupérer le nom du composant dans la liste des composants uml
				Iterator it=((Collection)fixthem.get("deploiement")).iterator();
				Object o;
				OneLegacy ol;
				
				while(it.hasNext())
				{
					o=it.next();
					if(((OneEntity)o).isLegacy())
					{
						ol=(OneLegacy)o;
						
						// on compare les noms
						if(ol.getSr_name().equals(name))
						{
							// oki, on a la classe de composant à enlever
							if(removeAppliBindingsReconf(ol,isClassVar,toWorkOnIt))
								Generator.logger.info("Successfully remove a "+name);
							else
								Generator.logger.info("Unable to remove a "+name);
						}
					}
				}
				
			}
			
		}
		catch(Exception e)
		{
			Generator.logger.error("Error", e);
		}
		
		try
		{
			// on redémarre le SR au cas où on ait pris une exception en cours de route
			((LifeCycleController)(getMainComponent().getFcInterface("lifecycle-controller"))).startFc();
		}
		catch(Exception e)
		{}
		
		
		return newElt;
		
	}
	
	public boolean removeAppliBindingsReconf(OneLegacy ol, boolean isClassVar, GenericWrapper toWorkOnIt) throws Exception
	{
		// on va chercher les liaisons qui relient ce composant à d'autres
		Collection c=(Collection)fixthem.get("deploiement");
		Iterator it=c.iterator();
		OneBinding ob=null;
		Object o;
		ArrayList al=new ArrayList();
		
		// on récupère toutes les liaisons attachées à ce composant
		
		while(it.hasNext())
		{
			o=it.next();
			if(!((OneEntity)o).isLegacy())
			{
				ob=((OneBinding)o);
				
				// la reconfiguration est prévue pour que le composant qu'on rajoute soit toujours sur le To.
				// il faut donc inverser le sens de la liaison si ob.getFrom == ol
				if(ob.getFrom()==ol)
					al.add(ob.invert());
				
				if(ob.getTo()==ol)
					al.add(ob);
				
			}
			
		}
		
		Component potentiallyComponentRemovable;
		GenericWrapper potentiallyGenericWrapperRemovable, targetGenericWrapper;
		Vector targetComponent;
		WrapperItf targetItf;
		
		int  card_2_to_1_Min;
		int nbLiaison, nbCassureMultiLiaison, nbCassure;
		
		
		// on va essayer d'enlever un composant de la classe ol
		// pour se faire, on boucle sur tous et on en cherche un qu'on peut enlever
//System.out.println("La classe de composant "+ol.getSr_name()+" en comprends: "+ol.getNbComponent());		
		for(int i=0;i<ol.getNbComponent();i++)
		{
			potentiallyComponentRemovable=ol.getComponent(i);
			potentiallyGenericWrapperRemovable=ol.getGw(i);
			
			if(isClassVar||potentiallyGenericWrapperRemovable==toWorkOnIt)
			{
			
//System.out.println("on regarde si on peut enlever: "+potentiallyGenericWrapperRemovable.getWrapperName());

				nbCassure=0;
				
				// on regarde si le composant est enlevable
				// pour se faire, on parcours toutes les liaisons qui sont attachées à ce composant
				for(int j=0;j<al.size();j++)
				{
					
					ob=(OneBinding)al.get(j);
	
					card_2_to_1_Min=ob.getMultiplicityFromLower();
					
					// pour cette liaison, on va chercher le composant auquel nous sommes lié
//System.out.println("-> nom de la liaison fractal: "+"wrapperitf-client_"+ob.getFrom().getSr_name());
					
					nbCassureMultiLiaison=0;
	
					targetComponent=potentiallyGenericWrapperRemovable.getAllFc("wrapperitf-client_"+ob.getFrom().getSr_name());
					for(int k=0;k<targetComponent.size();k++)
					{
						targetItf=((WrapperItf)targetComponent.get(k));
						targetGenericWrapper=targetItf.getGw();
						nbLiaison=targetGenericWrapper.getNbBindingForLink(ob.getXmiId());
						
//System.out.println("--> ce composant est relié à: "+targetGenericWrapper.getWrapperName()+" qui est lié "+nbLiaison+" fois. Il doit être lié au minimum "+card_2_to_1_Min+" fois");
	
						// on regarde si dans le cas de la suppression de la liaison on respecte la cardinalité basse
						if(nbLiaison>card_2_to_1_Min)
							nbCassureMultiLiaison++;
						
					}
					
					if(nbCassureMultiLiaison==targetComponent.size() && targetComponent.size()>0)
						// oki, on peut casser toutes les liaison de ce composant qui aurait des liaisons multiples
						nbCassure++;
					
					
				}
				
//System.out.println("il y a "+nbCassure+" cassures possibles sur "+al.size()+" type de liaisons en tout");
	
				if(nbCassure==al.size())
				{
//System.out.println("suppression du composant");
					if(removeComponent(al,potentiallyComponentRemovable,potentiallyGenericWrapperRemovable))
						return true;
					
				}
			}
			
			
		}
		
		return false;
	}
	
	public boolean makeAppliBindingsReconf(OneLegacy ol, Component newComponent, GenericWrapper gwEntity2) throws Exception
	{

		// on va chercher les liaisons qui relient ce composant à d'autres
		Collection c=(Collection)fixthem.get("deploiement");
		Iterator it=c.iterator();
		OneBinding ob=null;
		Object o;
		ArrayList al=new ArrayList();
		
		boolean possibleToMake=false;
		
		while(it.hasNext())
		{
			o=it.next();
			if(!((OneEntity)o).isLegacy())
			{
				ob=((OneBinding)o);
				
				// la reconfiguration est prévue pour que le composant qu'on rajoute soit toujours sur le To.
				// il faut donc inverser le sens de la liaison si ob.getFrom == ol
				if(ob.getFrom()==ol)
					al.add(ob.invert());
				
				if(ob.getTo()==ol)
					al.add(ob);
			}
			
		}
		
		it=al.iterator();
		

		String entity1Name, entity2Name;
		Component entity1, entity2;
		
		int card_1_to_2_Min, card_2_to_1_Min;
		int card_1_to_2_Max, card_2_to_1_Max;
		
		boolean find;
		
		GenericWrapper gwEntity1;
		
		// pour les commentaires, voir la méthode makeAppliBindings qui est sa soeur (au sens Africain hein...)
		while(it.hasNext())
		{
			ob=((OneBinding)it.next());
			
//System.out.println("ob: "+ob);			
			
			card_1_to_2_Max=ob.getMultiplicityTo();
			card_1_to_2_Min=ob.getMultiplicityToLower();
			card_2_to_1_Max=ob.getMultiplicityFrom();
			card_2_to_1_Min=ob.getMultiplicityFromLower();

			for(int k=0;k<ob.getFrom().getNbCurrent();k++)
			{
				entity1=ob.getFrom().getComponent(k);
				entity1Name=((AttributeControllerItf)entity1.getFcInterface("attribute-controller")).getAttribute("srname");
			
//				for(int i=0;i<card_1_to_2_Min;i++)
				{
					find=false;
					entity2Name="";

					entity2=newComponent;
					entity2Name=((AttributeControllerItf)entity2.getFcInterface("attribute-controller")).getAttribute("srname");

//System.out.println("try to bind one "+entity1Name+" to at least "+card_1_to_2_Min+" "+entity2Name+" and "+card_1_to_2_Max+" at the maximum");			
//System.out.println("try to bind one "+entity2Name+" to at least "+card_2_to_1_Min+" "+entity1Name+" and "+card_2_to_1_Max+" at the maximum");			

//System.out.println(" -> testing on "+entity2Name+" component");	

					gwEntity1=ob.getFrom().getGw(k);


//System.out.println(" --> "+entity2Name+" have already "+gwEntity2.getNbBindingForLink(ob.getXmiId())+" binding to an "+entity1Name+" class - gwEntity2: "+gwEntity2+" - name: "+gwEntity2.getWrapperName());							
//System.out.println(" --> "+entity1Name+" have already "+gwEntity1.getNbBindingForLink(ob.getXmiId())+" binding to an "+entity2Name+" class - gwEntity1: "+gwEntity1+" - name: "+gwEntity1.getWrapperName());							
						
					
					if(gwEntity2.getNbBindingForLink(ob.getXmiId())<card_2_to_1_Max && gwEntity1.getNbBindingForLink(ob.getXmiId())<card_1_to_2_Max)
						find=true;
						
					
					if(find)
					{
						gwEntity2.setNbBindingForLink(gwEntity2.getNbBindingForLink(ob.getXmiId())+1,ob.getXmiId());
						gwEntity1.setNbBindingForLink(gwEntity1.getNbBindingForLink(ob.getXmiId())+1,ob.getXmiId());

						((BindingController)(entity1.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+entity2Name, entity2.getFcInterface("wrapperitf"));							
						((BindingController)(entity2.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+entity1Name, entity1.getFcInterface("wrapperitf"));

						if(ob.isNamed())
						{
							((BindingController)(entity2.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+ob.getNameForBind(entity1Name), entity1.getFcInterface("wrapperitf"));
							((BindingController)(entity1.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+ob.getNameForBind(entity2Name), entity2.getFcInterface("wrapperitf"));							
						}

						
						Generator.logger.info("Binding "+entity1Name+" and "+entity2Name);
						
						possibleToMake=true;
					}
					
				}		
				
				
				
			}
			
		}
		
		return possibleToMake;
			
	}
	
	
	
	public void makeAppliBindings(Collection c) throws Exception
	{

		Iterator it=c.iterator();
		OneBinding ob;
		Object o;
		
		String entity1Name, entity2Name;
		Component entity1, entity2;
		
		int card_1_to_2_Min, card_2_to_1_Min;
		int card_1_to_2_Max, card_2_to_1_Max;
		
		
		boolean find;
		GenericWrapper gwEntity2, gwEntity1;

		// on boucle sur toutes les liaisons
		while(it.hasNext())
		{
			o=it.next();
			if(!((OneEntity)o).isLegacy())
			{
				
				// on a trouvé une liaison. Une liaison relie n entity1 à n entity2
				ob=((OneBinding)o);
				
//System.out.println("ob: "+ob);				
				
				// pour garder une compatibilité avec le parseur, entity1 correspond au from et entity2 au to
				
				// ici, on parle de classe de composant et de classe de liaison
				
				// 1 entity1 pourra être relié à card_1_to_2_Max entity2 au maximum
				card_1_to_2_Max=ob.getMultiplicityTo();
				
				// 1 entity1 devra être relié à card_1_to_2_Min entity2 au minimum
				card_1_to_2_Min=ob.getMultiplicityToLower();
				
				// 1 entity2 pour être relié à card_2_to_1_Max entity1 au maximum
				card_2_to_1_Max=ob.getMultiplicityFrom();
				
				// 1 entity2 devra être relié à card_2_to_1_Min entity1 au minimum
				card_2_to_1_Min=ob.getMultiplicityFromLower();
				

				// on boucle sur le nombre de composants de entity1
				for(int k=0;k<ob.getFrom().getNbCurrent();k++)
				{
					// ici, on parle de composant réel, plus de classe de composant.
					
					// on récupère le composant entity1
					entity1=ob.getFrom().getComponent(k);
					
					// on récupère le nom de ce composant
					entity1Name=((AttributeControllerItf)entity1.getFcInterface("attribute-controller")).getAttribute("srname");

					// boucle le nombre de fois minimum qu'on doit être relié
					for(int i=0;i<card_1_to_2_Min;i++)
					{
						// on cherche un composant entity2 qui puisse nous satisfaire
						find=false;
						gwEntity2=null;
						gwEntity1=null;
						int j;
						entity2Name="";
						entity2=null;
						
						for(j=0;j<ob.getTo().getNbCurrent();j++)
						{

							entity2=ob.getTo().getComponent(j);
							entity2Name=((AttributeControllerItf)entity2.getFcInterface("attribute-controller")).getAttribute("srname");

//System.out.println("try to bind one "+entity1Name+" to at least "+card_1_to_2_Min+" "+entity2Name+" and "+card_1_to_2_Max+" at the maximum");			
//System.out.println("try to bind one "+entity2Name+" to at least "+card_2_to_1_Min+" "+entity1Name+" and "+card_2_to_1_Max+" at the maximum");			

//System.out.println(" -> testing on "+entity2Name+" component");	

							
							gwEntity2=ob.getTo().getGw(j);
							gwEntity1=ob.getFrom().getGw(k);

//System.out.println(" --> "+entity2Name+" have already "+gwEntity2.getNbBindingForLink(ob.getXmiId())+" binding to an "+entity1Name+" class - gwEntity2: "+gwEntity2+" - name: "+gwEntity2.getWrapperName());							
//System.out.println(" --> "+entity1Name+" have already "+gwEntity1.getNbBindingForLink(ob.getXmiId())+" binding to an "+entity2Name+" class - gwEntity1: "+gwEntity1+" - name: "+gwEntity1.getWrapperName());							
							
							// il faut ici regarder si on peut binder les deux composants.
							// on regarde combien de liaison vont de cette classe de entity1 vers ce entity2.
							// -> il faut que ce soit inférieur à card_1_to_2_Max
							// on regarde combien de liaison vont de ce entity1 vers cette classe entity2.
							// -> il faut que ce soit inférieur à card_2_to_1_Max

//System.out.println("gwEntity2.getNbBindingForLink("+ob.getXmiId()+")<card_1_to_2_Max: "+(gwEntity2.getNbBindingForLink(ob.getXmiId())<card_1_to_2_Max));
//System.out.println("gwEntity1.getNbBindingForLink("+ob.getXmiId()+")<card_2_to_1_Max: "+(gwEntity1.getNbBindingForLink(ob.getXmiId())<card_2_to_1_Max));

							if(gwEntity2.getNbBindingForLink(ob.getXmiId())<card_2_to_1_Max && gwEntity1.getNbBindingForLink(ob.getXmiId())<card_1_to_2_Max)
							{
								find=true;
								break;
							}
						}
						
						if(find)
						{
							gwEntity2.setNbBindingForLink(gwEntity2.getNbBindingForLink(ob.getXmiId())+1,ob.getXmiId());
							gwEntity1.setNbBindingForLink(gwEntity1.getNbBindingForLink(ob.getXmiId())+1,ob.getXmiId());
							((BindingController)(entity1.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+entity2Name, entity2.getFcInterface("wrapperitf"));							

							// on binde dans l'autre sens (liaison double sens)
							((BindingController)(entity2.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+entity1Name, entity1.getFcInterface("wrapperitf"));
							
							// si la liaison est nommée on binde aussi avec le nom de la liaison
							if(ob.isNamed())
							{
								((BindingController)(entity2.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+ob.getNameForBind(entity1Name), entity1.getFcInterface("wrapperitf"));
								((BindingController)(entity1.getFcInterface("binding-controller"))).bindFc("wrapperitf-client_"+ob.getNameForBind(entity2Name), entity2.getFcInterface("wrapperitf"));							
							}

							
							Generator.logger.info("Binding "+entity1Name+" and "+entity2Name);
						}
						else
						{
							// on regarde si par hasard, on aurait pas une cardinalité qui nous permettrait de passer quand même
							if(gwEntity2.getNbBindingForLink(ob.getXmiId())<ob.getMultiplicityToLower())
								throw new TuneException("Unable to bind "+entity1Name+": no enough destination instanciated components ( "+ob.getTo().getSr_name()+" )");
						}
					}		
				}
				
			}
			
		}
	}

}
