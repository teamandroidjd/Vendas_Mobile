package com.jdsystem.br.vendasmobile;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

    String sTipoPessoa, sUF, sCodVend;
    private Handler handler = new Handler();
    ProgressDialog Dialogo;
    Spinner spCidade, spTipoPessoa, spBairro, spUF;
    int CodCidade;
    int CodBairro;

    EditText    nomerazao, nomefan,
                nomecompleto, cnpjcpf,
                Edtcpf, EdtRG, ie, endereco,
                numero, cep, estado, cidade, bairro, tel1, tel2, email, edtOBS;
    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_clientes);

        spTipoPessoa = (Spinner) findViewById(R.id.spnTipoPessoa);
        spUF = (Spinner) findViewById(R.id.spnUF);
        spCidade = (Spinner) findViewById(R.id.spnCidade);
        spBairro = (Spinner) findViewById(R.id.spnBairro);

        nomerazao = (EditText) findViewById(R.id.EdtNomeRazao);
        nomefan = (EditText) findViewById(R.id.EdtNomeFan);
        cnpjcpf = (EditText) findViewById(R.id.EdtCnpjCpf);
        ie = (EditText) findViewById(R.id.EdtIE);
        endereco = (EditText) findViewById(R.id.EdtEndereco);
        numero = (EditText) findViewById(R.id.EdtNumero);
        email = (EditText) findViewById(R.id.EdtEmail);
        cep = (EditText) findViewById(R.id.EdtCep);
        tel1 = (EditText) findViewById(R.id.EdtTel1);
        tel2 = (EditText) findViewById(R.id.EdtTel2);
        Edtcpf = (EditText) findViewById(R.id.Edtcpf);
        nomecompleto = (EditText) findViewById(R.id.EdtNomeCompleto);
        EdtRG = (EditText) findViewById(R.id.EdtRG);
        edtOBS = (EditText) findViewById(R.id.EdtOBS);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodVend = params.getString("codvendedor");
            }
        }

        final EditText etCNPJ = (EditText) findViewById(R.id.EdtCnpjCpf);
        etCNPJ.addTextChangedListener(Mask.insert(Mask.CNPJ_MASK, etCNPJ));

        final EditText etCPF = (EditText) findViewById(R.id.Edtcpf);
        etCPF.addTextChangedListener(Mask.insert(Mask.CPF_MASK, etCPF));

        final EditText etCEP = (EditText) findViewById(R.id.EdtCep);
        etCEP.addTextChangedListener(Mask.insert(Mask.CEP_MASK, etCEP));

        EditText etTelefone1 = (EditText) findViewById(R.id.EdtTel1);
        etTelefone1.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone1));

        EditText etTelefone2 = (EditText) findViewById(R.id.EdtTel2);
        etTelefone2.addTextChangedListener(Mask.insert(Mask.TELEFONE_MASK, etTelefone1));

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
                if (sTipoPessoa == "F") {
                    nomerazao.setVisibility(EditText.GONE);
                    nomefan.setVisibility(EditText.GONE);
                    cnpjcpf.setVisibility(EditText.GONE);
                    ie.setVisibility(EditText.GONE);
                    Edtcpf.setVisibility(EditText.VISIBLE);
                    nomecompleto.setVisibility(EditText.VISIBLE);
                    EdtRG.setVisibility(EditText.VISIBLE);
                } else {
                    nomerazao.setVisibility(EditText.VISIBLE);
                    nomefan.setVisibility(EditText.VISIBLE);
                    cnpjcpf.setVisibility(EditText.VISIBLE);
                    ie.setVisibility(EditText.VISIBLE);
                    Edtcpf.setVisibility(EditText.GONE);
                    nomecompleto.setVisibility(EditText.GONE);
                    EdtRG.setVisibility(EditText.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

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
                String NomeCidade = spCidade.getSelectedItem().toString();
                Cursor CurCidade = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "'", null);
                if (CurCidade.getCount() > 0) {
                    CurCidade.moveToFirst();
                    CodCidade = CurCidade.getInt(CurCidade.getColumnIndex("CODCIDADE"));
                }
                CurCidade.close();
                Cursor CurBairro = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + CodCidade, null);
                List<String> DadosListBairro = new ArrayList<String>();
                if (CurBairro.getCount() > 0) {
                    CurBairro.moveToFirst();
                    while (CurBairro.moveToNext()) {
                        String Bairro = CurBairro.getString(CurBairro.getColumnIndex("DESCRICAO"));
                        DadosListBairro.add(Bairro);
                    }
                }
                CurBairro.close();

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosListBairro);
                ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                spBairro.setAdapter(spinnerArrayAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spBairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String NomeBairro = spBairro.getSelectedItem().toString();
                try {
                    Cursor CurBai = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE DESCRICAO = '" + NomeBairro + "'", null);
                    if (CurBai.getCount() > 0) {
                        CurBai.moveToFirst();
                        CodBairro = CurBai.getInt(CurBai.getColumnIndex("CODBAIRRO"));
                    }
                    CurBai.close();
                }catch (Exception E){
                    System.out.println("Error" + E);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void btnsalvar(View view) {

        Cursor cursor = DB.rawQuery(" SELECT CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                " ENDERECO , NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                " CEP, CODCLIE_EXT, CODVENDEDOR, TIPOPESSOA, ATIVO FROM CLIENTES WHERE CNPJ_CPF = '" + cnpjcpf.getText() + "'", null);
        try {
            if (cursor.getCount() > 0) {
                DB.execSQL(" UPDATE CLIENTES SET NOMERAZAO = '" + nomerazao.getText() +
                        "', NOMEFAN = '" + nomefan.getText() +
                        "', INSCREST = '" + ie.getText() + "', EMAIL = '" + email.getText() +
                        "', TEL1 = '" + tel1.getText() + "', TEL2 = '"+ tel2.getText() + "', ENDERECO = '" + endereco.getText()  +
                        "', NUMERO = '" +  numero.getText() + "', COMPLEMENT = '" + nomecompleto.getText() +
                        "', CODBAIRRO = '" + CodBairro + "', OBS = '" + edtOBS.getText() + "', CODCIDADE = '" + CodCidade + "', UF = '" + sUF +
                        "', CEP = '" + cep.getText() +  "', " +
                        " TIPOPESSOA = '" + sTipoPessoa + "', ATIVO = '" + "S" + "'" +
                        "', CODVENDEDOR = '" + sCodVend + "'" +
                        " WHERE CNPJ_CPF = '" + cnpjcpf.getText() + "'");
            } else {
                DB.execSQL("INSERT INTO CLIENTES (CNPJ_CPF, NOMERAZAO, NOMEFAN, INSCREST, EMAIL, TEL1, TEL2, " +
                        "ENDERECO, NUMERO, COMPLEMENT, CODBAIRRO, OBS, CODCIDADE, UF, " +
                        "CEP, CODVENDEDOR, TIPOPESSOA, ATIVO, FLAGINTEGRADO) VALUES(" +
                        "'" + cnpjcpf.getText() + "','" +  nomerazao.getText() +
                        "',' " + nomefan.getText() + "',' " + ie.getText() + "',' " + email.getText() +
                        "',' " + tel1.getText() + "', '"+  tel2.getText() + "', '" + endereco.getText()  +
                        "',' " +  numero.getText() + "', '" + nomecompleto.getText() +
                        "'," + CodBairro + ",' " + edtOBS.getText() + "'," + CodCidade + ",' " + sUF +
                        "',' " + cep.getText() + "'," + sCodVend + ",'" + sTipoPessoa + "','" + "S" + "','"
                        + "1" + "');"); //
            }
            cursor.close();

            Toast.makeText(this, "Cliente salvo com sucesso!", Toast.LENGTH_SHORT).show();
            clearText();
        } catch (Exception E) {
            System.out.println("Error" + E);
            Toast.makeText(this, "Não foi possivel salvar o CLiente!", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearText() {
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
                    Cursor cursor = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO FROM CIDADES WHERE UF = '" + sUF + "'", null);
                    List<String> DadosList = new ArrayList<String>();
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        while (cursor.moveToNext()) {
                            String Cidade = cursor.getString(cursor.getColumnIndex("DESCRICAO"));
                            CodCidade = cursor.getInt(cursor.getColumnIndex("CODCIDADE"));
                            DadosList.add(Cidade);
                        }
                        cursor.close();

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act_CadClientes.this, android.R.layout.simple_spinner_dropdown_item, DadosList);
                        ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        spCidade.setAdapter(spinnerArrayAdapter);
                    }

                } catch (Exception E) {
                    System.out.println("Error" + E);
                }

            }
        });

    }
}
