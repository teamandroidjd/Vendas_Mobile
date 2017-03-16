package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jdsystem.br.vendasmobile.act_ListProdutos;
import com.jdsystem.br.vendasmobile.domain.FiltroProdutos;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by wks on 13/03/2017.
 */

public class ListAdapterFiltroProdutos extends RecyclerView.Adapter<ListAdapterFiltroProdutos.MyViewHolder> {

    private List<FiltroProdutos> mList;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ListAdapterFiltroProdutos(Context context, List<FiltroProdutos> Lista) {
        mList = Lista;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public ListAdapterFiltroProdutos.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.lstprodutos_card, viewGroup, false);
        ListAdapterFiltroProdutos.MyViewHolder mvh = new ListAdapterFiltroProdutos.MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(ListAdapterFiltroProdutos.MyViewHolder myViewHolder, int position) {

        myViewHolder.lblCodItem.setText(mList.get(position).getCodigoManual());
        myViewHolder.lblNomeItem.setText(mList.get(position).getDescricao());
        myViewHolder.lblUnidItem.setText(mList.get(position).getUnidVenda());

        myViewHolder.txtprecopadrao.setText(mList.get(position).getPrecoPadrao());
        myViewHolder.lblPreco.setText(mList.get(position).getPreco1());
        myViewHolder.lblpreco2.setText(mList.get(position).getPreco2());
        myViewHolder.lblpreco3.setText(mList.get(position).getPreco3());
        myViewHolder.lblpreco4.setText(mList.get(position).getPreco4());
        myViewHolder.lblpreco5.setText(mList.get(position).getPreco5());
        myViewHolder.lblprecop1.setText(mList.get(position).getPrecoP1());
        myViewHolder.lblprecop2.setText(mList.get(position).getPrecoP2());
        myViewHolder.lblApres.setText(mList.get(position).getApresentacao());

        myViewHolder.tabpadrao.setText("Tabela Padrão");
        myViewHolder.tab1.setText(mList.get(position).getTabela1 ());
        myViewHolder.tab2.setText(mList.get(position).getTabela2 ());
        myViewHolder.tab3.setText(mList.get(position).getTabela3 ());
        myViewHolder.tab4.setText(mList.get(position).getTabela4 ());
        myViewHolder.tab5.setText(mList.get(position).getTabela5 ());
        myViewHolder.tabp1.setText(mList.get(position).getTabpromo1 ());
        myViewHolder.tabp2.setText(mList.get(position).getTabpromo2());

        String QtdEstoqueNegativo = (mList.get(position).getQuantidade()).replaceAll("[1234567890,]", "").trim();
        String TipoEst = mList.get(position).getTipoEstoque();
        if (TipoEst.equals("Q")) {
            myViewHolder.lblquantidade.setText(mList.get(position).getQuantidade());
        }
        if (TipoEst.equals("D")) {
            if ((mList.get(position).getQuantidade()).equals("0,0000") || QtdEstoqueNegativo.equals("-") ) {
                myViewHolder.lblquantidade.setText("Indisponível");
            }else{
                myViewHolder.lblquantidade.setText("Disponível");
            }
        }
        if (TipoEst.equals("N")) {
            myViewHolder.layquantidade.setVisibility(View.GONE);
        }


        String Status = mList.get(position).getStatus();
        if (Status.equals("1")) {
            myViewHolder.lblStatus.setText("Ativo");
        } else {
            myViewHolder.lblStatus.setText("Inativo");
        }



    }


    @Override
    public int getItemCount() {

        return mList.size();
    }


    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    public void addListItem(FiltroProdutos c, int position) {
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
        public TextView lblStatus;
        public TextView lblApres;
        public TextView txtprecopadrao;
        public TextView lblPreco;
        public TextView lblpreco2;
        public TextView lblpreco3;
        public TextView lblpreco4;
        public TextView lblpreco5;
        public TextView lblprecop1;
        public TextView lblprecop2;
        public TextView lblquantidade;
        public TextView tabpadrao, tab1, tab2, tab3, tab4, tab5, tabp1, tabp2;
        public LinearLayout layquantidade;

        public MyViewHolder(View itemView) {
            super(itemView);

            lblCodItem = (TextView) itemView.findViewById(R.id.txt_codprod);
            lblNomeItem = (TextView) itemView.findViewById(R.id.txt_descricao);
            lblUnidItem = (TextView) itemView.findViewById(R.id.txtunvenda);
            lblPreco = (TextView) itemView.findViewById(R.id.txtpreco);
            lblStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            lblApres = (TextView) itemView.findViewById(R.id.txtapres);

            txtprecopadrao = (TextView) itemView.findViewById(R.id.txtprecopadrao);
            lblpreco2 = (TextView) itemView.findViewById(R.id.txtprecoauxiliara);
            lblpreco3 = (TextView) itemView.findViewById(R.id.txtprecoauxiliarb);
            lblpreco4 = (TextView) itemView.findViewById(R.id.txtprecoauxiliarc);
            lblpreco5 = (TextView) itemView.findViewById(R.id.txtprecoauxiliard);
            lblprecop1 = (TextView) itemView.findViewById(R.id.txtprecopromocaoa);
            lblprecop2 = (TextView) itemView.findViewById(R.id.txtprecopromocaob);
            lblquantidade = (TextView) itemView.findViewById(R.id.txt_qtdestoque);

            tabpadrao = (TextView) itemView.findViewById(R.id.tabpadrao) ;
            tab1 = (TextView) itemView.findViewById(R.id.tab1) ;
            tab2 = (TextView) itemView.findViewById(R.id.tab2) ;
            tab3 = (TextView) itemView.findViewById(R.id.tab3);
            tab4 = (TextView) itemView.findViewById(R.id.tab4);
            tab5 = (TextView) itemView.findViewById(R.id.tab5);
            tabp1 = (TextView) itemView.findViewById(R.id.tabp1);
            tabp2 = (TextView) itemView.findViewById(R.id.tabp2);

            layquantidade = (LinearLayout) itemView.findViewById(R.id.layquantidade);


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
