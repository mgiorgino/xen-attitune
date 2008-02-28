import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IRMIServer extends Remote {

	public SizeableObject pingPong(SizeableObject o) throws RemoteException;

}
