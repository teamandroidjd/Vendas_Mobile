package com.jdsystem.br.vendasmobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaCBean;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Rafael on 10/06/2017.
 */

public class ListAdapterPedidosClientes extends BaseAdapter {
    private Context ctx;
    private List<SqliteVendaCBean> listapedidosclientes;

    public ListAdapterPedidosClientes(Context ctx, List<SqliteVendaCBean> listapedidos) {
        this.ctx = ctx;
        this.listapedidosclientes = listapedidos;
    }

    @Override
    public int getCount() {
        return listapedidosclientes.size();
    }

    @Override
    public Object getItem(int position) {
        return listapedidosclientes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SqliteVendaCBean item = (SqliteVendaCBean) getItem(position);
        LayoutInflater layout = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View v = layout.inflate(R.layout.listview_clientespedidos, null);

        ImageView tipo = (ImageView) v.findViewById(R.id.listview_clientespedidos_tipo);
        TextView data = (TextView) v.findViewById(R.id.listview_clientespedidos_data);
        TextView pedido = (TextView) v.findViewById(R.id.listview_clientespedidos_ped);
        TextView desconto = (TextView) v.findViewById(R.id.listview_clientespedidos_desc);
        TextView valortotal = (TextView) v.findViewById(R.id.listview_clientespedidos_vltotal);

        String integrado = item.getIntegrado();
        switch (integrado){
            case "1":
                tipo.setImageResource(R.drawable.bola_vermelha);
                break;
            case "2":
                tipo.setImageResource(R.drawable.bola_verde);
                break;
            case "3":
                tipo.setImageResource(R.drawable.bola_azul);
                break;
            case "4":
                tipo.setImageResource(R.drawable.bola_preta);
                break;
            case "5":
                tipo.setImageResource(R.drawable.bola_laranja);
                break;

        }
        data.setText(Util.FormataDataDDMMAAAA(item.getVendac_datahoravenda()));
        if(!integrado.equals("2") && !integrado.equals("3")){
            pedido.setText(String.valueOf(item.getVendac_id()));
        }else {
            pedido.setText(String.valueOf(item.getNumPedErp()));
        }
        desconto.setText(String.valueOf(item.getVendac_percdesconto())+" %");
        valortotal.setText("R$ "+String.valueOf(item.getVendac_valor().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',')));
        return v;
    }
}
