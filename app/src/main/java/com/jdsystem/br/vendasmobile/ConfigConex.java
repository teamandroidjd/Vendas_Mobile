package com.jdsystem.br.vendasmobile;

/**
 * Created by eduardo.costa on 21/10/2016.
 */

public class ConfigConex {
    public static String NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    public static String URLCLIENTES = "/Clientes.exe/soap/IClientes";
    public static String URLUSUARIOS = "/Usuarios.exe/soap/IUsuarios";
    public static String URLPRODUTOS = "/Produtos.exe/soap/IProdutos";
    public static String URLPEDIDOS = "/Pedidos.exe/soap/IPedidos";
    public static String URLDADOSCEP = "http://jdserv.ddns.com.br:8080/webserv/CEP.exe/soap/ICEP";
    public static String URLDADOSHOST = "http://jdserv.ddns.com.br:8080/webserv/HostClientes.exe/soap/IHostClientes";

}
