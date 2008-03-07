package core.xmiparser;


public class OneEntity 
{
	private String xmi_id;
	
	public static int BINDING=0;
	public static int LEGACY=1;
	
	private int type;

	public OneEntity(String xmi_id, int type)
	{
		this.xmi_id=xmi_id;
		this.type=type;
	}
	
	public boolean isLegacy()
	{
		return type==LEGACY; 
	}
	
	public boolean isBinding()
	{
		return type==BINDING;
	}
	
	public String getXmiId()
	{
		return xmi_id;
	}
	
}
