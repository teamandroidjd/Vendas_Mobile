package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Pedidos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

public class ListAdapterPedidos extends RecyclerView.Adapter<ListAdapterPedidos.MyViewHolder> {
    private List<Pedidos> mList;

    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ListAdapterPedidos(Context c, List<Pedidos> l) {
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.i("LOG", "onCreateViewHolder()");
        View v = mLayoutInflater.inflate(R.layout.card_pedidos, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Log.i("LOG", "onBindViewHolder()");
        myViewHolder.dtemissao.setText(mList.get(position).getDataVenda());
        myViewHolder.lblNomeCliente.setText(mList.get(position).getNomeCliente());
        myViewHolder.lblStatusPed.setText(mList.get(position).getSituacao());
        myViewHolder.lblValorTotal.setText(mList.get(position).getValorTotal());
        myViewHolder.lblNumPedido.setText(mList.get(position).getNumPedido());
        myViewHolder.lblVendedor.setText(mList.get(position).getVendedor());
        myViewHolder.lblEmpresa.setText(mList.get(position).getEmpresa());

        String Status = mList.get(position).getSituacao();

        if (Status.equals("Orçamento")) {
            myViewHolder.imgStatus.setImageResource(R.drawable.bola_vermelha);
            myViewHolder.lblNPedidoExt.setVisibility(View.INVISIBLE);
            myViewHolder.lblPed.setVisibility(View.INVISIBLE);
            myViewHolder.lblNPedidoExt.setText(mList.get(position).getNumPedidoExt());
        } else if (Status.equals("Faturado")) {
            myViewHolder.imgStatus.setImageResource(R.drawable.bola_azul);
            myViewHolder.lblNPedidoExt.setVisibility(View.VISIBLE);
            myViewHolder.lblPed.setVisibility(View.VISIBLE);
            myViewHolder.lblPed.setText("NFe");
            myViewHolder.lblNPedidoExt.setText(mList.get(position).getNumFiscal());
        } else if (Status.equals("#")) {
            myViewHolder.imgStatus.setImageResource(R.drawable.bola_verde);
            myViewHolder.lblNPedidoExt.setVisibility(View.VISIBLE);
            myViewHolder.lblPed.setVisibility(View.VISIBLE);
            myViewHolder.lblNPedidoExt.setText(mList.get(position).getNumPedidoExt());
        } else if (Status.equals("Cancelado")) {
            myViewHolder.imgStatus.setImageResource(R.drawable.bola_preta);
            myViewHolder.lblNPedidoExt.setVisibility(View.INVISIBLE);
            myViewHolder.lblPed.setVisibility(View.INVISIBLE);
        } else if (Status.equals("Gerar Venda")) {
            myViewHolder.imgStatus.setImageResource(R.drawable.bola_laranja);
            myViewHolder.lblNPedidoExt.setVisibility(View.INVISIBLE);
            myViewHolder.lblPed.setVisibility(View.INVISIBLE);
            myViewHolder.lblNPedidoExt.setText(mList.get(position).getNumPedidoExt());
        }

    }

    @Override
    public int getItemCount() {

        return mList.size();
    }


    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    public String ChamaDados(int position) {
        return mList.get(position).getNumPedido();
    }

    public String ChamaNomeVendedor(int position) {
        return mList.get(position).getVendedor();
    }

    public String PedidoExterno(int position) {
        return mList.get(position).getNumPedidoExt();
    }

    public String StatusPedido(int position) {
        return mList.get(position).getSituacao();
    }

    public void addListItem(Pedidos c, int position) {
        mList.add(c);
        notifyItemInserted(position);
    }


    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView dtemissao;
        public TextView lblStatusPed;
        public TextView lblNumPedido;
        public TextView lblNomeCliente;
        public TextView lblValorTotal;
        public TextView lblVendedor;
        public TextView lblPed;
        public TextView lblNPedidoExt;
        public TextView lblEmpresa;
        public ImageView imgStatus;

        public MyViewHolder(View itemView) {
            super(itemView);

            dtemissao = (TextView) itemView.findViewById(R.id.dtemissao);
            lblStatusPed = (TextView) itemView.findViewById(R.id.lblStatusPed);
            lblNumPedido = (TextView) itemView.findViewById(R.id.lblNumPedido);
            lblNomeCliente = (TextView) itemView.findViewById(R.id.lblNomeCliente);
            lblValorTotal = (TextView) itemView.findViewById(R.id.lblValorTotal);
            lblVendedor = (TextView) itemView.findViewById(R.id.lblVendedor);
            lblPed = (TextView) itemView.findViewById(R.id.lblPed);
            lblNPedidoExt = (TextView) itemView.findViewById(R.id.lblNPedidoExt);
            lblEmpresa = (TextView) itemView.findViewById(R.id.lblEmpresa);
            imgStatus = (ImageView) itemView.findViewById(R.id.imgStatus);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mRecyclerViewOnClickListenerHack != null) {
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mRecyclerViewOnClickListenerHack != null) {
                mRecyclerViewOnClickListenerHack.onLongClickListener(v, getPosition());
            }
            return true;
        }
    }

}
