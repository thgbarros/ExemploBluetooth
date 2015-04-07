package br.com.barros.newbie.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import br.com.barros.newbie.Bluetooth.Exceptions.BluetoothException;
import br.com.barros.newbie.Bluetooth.Exceptions.BluetoothStatus;
import br.com.barros.newbie.Observers.Observable;
import br.com.barros.newbie.Observers.Observer;

/**
 * Created by thiagobarros on 05/04/15.
 */
public class BluetoothManager implements Observable, Observer {

    private Set<Observer> observers = new HashSet<>();
    private Set<BluetoothDevice> devices;

    private BluetoothAdapter defaultAdapter;
    private BluetoothReceiver defaultReceiver;
    private Logger logger = Logger.getLogger("BluetoothManager");

    private Activity activity;

    private static final int TEMPO_DE_DESCOBERTA = 30;
    private static final int VISIVEL = 1;

    private BluetoothStatus bluetoothStatus = BluetoothStatus.NONE;

    private static final UUID uuid = UUID.fromString("0bed0288-dfbf-4557-9699-0929daa7c2eb");

    private BluetoothAcceptSocketThread acceptSocketThread = null;
    private BluetoothConnectThread bluetoothConnectThread = null;

    public BluetoothManager(Activity activity) throws BluetoothException {
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();

        if (defaultAdapter == null)
            throw new BluetoothException("Adapter bluetooth not found.");

        logger.info("Adaptador bluetooth: " + defaultAdapter.getName() +
                        " no endereço: " + defaultAdapter.getAddress());

        defaultReceiver = new BluetoothReceiver(this);

        if (!defaultAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, 2);
        }

        IntentFilter filterActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        activity.registerReceiver(defaultReceiver, filterActionFound);
        activity.registerReceiver(defaultReceiver, filterDiscoveryFinished);
        this.activity = activity;
    }

    public void startDiscovery(){
        devices.clear();

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, TEMPO_DE_DESCOBERTA);
        activity.startActivityForResult(discoverableIntent, VISIVEL);

        defaultAdapter.startDiscovery();
        bluetoothStatus = BluetoothStatus.DISCOVERING;
    }

    public void acceptConnection(){
        acceptSocketThread = new BluetoothAcceptSocketThread(defaultAdapter, uuid, this);
        acceptSocketThread.start();
    }

    public void connect(BluetoothDevice device){
        bluetoothConnectThread = new BluetoothConnectThread(defaultAdapter, device, uuid, this);
        bluetoothConnectThread.start();
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
        for (Observer observer: observers) {
            observer.update(devices);
            observer.update(bluetoothStatus);
        }
    }

    @Override
    public void update(Object object) {
        if (object instanceof  Set) {
            this.devices = (Set<BluetoothDevice>) object;
            notifyObserver();
        }

        if (object instanceof BluetoothStatus) {
            bluetoothStatus = (BluetoothStatus) object;
            notifyObserver();
        }
    }
}
