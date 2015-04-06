package br.com.example.barros.exemplobluetooth.Handlers;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import br.com.example.barros.exemplobluetooth.util.Constantes;
import br.com.example.barros.exemplobluetooth.view.MainActivity;

/**
 * Created by thiagobarros on 05/04/15.
 */
public class TelaHandler extends Handler {
    private ArrayAdapter<String> historico;
    private Context context;

    public TelaHandler(Context context){
        historico =new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1);

        this.context = context;
    }


    public void handlerMessage(Message msg){
        super.handleMessage(msg);

        if (msg.what == Constantes.MSG_TEXTO){
            historico.add(msg.toString());
            historico.notifyDataSetChanged();
        }else if (msg.what == Constantes.MSG_DESCONECTOU){
            Toast.makeText(context, "Desconectou. "+msg.obj, Toast.LENGTH_LONG).show();
        }

    }

    public ArrayAdapter<String> getAdapter(){
        return historico;
    }


}
