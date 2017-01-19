package com.jdsystem.br.vendasmobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.actDadosCliente;
import com.jdsystem.br.vendasmobile.act_ListClientes;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.domain.Clientes;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by WKS22 on 29/11/2016.
 */

public class FragmentCliente extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<Clientes> mList;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cliente, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

       // mList = ((act_ListClientes) getActivity()).CarregarClientes();
        ListAdapterClientes adapter = new ListAdapterClientes(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClickListener(View view, int position) {
        ListAdapterClientes adapter = (ListAdapterClientes) mRecyclerView.getAdapter();

        String CodigoCliente =  adapter.ChamaCodigoCliente(position);
        Intent intentp = new Intent(getActivity(), actDadosCliente.class);
        Bundle params = new Bundle();
        params.putString("codCliente", CodigoCliente);
        intentp.putExtras(params);
        startActivity(intentp);
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }
}

