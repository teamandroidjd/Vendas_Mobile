package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Model.SqliteClienteBean;
import com.jdsystem.br.vendasmobile.Model.SqliteClienteDao;
import com.jdsystem.br.vendasmobile.adapter.DocsListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by eduardo.costa on 10/11/2016.
 */

/**
 * Created by hp1 on 21-01-2015.
 */
public class act_TH_contclie extends Fragment{

    String sCodVend,usuario,senha,URLPrincipal,NomeCliente;
    int CodCliente, sCodCliente;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    public ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> ListaContatos;
    ListView lstContatos;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_contato_clie,container,false);
        ctx = getContext();

        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView TAG_TELEFONE_1 = (TextView) v.findViewById(R.id.lblTel1Contato);
        TextView TAG_TELEFONE_2 = (TextView) v.findViewById(R.id.lblTel2Contato);

        Intent intent = ((actDadosCliente) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodCliente = params.getInt(getString(R.string.intent_codcliente));
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
            }
        }

        FloatingActionButton CadContatos = (FloatingActionButton) v.findViewById(R.id.cadcontatoclie);
        CadContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CadContatos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), sCodVend);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putInt(getString(R.string.intent_codcliente),CodCliente);
                params.putString(getString(R.string.intent_nomerazao),NomeCliente);
                i.putExtras(params);
                startActivity(i);
                getActivity().finish();

            }
        });



        try {
            Cursor CursorClie = DB.rawQuery(" SELECT CODCONTATO_INT AS _id, NOME, CARGO, EMAIL, TEL1, TEL2 FROM CONTATO WHERE CODCLIENTE = " + sCodCliente, null);
            if (CursorClie.getCount() > 0) {
                /*CursorClie.moveToFirst();
                String Tel1 =  CursorClie.getString(CursorClie.getColumnIndex("TEL1"));
                String Tel2 =  CursorClie.getString(CursorClie.getColumnIndex("TEL2"));
                Tel1.replaceAll("[^0123456789]", "");
                if (Tel1.length() == 11) {
                    TAG_TELEFONE_1.setText("Telefone 1: " + Mask.addMask(Tel1, "(##)#####-####"));
                } else if (Tel1.length() == 10) {
                    TAG_TELEFONE_1.setText("Telefone 1: " + Mask.addMask(Tel1, "(##)####-####"));
                }else  {
                    TAG_TELEFONE_1.setText("Telefone 1: " + CursorClie.getString(CursorClie.getColumnIndex("TEL1")));
                }

                Tel2.replaceAll("[^0123456789]", "");
                if (Tel2.length() == 11) {
                    TAG_TELEFONE_2.setText("Telefone 1: " + Mask.addMask(Tel2, "(##)#####-####"));
                } else if (Tel2.length() == 10) {
                    TAG_TELEFONE_2.setText("Telefone 1: " + Mask.addMask(Tel2, "(##)####-####"));
                }else  {
                    TAG_TELEFONE_2.setText("Telefone 2: " + CursorClie.getString(CursorClie.getColumnIndex("TEL2")));
                }*/



                String[] colunas = new String[]{"NOME", "CARGO", "EMAIL", "TEL1", "TEL2"};
                int[] para = new int[]{R.id.lblNomeContato, R.id.lblCargoContato, R.id.lblEmailContato, R.id.lblTel1Contato, R.id.lblTel2Contato};

                SimpleCursorAdapter Adapter = new SimpleCursorAdapter(ctx, R.layout.lstclie_contatos_card, CursorClie, colunas, para, 0);
                lstContatos = (ListView) v.findViewById(R.id.lstcontatos);
                lstContatos.setAdapter(Adapter);
            }
        }catch (Exception E){
            E.toString();
        }
        return v;
    }




}
