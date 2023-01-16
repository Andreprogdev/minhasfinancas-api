package com.abastos.minhasfinancas.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.abastos.minhasfinancas.exception.RegraNegocioException;
import com.abastos.minhasfinancas.model.entity.Lancamento;
import com.abastos.minhasfinancas.model.entity.Usuario;
import com.abastos.minhasfinancas.model.enums.StatusLancamento;
import com.abastos.minhasfinancas.model.repository.LancamentoRepository;
import com.abastos.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.abastos.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	//classe que a gente tá testando, precisamos que chame os métodos reais
	@SpyBean
	LancamentoServiceImpl service;
	
	//para simular comportamentos do repository que são chamados no service.
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//acao
		Lancamento lancamento = service.salvar(lancamentoASalvar);
	
		//verificacao
		Assertions.assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		
		//acao e verificacao
		Assertions.catchThrowableOfType(() -> service.salvar(lancamentoASalvar) , RegraNegocioException.class);
		
		//segunda verificação, garantir que ele não chamou o salvar.
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}

	@Test
	public void deveAtualizarLancamento() {
		//cenário
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//acao
		service.atualizar(lancamentoSalvo);
	
		//verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
		
	}

	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo(){
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();		
		
		//acao e verificacao
		Assertions.catchThrowableOfType(() -> service.atualizar(lancamentoASalvar) , NullPointerException.class);
		
		//segunda verificação, garantir que ele não chamou o salvar.
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}


	@Test
	public void deveDeletarUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		lancamento.setId(1l);
		
		//execucao
		service.deletar(lancamento);
		
		//verificacao
		Mockito.verify(repository).delete(lancamento);
		
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		
		//execucao
		Assertions.catchThrowableOfType(() -> service.deletar(lancamento) , NullPointerException.class);
		
		//verificacao
		Mockito.verify(repository, Mockito.never()).save(lancamento);
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		lancamento.setId(1l);
		
		List<Lancamento> lista = Arrays.asList(lancamento); //pega vários objetos e coloca dentro de uma lista p mim, cada argumento é um objeto
		Mockito.when(repository.findAll(Mockito.any(Example.class)) ).thenReturn(lista);
	
	
		//acao
		List<Lancamento> resultado = service.buscar(lancamento);
	
		//verificacao
		Assertions.assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
	}

	@Test
	public void deveAtualizarStatusDeUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		lancamento.setId(1l);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		//execucao
		service.atualizarStatus(lancamento, novoStatus);
		
		//verificacao
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		//cenário
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		//execucao
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificacao
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		//cenário
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();	
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execucao
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificacao
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}

	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		Lancamento lancamento = new Lancamento();
		
		Throwable erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida!");
	
		lancamento.setDescricao("");
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida!");
		
		lancamento.setDescricao("Salario");
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mes válido");
		
		lancamento.setMes(0);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mes válido");
		
		lancamento.setMes(13);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mes válido");
		
		lancamento.setMes(1);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido!");
		
		lancamento.setAno(202);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido!");
				
		lancamento.setAno(2022);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário!");
		
		lancamento.setUsuario(new Usuario());
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário!");		
		
		lancamento.getUsuario().setId(1l);
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido");
		
		lancamento.setValor(BigDecimal.ZERO);
		
		lancamento.setValor(BigDecimal.valueOf(1));
		
		erro = Assertions.catchThrowable(() -> service.validar(lancamento) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Coloque um tipo de lancamento válido!");
		
	}

}
