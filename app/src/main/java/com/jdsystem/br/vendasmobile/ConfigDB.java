package com.jdsystem.br.vendasmobile;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by eduardo.costa on 07/11/2016.
 */

public class ConfigDB {

    public static Boolean ConectarBanco(SQLiteDatabase DB){

        try {
            DB.execSQL(" CREATE TABLE IF NOT EXISTS CLIENTES ( CNPJ_CPF VARCHAR (14)  NOT NULL, " +
                    " NOMERAZAO VARCHAR (60) NOT NULL, NOMEFAN VARCHAR (50) NOT NULL, INSCREST VARCHAR (18), " +
                    " EMAIL VARCHAR (100) NOT NULL, TEL1 VARCHAR (15) NOT NULL, TEL2 VARCHAR (15), " +
                    " ENDERECO VARCHAR (50) NOT NULL, NUMERO VARCHAR (10) NOT NULL, COMPLEMENT VARCHAR (15), " +
                    " CODBAIRRO INTEGER NOT NULL, OBS TEXT, CODCIDADE INTEGER NOT NULL, UF CHAR (2) NOT NULL, " +
                    " CEP CHAR (8), CODCLIE_INT INTEGER PRIMARY KEY AUTOINCREMENT, CODCLIE_EXT INTEGER, CODVENDEDOR INTEGER, " +
                    " TIPOPESSOA VARCHAR(1), ATIVO VARCHAR(1), FLAGINTEGRADO VARCHAR(1) " +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS BAIRROS (" +
                    "    CODBAIRRO INTEGER      PRIMARY KEY AUTOINCREMENT," +
                    "    CODCIDADE INTEGER      NOT NULL," +
                    "    DESCRICAO VARCHAR (30) NOT NULL" +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS CIDADES (" +
                    "    CODCIDADE INTEGER      PRIMARY KEY AUTOINCREMENT," +
                    "    DESCRICAO VARCHAR (50) NOT NULL," +
                    "    UF        CHAR (2)     NOT NULL" +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS ESTADOS (" +
                    "    UF        CHAR (2)     PRIMARY KEY, " +
                    "    DESCRICAO VARCHAR (20) NOT NULL" +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS PARAMAPP (DT_ULT_ATU DATETIME DEFAULT CURRENT_TIMESTAMP );");

        } catch (Exception E) {
            return false;
        }
        return true;
    }
}
