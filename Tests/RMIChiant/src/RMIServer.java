import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer
extends UnicastRemoteObject
implements IRMIServer {

	protected RMIServer() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = -8347307876939156429L;

	public SizeableObject pingPong(SizeableObject o) throws RemoteException {
		return o;
	}

	public static void main(String[] args) {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		String host;
		try {
			if(args.length > 0){
				host = args[0];
			}else{
				host = InetAddress.getLocalHost().getCanonicalHostName();
			}
			String name = "//"+host+"/RMIServer";
			RMIServer server = new RMIServer();
			Naming.rebind(name, server);
			System.out.println("RMIServer bound");
		} catch (UnknownHostException e) {
			exception(e);
		} catch (RemoteException e) {
			exception(e);
		} catch (MalformedURLException e) {
			exception(e);
		}
	}

	static public void exception(Exception e){
		System.err.println("RMI exception: " + e.getMessage());
		e.printStackTrace();
	}

}
