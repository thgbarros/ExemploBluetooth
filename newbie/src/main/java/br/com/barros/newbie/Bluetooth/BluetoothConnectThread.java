package br.com.barros.newbie.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.barros.newbie.Bluetooth.Exceptions.BluetoothStatus;
import br.com.barros.newbie.Observers.Observable;
import br.com.barros.newbie.Observers.Observer;

/**
 * Created by thiagobarros on 06/04/15.
 */
public class BluetoothConnectThread extends Thread implements Observable {
    private BluetoothSocket socket;
    private final BluetoothAdapter adapter;
    private final BluetoothDevice device;
    private BluetoothStatus bluetoothStatus = BluetoothStatus.NOT_CONNECTED;
    private Set<Observer> observers = new HashSet<>();


    public BluetoothConnectThread(BluetoothAdapter adapter, BluetoothDevice device, UUID uuid, Observer observer) {
        this.socket = null;
        this.device = device;
        this.adapter = adapter;
        this.observers.add(observer);
        try{
            this.socket = device.createRfcommSocketToServiceRecord(uuid);
        }catch (IOException e){ }
    }

    public void run(){
        adapter.cancelDiscovery();
        try{
            socket.connect();
            bluetoothStatus = BluetoothStatus.CONNECTED;
        }catch (IOException connectionE){
            try{
                socket.close();
            }catch(IOException closeE){ }
        }
    }

    public void cancel(){
        try{
            socket.close();
            bluetoothStatus = BluetoothStatus.NOT_CONNECTED;
        }catch(IOException e){ }
    }

    public BluetoothSocket getSocket(){
        return socket;
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
            observer.update(bluetoothStatus);
    }
}
