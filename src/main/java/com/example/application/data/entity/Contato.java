package com.example.application.data.entity;

import javax.persistence.*;

@Embeddable
public class Contato{

    private Integer contato;

    public Contato(){}

    public Contato(Integer contato) {
        this.contato = contato;
    }

    public java.lang.Integer getContato() {
        return contato;
    }

    public void setContato(java.lang.Integer contato) {
        this.contato = contato;
    }
}
