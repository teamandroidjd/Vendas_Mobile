package com.jdsystem.br.vendasmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.fragments.FragmentContatos;

import java.util.ArrayList;
import java.util.List;


public class ConsultaContatos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Runnable {
    public static final String CONFIG_HOST = "CONFIG_HOST";
    private static final String NOME_USUARIO = "LOGIN_AUTOMATICO";
    public SharedPreferences prefs;
    String codVendedor, URLPrincipal, usuario, senha, UsuarioLogado, editQuery, telaInvocada;
    SQLiteDatabase DB;
    Contatos lstcontatos;
    //Contatos lstfiltrocontatos;
    ProgressDialog pDialog;
    Handler handler = new Handler();
    Toolbar toolbar;
    MenuItem searchItem;
    SearchView searchView;
    ProgressDialog Dialogo;
    int flag, idPerfil;
    private TextView txvqtdregcont;
    private EditText pesquisacliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consulta_contatos);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {

        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                telaInvocada = params.getString(getString(R.string.intent_telainvocada));
            }
        }

        pDialog = new ProgressDialog(ConsultaContatos.this);
        pDialog.setTitle(getString(R.string.wait));
        pDialog.setMessage(getString(R.string.loadingcontacts));
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread(ConsultaContatos.this);
        thread.start();

        FloatingActionButton CadContatos = (FloatingActionButton) findViewById(R.id.cadcontato);
        CadContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CadastroContatos.excluiBaseTempCadastroContatos(ConsultaContatos.this);
                CadastroContatos.excluiBaseTempContatos(ConsultaContatos.this);
                CadastroContatos.excluiProdutosContatosTemp(ConsultaContatos.this);
                Intent i = new Intent(ConsultaContatos.this, CadastroContatos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                i.putExtras(params);
                startActivity(i);
                finish();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(ConsultaContatos.this);

        carregausuariologado();
        carregarpreferencias();
        carregarobjetos();


    }

    private void carregarobjetos() {
        txvqtdregcont = (TextView) findViewById(R.id.txvqtdregistrocont);
    }

    private void carregarpreferencias() {
        prefs = getSharedPreferences(CONFIG_HOST, MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    private void carregausuariologado() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(ConsultaContatos.this);
        View header = navigationView.getHeaderView(0);
        TextView usuariologado = (TextView) header.findViewById(R.id.lblUsuarioLogado);
        SharedPreferences prefs = getSharedPreferences(NOME_USUARIO, MODE_PRIVATE);
        UsuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
        if (UsuarioLogado != null) {
            UsuarioLogado = prefs.getString(getString(R.string.intent_usuario), null);
            usuariologado.setText("Olá " + UsuarioLogado + "!");
        } else {
            usuariologado.setText("Olá " + usuario + "!");
        }
    }

    @Override
    public void onBackPressed() {
        switch (flag) {
            case 0:
                Intent intent = new Intent(ConsultaContatos.this, ConsultaPedidos.class);
                Bundle params = new Bundle();
                params.putString(getString(R.string.intent_codvendedor), codVendedor);
                params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params.putString(getString(R.string.intent_usuario), usuario);
                params.putString(getString(R.string.intent_senha), senha);
                params.putInt(getString(R.string.intent_flag), flag);
                intent.putExtras(params);
                startActivity(intent);
                finish();
                break;
            case 1:
                Intent intent1 = new Intent(ConsultaContatos.this, ConsultaContatos.class);
                Bundle params1 = new Bundle();
                params1.putString(getString(R.string.intent_codvendedor), codVendedor);
                params1.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                params1.putString(getString(R.string.intent_usuario), usuario);
                params1.putString(getString(R.string.intent_senha), senha);
                intent1.putExtras(params1);
                startActivity(intent1);
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_contatos, menu);
        searchItem = menu.findItem(R.id.action_searchable_activity);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Dialogo = new ProgressDialog(ConsultaContatos.this);
                Dialogo.setIndeterminate(true);
                Dialogo.setTitle(getString(R.string.wait));
                Dialogo.setMessage(getString(R.string.searchingcontacts));
                Dialogo.setCancelable(false);
                Dialogo.setProgress(0);
                Dialogo.show();

                query.toString();
                editQuery = query;
                searchView.setQuery("", false);
                // searchView.clearFocus();


                Thread thread = new Thread(ConsultaContatos.this);
                thread.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                // flag = 1;
                editQuery = null;
                searchView.onActionViewCollapsed();
                Thread thread = new Thread(ConsultaContatos.this);
                thread.start();

                return true;
            }
        });
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

    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clientes) {
            Intent intent = new Intent(ConsultaContatos.this, ConsultaClientes.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_produtos) {
            Intent iprod = new Intent(ConsultaContatos.this, ConsultaProdutos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            iprod.putExtras(params);
            startActivity(iprod);
            finish();

        } else if (id == R.id.nav_pedidos) {
            Intent iped = new Intent(ConsultaContatos.this, ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            iped.putExtras(params);
            startActivity(iped);
            finish();

        } else if (id == R.id.nav_contatos) {

        } else if (id == R.id.nav_agenda) {
            Intent i = new Intent(ConsultaContatos.this, ConsultaAgenda.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            i.putExtras(params);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_sincronismo) {
            Intent isinc = new Intent(ConsultaContatos.this, Sincronismo.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            isinc.putExtras(params);
            startActivity(isinc);
            finish();
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(ConsultaContatos.this, Login.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.exit(1);
            finish();
        } else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(ConsultaContatos.this, InfoJDSystem.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            intent.putExtras(params);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<Contatos> carregarcontatos() {
        ArrayList<Contatos> DadosListContatos = new ArrayList<Contatos>();
        if (editQuery == null) {

            DB = new ConfigDB(this).getReadableDatabase();


            try {
                Cursor cursorContatos = DB.rawQuery("SELECT CONTATO.CODCONTATO_EXT, CONTATO.CODCONTATO_INT, CONTATO.CODPERFIL, CONTATO.NOME, " +
                        "CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, CONTATO.CODCLIENTE, " +
                        "CONTATO.TEL2, CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, " +
                        "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                        "CONTATO.CODVENDEDOR, CONTATO.BAIRRO, CONTATO.TIPO, " +
                        "CLIENTES.NOMERAZAO, CONTATO.CODCIDADE, CLIENTES.CODCLIE_EXT, CONTATO.FLAGINTEGRADO " +
                        "FROM CONTATO " +
                        "LEFT OUTER JOIN CLIENTES ON CONTATO.CODCLIENTE = CLIENTES.CODCLIE_INT WHERE CONTATO.CODPERFIL = " + idPerfil + " " +
                        "ORDER BY NOME ", null);
                cursorContatos.moveToFirst();
                if (cursorContatos.getCount() > 0) {
                    txvqtdregcont.setText("Quantidade de registro: " + cursorContatos.getCount());
                    do {
                        String nome = cursorContatos.getString(cursorContatos.getColumnIndex("NOME"));
                        String cargo = cursorContatos.getString(cursorContatos.getColumnIndex("CARGO"));
                        String email = cursorContatos.getString(cursorContatos.getColumnIndex("EMAIL"));
                        String tel1 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL1"));
                        String tel2 = cursorContatos.getString(cursorContatos.getColumnIndex("TEL2"));
                        String Doc = cursorContatos.getString(cursorContatos.getColumnIndex("DOCUMENTO"));
                        String Data = cursorContatos.getString(cursorContatos.getColumnIndex("DATA"));
                        String Cep = cursorContatos.getString(cursorContatos.getColumnIndex("CEP"));
                        String Endereco = cursorContatos.getString(cursorContatos.getColumnIndex("ENDERECO"));
                        String Num = cursorContatos.getString(cursorContatos.getColumnIndex("NUMERO"));
                        String Compl = cursorContatos.getString(cursorContatos.getColumnIndex("COMPLEMENTO"));
                        String uf = cursorContatos.getString(cursorContatos.getColumnIndex("UF"));
                        String flagIntegrado = cursorContatos.getString(cursorContatos.getColumnIndex("CONTATO.FLAGINTEGRADO"));

                        int codClieExt = cursorContatos.getInt(cursorContatos.getColumnIndex("CLIENTES.CODCLIE_EXT"));
                        int codCliente = cursorContatos.getInt(cursorContatos.getColumnIndex("CONTATO.CODCLIENTE"));
                        String nomeCliente;
                        String tipoContato = cursorContatos.getString(cursorContatos.getColumnIndex("CONTATO.TIPO"));
                        if (tipoContato == null) {
                            nomeCliente = cursorContatos.getString(cursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));
                        } else if (tipoContato.equals("O")) {
                            nomeCliente = "OUTROS";
                        } else {
                            nomeCliente = cursorContatos.getString(cursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));
                        }
                        int codContato = cursorContatos.getInt(cursorContatos.getColumnIndex("CONTATO.CODCONTATO_INT"));
                        String codContatoExt = cursorContatos.getString(cursorContatos.getColumnIndex("CONTATO.CODCONTATO_EXT"));

                        lstcontatos = new Contatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null,
                                0, codCliente, codClieExt, nomeCliente, codContato, flagIntegrado, codContatoExt);
                        DadosListContatos.add(lstcontatos);
                    } while (cursorContatos.moveToNext());
                    cursorContatos.close();

                } else {
                    txvqtdregcont.setText("Quantidade de Registro: 0");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        Util.msg_toast_personal(ConsultaContatos.this, getString(R.string.no_contacts_found), Toast.LENGTH_SHORT);
                    else
                        Toast.makeText(this, getString(R.string.no_contacts_found), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception E) {
                Toast.makeText(this, "Houve um problema ao acessar a base de dados. Favor entrar em contato com o suporte técnico JD System.", Toast.LENGTH_SHORT).show();
            }

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } else {
            Cursor CursorContatos = DB.rawQuery("SELECT CONTATO.CODCONTATO_EXT, CONTATO.CODCONTATO_EXT, CONTATO.CODPERFIL, " +
                    "CONTATO.NOME, CONTATO.CARGO, CONTATO.EMAIL, CONTATO.TEL1, CONTATO.CODCLIENTE, " +
                    "CONTATO.TEL2, CONTATO.DOCUMENTO, CONTATO.DATA, CONTATO.CEP, " +
                    "CONTATO.ENDERECO, CONTATO.NUMERO, CONTATO.COMPLEMENTO, CONTATO.UF, " +
                    "CONTATO.CODVENDEDOR, CONTATO.BAIRRO, CONTATO.TIPO, " +
                    "CLIENTES.NOMERAZAO, CONTATO.CODCIDADE, CLIENTES.CODCLIE_EXT, CONTATO.CODCONTATO_INT, CONTATO.FLAGINTEGRADO " +
                    "FROM CONTATO " +
                    "LEFT OUTER JOIN CLIENTES ON CONTATO.CODCLIENTE = CLIENTES.CODCLIE_INT " +
                    "WHERE (CONTATO.CODPERFIL = " + idPerfil + ") AND CONTATO.NOME LIKE '%" + editQuery + "%' OR CLIENTES.NOMERAZAO " +
                    "LIKE '%" + editQuery + "%'" +
                    " order by CONTATO.NOME ", null);

            CursorContatos.moveToFirst();
            if (CursorContatos.getCount() > 0) {
                txvqtdregcont.setText("Quantidade de registro: " + CursorContatos.getCount());
                do {
                    String nome = CursorContatos.getString(CursorContatos.getColumnIndex("NOME"));
                    String cargo = CursorContatos.getString(CursorContatos.getColumnIndex("CARGO"));
                    String email = CursorContatos.getString(CursorContatos.getColumnIndex("EMAIL"));
                    String tel1 = CursorContatos.getString(CursorContatos.getColumnIndex("TEL1"));
                    String tel2 = CursorContatos.getString(CursorContatos.getColumnIndex("TEL2"));
                    String Doc = CursorContatos.getString(CursorContatos.getColumnIndex("DOCUMENTO"));
                    String Data = CursorContatos.getString(CursorContatos.getColumnIndex("DATA"));
                    String Cep = CursorContatos.getString(CursorContatos.getColumnIndex("CEP"));
                    String Endereco = CursorContatos.getString(CursorContatos.getColumnIndex("ENDERECO"));
                    String Num = CursorContatos.getString(CursorContatos.getColumnIndex("NUMERO"));
                    String Compl = CursorContatos.getString(CursorContatos.getColumnIndex("COMPLEMENTO"));
                    String uf = CursorContatos.getString(CursorContatos.getColumnIndex("UF"));
                    String flagIntegrado = CursorContatos.getString(CursorContatos.getColumnIndex("CONTATO.FLAGINTEGRADO"));
                    int codClieExt = CursorContatos.getInt(CursorContatos.getColumnIndex("CLIENTES.CODCLIE_EXT"));

                    String nomeCliente;
                    String tipoContato = CursorContatos.getString(CursorContatos.getColumnIndex("CONTATO.TIPO"));
                    if (tipoContato == null) {
                        nomeCliente = CursorContatos.getString(CursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));
                    } else if (tipoContato.equals("O")) {
                        nomeCliente = "OUTROS";
                    } else {
                        nomeCliente = CursorContatos.getString(CursorContatos.getColumnIndex("CLIENTES.NOMERAZAO"));
                    }

                    int codContato = CursorContatos.getInt(CursorContatos.getColumnIndex("CONTATO.CODCONTATO_INT"));
                    int codCliente = CursorContatos.getInt(CursorContatos.getColumnIndex("CONTATO.CODCLIENTE"));
                    String codContatoExt = CursorContatos.getString(CursorContatos.getColumnIndex("CONTATO.CODCONTATO_EXT"));

                    lstcontatos = new Contatos(nome, cargo, email, tel1, tel2, null, null, null, null, null, null, 0, 0, null,
                            0, codCliente, codClieExt, nomeCliente, codContato, flagIntegrado, codContatoExt);
                    DadosListContatos.add(lstcontatos);
                } while (CursorContatos.moveToNext());
            } else {
                txvqtdregcont.setText("Quantidade de Registro: 0");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    Util.msg_toast_personal(ConsultaContatos.this, getString(R.string.no_contacts_found), Toast.LENGTH_SHORT);
                else
                    Toast.makeText(this, getString(R.string.no_contacts_found), Toast.LENGTH_SHORT).show();
            }
            CursorContatos.close();
            if (Dialogo.isShowing()) {
                Dialogo.dismiss();
            }
        }

        return DadosListContatos;

    }

    @Override
    public void run() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag == 0) {
                    try {
                        FragmentContatos frag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFragA");
                        Bundle params = new Bundle();
                        if (frag == null) {
                            frag = new FragmentContatos();
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.rl_fragment_container2, frag, "mainFragA");
                            params.putInt(getString(R.string.intent_flag), flag);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                            frag.setArguments(params);
                            ft.commit();
                        } else {
                            FragmentContatos newfrag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFragB");
                            Bundle newparams = new Bundle();
                            if (newfrag == null) {
                                newfrag = new FragmentContatos();
                                FragmentTransaction newft = getSupportFragmentManager().beginTransaction();
                                newft.replace(R.id.rl_fragment_container2, newfrag, "mainFragB");
                                newparams.putInt(getString(R.string.intent_flag), flag);
                                newparams.putString(getString(R.string.intent_usuario), usuario);
                                newparams.putString(getString(R.string.intent_senha), senha);
                                newparams.putString(getString(R.string.intent_codvendedor), codVendedor);
                                newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                newparams.putString(getString(R.string.intent_telainvocada), telaInvocada);
                                newfrag.setArguments(newparams);
                                newft.commit();
                            }

                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                } else if (flag == 1) {
                    try {
                        FragmentContatos frag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFragA");
                        Bundle params = new Bundle();
                        if (frag == null) {
                            frag = new FragmentContatos();
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.rl_fragment_container2, frag, "mainFragA");
                            params.putInt(getString(R.string.intent_flag), flag);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                            params.putString(getString(R.string.intent_telainvocada), telaInvocada);
                            frag.setArguments(params);
                            ft.commit();
                        } else {
                            FragmentContatos newfrag = (FragmentContatos) getSupportFragmentManager().findFragmentByTag("mainFragB");
                            Bundle newparams = new Bundle();
                            if (newfrag == null) {
                                newfrag = new FragmentContatos();
                                FragmentTransaction newft = getSupportFragmentManager().beginTransaction();
                                newft.replace(R.id.rl_fragment_container2, newfrag, "mainFragB");
                                newparams.putInt(getString(R.string.intent_flag), flag);
                                newparams.putString(getString(R.string.intent_usuario), usuario);
                                newparams.putString(getString(R.string.intent_senha), senha);
                                newparams.putString(getString(R.string.intent_codvendedor), codVendedor);
                                newparams.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
                                newparams.putString(getString(R.string.intent_telainvocada), telaInvocada);
                                newfrag.setArguments(newparams);
                                newft.commit();
                            }

                        }
                    } catch (Exception E) {
                        E.toString();
                    }
                }

            }
        });
    }
}

