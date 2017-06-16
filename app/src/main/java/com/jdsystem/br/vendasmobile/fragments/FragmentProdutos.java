package com.jdsystem.br.vendasmobile.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jdsystem.br.vendasmobile.CadastroContatos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaProdutos;
import com.jdsystem.br.vendasmobile.DadosContato;
import com.jdsystem.br.vendasmobile.DadosProduto;
import com.jdsystem.br.vendasmobile.Model.SqliteProdutoBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempDao;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDao;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterProdutos;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class FragmentProdutos extends Fragment implements RecyclerViewOnClickListenerHack, Runnable {

    public static final String CONFIG_HOST = "CONFIG_HOST";
    public AlertDialog Dialog;
    public ProgressDialog eDialog;
    public int actCadastraContato, CodCliente, CodContato;
    SQLiteDatabase DB;
    Context context;
    int idPerfil, iPosition, flagRun, iCodProduto;
    private RecyclerView mRecyclerView;
    private int flag;
    private int CodProdExt;
    private String numPedido, vlminimovend, habalteraprecovenda, vendenegativo, habcontrolqtdmin, chavePedido, usuario, senha, codVendedor,
            urlprincipal, tab1, tab2, tab3, tab4, tab5, tab6, tab7, Preco1, Preco2, Preco3, Preco4, Preco5, Precop1, Precop2, sincprod;
    private Spinner spntabpreco;
    private EditText edtprecovend;
    private String PREFS_PRIVATE = "PREFS_PRIVATE", NomeCliente;
    private SharedPreferences prefs;
    private ListView prod_listview_itenstemp;
    private Double qtdestoque, qtdminvend;
    private Handler handler = new Handler();
    Activity activity = getActivity();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_produtos, container, false);


            Bundle params = getArguments();
            if (params != null) {
                flag = params.getInt(getString(R.string.intent_flag));
                numPedido = params.getString(getString(R.string.intent_numpedido));
                chavePedido = params.getString(getString(R.string.intent_chavepedido));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                urlprincipal = params.getString(getString(R.string.intent_urlprincipal));
                String telaInvocada = params.getString(getString(R.string.intent_telainvocada));
                actCadastraContato = params.getInt(getString(R.string.intent_cad_contato));
                CodCliente = params.getInt(getString(R.string.intent_codcliente));
                CodContato = params.getInt(getString(R.string.intent_codcontato));
                NomeCliente = params.getString(getString(R.string.intent_nomerazao));
            }
            declaraobjetos();
            carregarpreferencias();
            carregarparametros();


            mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_Prod);
            mRecyclerView.setHasFixedSize(true);

            //Utilizado para o fast Scroll
            VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
            fastScroller.setRecyclerView(mRecyclerView);
            mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());
            setRecyclerViewLayoutManager(mRecyclerView);

            /*RecyclerView.Adapter adapter = new ColorfulAdapter(new ColorDataSet());
            mRecyclerView.setAdapter(adapter);*/
            //termina aqui o código para o fast scroll

            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);

            List<Produtos> mList = ((ConsultaProdutos) getActivity()).carregarprodutos();
            ListAdapterProdutos adapter = new ListAdapterProdutos(getActivity(), mList);
            adapter.setRecyclerViewOnClickListenerHack(this);
            mRecyclerView.setAdapter(adapter);


            return view;
        } catch (Exception E) {
            System.out.println("Error" + E);
        }
        return mRecyclerView;
    }

    private void declaraobjetos() {
        DB = new ConfigDB(getActivity()).getReadableDatabase();
    }

    private void carregarparametros() {

        try {
            Cursor curosrparam = DB.rawQuery("SELECT DESCRICAOTAB1,HABALTPRECOVENDA,VLMINVENDA, DESCRICAOTAB2, DESCRICAOTAB3, DESCRICAOTAB4, DESCRICAOTAB5, DESCRICAOTAB6," +
                    " DESCRICAOTAB7, HABCONTROLQTDMINVEND,HABITEMNEGATIVO FROM PARAMAPP WHERE CODPERFIL = " + idPerfil, null);
            curosrparam.moveToFirst();
            if (curosrparam.getCount() > 0) {
                habcontrolqtdmin = curosrparam.getString(curosrparam.getColumnIndex("HABCONTROLQTDMINVEND"));
                habalteraprecovenda = curosrparam.getString(curosrparam.getColumnIndex("HABALTPRECOVENDA"));
                vlminimovend = curosrparam.getString(curosrparam.getColumnIndex("VLMINVENDA"));
                vendenegativo = curosrparam.getString(curosrparam.getColumnIndex("HABITEMNEGATIVO"));
                tab1 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB1"));
                tab2 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB2"));
                tab3 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB3"));
                tab4 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB4"));
                tab5 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB5"));
                tab6 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB6"));
                tab7 = curosrparam.getString(curosrparam.getColumnIndex("DESCRICAOTAB7"));
                if (habalteraprecovenda.equals("S")) {
                    switch (vlminimovend) {
                        case "V1":
                            vlminimovend = tab1;
                            break;
                        case "V2":
                            vlminimovend = tab2;
                            break;
                        case "V3":
                            vlminimovend = tab3;
                            break;
                        case "V4":
                            vlminimovend = tab4;
                            break;
                        case "V5":
                            vlminimovend = tab5;
                            break;
                        case "P1":
                            vlminimovend = tab6;
                            break;
                        case "P2":
                            vlminimovend = tab7;
                            break;
                    }
                }

            }
        } catch (Exception e) {
            e.toString();
        }

    }

    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) { // Utilizado para o fast scroll
        int scrollPosition = 0;

        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onClickListener(View v, final int position) {
        String codProd;
        if (flag == 0 && actCadastraContato == 1) {
            flagRun = 1;
            iPosition = position;

            eDialog = new ProgressDialog(getContext());
            eDialog.setTitle(getString(R.string.wait));
            eDialog.setMessage("Aguarde");
            eDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eDialog.setCancelable(false);
            eDialog.show();

            Thread thread = new Thread(FragmentProdutos.this);
            thread.start();

        } else if (flag == 0 && actCadastraContato == 2) {
            ListAdapterProdutos adapterProdutos = (ListAdapterProdutos) mRecyclerView.getAdapter();

            String CodProd = adapterProdutos.ChamaDados(position);
            int codIntItem = adapterProdutos.codInternoItem(position);
            try {
                Cursor cursor = DB.rawQuery("select cod_produto_manual, cod_interno_contato, cod_item " +
                        "from produtos_contatos " +
                        "where cod_item = '" + codIntItem + "' and cod_interno_contato = " + CodContato + " and cod_produto_manual = '" + CodProd + "'", null);
                cursor.moveToFirst();

                if (cursor.getCount() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        Util.msg_toast_personal(getContext(), "Este produto já está relacionado à este cliente. " +
                                "Verifique.", Toast.LENGTH_SHORT);
                    } else {
                        Toast.makeText(getActivity(), "Este produto já está relacionado à este cliente. " +
                                "Verifique.", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                } else {
                    Util.gravarItensContato(CodProd, codIntItem, CodContato, getContext());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        Util.msg_toast_personal(getContext(), "Produto relacionado com sucesso!", Toast.LENGTH_SHORT);
                    } else {
                        Toast.makeText(getActivity(), "Produto relacionado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();

                    Intent intentp = new Intent(getActivity(), DadosContato.class);
                    Bundle params = new Bundle();
                    params.putString(getString(R.string.intent_codproduto), CodProd);
                    params.putInt("codProdutoInt", codIntItem);
                    params.putString(getString(R.string.intent_codvendedor), codVendedor);
                    params.putString(getString(R.string.intent_usuario), usuario);
                    params.putString(getString(R.string.intent_senha), senha);
                    params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                    params.putInt(getString(R.string.intent_codcliente), CodCliente);
                    params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                    params.putInt(getString(R.string.intent_codcontato), CodContato);
                    intentp.putExtras(params);
                    startActivity(intentp);
                    getActivity().finish();
                }
            } catch (Exception E) {
                E.toString();
            }
        } else if (flag == 0 && numPedido == null) {

            ListAdapterProdutos adapter = (ListAdapterProdutos) mRecyclerView.getAdapter();

            codProd = adapter.ChamaDados(position).trim();
            CodProdExt = adapter.ChamaCodItemExt(position);
            Intent intentp = new Intent(getActivity(), DadosProduto.class);
            Bundle params = new Bundle();
            params.putInt(getString(R.string.intent_codproduto), CodProdExt);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
            intentp.putExtras(params);
            startActivity(intentp);

            //=============EXECUTA A CONSULTA DO ITEM NA INSERÇÃO DO PRODUTO NO CADASTRO DO PEDIDO================

        } else {
            flagRun = 2;
            iPosition = position;

            eDialog = new ProgressDialog(getContext());
            eDialog.setTitle(getString(R.string.wait));
            eDialog.setMessage("Verificando dados do produto...");
            eDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eDialog.setCancelable(false);
            eDialog.show();

            Thread thread = new Thread(FragmentProdutos.this);
            thread.start();
        }

    }

    private String validaprecominimo(String valorItem) {
        String validaok = "ok";
        if (vlminimovend.equals(tab1)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Preco1.replace(",", "."))) {
                validaok = "0";
            }
        } else if (vlminimovend.equals(tab2)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Preco2.replace(",", "."))) {
                validaok = "0";
            }

        } else if (vlminimovend.equals(tab3)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Preco3.replace(",", "."))) {
                validaok = "0";
            }

        } else if (vlminimovend.equals(tab4)) {
            valorItem = valorItem.replace(",", ".");

            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Preco4.replace(",", "."))) {
                validaok = "0";
            }

        } else if (vlminimovend.equals(tab5)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Preco5.replace(",", "."))) {
                validaok = "0";
            }

        } else if (vlminimovend.equals(tab6)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Precop1.replace(",", "."))) {
                validaok = "0";
            }

        } else if (vlminimovend.equals(tab7)) {
            if (Double.parseDouble(valorItem.replace(",", ".")) < Double.parseDouble(Precop2.replace(",", "."))) {
                validaok = "0";
            }

        }
        return validaok;
    }

    @Override
    public void onLongClickListener(View view, int position) {
    }

    public void GravaPreferencias(int preco) {

        prefs = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsPrivateEditor = prefs.edit();
        prefsPrivateEditor.putInt("spreco", preco);
        prefsPrivateEditor.commit();

    }

    private void carregarpreferencias() {
        prefs = getActivity().getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        urlprincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }

    @Override
    public void run() {
        if (flagRun == 1) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    ListAdapterProdutos adapterProdutos = (ListAdapterProdutos) mRecyclerView.getAdapter();

                    String CodProd = adapterProdutos.ChamaDados(iPosition);
                    int codIntItem = adapterProdutos.codInternoItem(iPosition);

                    try {
                        Cursor cursor = DB.rawQuery("select cod_produto_manual, cod_interno_contato, cod_item " +
                                "from produtos_contatos_temp " +
                                "where cod_item = " + codIntItem + " and cod_interno_contato = " + CodContato + " and cod_produto_manual = '" + CodProd + "'", null);
                        cursor.moveToFirst();

                        if (cursor.getCount() > 0) {
                            if (eDialog.isShowing()) eDialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Util.msg_toast_personal(getContext(), "Este produto já está relacionado à este cliente. " +
                                        "Verifique.", Toast.LENGTH_SHORT);
                            } else {
                                Toast.makeText(getContext(), "Este produto já está relacionado à este cliente. " +
                                        "Verifique.", Toast.LENGTH_SHORT).show();
                            }
                            cursor.close();
                        } else {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Util.msg_toast_personal(getContext(), "Produto relacionado com sucesso!", Toast.LENGTH_SHORT);
                            } else {
                                Toast.makeText(getContext(), "Produto relacionado com sucesso!", Toast.LENGTH_SHORT).show();
                            }
                            cursor.close();

                            if (eDialog.isShowing()) eDialog.dismiss();

                            Intent intentp = new Intent(getActivity(), CadastroContatos.class);
                            Bundle params = new Bundle();
                            params.putString(getString(R.string.intent_codproduto), CodProd);
                            params.putInt("codProdutoInt", codIntItem);
                            params.putString(getString(R.string.intent_codvendedor), codVendedor);
                            params.putString(getString(R.string.intent_usuario), usuario);
                            params.putString(getString(R.string.intent_senha), senha);
                            params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
                            params.putInt(getString(R.string.intent_codcliente), CodCliente);
                            params.putString(getString(R.string.intent_nomerazao), NomeCliente);
                            params.getInt(getString(R.string.intent_codcontato), CodContato);
                            intentp.putExtras(params);
                            startActivity(intentp);
                            getActivity().finish();
                        }
                    } catch (Exception E) {
                        E.toString();
                        if (eDialog.isShowing()) eDialog.dismiss();
                    }
                    if (eDialog.isShowing()) eDialog.dismiss();

                }
            });

            //=============EXECUTA A CONSULTA DO ITEM NA INSERÇÃO DO PRODUTO NO CADASTRO DO PEDIDO================
        } else if (flagRun == 2) {
            ListAdapterProdutos adapter = (ListAdapterProdutos) mRecyclerView.getAdapter();
            CodProdExt = adapter.ChamaCodItemExt(iPosition);
            if (Util.checarConexaoCelular(getActivity())) {
                sincprod = Sincronismo.sincronizaProdutos(getActivity(), usuario, senha, CodProdExt, eDialog, null, null);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {


                    activity = new Activity();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String codItem = null;
                            String descricao = null;
                            String unidadeMedida = null;
                            String tabelaPadrao = null;
                            Cursor cursoritem = null;

                            int sprecoprincipal;
                            if (numPedido.equals("0")) {
                                try {
                                    boolean ConexOk = Util.checarConexaoCelular(getActivity());
                                    if (vendenegativo.equals("N") && ConexOk) {

                                        if (sincprod.equals(getString(R.string.sync_products_successfully))) {
                                            Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM =" + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                            CursItens.moveToFirst();
                                            qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                            qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));
                                            CursItens.close();
                                            if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                                if (eDialog.isShowing()) eDialog.dismiss();
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                        } else {
                                            Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM =" + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                            CursItens.moveToFirst();

                                            qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                            qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));

                                            CursItens.close();
                                            if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                                if (eDialog.isShowing()) eDialog.dismiss();
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                        }

                                    } else {
                                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM =" + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                        CursItens.moveToFirst();
                                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                        qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));
                                        CursItens.close();
                                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                            if (eDialog.isShowing()) eDialog.dismiss();
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }
                                    }

                                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                    @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.input_produto_venda, null);
                                    final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                                    alerta.setCancelable(false);
                                    alerta.setView(view);

                                    final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                                    final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                                    final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                                    //final TextView info_txv_precoproduto = (TextView) view.findViewById(R.id.info_txv_precoproduto);
                                    edtprecovend = (EditText) view.findViewById(R.id.edtprecovenda);
                                    if (habalteraprecovenda.equals("N")) {
                                        edtprecovend.setEnabled(false);
                                    }
                                    final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);

                                    spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);

                                    spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                            GravaPreferencias(spntabpreco.getSelectedItemPosition());
                                            String spreco = spntabpreco.getSelectedItem().toString();

                                            if (!tab1.equals("")) {
                                                spreco = spreco.replace(tab1, "");
                                            }
                                            if (!tab2.equals("")) {
                                                spreco = spreco.replace(tab2, "");
                                            }
                                            if (!tab3.equals("")) {
                                                spreco = spreco.replace(tab3, "");
                                            }
                                            if (!tab4.equals("")) {
                                                spreco = spreco.replace(tab4, "");
                                            }
                                            if (!tab5.equals("")) {
                                                spreco = spreco.replace(tab5, "");
                                            }
                                            if (!tab6.equals("")) {
                                                spreco = spreco.replace(tab6, "");
                                            }
                                            if (!tab7.equals("")) {
                                                spreco = spreco.replace(tab7, "");
                                            }
                                            spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                                            edtprecovend.setText(spreco);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });

                                    try {
                                        List<String> DadosListTabPreco = new ArrayList<String>();

                                        cursoritem = DB.rawQuery("SELECT DESCRICAO, CODIGOITEM,UNIVENDA,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2,TABELAPADRAO,CODITEMANUAL FROM ITENS WHERE CODIGOITEM = " + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                        cursoritem.moveToFirst();
                                        if (cursoritem.getCount() > 0) {
                                            codItem = cursoritem.getString(cursoritem.getColumnIndex("CODITEMANUAL"));
                                            descricao = cursoritem.getString(cursoritem.getColumnIndex("DESCRICAO"));
                                            unidadeMedida = cursoritem.getString(cursoritem.getColumnIndex("UNIVENDA"));
                                            tabelaPadrao = cursoritem.getString(cursoritem.getColumnIndex("TABELAPADRAO"));
                                        }

                                        if (!tab1.equals("")) {
                                            String vlvenda1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                                            vlvenda1 = vlvenda1.trim();
                                            if (!vlvenda1.equals("0,0000")) {
                                                BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                                                Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Preco1 = Preco1.replace('.', ',');
                                                DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                                            }
                                        }
                                        if (!tab2.equals("")) {
                                            String vlvenda2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                                            vlvenda2 = vlvenda2.trim();
                                            if (!vlvenda2.equals("0,0000")) {
                                                BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                                                Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Preco2 = Preco2.replace('.', ',');
                                                DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                                            }
                                        }
                                        if (!tab3.equals("")) {
                                            String vlvenda3 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                                            vlvenda3 = vlvenda3.trim();
                                            if (!vlvenda3.equals("0,0000")) {
                                                BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                                                Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Preco3 = Preco3.replace('.', ',');
                                                DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                                            }
                                        }
                                        if (!tab4.equals("")) {
                                            String vlvenda4 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                                            vlvenda4 = vlvenda4.trim();
                                            if (!vlvenda4.equals("0,0000")) {
                                                BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                                                Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Preco4 = Preco4.replace('.', ',');
                                                DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                                            }
                                        }
                                        if (!tab5.equals("")) {
                                            String vlvenda5 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                                            vlvenda5 = vlvenda5.trim();
                                            if (!vlvenda5.equals("0,0000")) {
                                                BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                                                Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Preco5 = Preco5.replace('.', ',');
                                                DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                                            }
                                        }
                                        if (!tab6.equals("")) {
                                            String vlvendap1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                                            vlvendap1 = vlvendap1.trim();
                                            if (!vlvendap1.equals("0,0000")) {
                                                BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                                                Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Precop1 = Precop1.replace('.', ',');
                                                DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                                            }
                                        }
                                        if (!tab7.equals("")) {
                                            String vlvendap2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                                            vlvendap2 = vlvendap2.trim();
                                            if (!vlvendap2.equals("0,0000")) {
                                                BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                                                Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                                Precop2 = Precop2.replace('.', ',');
                                                DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                                            }
                                        }
                                        ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                                        arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spntabpreco.setAdapter(arrayAdapterTabPreco);

                                    } catch (Exception E) {
                                        E.toString();
                                    }
                                    SqliteProdutoBean prdBean = new SqliteProdutoBean();
                                    info_txv_codproduto.setText(codItem);
                                    info_txv_descricaoproduto.setText(descricao);
                                    info_txv_unmedida.setText(unidadeMedida);

                                    if (tabelaPadrao.equals(tab1)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab2)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab3)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab4)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab5)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab6)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');
                                        edtprecovend.setText(Preco);
                                    } else if (tabelaPadrao.equals(tab7)) {
                                        String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                        String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                        Preco = Preco.replace('.', ',');

                                        edtprecovend.setText(Preco);
                                        info_txt_quantidadecomprada.setText("");
                                    }
                                    cursoritem.close();

                                    final Double finalQtdestoque = qtdestoque;
                                    alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                                            if (TAMANHO_TEXTO <= 0) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), "A quantidade não foi informada", Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                            SqliteProdutoBean prdBean = new SqliteProdutoBean();
                                            String qtdinformada = Util.removeZerosEsquerda(info_txt_quantidadecomprada.getText().toString());
                                            Double QUANTIDADE_DIGITADA = Double.parseDouble(qtdinformada);

                                            String COD_PRODUTO = info_txv_codproduto.getText().toString();
                                            String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                                            String UNIDADE = info_txv_unmedida.getText().toString();

                                            if (QUANTIDADE_DIGITADA > 0) {
                                                if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > finalQtdestoque) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                        Util.msg_toast_personal(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                                                    } else {
                                                        Toast.makeText(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                                    return;
                                                }
                                                if (habcontrolqtdmin.equals("S") && QUANTIDADE_DIGITADA < qtdminvend) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                        Util.msg_toast_personal(getActivity(), "Quantidade solicitada abaixo do mínimo permitido para venda.Verifique!", Util.ALERTA);
                                                    } else {
                                                        Toast.makeText(getActivity(), "Quantidade solicitada abaixo do mínimo permitido para venda.Verifique!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                                    return;
                                                }
                                                SqliteVendaD_TempBean itemBean1 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempBean itemBean2 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempBean itemBean3 = new SqliteVendaD_TempBean();
                                                SqliteVendaD_TempDao itemDao = new SqliteVendaD_TempDao(getActivity());

                                                itemBean2.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                                itemBean3 = itemDao.buscar_item_na_venda(itemBean2);

                                                if (itemBean3 == null) {
                                                    itemBean1.setVendad_prd_codigoItemTEMP(CodProdExt);
                                                    itemBean1.setVendad_prd_codigoTEMP(COD_PRODUTO);
                                                    itemBean1.setVendad_prd_descricaoTEMP(DESCRICAO);
                                                    itemBean1.setVendad_prd_unidadeTEMP(UNIDADE);
                                                    itemBean1.setVendad_quantidadeTEMP(new BigDecimal(QUANTIDADE_DIGITADA));

                                                    String ValorItem = edtprecovend.getText().toString();

                                                    if (!ValorItem.equals("0,0000")) {
                                                        if (habalteraprecovenda.equals("S")) {
                                                            String validapreco = validaprecominimo(ValorItem);
                                                            if (!validapreco.equals("ok")) {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                                    Util.msg_toast_personal(getActivity(), "produto com preço de venda abaixo do minimo permitido", Util.ALERTA);
                                                                } else {
                                                                    Toast.makeText(getActivity(), "produto com preço de venda abaixo do minimo permitido", Toast.LENGTH_SHORT).show();
                                                                }
                                                                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                                                return;
                                                            }
                                                        }
                                                        BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                                        venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                                        itemBean1.setVendad_preco_vendaTEMP(venda);

                                                        itemBean1.setVendad_totalTEMP(itemBean1.getSubTotal());
                                                        itemDao.insere_item(itemBean1);
                                                        getActivity().finish();
                                                    } else {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                            Util.msg_toast_personal(getActivity(), "produto com preço de venda zerado", Util.ALERTA);
                                                        } else {
                                                            Toast.makeText(getActivity(), "produto com preço de venda zerado", Toast.LENGTH_SHORT).show();
                                                        }
                                                        return;
                                                    }
                                                } else {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                        Util.msg_toast_personal(getActivity(), "Este produto já foi adicionado", Util.ALERTA);
                                                    } else {
                                                        Toast.makeText(getActivity(), "Este produto já foi adicionado", Toast.LENGTH_SHORT).show();
                                                    }
                                                    return;
                                                }
                                            } else

                                            {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), "A quantidade não foi informada", Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }

                                        }

                                    });
                                    alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                        }
                                    });
                                    SharedPreferences prefsHost = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                                    sprecoprincipal = prefsHost.getInt("spreco", 0);
                                    spntabpreco.setSelection(sprecoprincipal);

                                    Configuration configuration = getResources().getConfiguration();

                                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                                    } else {
                                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                    }
                                    if (eDialog.isShowing()) eDialog.dismiss();
                                    alerta.show();
                                } catch (Exception e) {
                                    e.toString();
                                }

                            } else {
                                boolean ConexOk = Util.checarConexaoCelular(getActivity());
                                if (vendenegativo.equals("N") && ConexOk) {
                                    sincprod = Sincronismo.sincronizaProdutos(getActivity(), usuario, senha, CodProdExt, null, null, null);

                                    if (sincprod.equals(getString(R.string.sync_products_successfully))) {
                                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                        CursItens.moveToFirst();
                                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                        qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));
                                        CursItens.close();
                                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }
                                    } else {
                                        Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                        CursItens.moveToFirst();
                                        qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                        qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));
                                        CursItens.close();
                                        if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }
                                    }
                                } else {
                                    Cursor CursItens = DB.rawQuery(" SELECT * FROM ITENS WHERE CODIGOITEM = " + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                    CursItens.moveToFirst();
                                    qtdestoque = CursItens.getDouble(CursItens.getColumnIndex("QTDESTPROD"));
                                    qtdminvend = CursItens.getDouble(CursItens.getColumnIndex("QTDMINVEND"));
                                    CursItens.close();
                                    if (vendenegativo.equals("N") && qtdestoque <= 0) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            Util.msg_toast_personal(getActivity(), getString(R.string.item_sem_estoque), Util.ALERTA);
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.item_sem_estoque), Toast.LENGTH_SHORT).show();
                                        }
                                        return;
                                    }
                                }

                                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.input_produto_venda, null);
                                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                                alerta.setCancelable(false);
                                alerta.setView(view);

                                final TextView info_txv_codproduto = (TextView) view.findViewById(R.id.info_txv_codproduto);
                                final TextView info_txv_descricaoproduto = (TextView) view.findViewById(R.id.info_txv_descricaoproduto);
                                final TextView info_txv_unmedida = (TextView) view.findViewById(R.id.info_txv_unmedida);
                                edtprecovend = (EditText) view.findViewById(R.id.edtprecovenda);
                                if (habalteraprecovenda.equals("N")) {
                                    edtprecovend.setEnabled(false);
                                }
                                final EditText info_txt_quantidadecomprada = (EditText) view.findViewById(R.id.info_txt_quantidadecomprada);

                                spntabpreco = (Spinner) view.findViewById(R.id.spntabpreco);

                                spntabpreco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                        GravaPreferencias(spntabpreco.getSelectedItemPosition());

                                        String spreco = spntabpreco.getSelectedItem().toString();

                                        if (!tab1.equals("")) {
                                            spreco = spreco.replace(tab1, "");
                                        }
                                        if (!tab2.equals("")) {
                                            spreco = spreco.replace(tab2, "");
                                        }
                                        if (!tab3.equals("")) {
                                            spreco = spreco.replace(tab3, "");
                                        }
                                        if (!tab4.equals("")) {
                                            spreco = spreco.replace(tab4, "");
                                        }
                                        if (!tab5.equals("")) {
                                            spreco = spreco.replace(tab5, "");
                                        }
                                        if (!tab6.equals("")) {
                                            spreco = spreco.replace(tab6, "");
                                        }
                                        if (!tab7.equals("")) {
                                            spreco = spreco.replace(tab7, "");
                                        }
                                        spreco = spreco.replaceAll("[A-Za-z$ãç:/*%]", "").trim();
                                        edtprecovend.setText(spreco);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });

                                try {
                                    List<String> DadosListTabPreco = new ArrayList<String>();
                                    cursoritem = DB.rawQuery("SELECT DESCRICAO,CODIGOITEM,UNIVENDA,VLVENDA1,VLVENDA2,VLVENDA3,VLVENDA4,VLVENDA5,VLVENDAP1,VLVENDAP2,TABELAPADRAO,CODITEMANUAL FROM ITENS WHERE CODIGOITEM = " + CodProdExt + " AND CODPERFIL = " + idPerfil, null);
                                    cursoritem.moveToFirst();
                                    if (cursoritem.getCount() > 0) {
                                        codItem = cursoritem.getString(cursoritem.getColumnIndex("CODITEMANUAL"));
                                        descricao = cursoritem.getString(cursoritem.getColumnIndex("DESCRICAO"));
                                        unidadeMedida = cursoritem.getString(cursoritem.getColumnIndex("UNIVENDA"));
                                        tabelaPadrao = cursoritem.getString(cursoritem.getColumnIndex("TABELAPADRAO"));
                                    }
                                    if (!tab1.equals("")) {
                                        String vlvenda1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                                        vlvenda1 = vlvenda1.trim();
                                        if (!vlvenda1.equals("0,0000")) {
                                            BigDecimal venda1 = new BigDecimal(Double.parseDouble(vlvenda1.replace(',', '.')));
                                            Preco1 = venda1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco1 = Preco1.replace('.', ',');
                                            DadosListTabPreco.add(tab1 + " R$: " + Preco1);
                                        }
                                    }
                                    if (!tab2.equals("")) {
                                        String vlvenda2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                                        vlvenda2 = vlvenda2.trim();
                                        if (!vlvenda2.equals("0,0000")) {
                                            BigDecimal venda2 = new BigDecimal(Double.parseDouble(vlvenda2.replace(',', '.')));
                                            Preco2 = venda2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco2 = Preco2.replace('.', ',');
                                            DadosListTabPreco.add(tab2 + " R$: " + Preco2);
                                        }
                                    }
                                    if (!tab3.equals("")) {
                                        String vlvenda3 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                                        vlvenda3 = vlvenda3.trim();
                                        if (!vlvenda3.equals("0,0000")) {
                                            BigDecimal venda3 = new BigDecimal(Double.parseDouble(vlvenda3.replace(',', '.')));
                                            Preco3 = venda3.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco3 = Preco3.replace('.', ',');
                                            DadosListTabPreco.add(tab3 + " R$: " + Preco3);
                                        }
                                    }
                                    if (!tab4.equals("")) {
                                        String vlvenda4 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                                        vlvenda4 = vlvenda4.trim();
                                        if (!vlvenda4.equals("0,0000")) {
                                            BigDecimal venda4 = new BigDecimal(Double.parseDouble(vlvenda4.replace(',', '.')));
                                            Preco4 = venda4.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco4 = Preco4.replace('.', ',');
                                            DadosListTabPreco.add(tab4 + " R$: " + Preco4);
                                        }
                                    }
                                    if (!tab5.equals("")) {
                                        String vlvenda5 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                                        vlvenda5 = vlvenda5.trim();
                                        if (!vlvenda5.equals("0,0000")) {
                                            BigDecimal venda5 = new BigDecimal(Double.parseDouble(vlvenda5.replace(',', '.')));
                                            Preco5 = venda5.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Preco5 = Preco5.replace('.', ',');
                                            DadosListTabPreco.add(tab5 + " R$: " + Preco5);
                                        }
                                    }
                                    if (!tab6.equals("")) {
                                        String vlvendap1 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                                        vlvendap1 = vlvendap1.trim();
                                        if (!vlvendap1.equals("0,0000")) {
                                            BigDecimal vendap1 = new BigDecimal(Double.parseDouble(vlvendap1.replace(',', '.')));
                                            Precop1 = vendap1.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop1 = Precop1.replace('.', ',');
                                            DadosListTabPreco.add(tab6 + " R$: " + Precop1);
                                        }
                                    }
                                    if (!tab7.equals("")) {
                                        String vlvendap2 = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                                        vlvendap2 = vlvendap2.trim();
                                        if (!vlvendap2.equals("0,0000")) {
                                            BigDecimal vendap2 = new BigDecimal(Double.parseDouble(vlvendap2.replace(',', '.')));
                                            Precop2 = vendap2.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                            Precop2 = Precop2.replace('.', ',');
                                            DadosListTabPreco.add(tab7 + " R$: " + Precop2);
                                        }
                                    }
                                    ArrayAdapter<String> arrayAdapterTabPreco = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, DadosListTabPreco);
                                    arrayAdapterTabPreco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spntabpreco.setAdapter(arrayAdapterTabPreco);
                                } catch (Exception E) {
                                    E.toString();
                                }
                                SqliteProdutoBean prdBean = new SqliteProdutoBean();
                                info_txv_codproduto.setText(codItem);
                                info_txv_descricaoproduto.setText(descricao);
                                info_txv_unmedida.setText(unidadeMedida);

                                if (tabelaPadrao.equals(tab1)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA1"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab2)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA2"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab3)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA3"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab4)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA4"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab5)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDA5"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab6)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP1"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                } else if (tabelaPadrao.equals(tab7)) {
                                    String ValorItem = cursoritem.getString(cursoritem.getColumnIndex("VLVENDAP2"));
                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                    String Preco = venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
                                    Preco = Preco.replace('.', ',');
                                    edtprecovend.setText(Preco);
                                    info_txt_quantidadecomprada.setText("");
                                }
                                cursoritem.close();

                                alerta.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        Integer TAMANHO_TEXTO = info_txt_quantidadecomprada.getText().toString().length();

                                        if (TAMANHO_TEXTO <= 0) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), "A quantidade não foi informada", Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }

                                        SqliteProdutoBean prdBean = new SqliteProdutoBean();
                                        String qtdinformada = Util.removeZerosEsquerda(info_txt_quantidadecomprada.getText().toString());
                                        Double QUANTIDADE_DIGITADA = Double.parseDouble(qtdinformada);
                                        String COD_PRODUTO = info_txv_codproduto.getText().toString();
                                        String DESCRICAO = info_txv_descricaoproduto.getText().toString();
                                        String UNIDADE = info_txv_unmedida.getText().toString();

                                        if (QUANTIDADE_DIGITADA > 0) {
                                            if (vendenegativo.equals("N") && QUANTIDADE_DIGITADA > qtdestoque) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), "Quantidade solicitada insatisfeita.Verifique!", Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                            if (habcontrolqtdmin.equals("S") && QUANTIDADE_DIGITADA < qtdminvend) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "Quantidade solicitada abaixo do mínimo permitido para venda.Verifique!", Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), "Quantidade solicitada abaixo do mínimo permitido para venda.Verifique!", Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                            SqliteVendaDBean itemBean1 = new SqliteVendaDBean();
                                            final SqliteVendaDBean itemBean2 = new SqliteVendaDBean();
                                            SqliteVendaDBean itemBean3 = new SqliteVendaDBean();
                                            SqliteVendaDao itemDao = new SqliteVendaDao(getActivity(), codVendedor, true);

                                            itemBean2.setVendad_prd_codigo(COD_PRODUTO);
                                            itemBean3 = itemDao.altera_item_na_venda(itemBean2, chavePedido);

                                            if (itemBean3 == null) {
                                                itemBean1.setVendad_prd_codigoitem(CodProdExt);
                                                itemBean1.setVendad_prd_codigo(COD_PRODUTO);
                                                itemBean1.setVendad_prd_descricao(DESCRICAO);
                                                itemBean1.setVendad_prd_unidade(UNIDADE);
                                                itemBean1.setVendad_quantidade(new BigDecimal(QUANTIDADE_DIGITADA));
                                                itemBean1.setVendac_chave(chavePedido);
                                                itemBean1.setvendad_prd_view("T");

                                                String ValorItem = edtprecovend.getText().toString();
                                                ValorItem = ValorItem.trim();
                                                if (!ValorItem.equals("0,0000")) {
                                                    if (habalteraprecovenda.equals("S")) {
                                                        String validapreco = validaprecominimo(ValorItem);
                                                        if (!validapreco.equals("ok")) {
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                                Util.msg_toast_personal(getActivity(), "produto com preço de venda abaixo do minimo permitido", Util.ALERTA);
                                                            } else {
                                                                Toast.makeText(getActivity(), "produto com preço de venda abaixo do minimo permitido", Toast.LENGTH_SHORT).show();
                                                            }
                                                            return;
                                                        }
                                                    }
                                                    BigDecimal venda = new BigDecimal(Double.parseDouble(ValorItem.replace(',', '.')));
                                                    venda.setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
                                                    itemBean1.setVendad_preco_venda(venda);

                                                    itemBean1.setVendad_total(itemBean1.getSubTotal());
                                                    itemDao.insere_item_na_venda(itemBean1);
                                                    getActivity().finish();
                                                } else {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                        Util.msg_toast_personal(getActivity(), "produto com preço de venda zerado", Util.ALERTA);
                                                    } else {
                                                        Toast.makeText(getActivity(), "produto com preço de venda zerado", Toast.LENGTH_SHORT).show();
                                                    }
                                                    return;
                                                }
                                            } else {

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                    Util.msg_toast_personal(getActivity(), "Este produto já foi adicionado", Util.ALERTA);
                                                } else {
                                                    Toast.makeText(getActivity(), "Este produto já foi adicionado", Toast.LENGTH_SHORT).show();
                                                }
                                                return;
                                            }
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                Util.msg_toast_personal(getActivity(), "A quantidade não foi informada", Util.ALERTA);
                                            } else {
                                                Toast.makeText(getActivity(), "A quantidade não foi informada", Toast.LENGTH_SHORT).show();
                                            }
                                            return;
                                        }

                                    }
                                });
                                alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()

                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                SharedPreferences prefsHost = getActivity().getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);
                                sprecoprincipal = prefsHost.getInt("spreco", 0);
                                spntabpreco.setSelection(sprecoprincipal);

                                Configuration configuration = getResources().getConfiguration();

                                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                                } else {
                                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                }

                                alerta.show();
                            }
                        }


                    });
                }
            });
        }
        if (eDialog.isShowing())
            eDialog.dismiss();
    }
}
