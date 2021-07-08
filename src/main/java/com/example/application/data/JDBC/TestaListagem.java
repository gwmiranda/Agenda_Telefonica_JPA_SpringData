package com.example.application.data.JDBC;

import java.sql.*;

public class TestaListagem {
    public static void main(String[] args) throws SQLException {
        ConexaoAgenda conexaoAgenda = new ConexaoAgenda();
        Connection connection = conexaoAgenda.recuperarConexao();

        Statement stm = connection.createStatement();
        stm.execute("SELECT id, nome, sobrenome, dataNascimento, parentesco from Pessoa");

        ResultSet rst = stm.getResultSet();

        while (rst.next()){
            Integer id = rst.getInt("id");
            String nome = rst.getString("nome");
            String sobrenome = rst.getString("sobrenome");
            String nascimento = rst.getString("dataNascimento");
            String parentesco = rst.getString("parentesco");
            System.out.println(id);
            System.out.println(nome);
            System.out.println(sobrenome);
            System.out.println(nascimento);
            System.out.println(parentesco);
        }


    }
}
