package com.jdsystem.br.vendasmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;

public class DadosProduto extends AppCompatActivity {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    String codVendedor, URLPrincipal, usuario, senha;
    SQLiteDatabase DB;
    int idPerfil, sCodProduto;
    private Context ctx;
    private TextView TAG_CODMANUAL;
    private TextView TAG_DESCRICAO;
    private TextView TAG_UNIVENDA;
    private TextView TAG_TAB1;
    private TextView TAG_VLVENDA1;
    private TextView TAG_TAB2;
    private TextView TAG_VLVENDA2;
    private TextView TAG_TAB3;
    private TextView TAG_VLVENDA3;
    private TextView TAG_TAB4;
    private TextView TAG_VLVENDA4;
    private TextView TAG_TAB5;
    private TextView TAG_VLVENDA5;
    private TextView TAG_TAB6;
    private TextView TAG_VLVENDAP1;
    private TextView TAG_TAB7;
    private TextView TAG_VLVENDAP2;
    private TextView TAG_MARCA;
    private TextView TAG_CLASSE;
    private TextView TAG_FABRICANTE;
    private TextView TAG_FORNECEDOR;
    private TextView TAG_APRESENTACAO;
    private TextView TAG_ATIVO;
    private TextView TAG_QTDESTOQUE;
    private TextView TAG_QTDMINVENDA;
    private RelativeLayout TAG_LINEAR1, TAG_LINEAR2, TAG_LINEAR3, TAG_LINEAR4, TAG_LINEAR5, TAG_LINEAR6, TAG_LINEAR7;
    private LinearLayout TAG_LINEARESTOQUE, TAG_LINEARQTDMINVEND;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados_produtos);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }


        carregarpreferencias();
        declaraobjetos();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodProduto = params.getInt(getString(R.string.intent_codproduto));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }
        carregarprodutos();

    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
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
        TAG_QTDMINVENDA = (TextView) findViewById(R.id.txvqtdminvend);
        TAG_LINEARQTDMINVEND = (LinearLayout) findViewById(R.id.layqtdminvend);

        TextView TAG_VLVENDAPADRAO = (TextView) findViewById(R.id.txtprecopadrao);
        TextView TAG_TABPADRAO = (TextView) findViewById(R.id.txttabpadrao);

    }

    public void carregarprodutos() {
        try {
            Cursor CursorProd = DB.rawQuery("SELECT CODITEMANUAL, CODIGOITEM, CODPERFIL,QTDMINVEND, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA," +
                    "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2,QTDESTPROD, " +
                    "ATIVO, APRESENTACAO FROM ITENS WHERE CODIGOITEM = " + (sCodProduto) + " AND CODPERFIL = " + idPerfil, null);

            Cursor CursorParametro = DB.rawQuery(" SELECT DESCRICAOTAB1, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6, DESCRICAOTAB7,TIPOCRITICQTDITEM FROM PARAMAPP WHERE CODPERFIL =" + idPerfil, null);
            CursorParametro.moveToFirst();
            String tab1 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB1"));
            String tab2 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB2"));
            String tab3 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB3"));
            String tab4 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB4"));
            String tab5 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB5"));
            String tab6 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB6"));
            String tab7 = CursorParametro.getString(CursorParametro.getColumnIndex("DESCRICAOTAB7"));
            String TipoEstoque = CursorParametro.getString(CursorParametro.getColumnIndex("TIPOCRITICQTDITEM"));


            if (CursorProd.getCount() > 0 && CursorParametro.getCount() > 0) {
                CursorProd.moveToFirst();
                do {

                    TAG_CODMANUAL.setText(CursorProd.getString(CursorProd.getColumnIndex("CODITEMANUAL")));
                    TAG_DESCRICAO.setText(CursorProd.getString(CursorProd.getColumnIndex("DESCRICAO")));
                    TAG_UNIVENDA.setText("Unidade de medida: " + CursorProd.getString(CursorProd.getColumnIndex("UNIVENDA")));
                    TAG_APRESENTACAO.setText("Apresentação: " + CursorProd.getString(CursorProd.getColumnIndex("APRESENTACAO")));
                    String QtdEstoque = CursorProd.getString(CursorProd.getColumnIndex("QTDESTPROD"));
                    String Status = CursorProd.getString(CursorProd.getColumnIndex("ATIVO"));
                    String SituProd = null;
                    if (Status.equals("S")) {
                        SituProd = "Ativo";
                    } else {
                        SituProd = "Inativo";
                    }
                    TAG_ATIVO.setText("Status: " + SituProd);

                   /* String Precopadrao = CursorProd.getString(CursorProd.getColumnIndex("VENDAPADRAO"));
                    Precopadrao = Precopadrao.trim();
                    BigDecimal vendapadrao = new BigDecimal(Double.parseDouble(Precopadrao.replace(',', '.')));
                    TAG_VLVENDAPADRAO.setText(vendapadrao.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    TAG_TABPADRAO.setText("Tabela Padrão");*/

                    if (!tab1.equals("")) {
                        String Preco = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA1"));
                        Preco = Preco.trim();
                        BigDecimal venda = new BigDecimal(Double.parseDouble(Preco.replace(',', '.')));
                        TAG_VLVENDA1.setText(venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                        TAG_TAB1.setText(tab1);
                    } else {
                        TAG_LINEAR1.setVisibility(View.GONE);
                    }
                    if (!tab2.equals("")) {
                        String PrecoAuxA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA2"));
                        PrecoAuxA = PrecoAuxA.trim();
                        BigDecimal vendaAuxA = new BigDecimal(Double.parseDouble(PrecoAuxA.replace(',', '.')));
                        TAG_TAB2.setText(tab2);
                        TAG_VLVENDA2.setText(vendaAuxA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR2.setVisibility(View.GONE);
                    }

                    if (!tab3.equals("")) {
                        String PrecoAuxB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA3"));
                        PrecoAuxB = PrecoAuxB.trim();
                        BigDecimal vendaAuxb = new BigDecimal(Double.parseDouble(PrecoAuxB.replace(',', '.')));
                        TAG_TAB3.setText(tab3);
                        TAG_VLVENDA3.setText(vendaAuxb.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR3.setVisibility(View.GONE);
                    }

                    if (!tab4.equals("")) {
                        String PrecoAuxC = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA4"));
                        PrecoAuxC = PrecoAuxC.trim();
                        BigDecimal vendaAuxC = new BigDecimal(Double.parseDouble(PrecoAuxC.replace(',', '.')));
                        TAG_TAB4.setText(tab4);
                        TAG_VLVENDA4.setText(vendaAuxC.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR4.setVisibility(View.GONE);
                    }

                    if (!tab5.equals("")) {
                        String PrecoAuxD = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA5"));
                        PrecoAuxD = PrecoAuxD.trim();
                        BigDecimal vendaAuxD = new BigDecimal(Double.parseDouble(PrecoAuxD.replace(',', '.')));
                        TAG_TAB5.setText(tab5);
                        TAG_VLVENDA5.setText(vendaAuxD.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR5.setVisibility(View.GONE);
                    }


                    if (!tab6.equals("")) {
                        String PrecoPromoA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP1"));
                        PrecoPromoA = PrecoPromoA.trim();
                        BigDecimal vendaPromoA = new BigDecimal(Double.parseDouble(PrecoPromoA.replace(',', '.')));
                        TAG_TAB6.setText(tab6);
                        TAG_VLVENDAP1.setText(vendaPromoA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));
                    } else {
                        TAG_LINEAR6.setVisibility(View.GONE);
                    }

                    if (!tab7.equals("")) {
                        String PrecoPromoB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP2"));
                        PrecoPromoB = PrecoPromoB.trim();
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
                    Double qtdminvenda = CursorProd.getDouble(CursorProd.getColumnIndex("QTDMINVEND"));
                    if(qtdminvenda > 0){
                        TAG_QTDMINVENDA.setText(CursorProd.getString(CursorProd.getColumnIndex("QTDMINVEND")));
                    }else {
                        TAG_LINEARQTDMINVEND.setVisibility(View.GONE);
                    }
                }
                while (CursorProd.moveToNext());
                CursorProd.close();
                CursorParametro.close();
            }
        } catch (Exception E) {
            E.toString();
        }
    }

    @Override
    public void onBackPressed() {
        /*Intent intent = new Intent(DadosProduto.this, ConsultaProdutos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), codVendedor);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        intent.putExtras(params);
        startActivity(intent);*/
        finish();

    }
}

