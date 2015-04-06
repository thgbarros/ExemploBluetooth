package br.com.barros.newbie.Observers;

/**
 * Created by thiagobarros on 06/04/15.
 */
public interface BluetoothObservable {

    void attach(BluetoothObserver observer);
    void deatach(BluetoothObserver observer);
    void notifyObserver();

}
