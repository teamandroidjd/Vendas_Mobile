package com.jdsystem.br.vendasmobile;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;

import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;

public class actListClientPedidos  extends ListActivity implements Runnable {

    ListView edtCliente;
    SearchView sv;
    ProgressDialog pDialog;
    SQLiteDatabase DB;
    public ListAdapterClientes adapter;
    String sCodVend, URLPrincipal;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    CarregarClientes();
                }catch (Exception E){

                }
                finally {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
            };

        });



    }

    private void CarregarClientes() {
    }

    ;
}
