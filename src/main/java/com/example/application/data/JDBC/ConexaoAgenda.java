package com.example.application.data.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoAgenda {
    public Connection recuperarConexao(){
        try {
            return DriverManager.getConnection("jdbc:postgresql://localhost/agendaTelefonica","postgres", "123456");
        }catch (SQLException ex){
            System.out.println("Erro, não abri conexão");
            throw new RuntimeException(ex);
        }
    }
}
