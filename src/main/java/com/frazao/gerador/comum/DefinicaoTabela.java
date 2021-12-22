package com.frazao.gerador.comum;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
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
		return this.getEsquema().compareTo(o.getEsquema()) + this.getTabela().compareTo(o.getTabela());
	}

	public Set<DefinicaoEstruturaDados> getChavePrimariaList() {
		Set<DefinicaoEstruturaDados> result = new TreeSet<>();
		for (DefinicaoEstruturaDados cp : this.getEstruturaList()) {
			if (cp.isChavePrimaria()) {
				result.add(cp);
			}
		}
		return result;
	}

	public Set<DefinicaoEstruturaDados> getDemaisCamposList() {
		Set<DefinicaoEstruturaDados> result = new TreeSet<>();
		for (DefinicaoEstruturaDados cp : this.getEstruturaList()) {
			if (!cp.isChavePrimaria()) {
				result.add(cp);
			}
		}
		return result;
	}
	
	public boolean isChavePrimariaComposta() {
		if (this.getChavePrimariaList().size() == 0) {
			throw new IllegalStateException("Informações inconsistentes");
		}
		return this.getChavePrimariaList().size() > 1;
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
