package com.jdsystem.br.vendasmobile.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.jdsystem.br.vendasmobile.ConfigDB;

public class SqliteParametroDao {


    private Context ctx;
    private boolean gravacao;


    public SqliteParametroDao(Context ctx) {
        this.ctx = ctx;
    }

    public SqliteParametroBean busca_parametros() {

        SQLiteDatabase db = new ConfigDB(ctx).getReadableDatabase();
        SqliteParametroBean parametro = null;

        String sql = "select * from PARAMAPP";

        try {

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                try {

                    parametro = new SqliteParametroBean();
                    parametro.setP_usu_codigo(cursor.getInt(cursor.getColumnIndex(parametro.P_CODIGO_USUARIO)));
                    parametro.setP_importar_todos_clientes(cursor.getString(cursor.getColumnIndex(parametro.P_IMPORTAR_TODOS_CLIENTES)));
                    parametro.setP_end_ip_local(cursor.getString(cursor.getColumnIndex(parametro.P_ENDERECO_IP_LOCAL)));
                    parametro.setP_end_ip_remoto(cursor.getString(cursor.getColumnIndex(parametro.P_ENDERECO_IP_REMOTO)));
                    parametro.setP_trabalhar_com_estoque_negativo(cursor.getString(cursor.getColumnIndex(parametro.P_ESTOQUE_NEGATIVO)));
                    //parametro.setP_desconto_do_vendedor(cursor.getString(cursor.getColumnIndex(parametro.P_DESCONTO_MAX())));
                    parametro.setP_desconto_do_vendedor(cursor.getDouble(cursor.getColumnIndex(parametro.P_DESCONTO_VENDEDOR)));
                    int Desc = cursor.getInt(cursor.getColumnIndex(parametro.P_DESCONTO_VENDEDOR));
                    parametro.setP_usuario(cursor.getString(cursor.getColumnIndex(parametro.P_USUARIO)));
                    parametro.setP_senha(cursor.getString(cursor.getColumnIndex(parametro.P_SENHA)));
                    parametro.setP_qual_endereco_ip(cursor.getString(cursor.getColumnIndex(parametro.P_QUAL_END_IP)));
                    cursor.close();
                } catch (Exception E) {
                    E.toString();
                }
            }

        } catch (SQLiteException e) {
            Log.d("Script", e.getMessage());
        } finally {
            db.close();

        }

        return parametro;
    }


}


















