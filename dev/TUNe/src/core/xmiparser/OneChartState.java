package core.xmiparser;

import java.util.Vector;

import org.objectweb.fractal.api.Component;

import core.interfaces.WrapperItf;
import core.main.ComponentFactory;
import core.main.Generator;
import core.util.AttributeUtil;
import core.wrapper.local.GenericWrapper;

public class OneChartState extends OneChartEntity implements Runnable
{
	public final static int INITIAL=0;
	public final static int FINAL=2;
	public final static int BETWEEN=1;
	public final static int FORKJOIN=3;
	
	private String failedComponent;
	private String argComponent;
	
	private String argVar;
	
	private String xmi_id;
	
	private String[] triggers=null;
	
	
	private String name;
	private int type;
	
	private Vector nextState;
	private Vector previousState;
	
	private GenericWrapper gw;
	private GenericWrapper newElt=null;
	private boolean isNewElt=false;
	
	private Thread runner;
	
	
	private boolean executed;
	
	private ComponentFactory mcf;
	
	private Component []subComponents;
	
	public OneChartState(String xmi_id, String type, String name,ComponentFactory mcf)
	{
		super(xmi_id, OneChartEntity.STATE);
		this.mcf=mcf;
		this.xmi_id=xmi_id;
		this.name=name;
		this.type=Integer.parseInt(type);
		nextState=new Vector();
		previousState=new Vector();
		executed=false;
	}
	
	public OneChartState(String xmi_id, int type, String name, ComponentFactory mcf)
	{
		super(xmi_id, OneChartEntity.STATE);
		this.mcf=mcf;
		this.xmi_id=xmi_id;
		this.name=name;
		this.type=type;
		nextState=new Vector();
		previousState=new Vector();
		executed=false;
	}
	
	public void setComponentFactory(ComponentFactory mcf)
	{
		this.mcf=mcf;
	}
	
	public void setComponentArg(String argComponent)
	{
		this.argComponent=argComponent;
	}
	
	public void setArgVar(String argVar)
	{
		this.argVar=argVar;
	}
	
	public void setGw(GenericWrapper gw)
	{
		this.gw=gw;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public boolean isInitial()
	{
		return this.type==INITIAL;
	}
	
	public boolean isFinal()
	{
		return this.type==FINAL;
	}
	
	public void runNextState()
	{
		int i;
		for(i=0;i<nextState.size();i++)
			((OneChartState)nextState.elementAt(i)).startRunner(subComponents, failedComponent,argComponent, argVar, gw, newElt);
		
	}
	
	public void setTrigger(String triggers)
	{
		if(triggers!=null)
			this.triggers=triggers.split("\\,");
	}
	
	public boolean isActivable(String trigger)
	{
		if(triggers!=null)
		{
			for(int i=0;i<triggers.length;i++)
			{
				if(triggers[i].trim().equals(trigger))
					return true;
			}
		}
		return false;
	}
	
	public void waitPreviousState()
	{
		int i;
		for(i=0;i<previousState.size();i++)
		{
			try
			{
				((OneChartState)previousState.elementAt(i)).joinRunner();
			}
			catch(Exception e)
			{
				Generator.logger.error("Exception",e);
			}
		}
		
	}
	
	public void setFailedComponent(String failedComponent)
	{
		this.failedComponent=failedComponent;
	}
	
	public void run()
	{
//System.out.println("execution de "+name+" de type "+type+" - "+runner);		
		switch(type)
		{
			case INITIAL:
				// on démarre nos états suivants
				runNextState();	
				break;
				
			case BETWEEN:
				synchronized(this)
				{			
					if(!executed)
					{
						// soit nous avons une opération arithméthique soit l'appel à une méthode du DSL soit un ordre de resizing
						if(name.contains("="))
						{
							try
							{
								gw.setAttribute("arg", argVar);
								AttributeUtil.evaluateExpr(name, gw);
							}
							catch(Exception e)
							{
								Generator.logger.error("Exception",e);
							}
						}
						else
						{
							boolean sizing=false;
							boolean increase=false;
							
							if(name.contains("++"))
							{
								increase=true;
								sizing=true;
							}
							if(name.contains("--"))
								sizing=true;
							
							// on coupe la chaine
							// cette dernière se présente sous la forme:
							// ComposantClass.methode
							// où ComposantClass peut prendre les valeurs:
							//		- this pour le composant qui est à réparer ( ~ variable d'instance)
							//		- nom de la classe pour attaquer toutes les instances ( ~ variable de Classe)
							//		- arg pour le composant passé en paramètre
	
							String []spt=null;
							
							if(!sizing)
								spt=name.split("\\.");
							else
							{
								if(increase)
									spt=name.split("\\+");
								else
									spt=name.split("\\-");
							}
							
							// on récupère le nom de la méthode
							String methodeName=spt[spt.length-1];

							// on récupère le composant auquel s'applique ce state-chart
							String componentClass=spt[0];
							
							// on récupère le gw du component
							GenericWrapper base=null;
							boolean classe_var=false;

							for(int i=0;i<subComponents.length;i++)
							{
								try
								{
									if(componentClass.equals("this"))
									{
										if(((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getName().equals(failedComponent))
											base=((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getGw();
									}
									else if(componentClass.equals("arg"))
									{
										if(((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getName().equals(argComponent))
											base=((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getGw();
									}	
									else if(componentClass.equals("new"))
									{
										base=newElt;
										isNewElt=true;
									}
									else
									{
										// Ici, variable de classe, on applique donc la méthode sans se poser de question
										String radical=((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getName().split("\\_")[0];
										if(radical.equals(componentClass))
										{
											classe_var=true;
											if(!sizing)
												((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).meth(methodeName,null,null);
											else
												base=((WrapperItf)subComponents[i].getFcInterface("wrapperitf")).getGw();
										}
									}
								}
								catch(Exception e)
								{
									Generator.logger.error("Exception",e);
								}
							}

							if(sizing)
							{
								try
								{
									String name=base.getName().split("\\_")[0];
									if(increase)
										newElt=mcf.reconf(true,name,classe_var,base);
									else
										mcf.reconf(false,name,classe_var,base);
								}
								catch(Exception e)
								{
									Generator.logger.error("Exception", e);
								}
							}
							
							
							if(!classe_var && !sizing)
							{
							
								try
								{
									// on va déréférencer le chemin à partir de ce composant
									
									// on récrée le chemin sans la méthode
									String var="";
									for(int i=0;i<spt.length-1;i++)
										var=var+spt[i]+".";
		
									if(var.length()>1)
									{
										// on enlève le dernier '.'
										var=var.substring(0, var.length()-1);
										
										// on remplace le arg éventuel par this vu que nous sommes déjà dans le bon gw
										var=var.replace("arg", "this");
										
									}
									
									if(isNewElt)
										var=var.replace("new","this");
								
									Vector gw_targets=AttributeUtil.findComponents(var, base);
									GenericWrapper gw_target;
									for(int i=0;i<gw_targets.size();i++)
									{
										gw_target=(GenericWrapper)gw_targets.get(i);
										gw_target.setAttribute("arg",argVar);
										gw_target.meth(methodeName,null,null);
												
									}
								}
								catch(Exception e)
								{
									Generator.logger.error("Exception", e);
								}
							}
							
						}
					}
					
					executed=true;
					
				}
				runNextState();
				break;
				
			case FORKJOIN:
				waitPreviousState();
				runNextState();
				break;
				
			case FINAL:
				synchronized(this)
				{
					notifyAll();
				}
		}
//System.out.println("fin execution de "+name+" de type "+type+" - "+runner);		
		

	}
	
	public void startRunner(Component[] subComponents, String failedComponent, String argComponent, String argVar, GenericWrapper gw, GenericWrapper newElt)
	{
		this.subComponents=subComponents;
		this.failedComponent=failedComponent;
		this.argComponent=argComponent;
		this.argVar=argVar;
		this.gw=gw;
		this.newElt=newElt;
		runner=new Thread(this);
		runner.start();
	}
	
	public void joinRunner() throws Exception
	{
		if(runner!=null)
			runner.join();
		else
		{
			synchronized(this)
			{
				wait();
			}
			runner.join();
		}
	}
	
	public void startStateChart(Component[] subComponents)
	{
		// on démarre
		startRunner(subComponents, failedComponent,argComponent,argVar,gw,newElt);
	}
	
	public OneChartState simplyClone()
	{
		return new OneChartState(xmi_id,type,name,mcf);
	}
	
	public void addNextState(OneChartState ocs)
	{
		nextState.add(ocs);
	}
	
	public void addPreviousState(OneChartState ocs)
	{
		previousState.add(ocs);
	}
	
	
}
