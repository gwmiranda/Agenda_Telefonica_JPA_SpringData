package com.example.application.data.service;

import com.example.application.data.entity.Pessoa;
import com.example.application.data.repository.PessoaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService{

    private PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) { this.pessoaRepository = pessoaRepository; }

    public boolean salvar(Pessoa pessoa){
        try {
            pessoaRepository.save(pessoa);
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    public boolean deletar(Pessoa pessoa){
        try {
            pessoaRepository.deleteById(pessoa.getId());
            return true;
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public List<Pessoa> listarPessoas() {
        List<Pessoa> pessoas = (List<Pessoa>) pessoaRepository.findAll();
        return pessoas;
    }

    public Pessoa getPessoaID(java.lang.Integer id){
        Optional<Pessoa>  pessoaOptional = pessoaRepository.findById(id);
        Pessoa pessoa = pessoaOptional.get();
        return pessoa;
    }
}