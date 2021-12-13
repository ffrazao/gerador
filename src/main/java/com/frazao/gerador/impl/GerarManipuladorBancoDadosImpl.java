package com.frazao.gerador.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.frazao.gerador.GerarManipuladorBancoDados;
import com.frazao.gerador.comum.BancoDados;
import com.frazao.gerador.comum.ConteudoEstatico;
import com.frazao.gerador.comum.DefinicaoTabela;
import com.frazao.gerador.comum.GeradorEntidade;
import com.frazao.gerador.comum.InformacaoConexao;
import com.frazao.gerador.comum.ManipulaArquivo;

import lombok.Data;
import lombok.Getter;

@Data
public class GerarManipuladorBancoDadosImpl implements GerarManipuladorBancoDados {

	private List<DefinicaoTabela> definicaoTabelaList;
	@Getter
	private Set<String[]> excluiFiltroSet = new HashSet<>();
	@Getter
	private Set<String> filtroSet = new HashSet<>();
	private InformacaoConexao informacaoConexao;
	private String localSaida;
	private String nomeSistema;
	private String pacoteOrganizador = "banco_dados";
	private String pacotePadrao;

	@Override
	public GerarManipuladorBancoDados addFiltro(String padrao) {
		if (padrao == null || padrao.isBlank()) {
			throw new RuntimeException("Filtro inválido");
		}
		this.filtroSet.add(padrao);
		return this;
	}

	@Override
	public GerarManipuladorBancoDados carregarDefinicaoTabelaList() throws Exception {
		BancoDados bd = new BancoDados(this.informacaoConexao);
		this.definicaoTabelaList = bd.getDefinicaoBancoDados(filtroSet, excluiFiltroSet);
		return this;
	}

	@Override
	public String construirApplication_Yml() throws Exception {
		throw new RuntimeException("Não implementado ainda!");
	}

	@Override
	public GerarManipuladorBancoDados construirEntidade() throws Exception {
		ManipulaArquivo ma = new GeradorEntidade(this.informacaoConexao, this.definicaoTabelaList, this.localSaida,
				this.pacotePadrao);
		ma.executar();
		return this;
	}

	@Override
	public GerarManipuladorBancoDados construirNovoProjeto() throws Exception {
		ManipulaArquivo ma = new ConteudoEstatico(this.localSaida, this.pacotePadrao);
		ma.executar();
		return this;
	}

	@Override
	public GerarManipuladorBancoDados construirRepositorio() throws Exception {
		System.out.println(this.definicaoTabelaList);

		return this;
	}

	@Override
	public void excFiltro(String filtro) {
		this.excluiFiltroSet.add(filtro.split("\\."));
	}

	@Override
	public void setInformacaoConexao(String driver, String url, String username, String password) {
		this.informacaoConexao = InformacaoConexao.builder().driver(driver).url(url).username(username)
				.password(password).build();
	}

}
