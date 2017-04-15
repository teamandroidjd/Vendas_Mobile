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
import com.jdsystem.br.vendasmobile.DadosContato;
import com.jdsystem.br.vendasmobile.ConsultaContatos;
import com.jdsystem.br.vendasmobile.adapter.ListAdapterContatos;
import com.jdsystem.br.vendasmobile.domain.Contatos;
import com.jdsystem.br.vendasmobile.interfaces.RecyclerViewOnClickListenerHack;

import java.util.List;

/**
 * Created by Usuário on 03/03/2017.
 */

public class FragmentContatos extends Fragment implements RecyclerViewOnClickListenerHack {
    private RecyclerView mRecyclerView;
    private List<Contatos> mList;
    private String usuario,senha,codVendedor,urlprincipal;
    private int flag;


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_contatos, container, false);

        Bundle params = getArguments();
        if (params != null) {
            flag = params.getInt(getString(R.string.intent_flag));
            usuario = params.getString(getString(R.string.intent_usuario));
            senha = params.getString(getString(R.string.intent_senha));
            codVendedor = params.getString(getString(R.string.intent_codvendedor));
            urlprincipal = params.getString(getString(R.string.intent_urlprincipal));
        }

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
        if (flag == 0) {
            ListAdapterContatos adapterContatos = (ListAdapterContatos) mRecyclerView.getAdapter();

            int CodigoCliente = adapterContatos.ChamaCodigoContato(position);
            int CodigoContato = adapterContatos.CodigoContato(position);
            Intent intentp = new Intent(getActivity(), DadosContato.class);
            Bundle params = new Bundle();
            params.putInt("codCliente", CodigoCliente);
            params.putInt("codContato", CodigoContato);
            params.putString(getString(R.string.intent_codvendedor), codVendedor);
            params.putString(getString(R.string.intent_usuario), usuario);
            params.putString(getString(R.string.intent_senha), senha);
            params.putString(getString(R.string.intent_urlprincipal), urlprincipal);
            intentp.putExtras(params);
            startActivity(intentp);
            getActivity().finish();
        }
    }

    @Override
    public void onLongClickListener(View view, int position) {

    }
}
