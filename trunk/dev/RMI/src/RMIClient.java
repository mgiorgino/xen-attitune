import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

public class RMIClient {

	public static void main(String args[]) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		String name = "//" + args[0] + "/RMIServer";
		IRMIServer rmiChiant;
		try {
			rmiChiant = (IRMIServer) Naming.lookup(name);
			long begin = System.currentTimeMillis();
			rmiChiant.pingPong(new SizeableObject(Integer.parseInt(args[1])));
			long end = System.currentTimeMillis();
			System.out.println("Durée : "+(end-begin)/1000.+" s");
		} catch (MalformedURLException e) {
			exception(e);
		} catch (RemoteException e) {
			exception(e);
		} catch (NotBoundException e) {
			exception(e);
		} catch (NumberFormatException e) {
			exception(e);
		}
	}

	static public void exception(Exception e){
		System.err.println("RMI exception: " + e.getMessage());
		e.printStackTrace();
	}
}
