package core.xmiparser;

public class OneBinding extends OneEntity
{
	
	private OneLegacy from;
	private OneLegacy to;
	
	private String name;
	private int nb_bind_name;
	
	private String XMIFrom=null;
	private String XMITo=null;
	
	private int multiplicityFrom, multiplicityTo;
	
	private int multiplicityFromLower, multiplicityToLower;
	
	public OneBinding invert()
	{
		// dans le cadre de la reconf, on inverse le From & le To
		return new OneBinding(super.getXmiId(), name, nb_bind_name, to, from, XMITo, XMIFrom, multiplicityTo, multiplicityFrom, multiplicityToLower, multiplicityFromLower);
	}
	
	public OneBinding(String xmi_id, String name, int nb_bind_name, OneLegacy from, OneLegacy to, String XMIFrom, String XMITo, int multiplicityFrom, int multiplicityTo, int multiplicityFromLower, int multiplicityToLower)
	{
		super(xmi_id,OneEntity.BINDING);
		this.from=from;
		this.to=to;
		this.name=name;
		this.nb_bind_name=nb_bind_name;
		this.XMIFrom=XMIFrom;
		this.XMITo=XMITo;
		this.multiplicityFrom=multiplicityFrom;
		this.multiplicityTo=multiplicityTo;
		this.multiplicityFromLower=multiplicityFromLower;
		this.multiplicityToLower=multiplicityToLower;
	}
	
	
	public OneBinding(String xmi_id)
	{
		super(xmi_id,OneEntity.BINDING);
		multiplicityFrom=1;
		multiplicityTo=1;
		name=null;
		nb_bind_name=0;
	}
	
	public String getNameForBind(String linkedTo)
	{
		return getName()+"_"+linkedTo;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean isNamed()
	{
		return name!=null;
	}
	
	
	
	public int getMultiplicityFromLower() 
	{
		return multiplicityFromLower;
	}

	public void setMultiplicityFromLower(String multiplicityFromLower) 
	{
		this.multiplicityFromLower = Integer.parseInt(multiplicityFromLower);
	}

	public int getMultiplicityToLower() 
	{
		return multiplicityToLower;
	}

	public void setMultiplicityToLower(String multiplicityToLower) 
	{
		this.multiplicityToLower = Integer.parseInt(multiplicityToLower);
	}

	public void setMultiplicityFrom(String mult)
	{
		this.multiplicityFrom=Integer.parseInt(mult);
	}
	
	public void setMultiplicityTo(String mult)
	{
		this.multiplicityTo=Integer.parseInt(mult);
	}

	public int getMultiplicityFrom()
	{
		return multiplicityFrom;
	}
	
	public int getMultiplicityTo()
	{
		return multiplicityTo;
	}

	public void setFrom(OneLegacy ol)
	{
		from=ol;
	}
	
	public void setTo(OneLegacy ol)
	{
		to=ol;
	}
	
	public void setFromXMI(String xmi_id)
	{
		XMIFrom=xmi_id;
	}
	
	public void setToXMI(String xmi_id)
	{
		XMITo=xmi_id;
	}
	
	public String getFromXMI()
	{
		return XMIFrom;
	}
	
	public String getToXMI()
	{
		return XMITo;
	}
	
	public boolean isFromSetted()
	{
		return XMIFrom!=null;
	}
	
	public OneLegacy getFrom()
	{
		return from;
	}
	
	public OneLegacy getTo()
	{
		return to;
	}
}
