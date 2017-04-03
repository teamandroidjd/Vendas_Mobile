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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class actDadosProdutos extends AppCompatActivity {

    String sCodProduto;
    SQLiteDatabase DB;
    private Context ctx;
    private TextView TAG_CODMANUAL, TAG_DESCRICAO, TAG_UNIVENDA, TAG_TAB1, TAG_VLVENDA1, TAG_TAB2, TAG_VLVENDA2, TAG_TAB3, TAG_VLVENDA3, TAG_TAB4, TAG_VLVENDA4,
            TAG_TAB5, TAG_VLVENDA5, TAG_TAB6, TAG_VLVENDAP1, TAG_TAB7, TAG_VLVENDAP2, TAG_MARCA, TAG_CLASSE, TAG_FABRICANTE, TAG_FORNECEDOR, TAG_APRESENTACAO,
            TAG_ATIVO, TAG_QTDESTOQUE, TAG_VLVENDAPADRAO, TAG_TABPADRAO;
    private RelativeLayout TAG_LINEAR1, TAG_LINEAR2, TAG_LINEAR3, TAG_LINEAR4, TAG_LINEAR5, TAG_LINEAR6, TAG_LINEAR7;
    private LinearLayout TAG_LINEARESTOQUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados_produtos);

        declaraobjetos();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodProduto = (String) params.getString(getString(R.string.intent_codproduto));
            }
        }
        carregarprodutos();
    }

    private void declaraobjetos() {
        DB = new ConfigDB(this).getReadableDatabase();

        TAG_CODMANUAL = (TextView) findViewById(R.id.txt_codprod);
        TAG_DESCRICAO = (TextView) findViewById(R.id.txt_descricao);
        TAG_UNIVENDA = (TextView) findViewById(R.id.txtunvenda);
        TAG_TAB1 = (TextView) findViewById(R.id.txttab1); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR1 = (RelativeLayout) findViewById(R.id.lnrtabab1); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDA1 = (TextView) findViewById(R.id.txtpreco); // PREÇO DA TABELA
        TAG_TAB2 = (TextView) findViewById(R.id.txttab2); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR2 = (RelativeLayout) findViewById(R.id.lnrtabab2); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDA2 = (TextView) findViewById(R.id.txtprecoauxiliara);
        TAG_TAB3 = (TextView) findViewById(R.id.txttab3); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR3 = (RelativeLayout) findViewById(R.id.lnrtabab3); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDA3 = (TextView) findViewById(R.id.txtprecoauxiliarb);
        TAG_TAB4 = (TextView) findViewById(R.id.txttab4); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR4 = (RelativeLayout) findViewById(R.id.lnrtabab4); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDA4 = (TextView) findViewById(R.id.txtprecoauxiliarc);
        TAG_TAB5 = (TextView) findViewById(R.id.txttab5); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR5 = (RelativeLayout) findViewById(R.id.lnrtabab5); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDA5 = (TextView) findViewById(R.id.txtprecoauxiliard);
        TAG_TAB6 = (TextView) findViewById(R.id.txttab6); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR6 = (RelativeLayout) findViewById(R.id.lnrtabab6); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDAP1 = (TextView) findViewById(R.id.txtprecopromocaoa);
        TAG_TAB7 = (TextView) findViewById(R.id.txttab7); // DESCRIÇÃO DA TABELA DE PREÇO
        TAG_LINEAR7 = (RelativeLayout) findViewById(R.id.lnrtabab7); // DESCRIÇÃO DO SIMBOLO R$
        TAG_VLVENDAP2 = (TextView) findViewById(R.id.txtprecopromocaob);
        TAG_MARCA = (TextView) findViewById(R.id.txtmarca);
        TAG_CLASSE = (TextView) findViewById(R.id.txtclasse);
        TAG_FABRICANTE = (TextView) findViewById(R.id.txtfabricante);
        TAG_FORNECEDOR = (TextView) findViewById(R.id.txtforncedor);
        TAG_APRESENTACAO = (TextView) findViewById(R.id.txtapres);
        TAG_ATIVO = (TextView) findViewById(R.id.txtStatus);
        TAG_LINEARESTOQUE = (LinearLayout) findViewById(R.id.lnrtaEstoque);
        TAG_QTDESTOQUE = (TextView) findViewById(R.id.txt_qtdestoque);
        TAG_VLVENDAPADRAO = (TextView) findViewById(R.id.txtprecopadrao);
        TAG_TABPADRAO = (TextView) findViewById(R.id.txttabpadrao);

    }

    public void carregarprodutos(){
        try {
            Cursor CursorProd = DB.rawQuery("SELECT CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA," +
                    "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2,QTDESTPROD,VENDAPADRAO, " +
                    "ATIVO, APRESENTACAO FROM ITENS WHERE CODITEMANUAL = '" + (sCodProduto) + "'", null);

            Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7,TIPOCRITICQTDITEM FROM PARAMAPP", null);
            CursorParametro.moveToFirst();
            String tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
            String tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
            String tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
            String tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
            String tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
            String tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
            String tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
            String TipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));
            CursorParametro.close();

            if (CursorProd.getCount() > 0) {
                CursorProd.moveToFirst();
                do {

                    TAG_CODMANUAL.setText(CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL")));
                    TAG_DESCRICAO.setText(CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO")));
                    TAG_UNIVENDA.setText("Unidade de medida: " + CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA")));
                    TAG_APRESENTACAO.setText("Apresentação: " + CursorProd.getString(CursorProd.getColumnIndex("APRESENTACAO")));
                    String QtdEstoque = CursorProd.getString(CursorProd.getColumnIndex("QTDESTPROD"));
                    String Status = CursorProd.getString(CursorProd.getColumnIndex("ATIVO"));
                    String SituProd = null;
                    if (Status.equals("1")) {
                        SituProd = "Ativo";
                    } else {
                        SituProd = "Inativo";
                    }
                    TAG_ATIVO.setText("Status: " + SituProd);

                    String Precopadrao = CursorProd.getString(CursorProd.getColumnIndex("VENDAPADRAO"));
                    Precopadrao = Precopadrao.trim();
                    BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(Precopadrao.replace(',', '.')));
                    TAG_VLVENDAPADRAO.setText(vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    TAG_TABPADRAO.setText("Tabela Padrão");


                    String Preco = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));
                    Preco = Preco.trim();
                    if (!Preco.equals("0,0000")) {
                        BigDecimal venda = new BigDecimal(Double.parseDouble(Preco.replace(',', '.')));
                        TAG_VLVENDA1.setText(venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        TAG_TAB1.setText(tab1);
                    } else {
                        TAG_LINEAR1.setVisibility(View.GONE);
                    }

                    String PrecoAuxA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA2"));
                    PrecoAuxA = PrecoAuxA.trim();
                    if (!PrecoAuxA.equals("0,0000")) {
                        BigDecimal vendaAuxA = new BigDecimal(Double.parseDouble(PrecoAuxA.replace(',', '.')));
                        TAG_TAB2.setText(tab2);
                        TAG_VLVENDA2.setText(vendaAuxA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR2.setVisibility(View.GONE);
                    }

                    String PrecoAuxB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA3"));
                    PrecoAuxB = PrecoAuxB.trim();
                    if (!PrecoAuxB.equals("0,0000")) {
                        BigDecimal vendaAuxb = new BigDecimal(Double.parseDouble(PrecoAuxB.replace(',', '.')));
                        TAG_TAB3.setText(tab3);
                        TAG_VLVENDA3.setText(vendaAuxb.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR3.setVisibility(View.GONE);
                    }

                    String PrecoAuxC = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA4"));
                    PrecoAuxC = PrecoAuxC.trim();
                    if (!PrecoAuxC.equals("0,0000")) {
                        BigDecimal vendaAuxC = new BigDecimal(Double.parseDouble(PrecoAuxC.replace(',', '.')));
                        TAG_TAB4.setText(tab4);
                        TAG_VLVENDA4.setText(vendaAuxC.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR4.setVisibility(View.GONE);
                    }

                    String PrecoAuxD = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA5"));
                    PrecoAuxD = PrecoAuxD.trim();
                    if (!PrecoAuxD.equals("0,0000")) {
                        BigDecimal vendaAuxD = new BigDecimal(Double.parseDouble(PrecoAuxD.replace(',', '.')));
                        TAG_TAB5.setText(tab5);
                        TAG_VLVENDA5.setText(vendaAuxD.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR5.setVisibility(View.GONE);
                    }

                    String PrecoPromoA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP1"));
                    PrecoPromoA = PrecoPromoA.trim();
                    if (!PrecoPromoA.equals("0,0000")) {
                        BigDecimal vendaPromoA = new BigDecimal(Double.parseDouble(PrecoPromoA.replace(',', '.')));
                        TAG_TAB6.setText(tab6);
                        TAG_VLVENDAP1.setText(vendaPromoA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR6.setVisibility(View.GONE);
                    }

                    String PrecoPromoB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP2"));
                    PrecoPromoB = PrecoPromoB.trim();
                    if (!PrecoPromoB.equals("0,0000")) {
                        BigDecimal vendaPromoB = new BigDecimal(Double.parseDouble(PrecoPromoB.replace(',', '.')));
                        TAG_TAB7.setText(tab7);
                        TAG_VLVENDAP2.setText(vendaPromoB.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR7.setVisibility(View.GONE);
                    }

                    TAG_FABRICANTE.setText("Fabricante: " + CursorProd.getString(CursorProd.getColumnIndex("FABRICANTE")));
                    TAG_FORNECEDOR.setText("Fornecedor: " + CursorProd.getString(CursorProd.getColumnIndex("FORNECEDOR")));
                    TAG_CLASSE.setText("Classe: " + CursorProd.getString(CursorProd.getColumnIndex("CLASSE")));
                    TAG_MARCA.setText("Marca: " + CursorProd.getString(CursorProd.getColumnIndex("MARCA")));

                    String QtdEstoqueNegativo = QtdEstoque.replaceAll("[1234567890,]", "").trim();
                    if (TipoEstoque.equals("Q") || TipoEstoque.equals("")) {
                        TAG_QTDESTOQUE.setText(CursorProd.getString(CursorProd.getColumnIndex("QTDESTPROD")));
                    }
                    if (TipoEstoque.equals("D")) {
                        if (QtdEstoque.equals("0,0000") || QtdEstoqueNegativo.equals("-")) {
                            TAG_QTDESTOQUE.setText("Indisponível");
                        } else {
                            TAG_QTDESTOQUE.setText("Disponível");
                        }
                    }
                    if (TipoEstoque.equals("N")) {
                        TAG_LINEARESTOQUE.setVisibility(View.GONE);
                    }
                }
                while (CursorProd.moveToNext());
                CursorProd.close();
            }
        } catch (Exception E) {
            E.toString();
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }
}

