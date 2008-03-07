package core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import core.main.Generator;

public class StreamGlobbler extends Thread
{
	private InputStream is;
	private String name;
	private boolean dist=true;
	
	public StreamGlobbler(InputStream is,String name)
	{
		this.is = is;
		this.name=name;
		this.start();
	}
	
	public StreamGlobbler(InputStream is,String name, boolean dist)
	{
		this.is = is;
		this.name=name;
		this.dist=dist;
		this.start();
		
	}
	
	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ((line = br.readLine()) != null)
			{
				if(dist)
				{
					if(line.contains("[ CMD ] "))
					{
						// ce logge vient d'une commande legacy.
						// on l'affiche si le niveau de verbosité est assez élevé
						if(Generator.verbosityLevel > 1)
							Generator.logger.info("[ REMOTE ] "+name+" => "+line);
					}
					else
					{
						// ça vient du RemoteWrapper, on l'affiche suivant le niveau de verbosité
						if(Generator.verbosityLevel > 0)
							Generator.logger.info("[ REMOTE ] "+name+" => "+line);
					}
					
				}
				else
				{
					// nous sommes le StreamGlobber legacy
					System.out.println("[ CMD ] "+name+" => "+line);
				}
			}
		}
		catch (IOException ioe)
		{
			if(dist)
			{
				Generator.logger.error("[ REMOTE ] "+name+" Exception",ioe);
			}
			else
			{
				// nous sommes le StreamGlobber legacy
				ioe.printStackTrace(System.err);
			}
		}
	}
}
