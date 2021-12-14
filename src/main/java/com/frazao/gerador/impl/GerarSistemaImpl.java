package com.frazao.gerador.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.frazao.gerador.GerarSistema;
import com.frazao.gerador.comum.BancoDados;
import com.frazao.gerador.comum.ConteudoEstatico;
import com.frazao.gerador.comum.DefinicaoTabela;
import com.frazao.gerador.comum.GeradorEntidade;
import com.frazao.gerador.comum.InformacaoConexao;
import com.frazao.gerador.comum.ManipulaArquivo;

import lombok.Data;
import lombok.Getter;

@Data
public class GerarSistemaImpl implements GerarSistema {

	@Getter
	private Map<InformacaoConexao, List<DefinicaoTabela>> informacaoConexaoMap = new HashMap<>();
	
	private String localSaida;
	
	private String nomeSistema;
	
	private String pacoteOrganizador = "banco_dados";
	
	private String pacotePadrao;

	@Override
	public GerarSistema carregarDefinicaoTabelaList() throws Exception {
		for (Entry<InformacaoConexao, List<DefinicaoTabela>> informacaoConexao : this.informacaoConexaoMap.entrySet()) {
			BancoDados bd = new BancoDados(informacaoConexao.getKey());
			informacaoConexao.setValue(bd.getDefinicaoBancoDados());
		}
		return this;
	}

	@Override
	public String construirApplication_Yml() throws Exception {
		throw new RuntimeException("Não implementado ainda!");
	}

	@Override
	public GerarSistema construirEntidade() throws Exception {
		ManipulaArquivo ma = new GeradorEntidade(this);
		ma.executar();
		return this;
	}

	@Override
	public GerarSistema construirNovoProjeto() throws Exception {
		ManipulaArquivo ma = new ConteudoEstatico(this);
		ma.executar();
		return this;
	}

	@Override
	public GerarSistema construirRepositorio() throws Exception {
		// System.out.println(this.definicaoTabelaList);

		return this;
	}
	
	@Override
	public void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password) {
		this.informacaoConexaoMap.put(new InformacaoConexao(nomeBanco, driver, url, username, password), null);
	}

	@Override
	public void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList, List<String> excluiFiltroList) {
		this.informacaoConexaoMap.put(new InformacaoConexao(nomeBanco, driver, url, username, password,
				adicionaFiltroList, excluiFiltroList), null);
	}

}
