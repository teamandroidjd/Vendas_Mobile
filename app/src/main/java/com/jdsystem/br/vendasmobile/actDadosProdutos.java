package com.jdsystem.br.vendasmobile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class actDadosProdutos extends AppCompatActivity {

    String sCodProduto;
    SQLiteDatabase DB;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados_produtos);

        DB = new ConfigDB(this).getReadableDatabase();

        TextView TAG_CODMANUAL = (TextView) findViewById(R.id.txt_codprod);
        TextView TAG_DESCRICAO = (TextView) findViewById(R.id.txt_descricao);
        TextView TAG_UNIVENDA = (TextView) findViewById(R.id.txtunvenda);
        TextView TAG_VLVENDA1 = (TextView) findViewById(R.id.txtpreco);
        TextView TAG_VLVENDA2 = (TextView) findViewById(R.id.txtprecoauxiliara);
        TextView TAG_VLVENDA3 = (TextView) findViewById(R.id.txtprecoauxiliarb);
        TextView TAG_VLVENDA4 = (TextView) findViewById(R.id.txtprecoauxiliarc);
        TextView TAG_VLVENDA5 = (TextView) findViewById(R.id.txtprecoauxiliard);
        TextView TAG_VLVENDAP1 = (TextView) findViewById(R.id.txtprecopromocaoa);
        TextView TAG_VLVENDAP2 = (TextView) findViewById(R.id.txtprecopromocaob);
        TextView TAG_MARCA = (TextView) findViewById(R.id.txtmarca);
        TextView TAG_CLASSE = (TextView) findViewById(R.id.txtclasse);
        TextView TAG_FABRICANTE = (TextView) findViewById(R.id.txtfabricante);
        TextView TAG_FORNECEDOR = (TextView) findViewById(R.id.txtforncedor);
        TextView TAG_APRESENTACAO = (TextView) findViewById(R.id.txtapres);
        TextView TAG_ATIVO = (TextView) findViewById(R.id.txtStatus);
        TextView TAG_QTDESTOQUE = (TextView) findViewById(R.id.txt_qtdestoque);


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodProduto = (String) params.getString("codproduto");
            }
            try {
                Cursor CursorProd = DB.rawQuery("SELECT CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA," +
                        "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2,QTDESTPROD,VENDAPADRAO, " +
                        "ATIVO, APRESENTACAO FROM ITENS WHERE CODITEMANUAL = '" + (sCodProduto) + "'", null);

                Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7 FROM PARAMAPP", null);
                CursorParametro.moveToFirst();
                String tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
                String tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
                String tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
                String tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
                String tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
                String tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
                String tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
                CursorParametro.close();

                if (CursorProd.getCount() > 0) {
                    CursorProd.moveToFirst();
                    do {

                        TAG_CODMANUAL.setText(CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL")));
                        TAG_DESCRICAO.setText(CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO")));
                        TAG_UNIVENDA.setText("Unidade de medida: " + CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA")));
                        TAG_APRESENTACAO.setText("Apresentação: " + CursorProd.getString(CursorProd.getColumnIndex("APRESENTACAO")));
                        String Status = CursorProd.getString(CursorProd.getColumnIndex("ATIVO"));
                        String SituProd = null;
                        if (Status.equals("1")) {
                            SituProd = "Ativo";
                        } else {
                            SituProd = "Inativo";
                        }
                        TAG_ATIVO.setText("Status: " + SituProd);

                        String Preco = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));
                        if (!Preco.equals("0,0000")) {
                            BigDecimal venda = new BigDecimal(Double.parseDouble(Preco.replace(',', '.')));
                            TAG_VLVENDA1.setText(tab1 + " R$: " + venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        } else {
                            TAG_VLVENDA1.setVisibility(View.GONE);
                        }

                        String PrecoAuxA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA2"));
                        if (!PrecoAuxA.equals("0,0000")) {
                            BigDecimal vendaAuxA = new BigDecimal(Double.parseDouble(PrecoAuxA.replace(',', '.')));
                            TAG_VLVENDA2.setText(tab2 + " R$: " + vendaAuxA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        }else {
                            TAG_VLVENDA2.setVisibility(View.GONE);
                        }

                        String PrecoAuxB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA3"));
                        if(!PrecoAuxB.equals("0,0000")) {
                            BigDecimal vendaAuxb = new BigDecimal(Double.parseDouble(PrecoAuxB.replace(',', '.')));
                            TAG_VLVENDA3.setText(tab3 + " R$: " + vendaAuxb.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        }else {
                            TAG_VLVENDA3.setVisibility(View.GONE);
                        }

                        String PrecoAuxC = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA4"));
                        if(!PrecoAuxC.equals("0,0000")) {
                            BigDecimal vendaAuxC = new BigDecimal(Double.parseDouble(PrecoAuxC.replace(',', '.')));
                            TAG_VLVENDA4.setText(tab4 + " R$: " + vendaAuxC.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        }else{
                            TAG_VLVENDA4.setVisibility(View.GONE);
                        }

                        String PrecoAuxD = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA5"));
                        if(!PrecoAuxD.equals("0,0000")) {
                            BigDecimal vendaAuxD = new BigDecimal(Double.parseDouble(PrecoAuxD.replace(',', '.')));
                            TAG_VLVENDA5.setText(tab5 + " R$: " + vendaAuxD.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        }else {
                            TAG_VLVENDA5.setVisibility(View.GONE);
                        }

                        String PrecoPromoA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP1"));
                        if(!PrecoPromoA.equals("0,0000")) {
                            BigDecimal vendaPromoA = new BigDecimal(Double.parseDouble(PrecoPromoA.replace(',', '.')));
                            TAG_VLVENDAP1.setText(tab6 + " R$: " + vendaPromoA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        }else {
                            TAG_VLVENDAP1.setVisibility(View.GONE);
                        }

                        String PrecoPromoB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP2"));
                        if(!PrecoPromoB.equals("0,0000")) {
                            BigDecimal vendaPromoB = new BigDecimal(Double.parseDouble(PrecoPromoB.replace(',', '.')));
                            TAG_VLVENDAP2.setText(tab7 + " R$: " + vendaPromoB.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        } else {
                            TAG_VLVENDAP2.setVisibility(View.GONE);
                        }

                        TAG_FABRICANTE.setText("Fabricante: " + CursorProd.getString(CursorProd.getColumnIndex("FABRICANTE")));
                        TAG_FORNECEDOR.setText("Fornecedor: " + CursorProd.getString(CursorProd.getColumnIndex("FORNECEDOR")));
                        TAG_CLASSE.setText("Classe: " + CursorProd.getString(CursorProd.getColumnIndex("CLASSE")));
                        TAG_MARCA.setText("Marca: " + CursorProd.getString(CursorProd.getColumnIndex("MARCA")));
                        TAG_QTDESTOQUE.setText(CursorProd.getString(CursorProd.getColumnIndex("QTDESTPROD")));
                    }
                    while (CursorProd.moveToNext());
                    CursorProd.close();
                }
            } catch (Exception E) {
                E.toString();
            }

        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }
}

