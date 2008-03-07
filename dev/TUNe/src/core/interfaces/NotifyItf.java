package core.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyItf extends Remote
{
	public void notify(String notification, String component, String arg) throws RemoteException;

}
