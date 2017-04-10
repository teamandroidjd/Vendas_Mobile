package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Mask;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListAdapterClientes extends RecyclerView.Adapter<ListAdapterClientes.MyViewHolder> {
    private List<Clientes> mList;
    //private List<Clientes> itens_exibicao;
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
        String codclieint = mList.get(position).getCodClienteInt();
        String codclieext = mList.get(position).getCodClienteExt();
        //myViewHolder.razaoSocial.setText(mList.get(position).getNomeRazao());
        //myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
        //myViewHolder.documento.setText(mList.get(position).getDocumento());
        myViewHolder.cidade.setText(mList.get(position).getCidade());
        myViewHolder.estado.setText(mList.get(position).getEstado());
        myViewHolder.bairro.setText(mList.get(position).getBairro());
        myViewHolder.telefone1.setText(mList.get(position).getTelefone1());

        if(codclieext != null){
            myViewHolder.codClieExt.setText(mList.get(position).getCodClienteExt());
            myViewHolder.bolavermelha.setVisibility(View.GONE);

        }else {
            myViewHolder.codClieInt.setText("");
            myViewHolder.bolavermelha.setVisibility(View.VISIBLE);

        }

        if (mList.get(position).getDocumento().replaceAll("[^0123456789]", "").trim().length() == 14) {
            myViewHolder.documento.setText( Mask.addMask(mList.get(position).getDocumento(), "##.###.###/####-##"));
            myViewHolder.razaoSocial.setText(mList.get(position).getNomeRazao());
            myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
        } else {
            myViewHolder.documento.setText("CPF: " + Mask.addMask(mList.get(position).getDocumento(), "###.###.###-##"));
            myViewHolder.razaoSocial.setText("Nome Completo: " + mList.get(position).getNomeRazao());
        }
        String telefone = mList.get(position).getTelefone1().replaceAll("[^0123456789]", "").trim();
        if (telefone.length() == 11) {
            myViewHolder.telefone1.setText(Mask.addMask(telefone, "(##)#####-####"));
        } else if (telefone.length() == 10) {
            myViewHolder.telefone1.setText(Mask.addMask(telefone, "(##)####-####"));
        }else{
            myViewHolder.telefone1.setText(mList.get(position).getTelefone1());
        }
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

    public String ChamaCodigoClienteInterno(int position) {

        return mList.get(position).getCodClienteInt();
    }

    public String ChamaCodigoClienteExterno(int position) {

        return mList.get(position).getCodClienteExt();
    }

    public String ChamaBloqueioCliente(int position) {

        return mList.get(position).getbloqueio();
    }
    public String ChamaFlagIntegradoCliente(int position) {

        return mList.get(position).getflagintegrado();
    }
    public String ChamaNomeRazaoCliente(int position) {

        return mList.get(position).getNomeRazao();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView bolavermelha;
        public TextView codClieExt;
        public TextView codClieInt;
        public TextView razaoSocial;
        public TextView nomeFantasia;
        public TextView rg;
        public TextView ie;
        public TextView documento;
        public TextView estado;
        public TextView cidade;
        public TextView bairro;
        public TextView telefone1;
        public TextView telefone2;

        public MyViewHolder(View itemView) {
            super(itemView);

            bolavermelha = (ImageView) itemView.findViewById(R.id.bola_vermelha);
            codClieExt = (TextView) itemView.findViewById(R.id.lblCodClie);
            codClieInt = (TextView) itemView.findViewById(R.id.lblCodClie);
            razaoSocial = (TextView) itemView.findViewById(R.id.lblNomerazao);
            nomeFantasia = (TextView) itemView.findViewById(R.id.lblNomeFanClie);
            documento = (TextView) itemView.findViewById(R.id.lblCNPJ);
            cidade = (TextView) itemView.findViewById(R.id.lblCidade);
            estado = (TextView) itemView.findViewById(R.id.lblEstado);
            bairro = (TextView) itemView.findViewById(R.id.lblBairro);
            telefone1 = (TextView) itemView.findViewById(R.id.lblTel);

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
