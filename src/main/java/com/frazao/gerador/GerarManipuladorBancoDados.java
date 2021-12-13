package com.frazao.gerador;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.frazao.gerador.comum.InformacaoConexao;

@Service
public interface GerarManipuladorBancoDados {

	GerarManipuladorBancoDados addFiltro(String padrao);
	
	GerarManipuladorBancoDados carregarDefinicaoTabelaList() throws Exception;

	String construirApplication_Yml() throws Exception;

	GerarManipuladorBancoDados construirEntidade() throws Exception;

	GerarManipuladorBancoDados construirNovoProjeto() throws Exception;

	GerarManipuladorBancoDados construirRepositorio() throws Exception;

	void excFiltro(String string);

	Set<String> getFiltroSet();

	InformacaoConexao getInformacaoConexao();

	String getLocalSaida();

	String getNomeSistema();

	String getPacotePadrao();

	void setInformacaoConexao(InformacaoConexao informacaoConexao);

	void setInformacaoConexao(String driver, String url, String username, String password);

	void setLocalSaida(String string);

	void setNomeSistema(String nomeSistema);

//	void setPacoteOrganizador(String pacoteOrganizador);

	void setPacotePadrao(String pacotePadrao);

}
