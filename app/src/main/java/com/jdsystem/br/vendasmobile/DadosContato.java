package com.jdsystem.br.vendasmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.jdsystem.br.vendasmobile.Util.SlidingTabLayout;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterDadosContatos;

/**
 * Created by WKS22 on 27/03/2017.
 */

public class DadosContato extends AppCompatActivity {

    int CodCliente;
    int CodContato;
    Toolbar toolbar;
    ViewPager pager;
    ListAdapterDadosContatos adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Dados", "Obs.", "Produtos", "Horários"};
    int Numboftabs = 4;
    private String sCodVend;
    private String URLPrincipal;
    private String usuario;
    private String senha;
    String telaInvocada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_dados_contatos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ListAdapterDadosContatos(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager_cont);
        pager.setAdapter(adapter);


        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs_cont);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.ColorFundo);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                String docClie = params.getString("documento");
                sCodVend = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                String nomeCliente = params.getString(getString(R.string.intent_nomerazao));
                CodContato = params.getInt(getString(R.string.intent_codcontato));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
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
        /*Intent intent = new Intent(DadosContato.this, ConsultaContatos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codvendedor), sCodVend);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putInt("codCliente", CodCliente);
        params.putInt(getString(R.string.intent_codcontato), CodContato);
        params.putString(getString(R.string.intent_telainvocada), telaInvocada);
        intent.putExtras(params);
        startActivity(intent);*/
        finish();
    }

}



