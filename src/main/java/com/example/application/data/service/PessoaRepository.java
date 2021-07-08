package com.example.application.data.service;

import com.example.application.data.entity.Pessoa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {

}