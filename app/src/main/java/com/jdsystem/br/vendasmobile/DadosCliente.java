package com.jdsystem.br.vendasmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.jdsystem.br.vendasmobile.Util.SlidingTabLayout;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterViewPager;


public class DadosCliente extends AppCompatActivity {

    String CodCliente;
    Toolbar toolbar;
    ViewPager pager;
    ListAdapterViewPager adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Dados", "Contatos", "obs", "Pedidos"};
    int Numboftabs = 4;
    private String DocClie;
    private String sCodVend;
    private String URLPrincipal;
    private String usuario;
    private String senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_dados_cliente);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adapter = new ListAdapterViewPager(getSupportFragmentManager(), Titles, Numboftabs);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.ColorFundo);
            }
        });

        tabs.setViewPager(pager);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                //DocClie = params.getString("documento");
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getString(getString(R.string.intent_codcliente));
                String nomeCliente = params.getString(getString(R.string.intent_nomerazao));
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_act_dados_cliente, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*Intent intent1 = new Intent(DadosCliente.this, ConsultaClientes.class);
        Bundle params1 = new Bundle();
        params1.putString(getString(R.string.intent_codvendedor), sCodVend);
        params1.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params1.putString(getString(R.string.intent_usuario), usuario);
        params1.putString(getString(R.string.intent_senha), senha);
        intent1.putExtras(params1);
        startActivity(intent1);*/
        finish();
    }

}


