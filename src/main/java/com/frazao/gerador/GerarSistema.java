package com.frazao.gerador;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.frazao.gerador.entidade.DefinicaoTabela;
import com.frazao.gerador.entidade.InformacaoConexao;

@Service
public interface GerarSistema {

	final String DIRETORIO_FONTE_JAVA = "src" + File.separator + "main" + File.separator + "java" + File.separator;

	final String DIRETORIO_FONTE_RESOUCES = "src" + File.separator + "main" + File.separator + "resources"
			+ File.separator;

	final String NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS = "banco_dados";
	
	final String NOME_PACOTE_COMUM = "_comum";

	final String NOME_PACOTE_DAO = NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS + ".%s.dao";

	final String NOME_PACOTE_DOMINIO = NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS + ".%s.dominio";

	final String NOME_PACOTE_DTO = NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS + ".%s.dto";

	final String NOME_PACOTE_ENTIDADE = NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS + ".%s.entidade";

	void addInformacaoConexao(InformacaoConexao informacaoConexao);
	
	void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password);

	void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList);

	void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList, List<String> excluiFiltroList);

	GerarSistema carregarDefinicaoTabelaList() throws Exception;

	GerarSistema construirApplication_Yml() throws Exception;

	GerarSistema construirEntidade() throws Exception;

	GerarSistema construirNovoProjeto() throws Exception;

	GerarSistema construirRepositorio() throws Exception;

	Map<InformacaoConexao, List<DefinicaoTabela>> getInformacaoConexaoMap();

	String getLocalSaida();

	String getNomeSistema();

	String getPacotePadrao();

	void setLocalSaida(String string);

	void setNomeSistema(String nomeSistema);

	void setPacotePadrao(String pacotePadrao);

	void interfacesEspeciais(String tabela, String ...interfaceEspecial);

	List<String> getInterfacesEspeciais(String tabela);
	
	List<String> getInterfacesEspeciais();

}
