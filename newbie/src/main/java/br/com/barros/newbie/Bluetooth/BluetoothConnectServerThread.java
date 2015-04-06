package br.com.barros.newbie.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by thiago on 06/04/15.
 */
public class BluetoothConnectServerThread {

    private BluetoothDevice device;
    private BluetoothServerSocket severSocket;
    private UUID uuid;

    private BluetoothConnectServerThread(BluetoothDevice device, UUID uuid){
        this.device = device;
        this.uuid = uuid;
    }

    public void run(){
        BluetoothSocket socket = null;
        while(true){
            try{
                socket = severSocket.accept();
            }catch(IOException e){
                break;
            }

            if (socket != null) {
                manage
            }
        }

    }


}
