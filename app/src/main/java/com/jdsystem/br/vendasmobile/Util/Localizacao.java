package com.jdsystem.br.vendasmobile.Util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jdsystem.br.vendasmobile.ConfigDB;
import com.jdsystem.br.vendasmobile.R;
import com.jdsystem.br.vendasmobile.Sincronismo;
import com.jdsystem.br.vendasmobile.Util.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.security.AccessController.getContext;
import static java.util.Arrays.asList;

/**
 * Created by WKS22 on 05/06/2017.
 */

public class Localizacao extends AppCompatActivity {

    Handler handler;
    ProgressDialog Dialog;
    Context ctx;
    String strUF;
    public int estado = 0;
    public int cidade = 0;
    public int bairro = 0;

    public ArrayAdapter<String> Cidades(final Context context, final String sUF, Spinner spinner, ProgressDialog Dialog) {

        SQLiteDatabase DB = new ConfigDB(context).getReadableDatabase();
        ArrayAdapter<String> spinnerArrayAdapter = null;
        List<String> listaCidades = new ArrayList<>();
        listaCidades.add("Selecione a cidade");
        ProgressDialog dialog;
        ctx = context;
        strUF = sUF;

        try {
            if (sUF != null) {
                Cursor cursor = DB.rawQuery("SELECT CIDADES.UF, CIDADES.DESCRICAO AS DESC, CIDADES.CODCIDADE, CIDADES.CODCIDADE_EXT" +
                        " FROM CIDADES WHERE UF = '" + sUF + "'", null);

                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    do {
                        String cidades = cursor.getString(cursor.getColumnIndex("DESC"));
                        listaCidades.add(cidades);
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, listaCidades);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                //spinner.setAdapter(spinnerArrayAdapter);
                if (Dialog.isShowing())
                    Dialog.dismiss();
                return spinnerArrayAdapter;
            }
        } catch (Exception e) {
            e.toString();
            if (Dialog.isShowing()) {
                Dialog.dismiss();
            }
        }
        if (Dialog.isShowing()) {
            Dialog.dismiss();
        }
        return spinnerArrayAdapter;
    }

    public ArrayAdapter<String> Bairros(Context context, String NomeCidade, Spinner spinner, Dialog Dialog) {

        SQLiteDatabase DB = new ConfigDB(context).getReadableDatabase();
        ArrayAdapter<String> spinnerArrayAdapter = null;
        List<String> listaBairro = new ArrayList<>();
        listaBairro.add("Selecione o bairro");
        ProgressDialog dialog;
        int codCidadeInt = 0, codCidadeExt = 0, codBairro = 0;

        try {
            Cursor cursor = DB.rawQuery(" SELECT CODCIDADE, DESCRICAO, CODCIDADE_EXT FROM CIDADES WHERE DESCRICAO = '" + NomeCidade + "'", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                codCidadeExt = cursor.getInt(cursor.getColumnIndex("CODCIDADE_EXT"));
                codCidadeInt = cursor.getInt(cursor.getColumnIndex("CODCIDADE"));

                cursor.close();
            }
        } catch (Exception e) {
            e.toString();
        }
        try {
            Cursor cursorBairros = DB.rawQuery(" SELECT CODCIDADE, CODBAIRRO, DESCRICAO FROM BAIRROS WHERE CODCIDADE = " + codCidadeExt, null);
            cursorBairros.moveToFirst();

            if (cursorBairros.getCount() > 0) {
                do {
                    String nomeBairro = cursorBairros.getString(cursorBairros.getColumnIndex("DESCRICAO"));
                    listaBairro.add(nomeBairro);
                } while (cursorBairros.moveToNext());
            }

            spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, listaBairro);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
            //spinner.setAdapter(spinnerArrayAdapter);
            if (Dialog.isShowing())
                Dialog.dismiss();
            return spinnerArrayAdapter;
        } catch (Exception e) {
            e.toString();
            if (Dialog.isShowing())
                Dialog.dismiss();
        }
        if (Dialog.isShowing())
            Dialog.dismiss();

        return spinnerArrayAdapter;
    }

    public int retornaCodContatoExt(Context ctx, String NomeCidade, String sUF) {
        SQLiteDatabase DB = new ConfigDB(ctx).getReadableDatabase();
        int codCidade = 0;
        Cursor cursor = DB.rawQuery(" SELECT CODCIDADE_EXT, CODCIDADE FROM CIDADES WHERE DESCRICAO = '" + NomeCidade +
                "' AND UF = '" + sUF + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            codCidade = cursor.getInt(cursor.getColumnIndex("CODCIDADE_EXT"));
        }
        return codCidade;
    }

    public int retornaPositionUf(Context ctx, String uf, int CodContatoInt) {
        SQLiteDatabase DB = new ConfigDB(ctx).getReadableDatabase();
        String sUF;
        int codPositionUF = 0;
        try {
            Cursor cursor = DB.rawQuery("SELECT UF FROM CONTATO_TEMPORARIO", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                sUF = cursor.getString(cursor.getColumnIndex("UF"));
                //return codPositionUF;
                List<String> arrayList =  Arrays.<String>asList(String.valueOf(R.array.uf));
                int i=0;
                do {
                    i++;
                }while(arrayList.get(i).toString().equals(sUF));
            }


        } catch (Exception e) {
            e.toString();
        }

        return codPositionUF;
    }

    public int retornaPosicaoCidade(Context ctx, String nomeCidade, String uf){
        int posicao = 0;
        String cidade = "";
        SQLiteDatabase DB = new ConfigDB(ctx).getReadableDatabase();
        Cursor cursor = DB.rawQuery("SELECT CIDADES.UF, CIDADES.DESCRICAO AS DESC, CIDADES.CODCIDADE, CIDADES.CODCIDADE_EXT" +
                " FROM CIDADES WHERE UF = '" + uf + "'", null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
                for (int i = 0;i<cursor.getCount();i++) {
                    cidade = cursor.getString(cursor.getColumnIndex("DESC"));
                    if(cidade.equals(nomeCidade)){
                        posicao = i+1;
                        break;
                    }
                    cursor.moveToNext();
                }
        }
            return posicao;
    }
}