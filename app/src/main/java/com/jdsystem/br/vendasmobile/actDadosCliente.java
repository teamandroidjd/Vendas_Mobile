package com.jdsystem.br.vendasmobile;

import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class actDadosCliente extends AppCompatActivity {

    private String CodClie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_dados_cliente);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                CodClie = params.getString("codclie");
            }
        }
        try {
            TabHost tab = (TabHost) findViewById(R.id.tabHost);
            LocalActivityManager mLam = new LocalActivityManager(this, false);
            mLam.dispatchCreate(savedInstanceState);
            tab.setup(mLam);

            TabHost.TabSpec TabMenu1 = tab.newTabSpec("dadosclie");
            TabHost.TabSpec TabMenu2 = tab.newTabSpec("contatoclie");
            TabHost.TabSpec TabMenu3 = tab.newTabSpec("mapaclie");


            TabMenu1.setIndicator("Dados");
            Intent iDadosClie = new Intent(getApplicationContext(), act_TH_dadosclie.class);
            Bundle params = new Bundle();
            //params.putString("pedidonum", Pedido);
            iDadosClie.putExtras(params);
            TabMenu1.setContent(iDadosClie);

            TabMenu2.setIndicator("Contatos");
            Intent iContClie = new Intent(getApplicationContext(), act_TH_contclie.class);
            Bundle params2 = new Bundle();
            //params2.putString("obsped", ObsPed);
            iContClie.putExtras(params2);
            TabMenu2.setContent(iContClie);

            TabMenu3.setIndicator("Mapa");
            Intent iMapa = new Intent(getApplicationContext(), act_TH_mapaclie.class);
            Bundle params3 = new Bundle();
            //params3.putString("obsped", ObsPed);
            iMapa.putExtras(params3);
            TabMenu3.setContent(iMapa);

            tab.addTab(TabMenu1);
            tab.addTab(TabMenu2);
            tab.addTab(TabMenu3);

        } catch (Exception e) {
            System.out.println("Error" + e);
        }


    }
    @Override
    public void onBackPressed() {
        Intent it = new Intent();
        it.putExtra("param_ped", true);
        setResult(1, it);
        finish();
    }

}

