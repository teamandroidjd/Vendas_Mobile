package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;

import java.util.ArrayList;

/**
 * Created by WKS22 on 28/03/2017.
 */

public class act_TH_produtos_contatos extends Fragment {
    int sCodContato;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    String codVendedor, usuario, senha,URLPrincipal, NomeCliente, telaInvocada;
    int CodCliente;
    ArrayList<String> arrayListProd, produtosRelacionados;
    ArrayAdapter<String> arrayAdapterProdutos;
    ListView lv_informa_produtos;
    public SharedPreferences prefs;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    int idPerfil;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_produtos_contatos,container,false);
        ctx = getContext();

        Intent intent = ((DadosContato) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
                sCodContato = params.getInt(getString(R.string.intent_codcontato));
            }
        }

        lv_informa_produtos = (ListView) v.findViewById(R.id.lv_produtos_contatos);
        lv_informa_produtos.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        lv_informa_produtos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClickView, final int position, long id) {
                AlertDialog.Builder confirmRemove = new AlertDialog.Builder(ctx);
                //confirmRemove.setTitle();
                confirmRemove.setMessage(R.string.remove_prod_cont)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String itemLista = lv_informa_produtos.getItemAtPosition(position).toString();
                                arrayAdapterProdutos.remove(produtosRelacionados.get(position));
                                arrayAdapterProdutos.notifyDataSetChanged();
                                try {
                                    excluiProdutoSelecionado(ctx, sCodContato, itemLista);
                                } catch (Exception E) {
                                    E.toString();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = confirmRemove.create();
                alert.show();
            }
        });

        prefs = ctx.getSharedPreferences(CONFIG_HOST, ctx.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        listaProdutosContato();

        FloatingActionButton fabCadProdCont = (FloatingActionButton) v.findViewById(R.id.cad_produtos_contatos);
        fabCadProdCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               declaraProdutosContatos();
            }
        });

        return v;
    }

    public void declaraProdutosContatos() {

        Intent i = new Intent(ctx, ConsultaProdutos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putInt(getString(R.string.intent_cad_contato), 2);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putInt(getString(R.string.intent_codcliente), CodCliente);
        params.putString(getString(R.string.intent_nomerazao), NomeCliente);
        params.putString(getString(R.string.intent_telainvocada), "TAB_PRODUTOS_CONTATOS");
        params.putInt(getString(R.string.intent_codcontato),sCodContato);
        i.putExtras(params);
        startActivity(i);
    }


    private void listaProdutosContato() {
        ArrayList<String> produtosContatos = listaProdutosRelacionados();
        arrayAdapterProdutos = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, produtosContatos);
        lv_informa_produtos.setAdapter(arrayAdapterProdutos);
    }

    private ArrayList<String> listaProdutosRelacionados() {
        produtosRelacionados = new ArrayList<String>();
        DB = new ConfigDB(ctx).getReadableDatabase();
        try {
            Cursor cursor = DB.rawQuery("select produtos_contatos.cod_produto_manual, produtos_contatos.cod_interno_contato, " +
                    "itens.descricao as desc " +
                    "from produtos_contatos " +
                    "left outer join itens on produtos_contatos.cod_produto_manual = itens.CODITEMANUAL " +
                    "where produtos_contatos.cod_interno_contato = " + sCodContato, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String codProdutoCont = cursor.getString(cursor.getColumnIndex("produtos_contatos_temp.cod_produto_manual"));
                    String descProdCont = cursor.getString(cursor.getColumnIndex("desc"));
                    String itemLista;
                    if (descProdCont.length() <= 26) {
                        itemLista = codProdutoCont + " - " + descProdCont;
                    }else{
                        itemLista = codProdutoCont + " - " + descProdCont.substring(0,26);
                    }

                    produtosRelacionados.add(itemLista);

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
        return produtosRelacionados;

    }

    public static void excluiProdutoSelecionado(Context context, int codContato, String sItemLista){
        SQLiteDatabase db = new ConfigDB(context).getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select produtos_contatos.cod_produto_manual, produtos_contatos.cod_interno_contato, " +
                    "itens.descricao as desc " +
                    "from produtos_contatos " +
                    "left outer join itens on produtos_contatos.cod_produto_manual = itens.CODITEMANUAL " +
                    "where produtos_contatos.cod_interno_contato = " + codContato, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String codProdutoCont = cursor.getString(cursor.getColumnIndex("produtos_contatos_temp.cod_produto_manual"));
                    String descProdCont = cursor.getString(cursor.getColumnIndex("desc"));
                    String itemLista;
                    if (descProdCont.length() <= 26) {
                        itemLista = codProdutoCont + " - " + descProdCont;
                    }else{
                        itemLista = codProdutoCont + " - " + descProdCont.substring(0,26);
                    }

                    if(itemLista.equals(sItemLista)){
                        db.execSQL("delete from produtos_contatos where cod_produto_manual = '"+ codProdutoCont + "' and " +
                                "cod_interno_contato = " + codContato);
                    }

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception E) {
            E.toString();
        }
    }


}
