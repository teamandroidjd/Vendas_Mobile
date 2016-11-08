package com.jdsystem.br.vendasmobile;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class act_CadClientes extends AppCompatActivity implements Runnable {

    String sTipoPessoa, sUF = null;
    private Handler handler = new Handler();
    ProgressDialog Dialogo;
    Spinner spCidade, spTipoPessoa, spBairro, spUF;

    EditText nomerazao, nomefan, cnpjcpf, ie, endereco, numero, cep, estado, cidade, bairro, tel1, tel2, email;
    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_clientes);

        spTipoPessoa = (Spinner) findViewById(R.id.spnTipoPessoa);
        spUF = (Spinner) findViewById(R.id.spnUF);
        spCidade = (Spinner) findViewById(R.id.spnCidade);
        spBairro = (Spinner) findViewById(R.id.spnBairro);

        nomerazao   = (EditText) findViewById(R.id.EdtNomeRazao);
        nomefan     = (EditText) findViewById(R.id.EdtNomeFan);
        cnpjcpf     = (EditText) findViewById(R.id.EdtCnpjCpf);
        ie          = (EditText) findViewById(R.id.EdtIE);
        endereco    = (EditText) findViewById(R.id.EdtEndereco);
        numero      = (EditText) findViewById(R.id.EdtNumero);
        email       = (EditText) findViewById(R.id.EdtEmail);
        cep         = (EditText) findViewById(R.id.EdtCep);
        tel1        = (EditText) findViewById(R.id.EdtTel1);
        tel2        = (EditText) findViewById(R.id.EdtTel2);


        spTipoPessoa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sTipoPessoa = "F";
                        break;
                    case 1:
                        sTipoPessoa = "J";
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spUF.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        sUF = "AC";
                        break;
                    case 2:
                        sUF = "AL";
                        break;
                    case 3:
                        sUF = "AP";
                        break;
                    case 4:
                        sUF = "AM";
                        break;
                    case 5:
                        sUF = "BA";
                        break;
                    case 6:
                        sUF = "CE";
                        break;
                    case 7:
                        sUF = "DF";
                        break;
                    case 8:
                        sUF = "ES";
                        break;
                    case 9:
                        sUF = "GO";
                        break;
                    case 10:
                        sUF = "MA";
                        break;
                    case 11:
                        sUF = "MT";
                        break;
                    case 12:
                        sUF = "MS";
                        break;
                    case 13:
                        sUF = "MG";
                        break;
                    case 14:
                        sUF = "PA";
                        break;
                    case 15:
                        sUF = "PB";
                        break;
                    case 16:
                        sUF = "PE";
                        break;
                    case 17:
                        sUF = "PI";
                        break;
                    case 18:
                        sUF = "RJ";
                        break;
                    case 19:
                        sUF = "RN";
                        break;
                    case 20:
                        sUF = "RS";
                        break;
                    case 21:
                        sUF = "RO";
                        break;
                    case 22:
                        sUF = "RR";
                        break;
                    case 23:
                        sUF = "SC";
                        break;
                    case 24:
                        sUF = "SP";
                        break;
                    case 25:
                        sUF = "SE";
                        break;
                    case 26:
                        sUF = "TO";
                        break;
                }

                Thread thread = new Thread(act_CadClientes.this);
                thread.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        spCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spBairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);
    }

    public void btnsalvar (View view){

        try {
            DB.execSQL("INSERT INTO CLIENTES VALUES('" + sTipoPessoa + "', '" + nomerazao.getText() + "','" + nomefan.getText() + "','" + cnpjcpf.getText() + "','" + ie.getText()
                    + "','" + endereco.getText() + "','" + numero.getText() + "','" + cep.getText() + "','" + estado.getText() + "','" + cidade.getText()
                    + "','" + bairro.getText() + "','" + tel1.getText() + "');");

            Toast.makeText(this, "Cliente salvo com sucesso!", Toast.LENGTH_SHORT).show();
            clearText();
        }catch (Exception E){
            Toast.makeText(this, "Não foi possivel salvar o CLiente!", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearText(){
        nomerazao.setText("");
        nomefan.setText("");
        cnpjcpf.setText("");
        ie.setText("");
        email.setText("");
        endereco.setText("");
        numero.setText("");
        cep.setText("");
        estado.setText("");
        cidade.setText("");
        bairro.setText("");
        tel1.setText("");
        tel2.setText("");
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int CodCidade = 0;
                try {
                    Cursor cursor  = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO FROM CIDADES WHERE UF = '" + sUF +"'", null);
                    List<String> DadosList = new ArrayList<String>();
                    if(cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        while (cursor.moveToNext()) {
                            String Cidade = cursor.getString(cursor.getColumnIndex("DESCRICAO"));
                            CodCidade = cursor.getInt(cursor.getColumnIndex("CODCIDADE"));
                            DadosList.add(Cidade);
                        }
                        cursor.close();

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                        ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spCidade.setAdapter(spinnerArrayAdapter);
                    }

                } catch (Exception E) {
                    System.out.println("Error"+ E);
                }
                try {
                    Cursor CursosBairro  = DB.rawQuery(" SELECT DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidade, null);
                    List<String> DadosListBairro = new ArrayList<String>();
                    if(CursosBairro.getCount() > 0) {
                        CursosBairro.moveToFirst();
                        while (CursosBairro.moveToNext()) {
                            String Cidade = CursosBairro.getString(CursosBairro.getColumnIndex("DESCRICAO"));
                            DadosListBairro.add(Cidade);
                        }
                        CursosBairro.close();

                        ArrayAdapter<String> arrayAdapterBairro = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                        ArrayAdapter<String> spinnerArrayAdapterBairro = arrayAdapterBairro;
                        spinnerArrayAdapterBairro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spBairro.setAdapter(spinnerArrayAdapterBairro);
                    }

                } catch (Exception E) {
                    System.out.println("Error"+ E);
                }

            }
        });

    }
}
