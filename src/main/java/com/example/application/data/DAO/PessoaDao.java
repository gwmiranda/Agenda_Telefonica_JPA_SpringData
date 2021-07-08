package com.example.application.data.DAO;

import com.example.application.data.JDBC.ConexaoAgenda;
import com.example.application.data.entity.Pessoa;
import com.vaadin.flow.component.textfield.TextField;
import org.postgresql.jdbc.PgConnection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PessoaDao {
    private Connection con;

    public PessoaDao() {
        this.con = new ConexaoAgenda().recuperarConexao();
    }

    public boolean add(Pessoa p){
        testeConexao();
        String sql = "INSERT INTO Pessoa(nome, sobrenome, dataNascimento, parentesco) VALUES (?,?,?,?);";

        try{
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1,p.getNome());
            stmt.setString(2,p.getSobrenome());
            stmt.setDate(3, Date.valueOf(p.getData_nascimento()));
            stmt.setString(4,p.getParentesco());

            stmt.execute();
            stmt.close();
            con.close();
            return true;

        }catch (SQLException ex){
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean addContato(List<String> listContato){
        testeConexao();
        String sql = "INSERT INTO contato_pessoa(id_pessoa, contato) VALUES (?,?);";

        try{
            for(String contato:listContato) {
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, idMaiorPessoa());
                stmt.setInt(2, Integer.parseInt(contato.toString()));
                stmt.execute();
                stmt.close();

            }
            con.close();
            return true;

        }catch (SQLException ex){
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int idMaiorPessoa() {
        testeConexao();
        String sql = "SELECT MAX(id) FROM pessoa;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            int id = 999999999;
            while (rs.next()){
                id = rs.getInt(1);
            }
            stmt.close();
            rs.close();
            return id;
        }catch (SQLException ex) {
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
            return 999999999;
        }
    }

    public boolean update(Pessoa p) {
        testeConexao();
        String sql = "UPDATE Pessoa SET nome = ?,sobrenome = ?, datanascimento = ? , parentesco = ? WHERE id=?;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getSobrenome());
            stmt.setDate(3, Date.valueOf(p.getData_nascimento()));
            stmt.setString(4, p.getParentesco());
            stmt.setInt(5, p.getId());
            stmt.execute();
            stmt.close();
            con.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean updateContato(Integer id, List<String> listContato){
        testeConexao();
        String sql = "DELETE FROM contato_pessoa WHERE id_pessoa = ?;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1,id);
            stmt.execute();
            stmt.close();
            addContato(listContato);
            con.close();
            return true;
        }catch (SQLException ex){
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean delete(Pessoa p){
        testeConexao();
        String sql = "DELETE FROM Pessoa WHERE id = ?;";

        try{
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1,p.getId());

            stmt.execute();
            stmt.close();
            con.close();
            return true;

        }catch (SQLException ex){
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean deleteContato(Pessoa p){
        testeConexao();
        String sql = "DELETE FROM contato_pessoa WHERE id_pessoa = ?;";

        try{
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1,p.getId());

            stmt.execute();
            stmt.close();
            con.close();
            return true;

        }catch (SQLException ex){
            Logger.getLogger(PessoaDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public List<Pessoa> GetlList(){
        testeConexao();
        List<Pessoa> pessoas = new ArrayList<>();
        String sql = "SELECT * FROM Pessoa;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                Pessoa p = new Pessoa();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setSobrenome(rs.getString("sobrenome"));
                p.setData_nascimento(rs.getDate("datanascimento").toLocalDate());
                p.setParentesco(rs.getString("parentesco"));
                pessoas.add(p);
            }
            stmt.close();
            rs.close();
            con.close();

            }catch (SQLException ex) {
                System.out.println("Erro, Lista não retornada");
                return null;
            }
            return pessoas;
    }

    public List<Integer> getContatos(Integer id) {
        testeConexao();
        List<Integer> listaContatos = new ArrayList<>();
        String sql = "SELECT contato FROM contato_pessoa where id_pessoa=?;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                listaContatos.add(rs.getInt(1));
            }
            return listaContatos;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public Optional<Pessoa> getIdPessoa(int i){
        testeConexao();
        Pessoa p = new Pessoa();
        String sql = "SELECT * FROM Pessoa WHERE id=?;";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1,i);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setSobrenome(rs.getString("sobrenome"));
                p.setData_nascimento(rs.getDate("datanascimento").toLocalDate());
                p.setParentesco(rs.getString("parentesco"));
                return Optional.of(p);
            }
            stmt.close();
            rs.close();
            con.close();

        }catch (SQLException ex) {
            System.out.println("Erro, Lista não retornada");
            return null;
        }
        return Optional.of(p);
    }

    public void testeConexao(){
        try {
            if (con.isClosed()){
                this.con = new ConexaoAgenda().recuperarConexao();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}