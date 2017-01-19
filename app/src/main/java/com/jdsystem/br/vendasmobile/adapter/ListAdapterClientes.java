package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListAdapterClientes extends RecyclerView.Adapter<ListAdapterClientes.MyViewHolder> {
    private List<Clientes> mList;
    private List<Clientes> itens_exibicao;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;


    public ListAdapterClientes(Context c, List<Clientes> l) {
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.i("LOG", "onCreateViewHolder()");
        View v = mLayoutInflater.inflate(R.layout.lstclientes_card, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Log.i("LOG", "onBindViewHolder()");
        myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
        myViewHolder.razaoSocial.setText(mList.get(position).getNomeRazao());
        myViewHolder.documento.setText(mList.get(position).getDocumento());
        myViewHolder.cidade.setText(mList.get(position).getCidade());
        myViewHolder.estado.setText(mList.get(position).getEstado());
        myViewHolder.bairro.setText(mList.get(position).getBairro());
        myViewHolder.telefone.setText(mList.get(position).getTelefone());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r) {
        mRecyclerViewOnClickListenerHack = r;
    }


    public void addListItem(Clientes c, int position) {
        mList.add(c);
        notifyItemInserted(position);
    }


    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public String ChamaDados(int position) {

        return mList.get(position).getDocumento();
    }

    public String ChamaCodigoCliente(int position) {

        return mList.get(position).getCodCliente();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nomeFantasia;
        public TextView razaoSocial;
        public TextView documento;
        public TextView cidade;
        public TextView estado;
        public TextView bairro;
        public TextView telefone;

        public MyViewHolder(View itemView) {
            super(itemView);

            nomeFantasia = (TextView) itemView.findViewById(R.id.lblNomeFanClie);
            razaoSocial = (TextView) itemView.findViewById(R.id.lblNomerazao);
            documento = (TextView) itemView.findViewById(R.id.lblCNPJ);
            cidade = (TextView) itemView.findViewById(R.id.lblCidade);
            estado = (TextView) itemView.findViewById(R.id.lblEstado);
            bairro = (TextView) itemView.findViewById(R.id.lblBairro);
            telefone = (TextView) itemView.findViewById(R.id.lblTel);

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
