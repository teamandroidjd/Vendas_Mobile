package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by Usuário on 03/03/2017.
 */

public class ListAdapterContatos extends RecyclerView.Adapter<ListAdapterContatos.MyViewHolder> {
    private List<Contatos> mList;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;


    public ListAdapterContatos(Context c, List<Contatos> l) {
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.i("LOG", "onCreateViewHolder()");
        View v = mLayoutInflater.inflate(R.layout.card_contatos, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {

        Log.i("LOG", "onBindViewHolder()");

        //myViewHolder.numCnpj.setText(mList.get(position).getCnpj());
        myViewHolder.CARGO.setText(mList.get(position).getCargo());
        //myViewHolder.numEndereco.setText(mList.get(position).getNumEndereco());
        //myViewHolder.compEndereco.setText(mList.get(position).getComplemento());
        myViewHolder.EMAIL.setText(mList.get(position).getEMAIL());
        myViewHolder.TEL1.setText(mList.get(position).getTEL1());
        myViewHolder.TEL2.setText(mList.get(position).getTEL2());
        //myViewHolder.FLAGINTEGRADO.setText(mList.get(position).getTEL2());
       /* myViewHolder.telefone.setText(mList.get(position).getTelefone());
        myViewHolder.telefone2.setText(mList.get(position).getTelefone2());
        myViewHolder.email.setText(mList.get(position).getEmail());*/

        //myViewHolder.NOMERAZAO.setText(mList.get(position).getNOMECLIENTE());
        //myViewHolder.NOME.setText(mList.get(position).getNomeCont());

        String nomeContato = mList.get(position).getNomeCont();
        String nomeCliente = mList.get(position).getNOMECLIENTE();
        String flagIntegrado = mList.get(position).getFLAGINTEGRADO();
        if (nomeCliente == null){
            nomeCliente = "";
        }
        if(flagIntegrado ==null){
            flagIntegrado = "";
        }

        if (flagIntegrado.equals("S")){
            myViewHolder.FLAGINTEGRADO.setVisibility(View.GONE);
        }else{
            myViewHolder.FLAGINTEGRADO.setVisibility(View.VISIBLE);
        }

        /*if((nomeContato.isEmpty())&&(nomeCliente.isEmpty())){
            myViewHolder.NOME.setVisibility(View.GONE);
            myViewHolder.NOMERAZAO.setVisibility(View.GONE);
        } else*/
        if ((!nomeCliente.isEmpty()) && (nomeContato.isEmpty())) {
            myViewHolder.NOME.setVisibility(View.GONE);
            myViewHolder.NOMERAZAO.setText(mList.get(position).getNOMECLIENTE());
        } else if ((nomeCliente.isEmpty()) && (!nomeContato.isEmpty())) {
            myViewHolder.NOME.setText(mList.get(position).getNomeCont());
            myViewHolder.NOMERAZAO.setVisibility(View.GONE);
        } else //if ((!nomeCliente.isEmpty()) && (!nomeContato.isEmpty())){
        {
            myViewHolder.NOME.setText(mList.get(position).getNomeCont());
            myViewHolder.NOME.setVisibility(View.VISIBLE);
            myViewHolder.NOMERAZAO.setText(mList.get(position).getNOMECLIENTE());
        }


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    public void addListItem(Contatos c, int position) {
        mList.add(c);
        notifyItemInserted(position);
    }

    public int ChamaCodigoContato(int position) {

        return mList.get(position).getCODCLIEEXT();
    }

    public int CodigoContato(int position) {
        return mList.get(position).getCODCONTATO();
    }

    public String CodigoContatoExterno(int position){
        return mList.get(position).getCODCONTATO_EXT();
    }


    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public String ChamaDados(int position) {
        return mList.get(position).getNomeCont();
    }

    public String flagIntegrado(int position) {
        return mList.get(position).getFLAGINTEGRADO();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView NOME;
        private TextView CARGO;
        private TextView EMAIL;
        private TextView TEL1;
        private TextView TEL2;
        private TextView DOCUMENTO;
        private TextView DATA;
        private TextView CEP;
        private TextView ENDERECO;
        private TextView NUMERO;
        private TextView COMPLEMENTO;
        private int CODCIDADE;
        private TextView UF;
        private int CODVENDEDOR;
        private int CODBAIRRO;
        private int CODCLIENTE;
        private int CODCLIE_EXT;
        private TextView NOMERAZAO;
        private LinearLayout FLAGINTEGRADO;
        private String CODCONTATO_EXT;


        public MyViewHolder(View itemView) {
            super(itemView);


            NOME = (TextView) itemView.findViewById(R.id.lblNomeContato);
            // numCnpj = (TextView) itemView.findViewById(R.id.lblCnpj);
            CARGO = (TextView) itemView.findViewById(R.id.lblCargoContato);
            //numEndereco = (TextView) itemView.findViewById(R.id.lblNumEndereco);
            //compEndereco = (TextView) itemView.findViewById(R.id.lblCompEndereco);
            EMAIL = (TextView) itemView.findViewById(R.id.lblEmailContato);
            TEL1 = (TextView) itemView.findViewById(R.id.lblTel1Contato);
            TEL2 = (TextView) itemView.findViewById(R.id.lblTel2Contato);
            NOMERAZAO = (TextView) itemView.findViewById(R.id.lbl_nome_cliente_contato);
            FLAGINTEGRADO = (LinearLayout) itemView.findViewById(R.id.flag_integrado_cont);
            /*telefone = (TextView) itemView.findViewById(R.id.lblTel);
            telefone2 = (TextView) itemView.findViewById(R.id.lblTel2);
            email = (TextView) itemView.findViewById(R.id.lblEmail);*/


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





