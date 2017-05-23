package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.Mask;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

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
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Log.i("LOG", "onBindViewHolder()");
        String codclieint = mList.get(position).getCodClienteInt();
        String codclieext = mList.get(position).getCodClienteExt();
        String estado = String.valueOf(mList.get(position).getEstado());
        String cidade = String.valueOf(mList.get(position).getCidade());
        String bairro = String.valueOf(mList.get(position).getBairro());
        //myViewHolder.razaoSocial.setText(mList.get(position).getNomeRazao());
        //myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
        //myViewHolder.documento.setText(mList.get(position).getDocumento());
        //myViewHolder.cidade.setText(mList.get(position).getCidade());
        if (!cidade.equals("NULL") && !cidade.equals("null")) {
            myViewHolder.cidade.setText(mList.get(position).getCidade());
        } else {
            myViewHolder.cidade.setText("");
        }
        if (!bairro.equals("NULL") && !bairro.equals("null")) {
            myViewHolder.bairro.setText(mList.get(position).getBairro());
        } else {
            myViewHolder.bairro.setText("");
        }
        if (!estado.equals("NULL") && !estado.equals("null")) {
            myViewHolder.estado.setText(mList.get(position).getEstado());
        } else {
            myViewHolder.estado.setText("");
        }

        myViewHolder.telefone1.setText(mList.get(position).getTelefone1());

        if (codclieext != null) {
            myViewHolder.codClie.setText(mList.get(position).getCodClienteExt());
            myViewHolder.bolavermelha.setVisibility(View.GONE);

        } else {
            myViewHolder.codClie.setVisibility(View.GONE);
            myViewHolder.bolavermelha.setVisibility(View.VISIBLE);

        }

        if (mList.get(position).getDocumento() == null || mList.get(position).getDocumento().equals("")) {
            myViewHolder.documento.setText("CPF/CNPJ: ");
            myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
            myViewHolder.razaoSocial.setVisibility(View.GONE);
        } else if (mList.get(position).getDocumento().replaceAll("[^0123456789]", "").trim().length() == 14) {
            myViewHolder.documento.setText("CNPJ: " + Mask.addMask(mList.get(position).getDocumento(), "##.###.###/####-##"));
            myViewHolder.razaoSocial.setText(mList.get(position).getNomeRazao());
            myViewHolder.nomeFantasia.setText(mList.get(position).getNomeFan());
            myViewHolder.razaoSocial.setVisibility(View.VISIBLE);
        } else {
            myViewHolder.documento.setText("CPF: " + Mask.addMask(mList.get(position).getDocumento(), "###.###.###-##"));
            myViewHolder.nomeFantasia.setText(mList.get(position).getNomeRazao());
            myViewHolder.razaoSocial.setVisibility(View.GONE);
        }
        String telefone = mList.get(position).getTelefone1().replaceAll("[^0123456789]", "").trim();
        if (telefone.length() == 11) {
            myViewHolder.telefone1.setText("Celular: " + Mask.addMask(telefone, "(##)#####-####"));
        } else if (telefone.length() == 10) {
            myViewHolder.telefone1.setText("Telefone: " + Mask.addMask(telefone, "(##)####-####"));
        } else {
            myViewHolder.telefone1.setText("Telefone: " + mList.get(position).getTelefone1());
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView bolavermelha;
        public TextView codClie;
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
            codClie = (TextView) itemView.findViewById(R.id.lblCodClie);
            razaoSocial = (TextView) itemView.findViewById(R.id.lblNomerazao);
            nomeFantasia = (TextView) itemView.findViewById(R.id.lblNomeFanClie);
            documento = (TextView) itemView.findViewById(R.id.lblCNPJ);
            cidade = (TextView) itemView.findViewById(R.id.lblCidade);
            estado = (TextView) itemView.findViewById(R.id.lblEstado);
            bairro = (TextView) itemView.findViewById(R.id.lblBairro);
            telefone1 = (TextView) itemView.findViewById(R.id.lblTel);

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
