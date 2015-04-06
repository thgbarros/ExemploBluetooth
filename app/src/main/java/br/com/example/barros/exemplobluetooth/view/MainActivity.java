package br.com.example.barros.exemplobluetooth.view;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import android.content.Context;
import android.os.Message;

import android.content.BroadcastReceiver;

import br.com.example.barros.exemplobluetooth.Receivers.BluetoothReceiver;
import br.com.example.barros.exemplobluetooth.util.Constantes;

import br.com.example.barros.exemplobluetooth.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;

import static java.util.UUID.*;

public class MainActivity extends ActionBarActivity implements View.OnClickListener,
            DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private ListView listViewDispositivo;
    private Button buttonAtivarBluetooth;
    private Button buttonListarDispositivos;
    private ProgressDialog waitDialog;

    private static final UUID MEU_UUID = fromString("db12d1e9-caba-84ef-398b-12011984abcd");

    private EventosBluetoothReceiver receiver;
    private BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> dispositivosRemotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dispositivosRemotos = new ArrayList<BluetoothDevice>();

        listViewDispositivo = (ListView) findViewById(R.id.listViewDispositivos);
        buttonAtivarBluetooth = (Button) findViewById(R.id.buttonAtivarBluetooth);
        buttonListarDispositivos = (Button) findViewById(R.id.buttonListarDispositivos);

        buttonListarDispositivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constantes.BT_TEMPO_DESCOBERTA);
                startActivityForResult(discoverableIntent, Constantes.BT_VISIVEL);

                bluetoothAdapter.startDiscovery();
                showProgressDialog("Procurando por dispositivos...", 0);
            }
        });

        buttonAtivarBluetooth.setOnClickListener(this);

        receiver = new EventosBluetoothReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

    @Override
    public void onClick(View v) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constantes.BT_ATIVAR);
            }
        }else{
            Toast.makeText(this, "Aparelho não suporta Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter12 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(receiver, filter1);
        registerReceiver(receiver, filter12);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        BluetoothDevice device = dispositivosRemotos.get(which);

        try{
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MEU_UUID);
            socket.connect();
        }catch(IOException e){
            e.printStackTrace();

        }
    }

    private void showProgressDialog(String mensagem, long tempo){
        waitDialog = ProgressDialog.show(this, "Aguarde", mensagem, true, true);
        waitDialog.show();

        if (tempo > 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   waitDialog.dismiss();
                }
            }, tempo * 1000);
        }
    }

    private void exibirDispositivosEncontrados() {
        waitDialog.dismiss();

        String[] aparelhos = new String[dispositivosRemotos.size()];
        for (int i = 0; i < dispositivosRemotos.size(); i++) {
            aparelhos[i] = dispositivosRemotos.get(i).getName();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aparelhos encontrados")
                .setSingleChoiceItems(aparelhos, -1, this).create();
        dialog.show();
    }

    private class EventosBluetoothReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                dispositivosRemotos.add(device);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                exibirDispositivosEncontrados();
            }
        }
    }

    private class TelaHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == Constantes.MSG_TEXTO){
                //historico.add(msg.obj.toString());
                //historico.notifyDataSetChanged();

            } else if (msg.what == Constantes.MSG_DESCONECTOU){
                //Toast.makeText(ExemploBTActivity.this, "Desconectou. "+ msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
