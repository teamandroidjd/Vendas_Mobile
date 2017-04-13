package com.jdsystem.br.vendasmobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.act_DadosContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterContatos;
import com.jdsystem.br.vendasmobile.ConsultaContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterFiltroContatos;
import com.jdsystem.br.vendasmobile.domain.FiltroContatos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FragmentFiltroContatos extends Fragment implements RecyclerViewOnClickListenerHack {
    private RecyclerView mRecyclerView;
    private List<FiltroContatos> mList;
    CharSequence pesqcontato;
    String sCodVend, URLPrincipal, usuario, senha;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_contatos, container, false);

        Bundle params = getArguments();
        if (params != null) {
            pesqcontato = params.getCharSequence("pesquisa");
            sCodVend = params.getString(getString(R.string.intent_codvendedor));
            URLPrincipal = params.getString(getString(R.string.intent_urlprincipal));
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list_sinc);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


        mList = ((ConsultaContatos) getActivity()).listarContatos();
        ListAdapterFiltroContatos adapter = new ListAdapterFiltroContatos(getActivity(), mList);
        adapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.setAdapter(adapter);


        return view;
    }


    @Override
    public void onClickListener(View view, int position) {
        ListAdapterFiltroContatos adapterFiltroContatos = (ListAdapterFiltroContatos) mRecyclerView.getAdapter();

        int CodigoCliente =  adapterFiltroContatos.ChamaCodigoContato(position);
        int CodigoContato = adapterFiltroContatos.CodigoContato(position);
        Intent intentp = new Intent(getActivity(), act_DadosContatos.class);
        Bundle params = new Bundle();
        params.putInt("codCliente", CodigoCliente);
        params.putInt("codContato", CodigoContato);
        params.putString(getString(R.string.intent_codvendedor), sCodVend);
        params.putString(getString(R.string.intent_usuario), usuario);
        params.putString(getString(R.string.intent_senha), senha);
        params.putString(getString(R.string.intent_urlprincipal), URLPrincipal);
        intentp.putExtras(params);
        startActivity(intentp);
        getActivity().finish();
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }

}
