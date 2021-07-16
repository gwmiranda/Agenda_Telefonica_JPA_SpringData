package com.example.application.data.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Integer id;
    private String nome;
    private String sobrenome;
    private String parentesco;
    private LocalDate dataNascimento;
    @ElementCollection
    @CollectionTable(name = "contato", joinColumns = @JoinColumn(name = "pessoa_id"))
    @Fetch( FetchMode.JOIN)
    private List<Contato> contatos;

    public Pessoa(){}

    public Pessoa(java.lang.Integer id, String nome, String sobrenome, String parentesco, LocalDate dataNascimento, List<Contato> contatos) {
        this.id = id;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.parentesco = parentesco;
        this.dataNascimento = dataNascimento;
        this.contatos = contatos;
    }

    public java.lang.Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public List<Contato> getContato() {
        return contatos;
    }

    public void setContato(List<Contato> contatos) {
        this.contatos = contatos;
    }

}
