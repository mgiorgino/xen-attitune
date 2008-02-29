
public class RMIException extends Exception {

	private static final long serialVersionUID = -1192498830352827434L;

	public RMIException(Exception e){
		super("RMI Exception", e);
	}
	
}
