package com.abastos.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abastos.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
