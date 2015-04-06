package ngvl.android.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ExemploBTActivity extends Activity implements OnClickListener, DialogInterface.OnClickListener, OnCancelListener {

    private static final String SERVICO = "NGChat";
    private static final UUID MEU_UUID = UUID.fromString("db12d1e9-caba-84ef-398b-12011984abcd");
    
    private static final int BT_TEMPO_DESCOBERTA = 30;
    private static final int BT_ATIVAR = 0;
    private static final int BT_VISIVEL = 1;
    
    private static final int MSG_TEXTO = 0;
    private static final int MSG_DESCONECTOU = 2;
    
    private static final int INICIAR_SERVIDOR = 1;
    private static final int INICIAR_CLIENTE = 2;

    private ThreadServidor threadServidor;
    private ThreadCliente threadCliente;
    private ThreadComunicacao threadComunicacao;
    
	private BluetoothAdapter adaptador;
	private List<BluetoothDevice> dispositivosRemotos;
	private EventosBluetoothReceiver meuReceiver;
	
	private DataInputStream is;
	private DataOutputStream os;

	private ArrayAdapter<String> historico;
	private TelaHandler telaHandler;
	private ProgressDialog waitDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		telaHandler = new TelaHandler();
		historico = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		((ListView)findViewById(R.id.lstHistorico)).setAdapter(historico);
		meuReceiver = new EventosBluetoothReceiver();
		
		dispositivosRemotos = new ArrayList<BluetoothDevice>();
		adaptador = BluetoothAdapter.getDefaultAdapter();
		if (adaptador != null) {

			if (!adaptador.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, BT_ATIVAR);
			}
			
		} else {
			Toast.makeText(this, "Seu aparelho n‹o suporta Bluetooth", Toast.LENGTH_LONG).show();
			finish();
		}
		
		IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(meuReceiver, filter1);
		registerReceiver(meuReceiver, filter2);
		
		findViewById(R.id.btnEnviar).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(meuReceiver);
		
		paraTudo();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, INICIAR_SERVIDOR, 0, "Iniciar Servidor");
		menu.add(0, INICIAR_CLIENTE, 0, "Iniciar Cliente");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INICIAR_CLIENTE:
			dispositivosRemotos.clear();
			adaptador.startDiscovery();
			showProgressDialog("Procurando por dispositivos...", 0);
			break;

		case INICIAR_SERVIDOR:
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_TEMPO_DESCOBERTA);
			startActivityForResult(discoverableIntent, BT_VISIVEL);			
			break;
		}		
		return super.onMenuItemSelected(featureId, item);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BT_ATIVAR) {
			if (RESULT_OK != resultCode) {
				Toast.makeText(this, "Essa aplica‹o necessita de Bluetooth para executar.", Toast.LENGTH_SHORT).show();
				finish();
			}
			
		} else if (requestCode == BT_VISIVEL){
			if (resultCode == BT_TEMPO_DESCOBERTA) {
				iniciaThreadServidor();
				
			} else {
				Toast.makeText(this, "Para iniciar o servidor, seu aparelho deve estar vis’vel.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		iniciaThreadCliente(which);
		dialog.dismiss();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		adaptador.cancelDiscovery();
		paraTudo();
	}
	
	@Override
	public void onClick(View v) {
		EditText edt = (EditText)findViewById(R.id.edtMsg);
		String msg = edt.getText().toString();
		edt.setText("");
		try {
			os.writeUTF(msg);
			historico.add("Eu: "+ msg);
			historico.notifyDataSetChanged();
			
		} catch (IOException e) {
			e.printStackTrace();
			telaHandler.obtainMessage(MSG_DESCONECTOU, e.getMessage() +"[0]").sendToTarget();
		}
	}

	private void showProgressDialog(String mensagem, long tempo){
		waitDialog = ProgressDialog.show(this, "Aguarde", mensagem, true, true);
		waitDialog.show();
		
		if (tempo > 0){
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if (threadComunicacao == null){
						paraTudo();
						waitDialog.dismiss();
					}
				}
			}, tempo * 1000);
		}
	}
	
	private void paraTudo(){
		if (threadComunicacao != null){
			threadComunicacao.parar();
			threadComunicacao = null;
		}
		
		if (threadServidor != null){
			threadServidor.parar();
			threadServidor = null;
		}
		if (threadCliente != null){
			threadCliente.parar();
			threadCliente = null;
		}		
	}
	
	private void iniciaThreadServidor() {

		showProgressDialog("Aguardando por conex›es...", BT_TEMPO_DESCOBERTA);
		
		paraTudo();
		
		threadServidor = new ThreadServidor();
		threadServidor.iniciar();
	}

    private void iniciaThreadCliente(final int which) {
    	paraTudo();

    	threadCliente = new ThreadCliente();
    	threadCliente.iniciar(dispositivosRemotos.get(which));
	}

	private void trataSocket(final BluetoothSocket socket) {
		waitDialog.dismiss();

		threadComunicacao = new ThreadComunicacao();
		threadComunicacao.iniciar(socket);
	}
    
	private class ThreadServidor extends Thread {
		BluetoothServerSocket serverSocket;
		BluetoothSocket clientSocket;
		
		public void run() {
			try {
				serverSocket = adaptador.listenUsingRfcommWithServiceRecord(SERVICO, MEU_UUID);
				clientSocket = serverSocket.accept();
				trataSocket(clientSocket);
				
			} catch (IOException e) {
				telaHandler.obtainMessage(MSG_DESCONECTOU, e.getMessage()+"[1]").sendToTarget();
				e.printStackTrace();
			}
		}
		
		public void iniciar(){
			start();
		}
		
		public void parar(){
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ThreadCliente extends Thread {

		BluetoothDevice device;
		BluetoothSocket socket;
		
		public void run() {
			
			try {
				BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MEU_UUID);
				socket.connect();
				trataSocket(socket);
				
			} catch (IOException e) {
				e.printStackTrace();
				telaHandler.obtainMessage(MSG_DESCONECTOU, e.getMessage()+"[2]").sendToTarget();
			}
		}
		
		public void iniciar(BluetoothDevice device){
			this.device = device;
			start();
		}
		
		public void parar(){
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ThreadComunicacao extends Thread {

		String nome;
		BluetoothSocket socket;
		
		public void run() {
			try {
				nome = socket.getRemoteDevice().getName();
				is = new DataInputStream(socket.getInputStream());
				os = new DataOutputStream(socket.getOutputStream());
				
				String string;
				while (true) {
					string = is.readUTF();
					telaHandler.obtainMessage(MSG_TEXTO, nome +": "+ string).sendToTarget();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				telaHandler.obtainMessage(MSG_DESCONECTOU, e.getMessage()+"[3]").sendToTarget();
			}
		}
		
		public void iniciar(BluetoothSocket socket){
			this.socket = socket;
			start();
		}
		
		public void parar(){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
	
	private class TelaHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if (msg.what == MSG_TEXTO){
				historico.add(msg.obj.toString());
				historico.notifyDataSetChanged();
			
			} else if (msg.what == MSG_DESCONECTOU){
				Toast.makeText(ExemploBTActivity.this, "Desconectou. "+ msg.obj, Toast.LENGTH_SHORT).show();
			}
		}
	}
}