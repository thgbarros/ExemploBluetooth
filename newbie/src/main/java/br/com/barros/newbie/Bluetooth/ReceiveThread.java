package br.com.barros.newbie.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by thiagobarros on 06/04/15.
 */
public class ReceiveThread extends Thread {

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID uuid;

    private ReceiveThread(UUID uuid){
        this.uuid = uuid;
    }

    public void run(){
        try{
           socket = device.createRfcommSocketToServiceRecord(uuid);
           socket.connect();


        }catch(IOException e){
            e.printStackTrace();

            //É necessário criar uma forma de notificação quando alguma informação chegar pelo socket;
        }
    }

    public void init(BluetoothDevice device){
        this.device = device;
        start();
    }

    public void stopReceive(){
        try{
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
