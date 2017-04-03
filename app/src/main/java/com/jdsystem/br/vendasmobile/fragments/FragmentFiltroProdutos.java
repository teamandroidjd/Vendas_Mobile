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
import com.jdsystem.br.vendasmobile.actDadosProdutos;
import com.jdsystem.br.vendasmobile.act_ListProdutos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterFiltroProdutos;
import com.jdsystem.br.vendasmobile.adapter.ListProdutosAdapter;
import com.jdsystem.br.vendasmobile.domain.FiltroProdutos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FragmentFiltroProdutos extends Fragment implements RecyclerViewOnClickListenerHack {
    private RecyclerView mRecyclerView;
    private List<FiltroProdutos> mList;
    CharSequence pesqprodutos;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_produtos, container, false);

        Bundle params = getArguments();
        if (params != null) {
            pesqprodutos = params.getCharSequence("pesquisa");
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_Prod);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


        mList = ((act_ListProdutos) getActivity()).pesquisarprodutos(pesqprodutos);
        ListAdapterFiltroProdutos adapter = new ListAdapterFiltroProdutos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);


        return view;
    }


    @Override
    public void onClickListener(View view, int position) {
        ListAdapterFiltroProdutos adapter = (ListAdapterFiltroProdutos) mRecyclerView.getAdapter();

        String CodProd = adapter.ChamaDados(position);
        Intent intentp = new Intent(getActivity(), actDadosProdutos.class);
        Bundle params = new Bundle();
        params.putString(getString(R.string.intent_codproduto), CodProd);
        intentp.putExtras(params);
        startActivity(intentp);
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }

}
