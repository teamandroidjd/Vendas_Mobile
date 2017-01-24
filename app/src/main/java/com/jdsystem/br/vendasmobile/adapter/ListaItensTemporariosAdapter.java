package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Model.SqliteVendaD_TempBean;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.ItensPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ListaItensTemporariosAdapter extends BaseAdapter {


    private Context ctx;
    private List<SqliteVendaD_TempBean> listaItensTemporarios;

    public ListaItensTemporariosAdapter(Context ctx, List<SqliteVendaD_TempBean> listaItensTemporarios) {
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

        SqliteVendaD_TempBean item = (SqliteVendaD_TempBean)getItem(posicao);
        LayoutInflater layout = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layout.inflate(R.layout.prod_listview_itenstemp_item,null);

        TextView descriacao = (TextView) v.findViewById(R.id.prod_lisview_descricao);
        TextView quantidade = (TextView) v.findViewById(R.id.prod_lisview_quantidade);
        TextView preco = (TextView) v.findViewById(R.id.prod_lisview_preco);
        TextView total = (TextView) v.findViewById(R.id.prod_lisview_total);

        descriacao.setText(item.getVendad_prd_descricaoTEMP().toString());
        quantidade.setText(item.getVendad_quantidadeTEMP().toString());

        preco.setText(item.getVendad_preco_vendaTEMP().setScale(4, BigDecimal.ROUND_HALF_UP).toString().replace('.',','));
        total.setText(item.getVendad_totalTEMP().setScale(2,BigDecimal.ROUND_HALF_UP).toString().replace('.',','));


        return v;
    }
}
