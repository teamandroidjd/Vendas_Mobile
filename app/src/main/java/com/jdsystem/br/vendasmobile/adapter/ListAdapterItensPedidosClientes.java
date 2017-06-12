package com.jdsystem.br.vendasmobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.R;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Rafael on 10/06/2017.
 */

public class ListAdapterItensPedidosClientes extends BaseAdapter {
    private Context ctx;
    private List<SqliteVendaDBean> listaItensPedidosClientes;

    public ListAdapterItensPedidosClientes(Context ctx, List<SqliteVendaDBean> listaItensPedidos) {
        this.ctx = ctx;
        this.listaItensPedidosClientes = listaItensPedidos;
    }

    @Override
    public int getCount() {
        return listaItensPedidosClientes.size();
    }

    @Override
    public Object getItem(int position) {
        return listaItensPedidosClientes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SqliteVendaDBean item = (SqliteVendaDBean) getItem(position);
        LayoutInflater layout = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View v = layout.inflate(R.layout.listview_itensxcliexped, null);

        TextView codItem = (TextView) v.findViewById(R.id.listview_itensxclie_coditem);
        TextView descItem = (TextView) v.findViewById(R.id.listview_itensxclie_descitem);
        TextView qtdItem = (TextView) v.findViewById(R.id.listview_itensxclie_qtditem);
        TextView vlUnitItem = (TextView) v.findViewById(R.id.listview_itensxclie_vlunititem);
        TextView vlTotalItem = (TextView) v.findViewById(R.id.listview_itensxclie_vltotalitem);

        codItem.setText(String.valueOf(item.getVendad_prd_codigo()));
        descItem.setText(item.getVendad_prd_descricao());
        qtdItem.setText(String.valueOf(item.getVendad_quantidade()));
        vlUnitItem.setText(String.valueOf(item.getVendad_preco_venda().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',')));
        vlTotalItem.setText(String.valueOf(item.getVendad_total().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',')));
        return v;
    }
}
