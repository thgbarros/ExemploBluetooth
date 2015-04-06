package br.com.example.barros.exemplobluetooth.Receivers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import br.com.example.barros.exemplobluetooth.util.Constantes;

/**
 * Created by thiagobarros on 05/04/15.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>();
    private ProgressDialog waitDialog;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
            bluetoothDevices.add(device);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
            exibirDispositivosEncontrados();
        }
        this.context = context;
    }

    private void exibirDispositivosEncontrados() {
        CharSequence[] devices = new String[bluetoothDevices.size()];
        int i = 0;
        for (BluetoothDevice device: bluetoothDevices){
            devices[i++] = device.getName();
        }

        CharSequence title = "Aparelhos encontrados";

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setSingleChoiceItems(devices, -1, (DialogInterface.OnClickListener) context)
                .create();
        dialog.show();
    }



}
