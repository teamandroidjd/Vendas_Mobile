package com.jdsystem.br.vendasmobile;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class ConfigDB extends SQLiteOpenHelper {

    public static String Dbname = "WSGEDB.db";
    public static int versao = 6;

    public ConfigDB(Context ctx) {

        super(ctx, Dbname, null, versao);
    }


    private static String SQL_CLIENTE = (" CREATE TABLE IF NOT EXISTS CLIENTES ( CNPJ_CPF VARCHAR (14)  NOT NULL, " +
            "NOMERAZAO VARCHAR (60) NOT NULL,               " +
            "NOMEFAN VARCHAR (50) NOT NULL,                 " +
            "INSCREST VARCHAR (18),                         " +
            "EMAIL VARCHAR (100) NOT NULL,                  " +
            "TEL1 VARCHAR (15) NOT NULL,                    " +
            "TEL2 VARCHAR (15),                             " +
            "TELFAX VARCHAR (15),                           " +
            "ENDERECO VARCHAR (50) NOT NULL,                " +
            "NUMERO VARCHAR (10) NOT NULL,                  " +
            "COMPLEMENT VARCHAR (15),                       " +
            "CODBAIRRO INTEGER NOT NULL,                    " +
            "OBS TEXT,                                      " +
            "CODCIDADE INTEGER NOT NULL,                    " +
            "UF CHAR (2) NOT NULL,                          " +
            "CEP CHAR (8),                                  " +
            "CODCLIE_INT INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "CODCLIE_EXT INTEGER,                           " +
            "CODVENDEDOR INTEGER,                           " +
            "TIPOPESSOA VARCHAR(1),                         " +
            "ATIVO VARCHAR(1),                              " +
            "FLAGINTEGRADO VARCHAR(1),                      " +
            "REGIDENT VARCHAR(18),                          " +
            "LIMITECRED DOUBLE,                             " +
            "BLOQUEIO VARCHAR(2),                           " +
            "CODPERFIL INTEGER,                             " +
            "CHAVE VARCHAR(100)                             " +
            ");");

    private static String SQL_BAIRROS = ("CREATE TABLE IF NOT EXISTS BAIRROS (" +
            "    CODBAIRRO INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    CODBAIRRO_EXT INTEGER,                      " +
            "    CODCIDADE INTEGER      NOT NULL,            " +
            "    DESCRICAO VARCHAR (30) NOT NULL             " +
            ");");

    private static String SQL_CIDADES = ("CREATE TABLE IF NOT EXISTS CIDADES (" +
            "    CODCIDADE INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    CODCIDADE_EXT INTEGER,                      " +
            "    DESCRICAO VARCHAR (50) NOT NULL,            " +
            "    UF        CHAR (2)     NOT NULL             " +
            ");");

    private static String SQL_ESTADOS = ("CREATE TABLE IF NOT EXISTS ESTADOS (" +
            "    UF CHAR(2) PRIMARY KEY,         " +
            "    DESCRICAO VARCHAR (20) NOT NULL " +
            ");");

    private static String SQL_PARAMAPP = ("CREATE TABLE IF NOT EXISTS PARAMAPP (DT_ULT_ATU DATETIME," +
            " DT_ULT_CLIE DATETIME,                           " +
            " DT_ULT_ITENS DATETIME,                          " +
            " p_usu_codigo INTEGER,                           " +
            " p_importar_todos_clientes CHAR DEFAULT 1,       " +
            " p_qual_endereco_ip CHAR DEFAULT 1,              " +
            " p_usuario VARCHAR DEFAULT 20,                   " +
            " p_senha VARCHAR DEFAULT 20,                     " +
            " p_end_ip_local VARCHAR DEFAULT 50,              " +
            " p_end_ip_remoto VARCHAR DEFAULT 50,             " +
            " PERCACRESC DECIMAL (7, 4),                      " +
            " DESCRICAOTAB1 VARCHAR (20),                     " +
            " DESCRICAOTAB2 VARCHAR (20),                     " +
            " DESCRICAOTAB3 VARCHAR (20),                     " +
            " DESCRICAOTAB4 VARCHAR (20),                     " +
            " DESCRICAOTAB5 VARCHAR (20),                     " +
            " DESCRICAOTAB6 VARCHAR (20),                     " +
            " DESCRICAOTAB7 VARCHAR (20),                     " +
            " HABITEMNEGATIVO CHAR (1),                       " +
            " HABCRITSITCLIE CHAR (1),                        " +
            " TIPOCRITICQTDITEM CHAR(1),                      " +
            " p_trabalhar_com_estoque_negativo CHAR DEFAULT 1," +
            " p_desconto_do_vendedor INTEGER);");

    private static String SQL_CONTATOS = (" CREATE TABLE IF NOT EXISTS CONTATO (" +
            "    CODCONTATO_INT INTEGER       PRIMARY KEY AUTOINCREMENT," +
            "    CODCLIENTE                   INTEGER,                  " +
            "    CODCLIE_EXT                  INTEGER,                  " +
            "    NOME                         VARCHAR (60)  NOT NULL,   " +
            "    CARGO                        VARCHAR (30),             " +
            "    DOCUMENTO                    VARCHAR(15),              " +
            "    DATA                         VARCHAR(10),              " +
            "    CEP                          VARCHAR(10),              " +
            "    ENDERECO                     VARCHAR(15),              " +
            "    NUMERO                       VARCHAR(10),              " +
            "    COMPLEMENTO                  VARCHAR(15),              " +
            "    UF                           CHAR(2),                  " +
            "    CODVENDEDOR                  INTEGER,                  " +
            "    CODBAIRRO                    INTEGER,                  " +
            "    CODCIDADE                    INTEGER,                  " +
            "    EMAIL                        VARCHAR (100),            " +
            "    CODPERFIL                    INTEGER,                  " +
            "    TEL1                         VARCHAR (15),             " +
            "    TEL2                         VARCHAR (15))             " +
            ";");

    private static String SQL_ITENS = ("CREATE TABLE IF NOT EXISTS ITENS (" +
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
            "    VENDAPADRAO  DECIMAL (12, 4),          " +
            "    QTDESTPROD   VARCHAR(10),              " +
            "    CLASSE       VARCHAR(15),              " +
            "    FABRICANTE   VARCHAR(30),              " +
            "    FORNECEDOR   VARCHAR(30),              " +
            "    ATIVO        VARCHAR(1),               " +
            "    OBS          TEXT,                     " +
            "    MARCA        VARCHAR(15),              " +
            "    VLICMSST     FLOAT,                    " +
            "    VLIPI        FLOAT,                    " +
            "    CODPERFIL    INTEGER                   " +
            ");");

    private static String SQL_PEDOPER = ("CREATE TABLE IF NOT EXISTS PEDOPER ( " +
            " NUMPED              INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " CHAVE_PEDIDO        VARCHAR(70),                       " +
            " DATAEMIS            DATETIME        NOT NULL,          " +
            " DATAPREVISTAENTREGA DATE,                              " +
            " CODEMPRESA          INTEGER,                           " +
            " CODCLIE             INTEGER         NOT NULL,          " +
            " CODCLIE_EXT         INTEGER         NOT NULL,          " +
            " NOMECLIE            VARCHAR(50),                       " +
            " STATUS              CHAR(1),                           " +
            " CODVENDEDOR         INTEGER         NOT NULL,          " +
            " FORMAPGTO           VARCHAR(50),                       " +
            " VLMERCAD            NUMERIC (12, 2) NOT NULL,          " +
            " VLDESCONTO          NUMERIC (12, 2),                   " +
            " PERCDESCO           DECIMAL (7, 4),                    " +
            " VALORTOTAL          NUMERIC (12, 2) NOT NULL,          " +
            " OBS                 TEXT,                              " +
            " NUMPEDIDOERP        INTEGER,                           " +
            " VLPERCACRES         FLOAT,                             " +
            " PERCACRESC          DECIMAL (7, 4),                    " +
            " VLFRETE             FLOAT,                             " +
            " VLTOTALIPI          FLOAT,                             " +
            " VLTOTALICMSST       FLOAT,                             " +
            " VALORSEGURO         FLOAT,                             " +
            " FLAGINTEGRADO       CHAR(1),                           " +
            " LATITUDEPEDIDO      DOUBLE,                            " +
            " NUMFISCAL           INTEGER,                           " +
            " CODPERFIL           INTEGER,                           " +
            " LONGITUDEPEDIDO DOUBLE);");


    private static String SQL_PEDITENS = ("CREATE TABLE IF NOT EXISTS PEDITENS (" +
            " NUMIDEOPE     INTEGER         PRIMARY KEY AUTOINCREMENT " +
            "                               NOT NULL,                 " +
            " CHAVEPEDIDO   VARCHAR(70),                              " +
            " NUMPED        INTEGER,                                  " +
            " CODITEMANUAL  VARCHAR (15)    NOT NULL,                 " +
            " DESCRICAO     VARCHAR (60)    NOT NULL,                 " +
            " CODIGOITEM    INTEGER,                                  " +
            " NUMEROITEM    NUMERIC (5, 0),                           " +
            //" QTDEMBAPED    NUMERIC (11, 3) NOT NULL,                 " +
            " QTDMENORPED   FLOAT           NOT NULL,                 " +
            //" QTDMAIORPED   FLOAT           NOT NULL,                 " +
            " UNIDADE       VARCHAR (5),                              " +
            " VLUNIT        DECIMAL(10,2)   NOT NULL,                 " +
            " PERCACREDESC  DOUBLE,                                   " +
            " VALORDESCONTO DECIMAL(10,2),                            " +
            " VLTOTAL       DECIMAL(10,2)   NOT NULL,                 " +
            " VLICMSST      FLOAT,                                    " +
            " VLIPI         FLOAT,                                    " +
            " CODPERFIL     INTEGER                                   " +
            ");");

    private static String SQL_CONFPAGAMENTO = "CREATE TABLE IF NOT EXISTS [CONFPAGAMENTO] (" +
            " conf_codigo                 INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " conf_sementrada_comentrada  CHAR DEFAULT 1,                    " +
            " conf_tipo_pagamento         VARCHAR DEFAULT 20,                " +
            " conf_recebeucom_din_chq_car VARCHAR DEFAULT  20,               " +
            " conf_valor_recebido         DECIMAL(10,2),                     " +
            " conf_parcelas               INTEGER,                           " +
            " vendac_chave                VARCHAR DEFAULT 70 ,               " +
            " conf_enviado                CHAR DEFAULT 1 )";

    private static String SQL_VENDA_D_TEMP = "CREATE TABLE [VENDAD_TEMP] (" +
            " vendad_eanTEMP           VARCHAR DEFAULT 50, " +
            " vendad_prd_codigoTEMP    INTEGER,            " +
            " vendad_prd_descricaoTEMP VARCHAR DEFAULT 50, " +
            " vendad_quantidadeTEMP    DECIMAL(10,2),      " +
            " vendad_preco_vendaTEMP   DECIMAL(10,2),      " +
            " vendad_prd_unidadeTEMP   VARCHAR(5),         " +
            " vendad_totalTEMP         DECIMAL(10,2))";

    private static String SQL_RECEBER = "CREATE TABLE [CONREC] (" +
            " rec_codigo         INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " rec_numparcela     INTEGER,                           " +
            " rec_cli_codigo     INTEGER,                           " +
            " rec_cli_nome       VARCHAR DEFAULT 50,                " +
            " vendac_chave       VARCHAR DEFAULT 70,                " +
            " rec_datamovimento  DATE ,                             " +
            " rec_valor_receber  DECIMAL (10,2),                    " +
            " rec_valorpago      DECIMAL (10,2),                    " +
            " rec_datavencimento DATE ,                             " +
            " rec_data_que_pagou DATE ,                             " +
            " rec_recebeu_com    VARCHAR DEFAULT 20,                " +
            " rec_enviado        CHAR DEFAULT 1)";

    private static String SQL_EMPRESA = ("CREATE TABLE IF NOT EXISTS EMPRESAS (" +
            " CODEMPRESA  INTEGER      NOT NULL,                    " +
            " CNPJ        VARCHAR (14) PRIMARY KEY UNIQUE NOT NULL, " +
            " NOMEEMPRE   VARCHAR (60) NOT NULL,                    " +
            " NOMEABREV   VARCHAR (20) NOT NULL,                    " +
            " TEL1        VARCHAR (15) NOT NULL,                    " +
            " TEL2        VARCHAR (15),                             " +
            " EMAIL       VARCHAR (40) NOT NULL,                    " +
            " ATIVO       CHAR(1),                                  " +
            " LOGO        BLOB                                      " +
            ");");

    private static String SQL_USUARIOS = (" CREATE TABLE IF NOT EXISTS USUARIOS (" +
            " CODVEND INTEGER,      " +
            " USUARIO VARCHAR (30), " +
            " SENHA VARCHAR  (100), " +
            " CODPERFIL INTEGER,    " +
            " CODEMPRESA INTEGER ); ");

    private static String SQL_BLOQUEIOS = (" CREATE TABLE IF NOT EXISTS BLOQCLIE (" +
            " CODBLOQ   VARCHAR (3),  " +
            " DESCRICAO VARCHAR (30), " +
            " BLOQUEAR  CHAR    (1),  " +
            " LIBERAR   CHAR    (1),  " +
            " FPAVISTA  VARCHAR (3) ); ");

    private static String SQL_PERFIL = (" CREATE TABLE IF NOT EXISTS PERFIL (" +
            " CODPERFIL  INTEGER PRIMARY KEY AUTOINCREMENT,   " +
            " LICENCA    VARCHAR (20),                        " +
            " HOST       VARCHAR (50),                        " +
            " NOMEPERFIL VARCHAR (20) ); ");


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CLIENTE);
        db.execSQL(SQL_BAIRROS);
        db.execSQL(SQL_CIDADES);
        db.execSQL(SQL_CONTATOS);
        db.execSQL(SQL_ESTADOS);
        db.execSQL(SQL_ITENS);
        db.execSQL(SQL_PARAMAPP);
        db.execSQL(SQL_PEDOPER);
        db.execSQL(SQL_PEDITENS);
        db.execSQL(SQL_CONFPAGAMENTO);
        db.execSQL(SQL_VENDA_D_TEMP);
        db.execSQL(SQL_RECEBER);
        db.execSQL(SQL_EMPRESA);
        db.execSQL(SQL_USUARIOS);
        db.execSQL(SQL_BLOQUEIOS);
        db.execSQL(SQL_PERFIL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int VersaoAntiga, int VersaoNova) {

        if (VersaoNova > VersaoAntiga) {
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD PERCACRESC DECIMAL (7, 4)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD PERCACRESC DECIMAL (7, 4)");
            }
            try {
                db.execSQL("ALTER TABLE EMPRESAS ADD ATIVO CHAR(1)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE EMPRESAS ADD ATIVO CHAR(1)");
            }
            try {
                db.execSQL("ALTER TABLE ITENS ADD VENDAPADRAO DECIMAL(12,4) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE ITENS ADD VENDAPADRAO DECIMAL(12,4)");
            }
            try {
                db.execSQL("ALTER TABLE VENDAD_TEMP ADD vendad_prd_unidadeTEMP  VARCHAR(5) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE VENDAD_TEMP ADD vendad_prd_unidadeTEMP  VARCHAR(5)");
            }
            try {
                db.execSQL("DELETE FROM ITENS");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no DELETE FROM ITENS");
            }
            try {
                db.execSQL("DELETE FROM CLIENTES ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no DELETE FROM CLIENTES");
            }
            //MODIFICAÇÕES NA VERSÃO 4 DO BANCO DE DADOS
            try {
                db.execSQL("ALTER TABLE ITENS ADD QTDESTPROD VARCHAR(10) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE ITENS ADD QTDESTPROD VARCHAR(10)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB1 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB1 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD HABCRITSITCLIE CHAR (1)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD HABCRITSITCLIE CHAR (1)");
            }
            try {
                db.execSQL("ALTER TABLE CLIENTES ADD LIMITECRED DOUBLE");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CLIENTES ADD LIMITECRED DOUBLE");
            }
            try {
                db.execSQL("ALTER TABLE CLIENTES ADD BLOQUEIO VARCHAR(2)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CLIENTES ADD BLOQUEIO VARCHAR(2)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD TIPOCRITICQTDITEM CHAR(1)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD TIPOCRITICQTDITEM CHAR(1)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD DOCUMENTO VARCHAR(15)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD DOCUMENTO VARCHAR(15)");
            }
            try {
                db.execSQL("ALTER TABLE PEDITENS ADD NUMPED INTEGER");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PEDITENS ADD NUMPED INTEGER");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD DATA VARCHAR(10)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD DATA VARCHAR(10)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD CEP VARCHAR(10)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CEP VARCHAR(10)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD ENDERECO VARCHAR(15)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD ENDERECO VARCHAR(15)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD NUMERO VARCHAR(10) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD ENDERECO VARCHAR(15)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD COMPLEMENTO VARCHAR(15)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD COMPLEMENTO VARCHAR(15)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD UF CHAR(2) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD COMPLEMENTO VARCHAR(15)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD CODVENDEDOR INTEGER");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CODVENDEDOR INTEGER");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD CODCLIE_EXT INTEGER");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CODCLIE_EXT INTEGER");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD HABITEMNEGATIVO CHAR (1)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD HABITEMNEGATIVO CHAR (1)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB7 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB7 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB6 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB6 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB5 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB5 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB4 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB4 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB3 VARCHAR(20)");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB3 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DESCRICAOTAB2 VARCHAR(20) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DESCRICAOTAB2 VARCHAR(20)");
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD CODCIDADE INTEGER");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CODCIDADE INTEGER");
            }
            try {
                db.execSQL("ALTER TABLE CONREC ADD rec_valorpago DECIMAL (10,2) ");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CODCIDADE INTEGER");
            }
            try {
                db.execSQL(" CREATE TABLE IF NOT EXISTS BLOQCLIE (" +
                        " CODBLOQ   VARCHAR (3),  " +
                        " DESCRICAO VARCHAR (30), " +
                        " BLOQUEAR  CHAR    (1),  " +
                        " LIBERAR   CHAR    (1),  " +
                        " FPAVISTA  VARCHAR (3) ); ");

            } catch (Exception E) {
                System.out.println("ConfigDB, falha no CREATE TABLE IF NOT EXISTS BLOQCLIE");
            }

            //MODIFICAÇÕES NA VERSÃO 6 DO BANCO DE DADOS
            try {
                db.execSQL("ALTER TABLE CONTATO ADD CODCLIENTE INTEGER");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE CONTATO ADD CODCLIENTE INTEGER");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DT_ULT_CLIE DATETIME");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DT_ULT_CLIE DATETIME");
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD DT_ULT_ITENS DATETIME");
            } catch (Exception E) {
                System.out.println("ConfigDB, falha no ALTER TABLE PARAMAPP ADD DT_ULT_CLIE DATETIME");
            }
            try{
                db.execSQL("CREATE TABLE IF NOT EXISTS PERFIL (" +
                        " CODPERFIL  INTEGER PRIMARY KEY AUTOINCREMENT,   " +
                        " LICENCA    VARCHAR (20),                        " +
                        " HOST       VARCHAR (50),                        " +
                        " NOMEPERFIL VARCHAR (20) ); ");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE USUARIOS ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CLIENTES ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONTATO ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE ITENS ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE PEDITENS ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }

        }
    }
}
