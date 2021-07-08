package com.example.application.data.service;

import com.example.application.data.entity.Pessoa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import java.time.LocalDate;

@Service
public class PessoaService extends CrudService<Pessoa, Integer> {

    private PessoaRepository repository;

    public PessoaService(@Autowired PessoaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected PessoaRepository getRepository() {
        return repository;
    }

}
