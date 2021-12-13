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
		return this.getNomeEsquema().compareTo(o.getNomeEsquema()) + this.getNomeTabela().compareTo(o.getNomeTabela());
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

	@Override
	public String getNomeClasseCompletoJava() {
		return String.format("%s.%s", this.getNomePacoteJava(), this.getNomeClasseJava());
	}

	@Override
	public String getNomeClasseJava() {
		return converterCase(this.getNomeTabela(), true);
	}

	@Override
	public String getNomeEsquema() {
		return this.estruturaList.get(0).getEsquema();
	}

	@Override
	public String getNomeObjetoJava() {
		return converterCase(this.getNomeTabela(), false);
	}

	@Override
	public String getNomePacoteJava() {
		return this.getNomeEsquema().toLowerCase();
	}

	@Override
	public String getNomeTabela() {
		return this.estruturaList.get(0).getTabela();
	}

}
