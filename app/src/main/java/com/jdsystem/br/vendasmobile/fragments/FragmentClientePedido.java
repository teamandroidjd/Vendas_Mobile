package com.jdsystem.br.vendasmobile.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.act_ListClientesPedidos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientesPedido;
import com.jdsystem.br.vendasmobile.domain.ClientesPedido;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentClientePedido extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<ClientesPedido> mList;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cliente, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mList = ((act_ListClientesPedidos) getActivity()).CarregarClientesPedidos();
        ListAdapterClientesPedido adapter = new ListAdapterClientesPedido(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClickListener(View view, int position) {
        ListAdapterClientesPedido adapter = (ListAdapterClientesPedido) mRecyclerView.getAdapter();

        String Documento =  adapter.ChamaDados(position);


    }

    @Override
    public void onLongClickListener(View view, int position) {

    }
}

