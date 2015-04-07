package br.com.barros.newbie.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.barros.newbie.Bluetooth.Exceptions.BluetoothStatus;
import br.com.barros.newbie.Observers.Observable;
import br.com.barros.newbie.Observers.Observer;

/**
 * Created by thiago on 06/04/15.
 */
public class BluetoothAcceptSocketThread extends Thread implements Observable {
    private Set<Observer> observers = new HashSet<>();
    private BluetoothServerSocket severSocket;
    private BluetoothSocket socket;
    private UUID uuid;

    BluetoothAcceptSocketThread(BluetoothAdapter adapter, UUID uuid, Observer observer){
        this.uuid = uuid;
        observers.add(observer);
        severSocket = null;
        socket = null;
        try{
           severSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("", uuid);
        }catch(IOException e){ }
    }

    public void run(){
        while(true){
            try{
                socket = severSocket.accept();
            }catch(IOException e){
                break;
            }

            if (socket != null) {
                notifyObserver();
                break;
            }
        }
    }

    public void cancel(){
        try{
            severSocket.close();
        }catch(IOException e){}
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void deatach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver() {
        for (Observer observer: observers)
            observer.update(BluetoothStatus.ACCEPT);
    }
}
