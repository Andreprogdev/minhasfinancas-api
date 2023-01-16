package com.abastos.minhasfinancas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.abastos.minhasfinancas.model.entity.Lancamento;
import com.abastos.minhasfinancas.model.enums.StatusLancamento;
import com.abastos.minhasfinancas.model.enums.TipoLancamento;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {

	@Autowired
	LancamentoRepository repository;
	
	@Autowired
	TestEntityManager entityManager;

	
	@Test
	public void deveSalvarUmLancamento() {
		
		//cenário
		Lancamento lancamento = criarLancamento();
		
		//acao
		lancamento = repository.save(lancamento);
	
		//verificacao
		Assertions.assertThat(lancamento.getId()).isNotNull();
	}

	@Test
	public void deveDeletarUmLancamento() {
		//cenário
		  Lancamento lancamento = criarLancamento();
		  lancamento = entityManager.persist(lancamento);
		  lancamento =entityManager.find(Lancamento.class, lancamento.getId());
		  
		  //acao
		  repository.delete(lancamento);
		  
		  //verificacao
		  Lancamento lacamentoInexistente = entityManager.find(Lancamento.class, lancamento.getId());
		  Assertions.assertThat(lacamentoInexistente).isNull();
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		//cenário
		Lancamento lancamento = criarEPersistirUmLancamento();
		
		//acao
		lancamento.setAno(2018);
		lancamento.setDescricao("Teste atualizar");
		lancamento.setStatus(StatusLancamento.CANCELADO);
		repository.save(lancamento); //não só salva, mas também atualiza!
		
		//verificacao
		Lancamento lancamentoAtualizado = entityManager.find(Lancamento.class, lancamento.getId());
		
		Assertions.assertThat(lancamentoAtualizado.getAno()).isEqualTo(2018);
		Assertions.assertThat(lancamentoAtualizado.getDescricao()).isEqualTo("Teste atualizar");
		Assertions.assertThat(lancamentoAtualizado.getStatus()).isEqualTo(StatusLancamento.CANCELADO);
		
	}


	@Test
	public void deveBuscarUmLancamentoPorId() {
		//cenário
		Lancamento lancamento = criarEPersistirUmLancamento();
	
		//acao
		Optional<Lancamento> lancamentoEncontrado = repository.findById(lancamento.getId());
		
		//verificacao
		Assertions.assertThat(lancamentoEncontrado.isPresent()).isTrue();	
	}
	
	//public e static para conseguir acessar na outra classe de testes(na camada service)
	public static Lancamento criarLancamento() {
		return Lancamento.builder()
				.ano(2019)
				.mes(1)
				.descricao("lancamento qualquer")
				.valor(BigDecimal.valueOf(10))
				.tipo(TipoLancamento.RECEITA)
				.status(StatusLancamento.PENDENTE)
				.dataCadastro(LocalDate.now())
				.build();
	}
	
	private Lancamento criarEPersistirUmLancamento() {
		Lancamento lancamento = criarLancamento();
		 lancamento = entityManager.persist(lancamento);
		 return lancamento;
	}
	
}
