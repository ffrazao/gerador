package com.frazao.gerador;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.frazao.gerador.comum.DefinicaoTabela;
import com.frazao.gerador.comum.InformacaoConexao;

@Service
public interface GerarSistema {

	void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password);

	void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList, List<String> excluiFiltroList);

	GerarSistema carregarDefinicaoTabelaList() throws Exception;

	String construirApplication_Yml() throws Exception;

	GerarSistema construirEntidade() throws Exception;

	GerarSistema construirNovoProjeto() throws Exception;

	GerarSistema construirRepositorio() throws Exception;

	Map<InformacaoConexao, List<DefinicaoTabela>> getInformacaoConexaoMap();

	String getLocalSaida();

	String getNomeSistema();

	String getPacoteOrganizador();

	String getPacotePadrao();

	void setLocalSaida(String string);

	void setNomeSistema(String nomeSistema);
	
	void setPacoteOrganizador(String pacoteOrganizador);

	void setPacotePadrao(String pacotePadrao);

}
