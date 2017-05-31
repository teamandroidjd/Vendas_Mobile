package com.jdsystem.br.vendasmobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteConfPagamentoBean;
import com.jdsystem.br.vendasmobile.R;

import java.math.BigDecimal;
import java.util.List;

public class ListAdapterFormpgtoTemp extends BaseAdapter {


    private Context ctx;
    private List<SqliteConfPagamentoBean> listaformpgtoTemporarios;

    public ListAdapterFormpgtoTemp(Context ctx, List<SqliteConfPagamentoBean> listaformTemporarios) {
        this.ctx = ctx;
        this.listaformpgtoTemporarios = listaformTemporarios;
    }

    @Override
    public int getCount() {
        return listaformpgtoTemporarios.size();
    }

    @Override
    public Object getItem(int posicao) {
        return listaformpgtoTemporarios.get(posicao);
    }

    @Override
    public long getItemId(int posicao) {
        return posicao;
    }

    @Override
    public View getView(int posicao, View view, ViewGroup viewGroup) {

        SqliteConfPagamentoBean item = (SqliteConfPagamentoBean) getItem(posicao);
        LayoutInflater layout = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View v = layout.inflate(R.layout.form_pgto_listview_parcelas, null);

        TextView parcela = (TextView) v.findViewById(R.id.pgto_lisview_parcela);
        TextView descricaoformpgto = (TextView) v.findViewById(R.id.pgto_lisview_descformpgto);
        TextView diasvenc = (TextView) v.findViewById(R.id.pgto_lisview_diasvencimento);
        TextView datavencimento = (TextView) v.findViewById(R.id.pgto_lisview_datavencimento);
        TextView valorparcela = (TextView) v.findViewById(R.id.pgto_lisview_valorparcela);

        parcela.setText(item.getConf_parcelas().toString());
        descricaoformpgto.setText(item.getConf_descformpgto());
        diasvenc.setText(item.getConf_diasvencimento().toString());
        datavencimento.setText(item.getConf_datavencimento());
        valorparcela.setText(item.getConf_valor_recebido().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ','));


        return v;
    }
}
