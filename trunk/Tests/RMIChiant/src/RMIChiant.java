import java.rmi.RemoteException;


public class RMIChiant
		extends java.rmi.server.UnicastRemoteObject
		implements RMIChiantItf {

	private static final long serialVersionUID = -8347307876939156429L;
	private MonObjet o;
	
	protected RMIChiant(MonObjet arg0) throws RemoteException {
		o = arg0;
	}

	@Override
	synchronized public MonObjet getValue() throws RemoteException {
		return o;
	}

	@Override
	synchronized public void setValue(MonObjet o) throws RemoteException {
		this.o = o;
	}

}
