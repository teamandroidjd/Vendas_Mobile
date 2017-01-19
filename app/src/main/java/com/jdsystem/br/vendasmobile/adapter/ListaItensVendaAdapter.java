package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaDBean;
import com.jdsystem.br.vendasmobile.R;

import java.math.RoundingMode;
import java.util.List;

public class ListaItensVendaAdapter extends BaseAdapter {


    private Context ctx;
    private List<SqliteVendaDBean> listaItensTemporarios;

    public ListaItensVendaAdapter(Context ctx, List<SqliteVendaDBean> listaItensTemporarios) {
        this.ctx = ctx;
        this.listaItensTemporarios = listaItensTemporarios;
    }

    @Override
    public int getCount() {
        return listaItensTemporarios.size();
    }

    @Override
    public Object getItem(int posicao) {
        return listaItensTemporarios.get(posicao);
    }

    @Override
    public long getItemId(int posicao) {
        return posicao;
    }

    @Override
    public View getView(int posicao, View view, ViewGroup viewGroup) {

        SqliteVendaDBean item = (SqliteVendaDBean)getItem(posicao);
        LayoutInflater layout = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layout.inflate(R.layout.prod_listview_itenstemp_item,null);

        TextView descriacao = (TextView) v.findViewById(R.id.prod_lisview_descricao);
        TextView quantidade = (TextView) v.findViewById(R.id.prod_lisview_quantidade);
        TextView preco = (TextView) v.findViewById(R.id.prod_lisview_preco);
        TextView total = (TextView) v.findViewById(R.id.prod_lisview_total);

        descriacao.setText(item.getVendad_prd_codigo().toString());
        quantidade.setText(item.getVendad_quantidade().toString());

        preco.setText(item.getVendad_preco_venda().setScale(2, RoundingMode.HALF_UP).toString());
        total.setText(item.getVendad_total().setScale(2,RoundingMode.UP).toString());


        return v;
    }
}
