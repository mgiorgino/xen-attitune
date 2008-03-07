package core.xmiparser;

public class OneChartBinding extends OneChartEntity
{
	private String xmiFrom, xmiTo, xmi_id;
	
	public OneChartBinding(String xmi_from, String xmi_to, String id)
	{
		super("-1",OneChartEntity.TRANSITION);
		this.xmiFrom=xmi_from;
		this.xmiTo=xmi_to;
		this.xmi_id=id;
	}
	
	public String getXmiId()
	{
		return xmi_id;
	}

	public String getXmiFrom() {
		return xmiFrom;
	}

	public void setXmiFrom(String xmiFrom) {
		this.xmiFrom = xmiFrom;
	}

	public String getXmiTo() {
		return xmiTo;
	}

	public void setXmiTo(String xmiTo) {
		this.xmiTo = xmiTo;
	}
	
	
	
	
}
