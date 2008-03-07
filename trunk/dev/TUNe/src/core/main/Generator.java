package core.main;

/**
 * Cette classe est le point d'entrée.
 * 
 * Elle prend comme paramètre le nom du fichier XMI qui représente l'application à déployer.
 * 
 *
 */


import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;

import core.interfaces.DeployControllerItf;
import core.interfaces.WrapperItf;
import core.util.TuneException;
import core.xmiparser.Parser;

public class Generator 
{
	
	public static Logger logger=null;
	public static int verbosityLevel=0;
	
	private Hashtable appli_to_build;
	
	private ComponentFactory mcf;
	private ComponentRunning mcr;
	
	public static int retries=100;
	
	public String rep;
	
	public Generator(String uml, String verbosityLevel, String rep)
	{
		this.rep=rep+"/";
		makeATune(uml,verbosityLevel);
		
	}
	
	public Generator(String uml, String verbosityLevel)
	{
		rep="./";
		makeATune(uml,verbosityLevel);
	}
	
	public void undeploy()
	{
		// on lance le stop chart
		Generator.logger.info("Now, stop appli following the stopchart!");
		mcr.run((Collection)appli_to_build.get("stopchart"));
		Generator.logger.info("Apply successfully stopped");
		Generator.logger.info("Now, cleaning all TUNe software");
		mcf.stopGridComponent();
	}
	
	public void makeATune(String uml, String verbosityLevel)
	{
		
		// Préparation du logger
		if(logger==null)
		{
			logger = Logger.getLogger("TUNe Logger");
			BasicConfigurator.configure();
			logger.info("Logger log4j started");

			if(verbosityLevel.equals("-v"))
				Generator.verbosityLevel=1;
			else if(verbosityLevel.equals("-vv"))
				Generator.verbosityLevel=2;
			else if(verbosityLevel.equals("-silent"))
				Generator.verbosityLevel=0;
		}

		try
		{
			mcf=new ComponentFactory();
			
			// Parsing du fichier XMI
			Parser p=new Parser(rep+uml,mcf);
			appli_to_build=p.parse();
			
			// Génération de notre système en Fractal
			mcf.runComponentFactory(rep,appli_to_build);
			mcf.generateMainComponent();
			
			mcf.generateGridComponent();
			mcf.generateAppliComponent();
			
			mcf.addGridComponent();
			mcf.addAppliComponent();
			
			// Début des appels Fractal
			
			// Oki, on fait appel au LifeCycleController de notre composant principal pour faire un startFc avant d'appeller sa méthode run()
			Generator.logger.info("Deploying main component");
			
			// on démarre tout
			((LifeCycleController)mcf.getMainComponent().getFcInterface("lifecycle-controller")).startFc();
			
			ContentController cc=((ContentController)mcf.getAppliComponent().getFcInterface("content-controller"));
			Component [] internes=cc.getFcSubComponents();
			for(int i=0;i<internes.length;i++)
				((DeployControllerItf)internes[i].getFcInterface("deployer")).deploy();
			
			// on attends qu'ils soient tous déployés
			boolean running;
			for(int i=0;i<internes.length;i++)
			{
				running=false;
				for(int j=0;j<retries&&!running;j++)
				{
					running=((WrapperItf)internes[i].getFcInterface("wrapperitf")).isDeployed();

					if(!running)
						Thread.sleep(500);
				}
				if(!running)
					throw new TuneException("Unable to start application");
			}

			Generator.logger.info("Successfully deploy application");
			
			// on suit le startchart pour déployer et démarrer
			Generator.logger.info("Now, configure and run appli following the startchart!");
			mcr=new ComponentRunning(mcf.getAppliComponent());
			mcr.run((Collection)appli_to_build.get("startchart"));
			Generator.logger.info("Apply successfully configured and started");
			
		}
		catch(Exception e)
		{
			logger.error("Exception", e);
		}
		
	
	}
	
	// pour le déploiement hiérarchique
	public static void main(String [] args)
	{
		// paramètres à passer:
		// - fichier uml,
		// - verbosity (-v, -vv, -silent),
		
		new Generator(args[0], args[1]);
	}
	

}
