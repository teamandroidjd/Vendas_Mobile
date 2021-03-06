package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Util.Util;
import com.jdsystem.br.vendasmobile.domain.Agenda;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;
/**
 * Created by wks on 01/06/2017.
 */

public class ListAdapterAgenda extends RecyclerView.Adapter<ListAdapterAgenda.MyViewHolder> {
    private List<Agenda> mList;

    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ListAdapterAgenda(Context c, List<Agenda> l) {
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.i("LOG", "onCreateViewHolder()");
        View v = mLayoutInflater.inflate(R.layout.card_agenda, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Log.i("LOG", "onBindViewHolder()");
        try {
            myViewHolder.lblNomeContato.setText(mList.get(position).getNomeContato());
            myViewHolder.lblNumAgenda.setText(mList.get(position).getNumAgenda());
            myViewHolder.lblreagendado.setText(mList.get(position).getDtreagendado());
            myViewHolder.lblnovaagenda.setText(mList.get(position).getNovaagenda());

            String data = mList.get(position).getDataagendamento();
            myViewHolder.dtagendamento.setText(Util.FormataDataDDMMAAAA_ComHoras(data));

            Integer Situacao = mList.get(position).getSituacao();
            if (Situacao == 1) {
                myViewHolder.lblsituacao.setText("Agendado");
                myViewHolder.lblsituacao.setTextColor(Color.RED);
                myViewHolder.llnovaagenda.setVisibility(View.GONE);
                myViewHolder.llreagendar.setVisibility(View.GONE);
            } else if (Situacao == 2) {
                myViewHolder.lblsituacao.setText("Finalizado");
                myViewHolder.llnovaagenda.setVisibility(View.GONE);
                myViewHolder.llreagendar.setVisibility(View.GONE);
            } else if (Situacao == 3) {
                myViewHolder.lblsituacao.setText("Cancelado");
                myViewHolder.lblsituacao.setTextColor(Color.BLACK);
                myViewHolder.llnovaagenda.setVisibility(View.GONE);
                myViewHolder.llreagendar.setVisibility(View.GONE);
            } else if (Situacao == 4) {
                myViewHolder.lblsituacao.setText("Reagendado");
                myViewHolder.lblsituacao.setTextColor(Color.YELLOW);
                myViewHolder.llnovaagenda.setVisibility(View.VISIBLE);
                myViewHolder.llreagendar.setVisibility(View.VISIBLE);
            }

            String Status = mList.get(position).getStatus();
            if (Status.equals("N")) {
                myViewHolder.imgStatus.setImageResource(R.drawable.bola_vermelha);
            } else if (Status.equals("S")) {
                myViewHolder.imgStatus.setImageResource(R.drawable.bola_verde);
            }
        } catch (Exception e) {
            e.toString();
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
        return mList.get(position).getNumAgenda();
    }

    public String StatusAgenda(int position) {
        return mList.get(position).getStatus();
    }

    public Integer SituacaoAgenda(int position) {
        return mList.get(position).getSituacao();
    }

    public String ChamaNovaAgenda(int position) {
        return mList.get(position).getNovaagenda();
    }

    public void addListItem(Agenda c, int position) {
        mList.add(c);
        notifyItemInserted(position);
    }

    public void removeListItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView dtagendamento;
        public TextView lblNumAgenda;
        public TextView lblNomeContato;
        public TextView lblsituacao;
        public TextView lblreagendado;
        public TextView lblnovaagenda;
        public ImageView imgStatus;
        public LinearLayout llnovaagenda;
        public LinearLayout llreagendar;

        public MyViewHolder(View itemView) {
            super(itemView);

            dtagendamento = (TextView) itemView.findViewById(R.id.dtagenda);
            lblNumAgenda = (TextView) itemView.findViewById(R.id.lblNumAgenda);
            lblNomeContato = (TextView) itemView.findViewById(R.id.lblNomeContato);
            lblsituacao = (TextView) itemView.findViewById(R.id.lblsituacao);
            imgStatus = (ImageView) itemView.findViewById(R.id.imgStatus);
            lblreagendado= (TextView) itemView.findViewById(R.id.lblreagendado);
            lblnovaagenda= (TextView) itemView.findViewById(R.id.lblnovaagenda);
            llnovaagenda= (LinearLayout) itemView.findViewById(R.id.llnovaagenda);
            llreagendar= (LinearLayout) itemView.findViewById(R.id.llreagendar);

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

