package com.jdsystem.br.vendasmobile;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class ConfigDB extends SQLiteOpenHelper {
    public ConfigDB(Context ctx) {

        super(ctx, Dbname, null, versao);
    }

    public static String Dbname = "WSGEDB.db";
    public static int versao = 3;

    private static String SQL_CLIENTE = (" CREATE TABLE IF NOT EXISTS CLIENTES (" +
            " CNPJ_CPF      VARCHAR (14) NOT NULL,             " +
            " NOMERAZAO     VARCHAR (60) NOT NULL,             " +
            " NOMEFAN       VARCHAR (50) NOT NULL,             " +
            " INSCREST      VARCHAR (18),                      " +
            " EMAIL         VARCHAR (100) NOT NULL,            " +
            " TEL1          VARCHAR (15) NOT NULL,             " +
            " TEL2          VARCHAR (15),                      " +
            " TELFAX        VARCHAR (15),                      " +
            " ENDERECO      VARCHAR (50) NOT NULL,             " +
            " NUMERO        VARCHAR (10) NOT NULL,             " +
            " COMPLEMENT    VARCHAR (15),                      " +
            " CODBAIRRO     INTEGER NOT NULL,                  " +
            " OBS           TEXT,                              " +
            " CODCIDADE     INTEGER NOT NULL,                  " +
            " UF            CHAR (2) NOT NULL,                 " +
            " CEP           CHAR (8),                          " +
            " CODCLIE_INT   INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " CODCLIE_EXT   INTEGER,                           " +
            " CODVENDEDOR   INTEGER,                           " +
            " TIPOPESSOA    VARCHAR(1),                        " +
            " ATIVO         VARCHAR(1),                        " +
            " FLAGINTEGRADO VARCHAR(1),                        " +
            " REGIDENT      VARCHAR(18),                       " +
            " LIMITECRED    DOUBLE,                            " +
            " BLOQUEIO      VARCHAR(2),                        " +
            " CODPERFIL     INTEGER,                           " +
            " CHAVE         VARCHAR(100)                       " +
            ");");
    private static String SQL_BAIRROS = ("CREATE TABLE IF NOT EXISTS BAIRROS (" +
            " CODBAIRRO     INTEGER PRIMARY KEY AUTOINCREMENT," +
            " CODBAIRRO_EXT INTEGER,                          " +
            " CODCIDADE     INTEGER      NOT NULL,            " +
            " DESCRICAO     VARCHAR (30) NOT NULL,            " +
            " CODPERFIL     INTEGER                           " +
            ");");
    private static String SQL_CIDADES = ("CREATE TABLE IF NOT EXISTS CIDADES (" +
            " CODCIDADE     INTEGER PRIMARY KEY AUTOINCREMENT," +
            " CODCIDADE_EXT INTEGER,                          " +
            " DESCRICAO     VARCHAR (50) NOT NULL,            " +
            " UF            CHAR (2)     NOT NULL,            " +
            " CODPERFIL     INTEGER                           " +
            ");");
    private static String SQL_ESTADOS = ("CREATE TABLE IF NOT EXISTS ESTADOS (" +
            " UF        CHAR(2) PRIMARY KEY,   " +
            " DESCRICAO VARCHAR (20) NOT NULL, " +
            " CODPERFIL INTEGER                " +
            ");");
    private static String SQL_PARAMAPP = ("CREATE TABLE IF NOT EXISTS PARAMAPP (" +
            " DT_ULT_ATU                       DATETIME,           " +
            " DT_ULT_CLIE                      DATETIME,           " +
            " DT_ULT_CONT                      DATETIME,           " +
            " DT_ULT_ITENS                     DATETIME,           " +
            " p_usu_codigo                     INTEGER,            " +
            " p_importar_todos_clientes        CHAR DEFAULT 1,     " +
            " p_qual_endereco_ip               CHAR DEFAULT 1,     " +
            " p_usuario                        VARCHAR DEFAULT 20, " +
            " p_senha                          VARCHAR DEFAULT 20, " +
            " p_end_ip_local                   VARCHAR DEFAULT 50, " +
            " p_end_ip_remoto                  VARCHAR DEFAULT 50, " +
            " PERCACRESC                       DECIMAL (7, 4),     " +
            " DESCRICAOTAB1                    VARCHAR (20),       " +
            " DESCRICAOTAB2                    VARCHAR (20),       " +
            " DESCRICAOTAB3                    VARCHAR (20),       " +
            " DESCRICAOTAB4                    VARCHAR (20),       " +
            " DESCRICAOTAB5                    VARCHAR (20),       " +
            " DESCRICAOTAB6                    VARCHAR (20),       " +
            " DESCRICAOTAB7                    VARCHAR (20),       " +
            " HABITEMNEGATIVO                  CHAR (1),           " +
            " HABCLIEXVEND                     CHAR (1),           " +
            " HABCADASTRO_CLIE                 INTEGER,            " +
            " HABCONTROLQTDMINVEND             CHAR (1),           " +
            " HABCRITSITCLIE                   CHAR (1),           " +
            " TIPOCRITICQTDITEM                CHAR (1),           " +
            " CODPERFIL                        INTEGER,            " +
            " HABALTPRECOVENDA                 CHAR(1),            " +
            " VLMINVENDA                       VARCHAR(30),        " +
            " p_trabalhar_com_estoque_negativo CHAR DEFAULT 1,     " +
            " p_desconto_do_vendedor INTEGER" +
            ");");
    private static String SQL_CONTATOS = (" CREATE TABLE IF NOT EXISTS CONTATO (" +
            "    CODCONTATO_INT INTEGER       PRIMARY KEY AUTOINCREMENT," +
            "    CODCLIENTE                   INTEGER,                  " +
            "    CODCLIE_EXT                  INTEGER,                  " +
            "    CODCONTATO_EXT               VARCHAR (7),              " +
            "    CODCLIENTE_EXT               VARCHAR (7),              " +
            "    NOME                         VARCHAR (60)  NOT NULL,   " +
            "    CARGO                        VARCHAR (30),             " +
            "    CODCARGO                     INTEGER,                  " +
            "    SETOR                        VARCHAR (30),             " +
            "    DOCUMENTO                    VARCHAR(15),              " +
            "    DATA                         VARCHAR(10),              " +
            "    CEP                          VARCHAR(10),              " +
            "    ENDERECO                     VARCHAR(15),              " +
            "    NUMERO                       VARCHAR(10),              " +
            "    COMPLEMENTO                  VARCHAR(15),              " +
            "    UF                           CHAR(2),                  " +
            "    CODVENDEDOR                  INTEGER,                  " +
            "    CODBAIRRO                    INTEGER,                  " +
            "    BAIRRO                       VARCHAR (30),             " +
            "    CODCIDADE                    INTEGER,                  " +
            "    DESC_CIDADE                  VARCHAR (30),             " +
            "    EMAIL                        VARCHAR (100),            " +
            "    TEL1                         VARCHAR (15),             " +
            "    TEL2                         VARCHAR (15),             " +
            "    TIPO                         CHAR(1),                  " +
            "    OBS                          TEXT,                     " +
            "    FLAGINTEGRADO                CHAR(1),                  " +
            "    CODPERFIL                    INTEGER)                  " +
            ";");
    private static String SQL_ITENS = ("CREATE TABLE IF NOT EXISTS ITENS (" +
            " CODIGOITEM   INTEGER        PRIMARY KEY" +
            "                              NOT NULL, " +
            " CODITEMANUAL VARCHAR (15)    NOT NULL, " +
            " DESCRICAO    VARCHAR (60)    NOT NULL, " +
            " UNIVENDA     VARCHAR (5)     NOT NULL, " +
            " APRESENTACAO VARCHAR (20)    NOT NULL, " +
            " VLVENDA1     DECIMAL (12, 4) NOT NULL, " +
            " VLVENDA2     DECIMAL (12, 4),          " +
            " VLVENDA3     DECIMAL (12, 4),          " +
            " VLVENDA4     DECIMAL (12, 4),          " +
            " VLVENDA5     DECIMAL (12, 4),          " +
            " VLVENDAP1    DECIMAL (12, 4),          " +
            " VLVENDAP2    DECIMAL (12, 4),          " +
            " TABELAPADRAO VARCHAR(20),              " +
            " QTDESTPROD   VARCHAR(10),              " +
            " CLASSE       VARCHAR(15),              " +
            " FABRICANTE   VARCHAR(30),              " +
            " FORNECEDOR   VARCHAR(30),              " +
            " ATIVO        VARCHAR(1),               " +
            " OBS          TEXT,                     " +
            " MARCA        VARCHAR(15),              " +
            " VLICMSST     FLOAT,                    " +
            " VLIPI        FLOAT,                    " +
            " CODPERFIL    INTEGER,                  " +
            " QTDMINVEND   FLOAT                     " +
            ");");
    private static String SQL_PEDOPER = ("CREATE TABLE IF NOT EXISTS PEDOPER ( " +
            " NUMPED              INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " CHAVE_PEDIDO        VARCHAR(70),                       " +
            " DATAEMIS            DATETIME        NOT NULL,          " +
            " DATAVENDA           DATETIME,                          " +
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
            " NUMIDEOPE       INTEGER         PRIMARY KEY AUTOINCREMENT," +
            " CHAVEPEDIDO     VARCHAR(70),                              " +
            " NUMPED          INTEGER,                                  " +
            " CODITEMANUAL    VARCHAR (15)    NOT NULL,                 " +
            " DESCRICAO       VARCHAR (60)    NOT NULL,                 " +
            " CODIGOITEM      INTEGER,                                  " +
            " NUMEROITEM      NUMERIC (5, 0),                           " +
            " VLUNITTEMP      DECIMAL(10,2),                            " +
            " QTDMENORPED     FLOAT           NOT NULL,                 " +
            " QTDMAIORPEDTEMP FLOAT,                                    " +
            " UNIDADE         VARCHAR (5),                              " +
            " VLUNIT          DECIMAL(10,2)   NOT NULL,                 " +
            " PERCACREDESC    DOUBLE,                                   " +
            " VALORDESCONTO   DECIMAL(10,2),                            " +
            " VLTOTAL         DECIMAL(10,2)   NOT NULL,                 " +
            " VLICMSST        FLOAT,                                    " +
            " VLIPI           FLOAT,                                    " +
            " VIEW            CHAR(1),                                  " +
            " CODPERFIL       INTEGER                                   " +
            ");");
    private static String SQL_CONFPAGAMENTO = ("CREATE TABLE IF NOT EXISTS CONFPAGAMENTO (" +
            " CONF_CODIGO                 INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " CONF_CODFORMPGTO_EXT        VARCHAR(5),                        " +
            " CONF_DIAS_VENCIMENTO        VARCHAR(5),                        " +
            " CODPERFIL                   INTEGER,                           " +
            " CONF_DATA_VENCIMENTO        DATE,                              " +
            " conf_descricao_formpgto     VARCHAR(30),                       " +
            " conf_sementrada_comentrada  CHAR(1),                           " +
            " conf_tipo_pagamento         VARCHAR(20),                       " +
            " conf_recebeucom_din_chq_car VARCHAR(20),                       " +
            " conf_valor_recebido         DECIMAL(10,2),                     " +
            " conf_parcelas               INTEGER,                           " +
            " vendac_chave                VARCHAR(70),                       " +
            " conf_temp                   CHAR DEFAULT N,                    " +
            " conf_enviado                CHAR DEFAULT 1                     " +
            ");");
    private static String SQL_VENDA_D_TEMP = ("CREATE TABLE VENDAD_TEMP (" +
            " vendad_eanTEMP                VARCHAR DEFAULT 50, " +
            " vendad_prd_codigoTEMP         INTEGER,            " +
            " vendad_prd_codigo_internoTEMP INTEGER, " +
            " vendad_prd_descricaoTEMP      VARCHAR DEFAULT 50, " +
            " vendad_quantidadeTEMP         DECIMAL(10,2),      " +
            " vendad_preco_vendaTEMP        DECIMAL(10,2),      " +
            " vendad_prd_unidadeTEMP        VARCHAR(5),         " +
            " vendad_totalTEMP              DECIMAL(10,2)" +
            ");");
    private static String SQL_RECEBER = ("CREATE TABLE CONREC (" +
            " rec_codigo              INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " rec_codformpgto_ext     VARCHAR(5),                        " +
            " rec_dias_vencimento     VARCHAR(5),                        " +
            " CODPERFIL               INTEGER,                           " +
          //" conf_descricao_formpgto VARCHAR(30),                       " +
            " rec_descricao_formpgto  VARCHAR(30),                       " +
            " rec_numparcela          INTEGER,                           " +
            " rec_cli_codigo          INTEGER,                           " +
            " rec_cli_nome            VARCHAR DEFAULT 50,                " +
            " vendac_chave            VARCHAR DEFAULT 70,                " +
            " rec_datamovimento       DATE,                              " +
            " rec_valor_receber       DECIMAL (10,2),                    " +
            " rec_valorpago           DECIMAL (10,2),                    " +
            " rec_datavencimento      DATE,                              " +
            " rec_data_que_pagou      DATE,                              " +
            " rec_recebeu_com         VARCHAR DEFAULT 20,                " +
            " rec_enviado             CHAR DEFAULT 1                     " +
            ");");
    private static String SQL_EMPRESA = ("CREATE TABLE IF NOT EXISTS EMPRESAS (" +
            " CODEMPRESA  INTEGER      NOT NULL,                    " +
            " CODPERFIL   INTEGER,                                  " +
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
            " CODVEND         INTEGER,        " +
            " HABCADCLIE      CHAR(1),        " +
            " DT_ULT_ATU_CLIE DATETIME,       " +
            " DT_ULT_ATU_CONT DATETIME,       " +
            " USUARIO         VARCHAR (30),   " +
            " SENHA           VARCHAR  (100), " +
            " CODEMPRESA      INTEGER,        " +
            " CODPERFIL       INTEGER" +
            " ); ");
    private static String SQL_BLOQUEIOS = (" CREATE TABLE IF NOT EXISTS BLOQCLIE (" +
            " CODBLOQ   VARCHAR (3),  " +
            " DESCRICAO VARCHAR (30), " +
            " BLOQUEAR  CHAR    (1),  " +
            " LIBERAR   CHAR    (1),  " +
            " CODPERFIL INTEGER,      " +
            " FPAVISTA  VARCHAR (3)" +
            " ); ");
    private static String SQL_PERFIL = (" CREATE TABLE IF NOT EXISTS PERFIL (" +
            " CODPERFIL  INTEGER PRIMARY KEY AUTOINCREMENT,   " +
            " LICENCA    VARCHAR (20),                        " +
            " HOST       VARCHAR (50),                        " +
            " NOMEPERFIL VARCHAR (20)" +
            " ); ");
    private static String SQL_DIAS_CONTATOS = ("create table if not exists diascontatotemporario (" +
            " coddiacontato  integer primary key autoincrement, " +
            " dia_visita     varchar(40), " +
            " cod_dia_semana integer,     " +
            " hora_inicio    integer,     " +
            " minuto_inicio  integer,     " +
            " hora_final     integer,     " +
            " minuto_final   integer      " +
            ");");
    private static String SQL_DIAS_CONTATOS_FINAL = ("create table if not exists dias_contatos (" +
            " coddiacontato      integer primary key autoincrement, " +
            " cod_dia_semana     integer,                           " +
            " codcontatoint      integer,                           " +
            " codcontatoext      integer,                           " +
            " hora_inicio        integer,                           " +
            " minuto_inicio      integer,                           " +
            " hora_final         integer,                           " +
            " minuto_final       integer                            " +
            ");");
    private static String SQL_PRODUTOS_CONTATOS = ("create table if not exists produtos_contatos (" +
            " cod_produto_contato        integer primary key autoincrement, " +
            " cod_produto_manual         varchar(15), " +
            " cod_interno_contato        integer,     " +
            " cod_externo_contato        integer,     " +
            " cod_item                   integer" +
            ");");

    private static String SQL_PRODUTOS_CONTATOS_TEMP = ("create table if not exists produtos_contatos_temp (" +
            " cod_produto_contato        integer primary key autoincrement, " +
            " cod_produto_manual         varchar(15), " +
            " cod_interno_contato        integer,     " +
            " cod_externo_contato        integer,     " +
            " cod_item                   integer" +
            ");");

    private static String SQL_CONTATOS_TEMPORARIOS = (" CREATE TABLE IF NOT EXISTS CONTATO_TEMPORARIO (" +
            " CODCONTATO_INT INTEGER       PRIMARY KEY AUTOINCREMENT," +
            " CODCLIENTE                   INTEGER,                  " +
            " CODCLIE_EXT                  INTEGER,                  " +
            " CODCONTATO_EXT               VARCHAR (7),              " +
            "CODCLIENTE_EXT                VARCHAR (7),              " +
            " NOME                         VARCHAR (60)  NOT NULL,   " +
            " CARGO                        VARCHAR (30),             " +
            " SETOR                        VARCHAR (30),             " +
            " CODCARGO                     INTEGER,                  " +
            " CODCARGO_EXT                 INTEGER,                  " +
            " DOCUMENTO                    VARCHAR(15),              " +
            " DATA                         VARCHAR(10),              " +
            " CEP                          VARCHAR(10),              " +
            " ENDERECO                     VARCHAR(15),              " +
            " NUMERO                       VARCHAR(10),              " +
            " COMPLEMENTO                  VARCHAR(15),              " +
            " UF                           CHAR(2),                  " +
            " CODVENDEDOR                  INTEGER,                  " +
            " CODBAIRRO                    INTEGER,                  " +
            " BAIRRO                       VARCHAR (30),             " +
            " CODCIDADE                    INTEGER,                  " +
            " UFPOSITION                   INTEGER,                  " +
            " DESC_CIDADE                  VARCHAR (30),             " +
            " EMAIL                        VARCHAR (100),            " +
            " TEL1                         VARCHAR (15),             " +
            " TEL2                         VARCHAR (15),             " +
            " TIPO                         CHAR(1),                  " +
            " TIPO_POS                     INTEGER,                  " +
            " FLAGINTEGRADO                CHAR(1),                  " +
            " OBS                          TEXT,                     " +
            " CODPERFIL                    INTEGER,                  " +
            " CARGO_POS                    INTEGER                   " +
            ");");
    private static String SQL_CARGOS = ("CREATE TABLE IF NOT EXISTS CARGOS (" +
            "   DES_CARGO           VARCHAR(30),                         " +
            "   CODCARGO_EXT        INTEGER,                             " +
            "   ATIVO               CHAR(1),                             " +
            "   CODCARGO            INTEGER PRIMARY KEY AUTOINCREMENT); ");

    private static String SQL_FORMAPAGAMENTOS = ("CREATE TABLE IF NOT EXISTS FORMAPAGAMENTO (" +
            " CODIGO           INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " DESCRICAO        VARCHAR(30),                       " +
            " STATUS           CHAR(1),                           " +
            " CODPERFIL        INTEGER,                           " +
            " CODEXTERNO       INTEGER" +
            ");");

    private static String SQL_AGENDA = ("CREATE TABLE IF NOT EXISTS AGENDA (" +
            " CODIGO      INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " NOMECONTATO VARCHAR(30),                       " +
            " CODCONTATO  INTEGER,                           " +
            " STATUS      CHAR(1),                           " +
            " SITUACAO    CHAR(1),                           " +
            " CODPERFIL   INTEGER,                           " +
            " DATAAGEND   DATETIME NOT NULL,                 " +
            " DESCRICAO   TEXT                               " +
            ");");



    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
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
            db.execSQL(SQL_DIAS_CONTATOS);
            db.execSQL(SQL_DIAS_CONTATOS_FINAL);
            db.execSQL(SQL_PRODUTOS_CONTATOS);
            db.execSQL(SQL_CONTATOS_TEMPORARIOS);
            db.execSQL(SQL_PRODUTOS_CONTATOS_TEMP);
            db.execSQL(SQL_CARGOS);
            db.execSQL(SQL_FORMAPAGAMENTOS);
            db.execSQL(SQL_AGENDA);
        } catch (Exception e) {
            e.toString();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int VersaoAntiga, int VersaoNova) {

        if (VersaoNova > VersaoAntiga) {
            //VENDAD_TEMP

            try {
                db.execSQL("ALTER TABLE VENDAD_TEMP ADD vendad_prd_codigo_internoTEMP INTEGER");
            } catch (Exception e) {
                e.toString();
            }

            //PARAMAPP
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD HABCLIEXVEND CHAR(1)");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE PARAMAPP ADD HABCADASTRO_CLIE INTEGER");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE PARAMAPP ADD HABCONTROLQTDMINVEND CHAR(1)");
            }catch (Exception e){
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD HABALTPRECOVENDA CHAR(1)");
            }catch(Exception e){
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE PARAMAPP ADD VLMINVENDA VARCHAR(30)");
            }catch(Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE PARAMAPP ADD DT_ULT_CONT DATETIME");
            }catch (Exception e){
                e.toString();
            }


            //CONTATO TEMPORARIO
            try {
                db.execSQL("ALTER TABLE CONTATO_TEMPORARIO ADD FLAGINTEGRADO CHAR(1)");
            } catch (Exception e) {
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE CONTATO_TEMPORARIO ADD SETOR VARCHAR(30)");
            } catch (Exception e) {
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE CONTATO_TEMPORARIO ADD CARGO_POS INTEGER");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONTATO_TEMPORARIO ADD CODCARGO INTEGER");
            }catch (Exception e){
                e.toString();
            }

            //CONTATOS

            try {
                db.execSQL("ALTER TABLE CONTATO ADD FLAGINTEGRADO CHAR(1)");
            } catch (Exception e) {
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE CONTATO ADD SETOR VARCHAR(30)");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONTATO ADD CODCARGO INTEGER");
            }catch (Exception e){
                e.toString();
            }


            //USUÁRIOS
            try {
                db.execSQL("ALTER TABLE USUARIOS ADD DT_ULT_ATU_CLIE DATETIME");
            } catch (Exception e) {
                e.toString();

            }
            try{
                db.execSQL("ALTER TABLE USUARIOS ADD HABCADCLIE CHAR(1)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE USUARIOS ADD DT_ULT_ATU_CONT DATETIME");
            }catch (Exception e){
                e.toString();
            }


            //PRODUTOS_CONTATOS_TEMP

            try {
                db.execSQL("ALTER TABLE produtos_contatos_temp ADD cod_item integer");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("alter table produtos_contatos_temp add cod_externo_contato integer");
            }catch (Exception e){
                e.toString();
            }

            //PRODUTOS CONTATOS

            try {
                db.execSQL("alter table produtos_contatos add cod_item integer");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("alter table produtos_contatos add cod_externo_contato integer");
            }catch (Exception e){
                e.toString();
            }

            //CARGOS

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS CARGOS (" +
                        "   DES_CARGO           VARCHAR(30)                       " +
                        "   ATIVO               CHAR(1),                          " +
                        "   CODCARGO_EXT        INTEGER,                          " +
                        "   CODCARGO            INTEGER PRIMAY KEY AAUTOINCREMENT " +
                        "); ");
            } catch (Exception e) {
                e.toString();
            }
            try{
                db.execSQL("alter table CARGOS add ATIVO CHAR(1)");
            }catch (Exception e){
                e.toString();
            }

            //ITENS

            try{
                db.execSQL("ALTER TABLE ITENS ADD QTDMINVEND FLOAT ");
            }catch (Exception e){
                e.toString();
            }

            //PEDOPER

            try{
                db.execSQL("ALTER TABLE PEDOPER ADD DATAVENDA DATETIME");
            }catch (Exception e){
                e.toString();
            }

            //CONFPAGAMENTO

            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD conf_codformpgto_ext VARCHAR(5)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD conf_descricao_formpgto VARCHAR(30)");
            }catch (Exception e){
                e.toString();
            }
            try {
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD CODPERFIL INTEGER");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD conf_dias_vencimento VARCHAR(5)");
            }catch (Exception e){
                e.toString();
            }

            //FORMAPAGAMENTO
            try{
                db.execSQL("CREATE TABLE IF NOT EXISTS FORMAPAGAMENTO (" +
                        "   CODIGO           INTEGER PRIMAY KEY AAUTOINCREMENT, " +
                        "   DESCRICAO        VARCHAR(30),                       " +
                        "   STATUS           CHAR(1),                           " +
                        "   CODPERFIL        INTEGER,                           " +
                        "   CODEXTERNO       INTEGER); ");
            }catch (Exception e){
                e.toString();
            }

            //ESTADOS
            try {
                db.execSQL("ALTER TABLE ESTADOS ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }

            //CIDADES
            try {
                db.execSQL("ALTER TABLE CIDADES ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }

            //BAIRROS
            try {
                db.execSQL("ALTER TABLE BAIRROS ADD CODPERFIL INTEGER");
            }catch(Exception e){
                e.toString();
            }

            //CONREC

            try{
                db.execSQL("ALTER TABLE CONREC ADD rec_codformpgto_ext VARCHAR(5)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONREC ADD rec_dias_vencimento VARCHAR(5)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONREC ADD CODPERFIL INTEGER");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONREC ADD rec_descricao_formpgto VARCHAR(30)");
            }catch (Exception e){
                e.toString();
            }

            //DIAS_CONTATOS

            try{
                db.execSQL("alter table dias_contatos add codcontatoext integer");
            }catch (Exception e){
                e.toString();
            }

            //NOVOS CAMPOS PARA O DB 4

            // TABELA CONFPAGAMENTO
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD CONF_DATA_VENCIMENTO DATE");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD MODIFY conf_sementrada_comentrada CHAR(1)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD MODIFY conf_tipo_pagamento VARCHAR(20)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD MODIFY conf_recebeucom_din_chq_car VARCHAR(20)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONFPAGAMENTO ADD MODIFY vendac_chave VARCHAR(70)");
            }catch (Exception e){
                e.toString();
            }

            try{
                db.execSQL("ALTER TABLE CONTATO ADD CODCLIENTE_EXT VARCHAR(7)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("ALTER TABLE CONTATO_TEMPORARIO ADD CODCLIENTE_EXT VARCHAR(7)");
            }catch (Exception e){
                e.toString();
            }
            try{
                db.execSQL("CREATE TABLE IF NOT EXISTS AGENDA (" +
                        " CODIGO      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " NOMECONTATO VARCHAR(30),                       " +
                        " CODCONTATO  INTEGER,                           " +
                        " STATUS      CHAR(1),                           " +
                        " SITUACAO    CHAR(1),                           " +
                        " CODPERFIL   INTEGER,                           " +
                        " DATAAGEND   DATETIME NOT NULL,                 " +
                        " DESCRICAO   TEXT                               " +
                        ");");
            }catch (Exception e){
                e.toString();
            }
        }
    }
}
