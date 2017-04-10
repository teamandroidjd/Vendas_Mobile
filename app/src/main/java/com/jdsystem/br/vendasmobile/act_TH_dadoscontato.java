package com.jdsystem.br.vendasmobile;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.parser.Line;

import org.jetbrains.annotations.Nullable;

/**
 * Created by WKS22 on 27/03/2017.
 */

public class act_TH_dadoscontato extends Fragment {
    int sCodCliente;
    SQLiteDatabase DB;
    private Context ctx;
    private Activity act;
    int sCodContato;
    Cursor CursorClie;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.act_dados_contatos, container, false);
        ctx = getContext();
        DB = new ConfigDB(ctx).getReadableDatabase();

        TextView NOMEFANTASIA = (TextView) v.findViewById(R.id.txv_nomefantasia_cliente);
        TextView NOMECONTATO = (TextView) v.findViewById(R.id.txt_nome_contato);
        TextView CARGOCONTATO = (TextView) v.findViewById(R.id.txt_cargo_contato);
        TextView DOCUMENTOCONTATO = (TextView) v.findViewById(R.id.txt_documento_contato);
        TextView ENDERECOCONTATO = (TextView) v.findViewById(R.id.txtEndereco_contato);
        TextView BAIRROCONTATO = (TextView) v.findViewById(R.id.txt_bairro_contato);
        TextView CIDADEBAIRRO = (TextView) v.findViewById(R.id.txt_cidade_contato);
        TextView UFCONTATO = (TextView) v.findViewById(R.id.txt_uf_contato);
        TextView COMPLCONTATO = (TextView) v.findViewById(R.id.txt_Complemento_contato);
        TextView CEPCONTATO = (TextView) v.findViewById(R.id.txt_cep_contato);
        TextView TEL1CONTATO = (TextView) v.findViewById(R.id.txt_telefone_1_contato);
        TextView TEL2CONTATO = (TextView) v.findViewById(R.id.txt_telefone_2_contato);
        TextView EMAILCONTATO = (TextView) v.findViewById(R.id.txt_email_contato);
        TextView ANIVERSARIOCONTATO = (TextView) v.findViewById(R.id.txt_data_aniversario_contato);

        Intent intent = ((act_DadosContatos) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodCliente = params.getInt("codCliente");
                sCodContato = params.getInt("codContato");

            }
        }
        try {

            CursorClie = DB.rawQuery("SELECT CONTATO.CODCONTATO_EXT, CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, " +
                    "CONTATO.TEL2, CONTATO.CEP , CONTATO.DOCUMENTO, CONTATO.DATA,  " +
                    "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                    "CONTATO.CODVENDEDOR,  CONTATO.BAIRRO, " +
                    "CLIENTES.NOMERAZAO, CONTATO.DESC_CIDADE, CONTATO.CODCLIENTE, CLIENTES.CODCLIE_EXT, CONTATO.CODCONTATO_INT " +
                    "FROM CONTATO " +
                    "LEFT OUTER JOIN CLIENTES ON CONTATO.CODCLIENTE = CLIENTES.CODCLIE_INT " +
                    "WHERE CONTATO.CODCONTATO_INT = " + sCodContato, null);

            CursorClie.moveToFirst();

            String nomeContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.NOME"));
            String documentoContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.DOCUMENTO"));
            String nomeFantasia = CursorClie.getString(CursorClie.getColumnIndex("CLIENTES.NOMERAZAO"));
            String cargoContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.CARGO"));
            String enderecoContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.ENDERECO"));
            String bairroContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.BAIRRO"));
            String cidadeContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.DESC_CIDADE"));
            String ufContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.UF"));
            String complementoContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.COMPLEMENTO"));
            String cepContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.CEP"));
            String tel1Contato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.TEL1"));
            String tel2Contato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.TEL2"));
            String emailContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.EMAIL"));
            String aniversarioContato = CursorClie.getString(CursorClie.getColumnIndex("CONTATO.DATA"));

            if ((nomeFantasia==null) || (nomeFantasia.equals("")) || (nomeFantasia.equals("0")) ||  (nomeFantasia.equals("null"))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_nome_fantasia);
                linearLayout.setVisibility(View.GONE);
            } else {
                NOMEFANTASIA.setText(nomeFantasia);
            }

            if ((nomeContato == null) || (nomeContato.equals("")) || nomeContato.equals("0") || nomeContato.equals("null")) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_nome_contato);
                linearLayout.setVisibility(View.GONE);
            } else {
                NOMECONTATO.setText(nomeContato);
            }

            if ((documentoContato == null) || (documentoContato.equals("")) || (documentoContato.equals("0")) || (documentoContato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_documento);
                linearLayout.setVisibility(View.GONE);
            } else {
                DOCUMENTOCONTATO.setText(nomeContato);
            }

            if ((cargoContato == null) || (cargoContato.equals(""))|| (cargoContato.equals("0")) || (cargoContato.equals("null"))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_cargo);
                linearLayout.setVisibility(View.GONE);
            } else {
                CARGOCONTATO.setText(cargoContato);
            }

            if ((enderecoContato == null) || (enderecoContato.equals("")) || (enderecoContato.equals("0")) || (enderecoContato.equals("null"))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_endereco);
                linearLayout.setVisibility(View.GONE);
            } else {
                ENDERECOCONTATO.setText(enderecoContato);
            }

            if ((bairroContato == null) || (bairroContato.equals("") || (bairroContato.equals("0")) || (bairroContato.equals("null")))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_bairro);
                linearLayout.setVisibility(View.GONE);
            } else {
                BAIRROCONTATO.setText(bairroContato);
            }
            if ((cidadeContato == null) || (cidadeContato.equals("")) || (cidadeContato.equals("0")) || (cidadeContato.equals("null"))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_cidade);
                linearLayout.setVisibility(View.GONE);
            } else {
                CIDADEBAIRRO.setText(cidadeContato);
            }

            if ((ufContato == null) || (ufContato.equals("")) || (ufContato.equals("0")) || (ufContato.equals("null"))) {
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_uf);
                linearLayout.setVisibility(View.GONE);
            } else {
                UFCONTATO.setText(ufContato);
            }

            if((complementoContato == null) || (complementoContato.equals("")) || (complementoContato.equals("0")) ||
                    (complementoContato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_complemento);
                linearLayout.setVisibility(View.GONE);
            } else {
                COMPLCONTATO.setText(complementoContato);
            }

            if((cepContato == null) ||  (cepContato.equals("")) || (cepContato.equals("0")) || (cepContato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_cep);
                linearLayout.setVisibility(View.GONE);
            } else {
                CEPCONTATO.setText(cepContato);
            }

            if((tel1Contato == null) ||  (tel1Contato.equals("")) || (tel1Contato.equals("0")) || (tel1Contato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_tel1);
                linearLayout.setVisibility(View.GONE);
            } else {
                TEL1CONTATO.setText(tel1Contato);
            }

            if((tel2Contato== null) ||  (tel2Contato.equals("")) || (tel2Contato.equals("0")) || (tel2Contato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_tel2);
                linearLayout.setVisibility(View.GONE);
            } else {
                TEL2CONTATO.setText(tel2Contato);
            }

            if((emailContato == null) ||  (emailContato.equals("")) || (emailContato.equals("0")) || (emailContato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_email);
                linearLayout.setVisibility(View.GONE);
            } else {
                EMAILCONTATO.setText(emailContato);
            }

            if((aniversarioContato == null) || (aniversarioContato.equals("")) || (aniversarioContato.equals("0")) ||
                    (aniversarioContato.equals("null"))){
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_aniversario);
                linearLayout.setVisibility(View.GONE);
            } else {
                ANIVERSARIOCONTATO.setText(aniversarioContato);
            }
            CursorClie.close();
        } catch (Exception E) {
            Toast.makeText(ctx, R.string.no_contacts_found, Toast.LENGTH_SHORT).show();
        }
        return v;
    }
}
