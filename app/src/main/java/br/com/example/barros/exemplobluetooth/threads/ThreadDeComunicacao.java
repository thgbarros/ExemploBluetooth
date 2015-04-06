package br.com.example.barros.exemplobluetooth.threads;

import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.com.example.barros.exemplobluetooth.Handlers.TelaHandler;
import br.com.example.barros.exemplobluetooth.util.Constantes;

/**
 * Created by thiagobarros on 05/04/15.
 */
public class ThreadDeComunicacao extends Thread {
    private String nome;
    private BluetoothSocket socket;

    private DataInputStream is;
    private DataOutputStream os;

    private TelaHandler handler;

    public ThreadDeComunicacao(TelaHandler handler){
        this.handler = handler;
    }

    public void run(){
        try{
            nome = socket.getRemoteDevice().getName();
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            String string;

            while(true){
                string = is.readUTF();
                handler.obtainMessage(Constantes.MSG_TEXTO, nome + ": " + string)
                        .sendToTarget();
            }
        }catch(IOException e){
            e.printStackTrace();
            handler.obtainMessage(Constantes.MSG_DESCONECTOU, e.getMessage()+"[3]")
                    .sendToTarget();
        }
    }

    public void iniciar(BluetoothSocket socket){
        this.socket = socket;
        start();
    }

    public void parar(){
        try {
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
