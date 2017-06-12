package com.jdsystem.br.vendasmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.Model.SqliteVendaDao;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterItensPedidosClientes;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterPedidosClientes;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TH_ClientesXVendas extends Fragment {
    public static final String CONFIG_HOST = "CONFIG_HOST";
    public SharedPreferences prefs;
    String CodCliente, codVendedor, URLPrincipal, usuario, senha;
    SQLiteDatabase DB;
    private Activity act;
    Context ctx;
    View v;
    private SqliteVendaDao vendaDao;
    private SqliteVendaCBean vendaCBean;
    private List<SqliteVendaDBean> itensPed = new ArrayList<>();;
    private List<SqliteVendaCBean> cliePed = new ArrayList<>();
    private ListView listViewCliePed, listViewCliePedItens;
    private int idPerfil;
    private LinearLayout legendapedXClie, legendaItensXPedXClie;
    private RelativeLayout leganda_tipopedido;
    private TextView txvinfopedxclie;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.clientes_vendas, container, false);

        declaraObjetos();
        carreegaPreferencias();

        Intent intent = ((DadosCliente) getActivity()).getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                CodCliente = params.getString(getString(R.string.intent_codcliente));
                codVendedor = params.getString(getString(R.string.intent_codvendedor));
                URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
                usuario = params.getString(getString(R.string.intent_usuario));
                senha = params.getString(getString(R.string.intent_senha));
            }
        }
        carregaPedidos();



        return v;
    }

    private void carregaPedidos() {
        cliePed = new SqliteVendaDao(getContext(),codVendedor,false).lista_pedidos_do_cliente(Integer.parseInt(CodCliente));
        if(cliePed.isEmpty()){
            legendapedXClie.setVisibility(View.GONE);
            listViewCliePed.setVisibility(View.GONE);
            legendaItensXPedXClie.setVisibility(View.GONE);
            listViewCliePedItens.setVisibility(View.GONE);
            leganda_tipopedido.setVisibility(View.GONE);
            txvinfopedxclie.setVisibility(View.VISIBLE);
        }
        listViewCliePed.setAdapter(new ListAdapterPedidosClientes(getContext(), cliePed));
        listViewCliePed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private int save = -1;
            @Override
            public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
                listview1.getChildAt(posicao).setBackgroundResource(R.color.colorAccent);
                if (save != -1 && save != posicao){
                    listview1.getChildAt(save).setBackgroundResource(R.color.branco);
                }
                save = posicao;
                carregaItens(listview1,posicao);
            }
        });
    }
    private void carregaItens(final AdapterView<?> listview1, final int posicao){
        final SqliteVendaCBean pedido = (SqliteVendaCBean) listview1.getItemAtPosition(posicao);
        itensPed = new SqliteVendaDao(getContext(),codVendedor,false).buscar_itens_vendas_por_numeropedido(pedido.getVendac_chave());
        listViewCliePedItens.setAdapter(new ListAdapterItensPedidosClientes(getContext(), itensPed));
        listViewCliePedItens.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listview1, View v, int posicao, long m) {
            }
        });

    }

    private void declaraObjetos() {
        ctx = getContext();
        DB = new ConfigDB(ctx).getReadableDatabase();
        listViewCliePed = (ListView) v.findViewById(R.id.lstinfopedidos);
        listViewCliePedItens = (ListView) v.findViewById(R.id.lstinfoitens);
        legendapedXClie = (LinearLayout) v.findViewById(R.id.legenda_cliexped);
        legendaItensXPedXClie = (LinearLayout) v.findViewById(R.id.legenda_itensxpedxclie);
        leganda_tipopedido = (RelativeLayout) v.findViewById(R.id.leganda_tipopedido);
        txvinfopedxclie = (TextView) v.findViewById(R.id.txvinfopedxclie);

    }

    private void carreegaPreferencias() {
        prefs = ctx.getSharedPreferences(CONFIG_HOST, Context.MODE_PRIVATE);
        URLPrincipal = prefs.getString("host", null);
        idPerfil = prefs.getInt("idperfil", 0);
    }
}
