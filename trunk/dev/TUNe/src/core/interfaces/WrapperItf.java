
package core.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import core.wrapper.local.GenericWrapper;

public interface WrapperItf extends Remote
{
    public void meth(String className, String methName, Object[] args) throws RemoteException;
    public boolean isDeployed()throws RemoteException ;
    public String getName() throws RemoteException;
    public void setName(String name, int numero) throws RemoteException;
    public void setNotifyNode(String node) throws RemoteException;
    
    public void killRemoteWrapper() throws RemoteException;
    
    public GenericWrapper getGw() throws RemoteException;
}
