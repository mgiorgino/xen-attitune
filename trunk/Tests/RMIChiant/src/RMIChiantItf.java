import java.rmi.RemoteException;


public interface RMIChiantItf extends java.rmi.Remote {
	
    public MonObjet getValue() throws RemoteException;
    
    public void setValue(MonObjet o) throws RemoteException; 
	
}
