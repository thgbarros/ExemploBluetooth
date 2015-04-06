package br.com.barros.newbie.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.*;

import br.com.barros.newbie.Observers.BluetoothObservable;
import br.com.barros.newbie.Observers.BluetoothObserver;

/**
 * Created by thiagobarros on 06/04/15.
 */
public class BluetoothReceiver extends BroadcastReceiver implements BluetoothObservable {
    private Set<BluetoothObserver> observers;
    private Set<BluetoothDevice> devices;

    public BluetoothReceiver(){
        observers = new HashSet<>();
        devices = new HashSet<>();
    }

    public BluetoothReceiver(BluetoothObserver observer){
        this();
        observers.add(observer);
    }

    public BluetoothReceiver(Set<BluetoothObserver> observers){
        this();
        this.observers = observers;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            devices.add(device);
        }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
            notifyObserver();
        }
    }

    @Override
    public void attach(BluetoothObserver observer) {
        observers.add(observer);
    }

    @Override
    public void deatach(BluetoothObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver() {
        for (BluetoothObserver observer: observers)
            observer.update(devices);
    }
}
