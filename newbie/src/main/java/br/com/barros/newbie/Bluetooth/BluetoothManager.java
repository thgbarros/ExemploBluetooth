package br.com.barros.newbie.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import br.com.barros.newbie.Bluetooth.Exceptions.BluetoothException;
import br.com.barros.newbie.Observers.BluetoothObservable;
import br.com.barros.newbie.Observers.BluetoothObserver;

/**
 * Created by thiagobarros on 05/04/15.
 */
public class BluetoothManager implements BluetoothObservable, BluetoothObserver {

    private Set<BluetoothObserver> observers = new HashSet<>();
    private Set<BluetoothDevice> devices;

    private BluetoothAdapter defaultAdapter;
    private BluetoothReceiver defaultReceiver;
    private Logger logger = Logger.getLogger("BluetoothManager");

    private Activity activity;

    private static final int TEMPO_DE_DESCOBERTA = 30;
    private static final int VISIVEL = 1;

    private static final UUID uuid = UUID.fromString("0bed0288-dfbf-4557-9699-0929daa7c2eb");

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
    }

    public void connect(BluetoothDevice device){

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

    @Override
    public void update(Set<BluetoothDevice> devices) {
        this.devices = devices;
        notifyObserver();
    }
}
