package com.jdsystem.br.vendasmobile.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.CadastroContatos;
import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.ConsultaClientes;
import com.jdsystem.br.vendasmobile.ConsultaPedidos;
import com.jdsystem.br.vendasmobile.CadastroPedidos;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.DadosCliente;
import com.jdsystem.br.vendasmobile.RecyclerViewFastScroller.VerticalRecyclerViewFastScroller;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentCliente extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<Clientes> mList;
    int flag,cadContato;
    String numPedido, chavePedido, usuario, senha, codVendedor, CodEmpresa, dataEntrega, telaInvocada, urlPrincipal;
    boolean consultaPedido;
    SQLiteDatabase DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cliente, container, false);

        Bundle params = getArguments();
        if (params != null) {
            flag = params.getInt(getString(R.string.intent_flag));
            CodEmpresa = params.getString(getString(R.string.intent_codigoempresa));
            numPedido = params.getString(getString(R.string.intent_numpedido));
            chavePedido = params.getString(getString(R.string.intent_chavepedido));
            telaInvocada = params.getString("TELA_QUE_CHAMOU");
            dataEntrega = params.getString("dataentrega");
            urlPrincipal = params.getString(getString(R.string.intent_urlprincipal));
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
            codVendedor = params.getString(getString(R.string.intent_codvendedor));
            cadContato = params.getInt(getString(R.string.intent_cad_contato));
            consultaPedido = params.getBoolean("consultapedido");
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);

        //Utilizado para o fast Scroll
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(mRecyclerView);
        mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());
        setRecyclerViewLayoutManager(mRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mList = ((ConsultaClientes) getActivity()).CarregarClientes();
        ListAdapterClientes adapter = new ListAdapterClientes(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
        //return mRecyclerView;
    }

    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) { // Utilizado para o fast scroll
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onClickListener(View view, int position) {
        if(consultaPedido == true){
            ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            Intent intentp = new Intent(getActivity(), ConsultaPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
            params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
            params.putString(getString(R.string.intent_codvendedor),codVendedor);
            params.putString(getString(R.string.intent_usuario),usuario);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_senha),senha);
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();

        }else if (flag == 0 && cadContato == 0) {
            ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            Intent intentp = new Intent(getActivity(), DadosCliente.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
            params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
            params.putString(getString(R.string.intent_codvendedor),codVendedor);
            params.putString(getString(R.string.intent_usuario),usuario);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_senha),senha);
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();

        } else if(flag == 0 && cadContato == 1){
            ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
            String nomeRazao = adapter.ChamaNomeRazaoCliente(position);
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);

            Intent intent = new Intent(getActivity(), CadastroContatos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlPrincipal);
            params.putInt(getString(R.string.intent_codcliente), Integer.parseInt(CodigoClienteInterno));
            params.putString(getString(R.string.intent_nomerazao), nomeRazao);
            //params.putString("C",TipoContato);
            intent.putExtras(params);
            startActivity(intent);
            getActivity().finish();

        } else {
            ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();
            String CodigoClienteExterno = adapter.ChamaCodigoClienteExterno(position);
            String CodigoClienteInterno = adapter.ChamaCodigoClienteInterno(position);

            String BloqClie = null;
            String bloqueio = null;
            String FlagIntegrado = null;
            DB = new ConfigDB(getActivity()).getReadableDatabase();
            try {
                Cursor cursorbloqclie = DB.rawQuery("SELECT HABCRITSITCLIE FROM PARAMAPP", null);
                cursorbloqclie.moveToFirst();
                BloqClie = cursorbloqclie.getString(cursorbloqclie.getColumnIndex("HABCRITSITCLIE"));
                cursorbloqclie.close();
                bloqueio = adapter.ChamaBloqueioCliente(position);
                FlagIntegrado = adapter.ChamaFlagIntegradoCliente(position);
            } catch (Exception e) {
                e.toString();
            }

            if (FlagIntegrado.equals("1")) {
                if (numPedido == null) {
                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                    Bundle params = new Bundle();
                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                    params.putString("numpedido", "0");
                    params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                    params.putString(getString(R.string.intent_usuario),usuario);
                    params.putString(getString(R.string.intent_senha),senha);
                    params.putString("codempresa", CodEmpresa);
                    intent.putExtras(params);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                    Bundle params = new Bundle();
                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                    params.putString("numpedido", numPedido);
                    params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                    params.putString(getString(R.string.intent_usuario),usuario);
                    params.putString(getString(R.string.intent_senha),senha);
                    params.putString("codempresa", CodEmpresa);
                    intent.putExtras(params);
                    startActivity(intent);
                    getActivity().finish();
                }
            } else if (BloqClie.equals("S")) {
                Boolean ConexOk = Util.checarConexaoCelular(getActivity());
                if (ConexOk == true) {
                    Sincronismo.SincronizarClientesStatic(codVendedor, getActivity(), usuario, senha, Integer.parseInt(CodigoClienteExterno));
                    Cursor cursorclie = DB.rawQuery("SELECT BLOQUEIO, CODCLIE_INT FROM CLIENTES WHERE CODCLIE_INT = " + CodigoClienteInterno + "", null);
                    cursorclie.moveToFirst();
                    bloqueio = cursorclie.getString(cursorclie.getColumnIndex("BLOQUEIO"));
                }
                if (bloqueio.equals("01") || bloqueio.equals("1")) {
                    if (numPedido == null) {
                        Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                        Bundle params = new Bundle();
                        params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                        params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                        params.putString("numpedido", "0");
                        params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                        params.putString(getString(R.string.intent_usuario),usuario);
                        params.putString(getString(R.string.intent_senha),senha);
                        params.putString("codempresa", CodEmpresa);
                        params.putString("dataentrega", dataEntrega);
                        intent.putExtras(params);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                        Bundle params = new Bundle();
                        params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                        params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                        params.putString("numpedido", numPedido);
                        params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                        params.putString(getString(R.string.intent_usuario),usuario);
                        params.putString(getString(R.string.intent_senha),senha);
                        params.putString("codempresa", CodEmpresa);
                        intent.putExtras(params);
                        startActivity(intent);
                        getActivity().finish();
                    }
                } else {
                    Util.msg_toast_personal(getActivity(), getString(R.string.customer_without_purchase_permission), Util.ALERTA);
                    return;
                }
            }
            if (BloqClie.equals("N")) {
                if (numPedido == null) {
                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                    Bundle params = new Bundle();
                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                    params.putString("numpedido", "0");
                    params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                    params.putString(getString(R.string.intent_usuario),usuario);
                    params.putString(getString(R.string.intent_senha),senha);
                    params.putString("codempresa", CodEmpresa);
                    intent.putExtras(params);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Intent intent = new Intent(getActivity(), CadastroPedidos.class);
                    Bundle params = new Bundle();
                    params.putInt("CLI_CODIGO", Integer.parseInt(CodigoClienteInterno));
                    params.putString((getString(R.string.intent_codvendedor)), codVendedor);
                    params.putString("numpedido", numPedido);
                    params.putString(getString(R.string.intent_urlprincipal),urlPrincipal);
                    params.putString(getString(R.string.intent_usuario),usuario);
                    params.putString(getString(R.string.intent_senha),senha);
                    params.putString("codempresa", CodEmpresa);
                    intent.putExtras(params);
                    startActivity(intent);
                    getActivity().finish();
                }
            }

            /*Intent intentp = new Intent(getActivity(), CadastroPedidos.class);
            Bundle params = new Bundle();
            params.putString(getString(R.string.intent_codcliente), CodigoClienteInterno);
            intentp.putExtras(params);
            startActivity(intentp);*/

        }
    }


    @Override
    public void onLongClickListener(View view, int position) {

    }
}

