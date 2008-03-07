package core.xmiparser;

public class OneChartEntity 
{
	public static final int STATE=0;
	public static final int TRANSITION=1;
	
	private int type;
	private String xmi_id;
	
	public OneChartEntity(String xmi_id, int type)
	{
		this.type=type;
		this.xmi_id=xmi_id;
		
	}
}
