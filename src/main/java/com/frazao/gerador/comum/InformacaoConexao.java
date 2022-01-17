package com.frazao.gerador.comum;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InformacaoConexao implements Comparable<InformacaoConexao> {

	private String driver;

	@Setter(AccessLevel.NONE)
	private final Collection<String> filtroAdicionaList = new TreeSet<>();

	@Setter(AccessLevel.NONE)
	private final Collection<String> filtroExcluiList = new TreeSet<>();

	@EqualsAndHashCode.Include
	private String nomeBanco;

	private String password;

	private String url;

	private String username;
	
	private boolean somenteTabelas = false;

	public InformacaoConexao(String nomeBanco, String driver, String url, String username, String password) {
		this(nomeBanco, driver, url, username, password, null, null);
	}

	public InformacaoConexao(String nomeBanco, String driver, String url, String username, String password, Collection<String> adicionaFiltroList) {
		this(nomeBanco, driver, url, username, password, adicionaFiltroList, null);
	}

	public InformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			Collection<String> adicionaFiltroList, Collection<String> excluiFiltroList) {
		this.nomeBanco = nomeBanco;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.filtroAdicionaList.addAll((adicionaFiltroList == null || adicionaFiltroList.isEmpty()) ? Arrays.asList("%") : adicionaFiltroList);
		if (excluiFiltroList != null) {			
			this.filtroExcluiList.addAll(excluiFiltroList);
		}
		// excluir schemas padrão de bancos de dados
		this.filtroExcluiList.addAll(Arrays.asList("information_schema", "pg_catalog"));
	}

	public PlataformaBanco getPlataformaBanco() {
		return PlataformaBanco.encontrarPlataforma(this.driver);
	}

	public void filtroAdicionaListAdd(String filtro) {
		this.filtroAdicionaList.add(filtro);
	}

	public void filtroAdicionaListAddAll(Collection<String> filtro) {
		this.filtroAdicionaList.addAll(filtro);
	}

	public void filtroAdicionaListRemove(String filtro) {
		this.filtroAdicionaList.remove(filtro);
	}

	public void filtroAdicionaListRemoveAll(Collection<String> filtro) {
		this.filtroAdicionaList.removeAll(filtro);
	}

	public void filtroAdicionaListClear() {
		this.filtroAdicionaList.clear();
	}

	public void filtroExcluiListAdd(String filtro) {
		this.filtroExcluiList.add(filtro);
	}

	public void filtroExcluiListAddAll(Collection<String> filtro) {
		this.filtroExcluiList.addAll(filtro);
	}

	public void filtroExcluiListRemove(String filtro) {
		this.filtroExcluiList.remove(filtro);
	}

	public void filtroExcluiListRemoveAll(Collection<String> filtro) {
		this.filtroExcluiList.removeAll(filtro);
	}

	public void filtroExcluiListClear() {
		this.filtroExcluiList.clear();
	}

	@Override
	public int compareTo(InformacaoConexao o) {
		return Comparator.comparing(InformacaoConexao::getNomeBanco).compare(this, o);
	}

}
