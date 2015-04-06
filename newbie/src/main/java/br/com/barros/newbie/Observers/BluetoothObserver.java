package br.com.barros.newbie.Observers;

import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * Created by thiagobarros on 06/04/15.
 */
public interface BluetoothObserver {

    void update(Set<BluetoothDevice> devices);
}
