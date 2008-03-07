
package extension;

import core.dslparser.WrapperVariable;


public class GenericStop
{
	public static WrapperVariable __wv;
	private Process agent_pid=null; 
	
	public void stop_with_pid_linux(String cmd)
	{
		System.out.println("GenericStop: stopping "+cmd+" ( BashOnly )");
		
		try
		{
			Runtime.getRuntime().exec("kill -9 "+cmd);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
  
	public void stop()
	{
		
		System.out.println("GenericStop: stopping");
		try 
		{
			
			
			if (__wv !=null)
			{
				agent_pid = (Process) __wv.getVar("pid");
				if (agent_pid !=null)
				{
					agent_pid.destroy();
					agent_pid=null;
				}
			}
			else
			{
				System.out.println("GenericStop : __pid null");
				
			}
		} 
		catch (Exception e) 
		{
    		
			e.printStackTrace();
		}
		
		
		
		
	}
	
}
