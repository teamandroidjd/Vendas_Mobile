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
import com.jdsystem.br.vendasmobile.DadosCliente;
import com.jdsystem.br.vendasmobile.act_DadosContatos;
import com.jdsystem.br.vendasmobile.ConsultaContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterClientes;
import com.jdsystem.br.vendasmobile.ConsultaContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterFiltroContatos;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FragmentContatos extends Fragment implements RecyclerViewOnClickListenerHack {
    private RecyclerView mRecyclerView;
    private List<Contatos> mList;


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_contatos, container, false);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_sinc);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


        mList = ((ConsultaContatos) getActivity()).carregarcontatos();
        ListAdapterContatos adapter = new ListAdapterContatos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);


        return view;
    }


    @Override
    public void onClickListener(View view, int position) {
        ListAdapterContatos adapterContatos = (ListAdapterContatos) mRecyclerView.getAdapter();

        int CodigoCliente = adapterContatos.ChamaCodigoContato(position);
        int CodigoContato = adapterContatos.CodigoContato(position);
        Intent intentp = new Intent(getActivity(), act_DadosContatos.class);
        Bundle params = new Bundle();
        params.putInt("codCliente", CodigoCliente);
        params.putInt("codContato", CodigoContato);
        intentp.putExtras(params);
        startActivity(intentp);
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }
}
