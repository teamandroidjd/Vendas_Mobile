package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.ItensPedido;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by eduardo.costa on 25/11/2016.
 */

public class ListProdutosAdapter extends RecyclerView.Adapter<ListProdutosAdapter.MyViewHolder> {

    private List<Produtos> mList;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ListProdutosAdapter(Context context, List<Produtos> Lista) {
        mList = Lista;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.lstprodutos_card, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {

        myViewHolder.lblCodItem.setText(mList.get(position).getCodigoManual());
        myViewHolder.lblNomeItem.setText(mList.get(position).getDescricao());
        myViewHolder.lblUnidItem.setText(mList.get(position).getUnidVenda());
        myViewHolder.lblPreco.setText(mList.get(position).getPreco());
        myViewHolder.lblStatus.setText(mList.get(position).getStatus());
        myViewHolder.lblApres.setText(mList.get(position).getApresentacao());

    }


    @Override
    public int getItemCount() {

        return mList.size();
    }


    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    public void addListItem(Produtos c, int position) {
        mList.add(c);
        notifyItemInserted(position);
    }

    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public String ChamaDados(int position) {
        return mList.get(position).getCodigoManual();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView lblCodItem;
        public TextView lblNomeItem;
        public TextView lblUnidItem;
        public TextView lblPreco;
        public TextView lblStatus;
        public TextView lblApres;

        public MyViewHolder(View itemView) {
            super(itemView);

            lblCodItem = (TextView) itemView.findViewById(R.id.txt_codprod);
            lblNomeItem = (TextView) itemView.findViewById(R.id.txt_descricao);
            lblUnidItem = (TextView) itemView.findViewById(R.id.txtunvenda);
            lblPreco = (TextView) itemView.findViewById(R.id.txtpreco);
            lblStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            lblApres = (TextView) itemView.findViewById(R.id.txtapres);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mRecyclerViewOnClickListenerHack != null) {
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }
    }
}
