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


        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                sCodProduto = (String) params.getString("codproduto");
            }
            try {
                Cursor CursorProd = DB.rawQuery("SELECT CODITEMANUAL, DESCRICAO, FABRICANTE, FORNECEDOR, CLASSE, MARCA, UNIVENDA," +
                        "VLVENDA1, VLVENDA2, VLVENDA3, VLVENDA4, VLVENDA5, VLVENDAP1, VLVENDAP2,VENDAPADRAO, " +
                        "ATIVO, APRESENTACAO FROM ITENS WHERE CODITEMANUAL = '" + (sCodProduto) + "'", null);

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
                        BigDecimal venda = new BigDecimal(Double.parseDouble(Preco.replace(',', '.')));
                        TAG_VLVENDA1.setText("Tabela base:      R$ " + venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoAuxA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA2"));
                        BigDecimal vendaAuxA = new BigDecimal(Double.parseDouble(PrecoAuxA.replace(',', '.')));
                        TAG_VLVENDA2.setText("Auxiliar A:           R$ " + vendaAuxA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoAuxB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA3"));
                        BigDecimal vendaAuxb = new BigDecimal(Double.parseDouble(PrecoAuxB.replace(',', '.')));
                        TAG_VLVENDA3.setText("Auxiliar B:           R$ " + vendaAuxb.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoAuxC = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA4"));
                        BigDecimal vendaAuxC = new BigDecimal(Double.parseDouble(PrecoAuxC.replace(',', '.')));
                        TAG_VLVENDA4.setText("Auxiliar C:           R$ " + vendaAuxC.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoAuxD = CursorProd.getString(CursorProd.getColumnIndex("VLVENDA5"));
                        BigDecimal vendaAuxD = new BigDecimal(Double.parseDouble(PrecoAuxD.replace(',', '.')));
                        TAG_VLVENDA5.setText("Auxiliar D:           R$ " + vendaAuxD.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoPromoA = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP1"));
                        BigDecimal vendaPromoA = new BigDecimal(Double.parseDouble(PrecoPromoA.replace(',', '.')));
                        TAG_VLVENDAP1.setText("Promocional A:  R$ " + vendaPromoA.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        String PrecoPromoB = CursorProd.getString(CursorProd.getColumnIndex("VLVENDAP2"));
                        BigDecimal vendaPromoB = new BigDecimal(Double.parseDouble(PrecoPromoB.replace(',', '.')));
                        TAG_VLVENDAP2.setText("Promocional B:  R$ " + vendaPromoB.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));

                        TAG_FABRICANTE.setText("Fabricante: " + CursorProd.getString(CursorProd.getColumnIndex("FABRICANTE")));
                        TAG_FORNECEDOR.setText("Fornecedor: " + CursorProd.getString(CursorProd.getColumnIndex("FORNECEDOR")));
                        TAG_CLASSE.setText("Classe: " + CursorProd.getString(CursorProd.getColumnIndex("CLASSE")));
                        TAG_MARCA.setText("Marca: " + CursorProd.getString(CursorProd.getColumnIndex("MARCA")));
                    }
                    while (CursorProd.moveToNext());
                    CursorProd.close();
                }
            }catch (Exception E){
                E.toString();
            }

        }
    }
    @Override
    public void onBackPressed() {

        finish();
    }
}

