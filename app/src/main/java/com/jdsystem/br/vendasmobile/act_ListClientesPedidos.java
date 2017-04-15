package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientesPedido;
import com.jdsystem.br.vendasmobile.domain.*;
import com.jdsystem.br.vendasmobile.fragments.FragmentCliente;
import com.jdsystem.br.vendasmobile.fragments.FragmentClientePedido;

import java.util.ArrayList;
import java.util.List;


public class act_ListClientesPedidos extends AppCompatActivity implements Runnable {

    ProgressDialog pDialog;
    private Handler handler = new Handler();
    public ListAdapterClientesPedido adapter;
    String sCodVend, URLPrincipal;
    ListView edtCliente;
    SearchView sv;
    ClientesPedido lstclientespedidos;
    private Context ctx;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_clientes_pedido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FragmentClientePedido frag = (FragmentClientePedido) getSupportFragmentManager().findFragmentByTag("mainFrag");
        if(frag == null) {
            frag = new FragmentClientePedido();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rl_fragment_container, frag, "mainFrag");
            ft.commit();
        }


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
                URLPrincipal = params.getString("urlPrincipal");
            }
        }

        DB = new ConfigDB(ctx).getReadableDatabase();

        pDialog = new ProgressDialog(act_ListClientesPedidos.this);
        pDialog.setTitle("Aguarde...");
        pDialog.setMessage("Carregando Clientes");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(act_ListClientesPedidos.this);
        thread.start();
    }


    public List<ClientesPedido> CarregarClientesPedidos() {
        Cursor CursorClie = DB.rawQuery(" SELECT CLIENTES.*, CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                " WHERE CODVENDEDOR = " + sCodVend +
                " ORDER BY NOMEFAN, NOMERAZAO ", null);
        String docFormatado;
        ArrayList<ClientesPedido> DadosList = new ArrayList<ClientesPedido>();

        if (CursorClie.getCount() > 0) {
            CursorClie.moveToFirst();
            do {
                String NomeFan = CursorClie.getString(CursorClie.getColumnIndex("NOMEFAN"));
                String NomeRazao = CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO"));
                String Cnpj = CursorClie.getString(CursorClie.getColumnIndex("CNPJ_CPF"));
                Cnpj = Cnpj.replaceAll("[^0123456789]", "");
                if (Cnpj.length() == 14) {
                    docFormatado = Mask.addMask(Cnpj, "##.###.###/####-##");
                } else {
                    docFormatado = Mask.addMask(Cnpj.replaceAll("[^0123456789]", ""), "###.###.###-##");
                }
                String Cidade = CursorClie.getString(CursorClie.getColumnIndex("CIDADE"));
                String Estado = CursorClie.getString(CursorClie.getColumnIndex("UF"));
                String Bairro = CursorClie.getString(CursorClie.getColumnIndex("BAIRRO"));
                String Telefone = CursorClie.getString(CursorClie.getColumnIndex("TEL1"));

                lstclientespedidos = new ClientesPedido(NomeFan, NomeRazao, docFormatado, Estado, Cidade, Bairro, Telefone);
                DadosList.add(lstclientespedidos);

            }
            while (CursorClie.moveToNext());
            CursorClie.close();

        }
        return DadosList;

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    CarregarClientesPedidos();
                } catch (Exception E) {

                } finally {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
            }

            ;

        });

    }

}
