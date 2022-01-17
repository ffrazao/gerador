package com.frazao.gerador.comum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DefinicaoTabela extends Definicao implements Comparable<DefinicaoTabela> {

	@Setter(AccessLevel.NONE)
	private List<DefinicaoEstruturaDados> dadosAcessorios = new ArrayList<>();

	@Setter(AccessLevel.NONE)
	private List<DefinicaoEstruturaDados> estruturaList;

	public DefinicaoTabela(List<DefinicaoEstruturaDados> estruturaList) {
		if (CollectionUtils.isEmpty(estruturaList)) {
			throw new RuntimeException("Informação inválida");
		}
		this.estruturaList = estruturaList;
	}

	public void addDadosAcessorios(DefinicaoEstruturaDados ded) {
		this.dadosAcessorios.add(ded);
	}

	@Override
	public int compareTo(DefinicaoTabela o) {
		return Comparator.comparing(DefinicaoTabela::getEsquema).thenComparing(DefinicaoTabela::getTabela).compare(this,
				o);
	}

	public Set<DefinicaoEstruturaDados> getChavePrimariaList() {
		return this.getEstruturaList().stream().filter(e -> e.isChavePrimaria()).collect(Collectors.toSet());
	}

	public Set<DefinicaoEstruturaDados> getDemaisCamposList() {
		return this.getEstruturaList().stream().filter(e -> !e.isChavePrimaria()).collect(Collectors.toSet());
	}

	public boolean isChavePrimariaComposta() {
		int tamanho = this.getChavePrimariaList().size();
		if (tamanho == 0) {
			throw new IllegalStateException("Informações inconsistentes");
		}
		return tamanho > 1;
	}

	@EqualsAndHashCode.Include
	public String getEsquema() {
		return this.estruturaList.get(0).getEsquema();
	}

	@Override
	public String getNomeJavaClasse() {
		return this.estruturaList.get(0).getNomeJavaClasse();
	}

	@Override
	public String getNomeJavaClasseCompleto() {
		return this.estruturaList.get(0).getNomeJavaClasseCompleto();
	}

	@Override
	public String getNomeJavaObjeto() {
		return this.estruturaList.get(0).getNomeJavaObjeto();
	}

	@Override
	public String getNomeJavaPacote() {
		return this.estruturaList.get(0).getNomeJavaPacote();
	}

	@EqualsAndHashCode.Include
	public String getTabela() {
		return this.estruturaList.get(0).getTabela();
	}

}
