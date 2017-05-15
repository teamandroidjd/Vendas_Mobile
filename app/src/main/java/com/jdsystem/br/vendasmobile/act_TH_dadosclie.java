package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.jetbrains.annotations.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by hp1 on 21-01-2015.
 */
public class act_TH_dadosclie extends Fragment {

    String CodCliente, codVendedor, URLPrincipal, usuario, senha;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    public SharedPreferences prefs;
    public static final String CONFIG_HOST = "CONFIG_HOST";
    int idPerfil;


    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_dadosclie, container, false);
        ctx = getContext();

        prefs = ctx.getSharedPreferences(CONFIG_HOST, ctx.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);

        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView TAG_NOMEFANTASIA = (TextView) v.findViewById(R.id.txt_nomefantasia);
        TextView TEXTO_NOMEFANTASIA = (TextView) v.findViewById(R.id.nomefantasia);
        TextView TEXTO_RAZAOSOCIAL = (TextView) v.findViewById(R.id.nomerazaocliente);
        TextView TAG_RAZAOSOCIAL = (TextView) v.findViewById(R.id.txt_nomerazaocliente);
        TextView TAG_DOCUMENTO = (TextView) v.findViewById(R.id.txt_documento);
        TextView TAG_CIDADE = (TextView) v.findViewById(R.id.txtcidadecliente);
        TextView TAG_ESTADO = (TextView) v.findViewById(R.id.txtEstatoCliente);
        TextView TAG_BAIRRO = (TextView) v.findViewById(R.id.txtBairroCliente);
        TextView TAG_RG = (TextView) v.findViewById(R.id.txt_rg);
        TextView TAG_ENDERECO = (TextView) v.findViewById(R.id.txtEndereco);
        TextView TAG_IE = (TextView) v.findViewById(R.id.txt_ie);
        TextView TAG_CEP = (TextView) v.findViewById(R.id.txtCEP);
        TextView TAG_COMPLEMENTO = (TextView) v.findViewById(R.id.txtComplemento);
        TextView TAG_TELEFONE_1 = (TextView) v.findViewById(R.id.txt_telefone_1);
        TextView TAG_TELEFONE_2 = (TextView) v.findViewById(R.id.txt_telefone_2);
        TextView TAG_EMAIL = (TextView) v.findViewById(R.id.txt_email);

        Intent intent = ((DadosCliente) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                CodCliente = params.getString(getString(R.string.intent_codcliente));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }

        try {
            Cursor CursorClie = DB.rawQuery(" SELECT CLIENTES.*, CLIENTES.CODCLIE_EXT AS _id, TEL1, TEL2, EMAIL, REGIDENT, CNPJ_CPF, " +
                    " CIDADES.DESCRICAO AS CIDADE, BAIRROS.DESCRICAO AS BAIRRO FROM CLIENTES LEFT OUTER JOIN " +
                    " CIDADES ON CLIENTES.CODCIDADE = CIDADES.CODCIDADE LEFT OUTER JOIN " +
                    " ESTADOS ON CLIENTES.UF = ESTADOS.UF LEFT OUTER JOIN " +
                    " BAIRROS ON CLIENTES.CODBAIRRO = BAIRROS.CODBAIRRO " +
                    " WHERE CODCLIE_INT = " + Integer.parseInt(CodCliente) + " AND CODPERFIL =" + idPerfil +
                    " ORDER BY NOMEFAN, NOMERAZAO ", null);

            if (CursorClie.getCount() > 0) {
                CursorClie.moveToFirst();
                do {
                    String Documento = CursorClie.getString(CursorClie.getColumnIndex("CNPJ_CPF"));
                    String CEP = CursorClie.getString(CursorClie.getColumnIndex("CEP"));
                    String Tel1 = CursorClie.getString(CursorClie.getColumnIndex("TEL1"));
                    String Tel2 = CursorClie.getString(CursorClie.getColumnIndex("TEL2"));
                    Documento.replaceAll("[^0123456789]", "");

                    if (Documento.equals("")) {
                        TAG_DOCUMENTO.setText("CPF/CNPJ: ");
                        if (CursorClie.getString(CursorClie.getColumnIndex("REGIDENT")).equals("")) {
                            TAG_RG.setVisibility(EditText.GONE);
                        } else {
                            TAG_RG.setText("Identidade: " + CursorClie.getString(CursorClie.getColumnIndex("REGIDENT")));
                            TAG_RG.setVisibility(EditText.VISIBLE);
                        }
                        if (CursorClie.getString(CursorClie.getColumnIndex("INSCREST")).equals("")) {
                            TAG_IE.setVisibility(EditText.GONE);
                        } else {
                            TAG_IE.setText("Inscrição Estadual: " + CursorClie.getString(CursorClie.getColumnIndex("INSCREST")));
                            TAG_IE.setVisibility(EditText.VISIBLE);
                        }
                        TAG_NOMEFANTASIA.setText(CursorClie.getString(CursorClie.getColumnIndex("NOMEFAN")));
                        TAG_RAZAOSOCIAL.setText(CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO")));
                        TEXTO_NOMEFANTASIA.setVisibility(TextView.VISIBLE);
                        TEXTO_RAZAOSOCIAL.setVisibility(TextView.VISIBLE);

                    } else if (Documento.length() == 14) {
                        TAG_DOCUMENTO.setText("CNPJ: " + Mask.addMask(Documento, "##.###.###/####-##"));
                        TAG_NOMEFANTASIA.setText(CursorClie.getString(CursorClie.getColumnIndex("NOMEFAN")));
                        TAG_RAZAOSOCIAL.setText(CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO")));
                        TEXTO_NOMEFANTASIA.setVisibility(TextView.VISIBLE);
                        TEXTO_RAZAOSOCIAL.setVisibility(TextView.VISIBLE);
                        TAG_RG.setVisibility(EditText.GONE);
                        TAG_IE.setText("Inscrição Estadual: " + CursorClie.getString(CursorClie.getColumnIndex("INSCREST")));
                        TAG_IE.setVisibility(EditText.VISIBLE);
                    } else {
                        TEXTO_NOMEFANTASIA.setVisibility(TextView.GONE);
                        TEXTO_RAZAOSOCIAL.setVisibility(TextView.GONE);
                        TAG_DOCUMENTO.setText("CPF: " + Mask.addMask(Documento.replaceAll("[^0123456789]", ""), "###.###.###-##"));
                        TAG_RAZAOSOCIAL.setText("Nome Completo: " + CursorClie.getString(CursorClie.getColumnIndex("NOMERAZAO")));
                        TAG_NOMEFANTASIA.setVisibility(EditText.GONE);
                        TAG_RG.setText("Identidade: " + CursorClie.getString(CursorClie.getColumnIndex("REGIDENT")));
                        TAG_RG.setVisibility(EditText.VISIBLE);
                        TAG_IE.setVisibility(EditText.GONE);
                    }
                    String cidade = CursorClie.getString(CursorClie.getColumnIndex("CIDADE"));
                    String bairro = CursorClie.getString(CursorClie.getColumnIndex("BAIRRO"));
                    if(cidade != null && !cidade.equals("NULL")){
                        TAG_CIDADE.setText("Cidade: "+cidade);
                    }else {
                        TAG_CIDADE.setText("Cidade:");
                    }
                    if(bairro != null && !bairro.equals("NULL")){
                        TAG_BAIRRO.setText("Bairro: "+bairro);
                    } else {
                        TAG_BAIRRO.setText("Bairro:");
                    }

                    TAG_ESTADO.setText("Estado: " + CursorClie.getString(CursorClie.getColumnIndex("UF")));
                    TAG_ENDERECO.setText("Endereço: " + CursorClie.getString(CursorClie.getColumnIndex("ENDERECO")) + ", " + CursorClie.getString(CursorClie.getColumnIndex("NUMERO")));
                    String comp = CursorClie.getString(CursorClie.getColumnIndex("COMPLEMENT"));
                    if (comp == null || comp.equals("")) {
                        TAG_COMPLEMENTO.setVisibility(EditText.GONE);
                    } else {
                        TAG_COMPLEMENTO.setText("Complemento: " + comp);
                    }

                    TAG_CEP.setText("CEP: " + Mask.addMask(CEP, "##.###-###"));
                    Tel1 = Tel1.replaceAll("[^0123456789]", "");
                    if (Tel1.length() == 11) {
                        TAG_TELEFONE_1.setText("Telefone 1: " + Mask.addMask(Tel1, "(##)#####-####"));
                    } else if (Tel1.length() == 10) {
                        TAG_TELEFONE_1.setText("Telefone 1: " + Mask.addMask(Tel1, "(##)####-####"));
                    } else {
                        TAG_TELEFONE_1.setText("Telefone 1: " + CursorClie.getString(CursorClie.getColumnIndex("TEL1")));
                    }

                    Tel2 = Tel2.replaceAll("[^0123456789]", "");
                    if (Tel2.length() == 11) {
                        TAG_TELEFONE_2.setText("Telefone 2: " + Mask.addMask(Tel2, "(##)#####-####"));
                    } else if (Tel2.length() == 10) {
                        TAG_TELEFONE_2.setText("Telefone 2: " + Mask.addMask(Tel2, "(##)####-####"));
                    } else {
                        TAG_TELEFONE_2.setText("Telefone 2: " + CursorClie.getString(CursorClie.getColumnIndex("TEL2")));
                    }

                    TAG_EMAIL.setText("Email: " + CursorClie.getString(CursorClie.getColumnIndex("EMAIL")));
                }
                while (CursorClie.moveToNext());
                CursorClie.close();
            }
        } catch (Exception E) {
            Toast.makeText(ctx, E.toString(), Toast.LENGTH_SHORT).show();
        }


        return v;
    }


    /*@Override
    public void onBackPressed() {
        finish();
    }*/
}
