package com.frazao.gerador.comum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
public class InformacaoConexao {

	private String driver;

	@Setter(AccessLevel.NONE)
	private final Collection<String> filtroAdicionaList = new ArrayList<>();

	@Setter(AccessLevel.NONE)
	private final Collection<String> filtroExcluiList = new ArrayList<>();

	private String nomeBanco;

	private String password;

	private String url;

	private String username;

	public InformacaoConexao(String nomeBanco, String driver, String url, String username, String password) {
		this(nomeBanco, driver, url, username, password, Arrays.asList("%"), Arrays.asList("information_schema"));
	}

	public InformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList, List<String> excluiFiltroList) {
		this.nomeBanco = nomeBanco;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.filtroAdicionaList.addAll(adicionaFiltroList);
		this.filtroExcluiList.addAll(adicionaFiltroList);
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

}
