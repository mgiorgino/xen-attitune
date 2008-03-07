package core.interfaces;

import java.rmi.RemoteException;

import core.node.Node;
import core.wrapper.local.GenericWrapper;

public interface NodeAllocatorItf 
{
	public Node getNode();
    public GenericWrapper getGw() throws RemoteException;
	
}
