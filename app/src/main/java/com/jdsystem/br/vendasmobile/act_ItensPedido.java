package com.jdsystem.br.vendasmobile;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.HashMap;

public class act_ItensPedido extends ListActivity
        implements Runnable {

    ProgressDialog prodDialog;
    private Handler handlerp = new Handler();
    public ListAdapter adapterp;
    String sCodVend, URLPrincipal;
    ListView edtProdutos;
    SearchView sv;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__itens_pedido);

        edtProdutos = (ListView) findViewById(android.R.id.list);
        sv = (SearchView) findViewById(R.id.EdtPesquisa);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapterp.getFilter().filter(text);
                return false;
            }
        });

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

        Thread thread = new Thread(act_ItensPedido.this);
        thread.start();
    }

    @Override
    public void run() {
        handlerp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    CarregarProdutos();
                } catch (Exception E) {

                } finally {
                    if (prodDialog.isShowing())
                        prodDialog.dismiss();
                }
            }

            ;

        });
    }

    private void CarregarProdutos() {

        Cursor CursorProd = DB.rawQuery(" SELECT CODITEMANUAL, DESCRICAO, UNIVENDA, VLVENDA1  FROM ITENS ", null);
        ArrayList<HashMap<String, String>> DadosList = new ArrayList<HashMap<String, String>>();

        if (CursorProd.getCount() > 0) {
            CursorProd.moveToFirst();
            while (CursorProd.moveToNext()) {
                String CodigoManual = CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL"));
                String Descricao = CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO"));
                String UnidVenda = CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA"));
                String Preco = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));

                HashMap<String, String> DadosProdutos = new HashMap<String, String>();

                DadosProdutos.put("CODITEMANUAL", CodigoManual);
                DadosProdutos.put("DESCRICAO", Descricao);
                DadosProdutos.put("UNIVENDA", UnidVenda);
                DadosProdutos.put("VLVENDA1", Preco);

                DadosList.add(DadosProdutos);
            }
            CursorProd.close();
            adapterp = new ListAdapter(act_ItensPedido.this, DadosList, R.layout.lyitenspedido,
                    new String[]{"CODITEMANUAL", "DESCRICAO", "UNIVENDA", "VLVENDA1"},
                    new int[]{R.id.lblCodItem, R.id.lblNomeItem, R.id.lblUnidItem, R.id.lblPreco});

            setListAdapter(adapterp);
        }
    }
}
