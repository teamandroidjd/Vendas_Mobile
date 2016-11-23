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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class actDadosProdutos extends AppCompatActivity {

    String CodManual;
    String sCodVend;
    String URLPrincipal;
    int position;

    SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dados_produtos);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DB = openOrCreateDatabase("WSGEDB", Context.MODE_PRIVATE, null);
        ConfigDB.ConectarBanco(DB);

        TextView TAG_CODMANUAL = (TextView) findViewById(R.id.txt_codprod);
        TextView TAG_DESCRICAO = (TextView) findViewById(R.id.txt_descricao);
        TextView TAG_UNIVENDA = (TextView) findViewById(R.id.txtunvenda);
        TextView TAG_VLVENDA1 = (TextView) findViewById(R.id.txtpreco);
        TextView TAG_VLVENDA2 = (TextView) findViewById(R.id.txtpreco2);
        TextView TAG_VLVENDA3 = (TextView) findViewById(R.id.txtpreco3);
        TextView TAG_VLVENDA4 = (TextView) findViewById(R.id.txtpreco4);
        TextView TAG_VLVENDA5 = (TextView) findViewById(R.id.txtpreco5);
        TextView TAG_VLVENDAP1 = (TextView) findViewById(R.id.txtprecop1);
        TextView TAG_VLVENDAP2 = (TextView) findViewById(R.id.txtprecop2);
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
                position = (int) params.getLong("position");
            }
        }

        /*helpher = new DatabaseHelpher(this);
        dbList = new ArrayList<DatabaseModel>();
        dbList = helpher.getDataFromDB();

        if (dbList.size() > 0) {
            String name = dbList.get(position).getName();
            String email = dbList.get(position).getEmail();
            String roll = dbList.get(position).getRoll();
            String address = dbList.get(position).getAddress();
            String branch = dbList.get(position).getBranch();
            tvname.setText(name);
            tvemail.setText(email);
            tvroll.setText(roll);
            tvaddress.setText(address);
            tvbranch.setText(branch);
        }*/

    }




}

