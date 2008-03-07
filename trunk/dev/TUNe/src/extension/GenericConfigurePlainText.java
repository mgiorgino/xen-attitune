package extension;


public class GenericConfigurePlainText 
{
	public void configure(String file, String [] args)
	{
		System.out.println("configuring file: "+file);
		
		try
		{
			String cle, value;
			String separator=args[0];
			KeyValueParser kvp=new KeyValueParser(file);
			kvp.open();
			for(int i=1;i<args.length;i++)
			{
			    cle=args[i].split(":",2)[0];
			    value=args[i].split(":",2)[1];
				
				kvp.setProperty(cle, value, separator);
				System.out.println("[ configure ] appelé avec: "+cle+ ", " + value + ", "+ separator);

			}
			kvp.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void configureList(String file, String [] args)
	{
		System.out.println("configuring file: "+file);
		
		try
		{
			String value;
			ValueListParser vlp=new ValueListParser(file);
			vlp.open();
			for(int i=0;i<args.length;i++)
			{
			    value=args[i];
				
			    vlp.setProperty(value);
			    System.out.println("[ configure ] appelé avec: "+value);

			}
			vlp.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
