package com.jdsystem.br.vendasmobile;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by eduardo.costa on 07/11/2016.
 */

public class ConfigDB {

    @NonNull
    public static Boolean ConectarBanco(SQLiteDatabase DB) {

        try {
            DB.execSQL(" CREATE TABLE IF NOT EXISTS CLIENTES ( CNPJ_CPF VARCHAR (14)  NOT NULL, " +
                    " NOMERAZAO VARCHAR (60) NOT NULL, NOMEFAN VARCHAR (50) NOT NULL, INSCREST VARCHAR (18), " +
                    " EMAIL VARCHAR (100) NOT NULL, TEL1 VARCHAR (15) NOT NULL, TEL2 VARCHAR (15), TELFAX VARCHAR (15), " +
                    " ENDERECO VARCHAR (50) NOT NULL, NUMERO VARCHAR (10) NOT NULL, COMPLEMENT VARCHAR (15), " +
                    " CODBAIRRO INTEGER NOT NULL, OBS TEXT, CODCIDADE INTEGER NOT NULL, UF CHAR (2) NOT NULL, " +
                    " CEP CHAR (8), CODCLIE_INT INTEGER PRIMARY KEY AUTOINCREMENT, CODCLIE_EXT INTEGER, CODVENDEDOR INTEGER, " +
                    " TIPOPESSOA VARCHAR(1), ATIVO VARCHAR(1), FLAGINTEGRADO VARCHAR(1), REGIDENT VARCHAR(18) " +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS BAIRROS (" +
                    "    CODBAIRRO INTEGER      PRIMARY KEY AUTOINCREMENT," +
                    "    CODCIDADE INTEGER      NOT NULL," +
                    "    DESCRICAO VARCHAR (30) NOT NULL " +
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

            DB.execSQL("CREATE TABLE IF NOT EXISTS PARAMAPP (DT_ULT_ATU DATETIME);");

            DB.execSQL(" CREATE TABLE IF NOT EXISTS CONTATO (" +
                    "    CODCONTATO_INT INTEGER       PRIMARY KEY AUTOINCREMENT," +
                    "    CODCLIENTE INTEGER           NOT NULL," +
                    "    NOME           VARCHAR (60)  NOT NULL," +
                    "    CARGO          VARCHAR (30)," +
                    "    EMAIL          VARCHAR (100)," +
                    "    TEL1           VARCHAR (15)," +
                    "    TEL2           VARCHAR (15))" +
                    ";");

            DB.execSQL("CREATE TABLE IF NOT EXISTS ITENS (" +
                    "    CODIGOITEM   INTEGER        PRIMARY KEY" +
                    "                                 NOT NULL, " +
                    "    CODITEMANUAL VARCHAR (15)    NOT NULL, " +
                    "    DESCRICAO    VARCHAR (60)    NOT NULL, " +
                    "    UNIVENDA     VARCHAR (5)     NOT NULL, " +
                    "    APRESENTACAO VARCHAR (20)    NOT NULL, " +
                    "    VLVENDA1     DECIMAL (12, 4) NOT NULL, " +
                    "    VLVENDA2     DECIMAL (12, 4),          " +
                    "    VLVENDA3     DECIMAL (12, 4),          " +
                    "    VLVENDA4     DECIMAL (12, 4),          " +
                    "    VLVENDA5     DECIMAL (12, 4),          " +
                    "    VLVENDAP1    DECIMAL (12, 4),          " +
                    "    VLVENDAP2    DECIMAL (12, 4),          " +
                    "    CLASSE       VARCHAR(15),              " +
                    "    FABRICANTE   VARCHAR(30),              " +
                    "    FORNECEDOR   VARCHAR(30),              " +
                    "    ATIVO        VARCHAR(1),               " +
                    "    OBS          TEXT,                     " +
                    "    MARCA        VARCHAR(15),              " +
                    "    VLICMSST      FLOAT,                   " +
                    "    VLIPI         FLOAT                    " +
                    ");");

            DB.execSQL("CREATE TABLE IF NOT EXISTS PEDOPER (        " +
                    "    NUMPED        INTEGER         PRIMARY KEY  " +
                    "                                  UNIQUE       " +
                    "                                  NOT NULL,    " +
                    "    DATAEMIS      DATETIME        NOT NULL,    " +
                    "    CODEMPRESA    INTEGER         NOT NULL,    " +
                    "    CODCLIE       INTEGER         NOT NULL,    " +
                    "    STATUS        INTEGER         NOT NULL,    " +
                    "    CODVENDEDOR   INTEGER         NOT NULL,    " +
                    "    VLMERCAD      NUMERIC (12, 2) NOT NULL,    " +
                    "    VLDESCONTO    NUMERIC (12, 2),             " +
                    "    PERCDESCO     DECIMAL (7, 4),              " +
                    "    VALORTOTAL    NUMERIC (12, 2) NOT NULL,    " +
                    "    OBS           TEXT,                        " +
                    "    NUMPEDIDOERP  INTEGER,                     " +
                    "    VLPERCACRES   FLOAT,                       " +
                    "    PERCACRESC    DECIMAL (7, 4),              " +
                    "    VLFRETE       FLOAT,                       " +
                    "    VLTOTALIPI    FLOAT,                       " +
                    "    VLTOTALICMSST FLOAT                        " +
                    ");");


            DB.execSQL("CREATE TABLE IF NOT EXISTS PEDITENS (                 " +
                    "    NUMIDEOPE     INTEGER         PRIMARY KEY AUTOINCREMENT " +
                    "                                  UNIQUE       " +
                    "                                  NOT NULL,    " +
                    "    NUMPED        INTEGER         NOT NULL,    " +
                    "    CODIGOITEM    INTEGER         NOT NULL,    " +
                    "    NUMEROITEM    NUMERIC (5, 0),              " +
                    "    QTDEMBAPED    NUMERIC (11, 3) NOT NULL,    " +
                    "    QTDMENORPED   FLOAT           NOT NULL,    " +
                    "    QTDMAIORPED   FLOAT           NOT NULL,    " +
                    "    UNIDADE       VARCHAR (5)     NOT NULL,    " +
                    "    VLUNIT        NUMERIC (14, 4) NOT NULL,    " +
                    "    PERCACREDESC  DOUBLE,                      " +
                    "    VALORDESCONTO NUMERIC (14, 4),             " +
                    "    VLTOTAL       NUMERIC (14, 2) NOT NULL,    " +
                    "    VLICMSST      FLOAT,                       " +
                    "    VLIPI         FLOAT                        " +
                    ");");

        } catch (Exception E) {
            return false;
        }
        return true;
    }
}
