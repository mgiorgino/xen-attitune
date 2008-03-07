package core.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LauncherItf extends Remote
{
	public void deployARemoteWrapper(String wrapperName, String srmi_port, String tubeAddr) throws RemoteException;
	public boolean isDeployed() throws RemoteException;
	public void shutdown() throws RemoteException;
}
