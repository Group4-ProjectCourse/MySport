package chatServer.observable.subjects;



import chatServer.observable.listeners.LocalListener;

import java.rmi.RemoteException;

public interface LocalSubject<T, U, V> {

    boolean addListener(LocalListener<T, U, V> var) throws RemoteException;

    boolean removeListener(LocalListener<T, U, V> var) throws RemoteException;
}
