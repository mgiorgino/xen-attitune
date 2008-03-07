package core.util;

public class TuneException extends Exception	
{
	private String msg;
	
	public TuneException(String ex)
	{
		this.msg=ex;
	}
	
	public String getMessage()
	{
		return msg;
	}
	 

}
