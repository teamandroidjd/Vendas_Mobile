package com.jdsystem.br.vendasmobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.adapter.ListProdutosAdapter;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.actDadosProdutos;
import com.jdsystem.br.vendasmobile.act_ListProdutos;
import com.jdsystem.br.vendasmobile.domain.Produtos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;


public class ProdutosFragment extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private List<Produtos> mList;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_produtos, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_Prod);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        //mList = ((act_ListProdutos) getActivity()).CarregarProdutos();
        ListProdutosAdapter adapter = new ListProdutosAdapter(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onClickListener(View view, int position) {
        ListProdutosAdapter adapter = (ListProdutosAdapter) mRecyclerView.getAdapter();

        String CodProd = adapter.ChamaDados(position);
        Intent intentp = new Intent(getActivity(), actDadosProdutos.class);
        Bundle params = new Bundle();
        params.putString("codproduto", CodProd);
        intentp.putExtras(params);
        startActivity(intentp);

    }

    @Override
    public void onLongClickListener(View view, int position) {

    }
}
