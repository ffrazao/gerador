package com.frazao.gerador.comum;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class DefinicaoEstruturaDados extends Definicao implements Comparable<DefinicaoEstruturaDados> {

	private String coluna;
	private String esquema;
	@Setter(AccessLevel.NONE)
	private Map<String, Object> propriedadeList = new HashMap<>();
	private DefinicaoEstruturaDados referencia;
	private String tabela;

	public DefinicaoEstruturaDados(String esquema, String tabela, String coluna) {
		this.esquema = esquema;
		this.tabela = tabela;
		this.coluna = coluna;
	}

	public void addPropriedade(String nome, Object valor) {
		this.propriedadeList.put(nome, valor);
	}

	@Override
	public int compareTo(DefinicaoEstruturaDados o) {
		return this.getEsquema().compareTo(o.getEsquema()) + this.getTabela().compareTo(o.getTabela())
				+ this.getColuna().compareTo(o.getColuna());
	}

	public String getNomeClasseCompletoJava() {
		return String.format("%s.%s", this.getNomePacoteJava(), this.getNomeClasseJava());
	}

	public String getNomeClasseJava() {
		return converterCase(this.getNomeTabela(), true);
	}

	public String getNomeEsquema() {
		return this.getEsquema();
	}

	public String getNomeObjetoJava() {
		return converterCase(this.getNomeTabela(), false);
	}

	public String getNomePacoteJava() {
		return this.getNomeEsquema().toLowerCase();
	}

	public String getNomePropriedade() {
		return converterCase(this.getColuna(), false);
	}

	public String getNomeTabela() {
		return this.getTabela();
	}

	public Object getPropriedade(String nome) {
		return this.propriedadeList.get(nome);
	}

	public String getTipoPropriedade() {
		if (isChaveEstrangeira()) {
			return String.format("%s", this.getReferencia().getNomeClasseCompletoJava());
		} else {
			String tipo = (String) this.getPropriedade("TYPE_NAME");
			if (tipo == null) {
				System.out.println("erro");
			}
			System.out.println("~>" + tipo);
			switch (tipo) {
			case "_bool":
			case "bool":
				return "Boolean";
			case "date":
			case "time":
			case "timestamp":
			case "timestamptz":
				return "Calendar";
			case "_int2":
			case "_int4":
			case "int2":
			case "int2vector":
			case "int4":
			case "int8":
			case "int":
			case "smallint":
			case "bytea":
			case "integer":
			case "bigint":
			case "smallserial":
			case "serial":
			case "bigserial":
				return "Integer";
			case "decimal":
			case "numeric":
			case "real":
			case "float4":
			case "float8":
			case "double":
			case "_numeric":
				return "Double";
			case "_char":
			case "_name":
			case "_regtype":
			case "_text":
			case "anyarray":
			case "bpchar":
			case "char":
			case "inet":
			case "interval":
			case "json":
			case "name":
			case "varchar":
			case "xml":
			case "pg_dependencies":
			case "pg_lsn":
			case "pg_mcv_list":
			case "pg_ndistinct":
			case "pg_node_tree":
			case "regproc":
			case "regtype":
			case "text":
			case "xid":
			case "oid":
			case "oidvector":
			case "_oid":
			default:
				return "String";
			}
		}
	}

	public boolean isChaveEstrangeira() {
		if (this.getReferencia() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isChavePrimaria() {
		return this.getPropriedade("PK") != null;
	}

	@Override
	public String toString() {
		return String.format("%s.%s.%s\n", this.getEsquema(), this.getTabela(),
				this.getColuna() + (this.isChavePrimaria() ? "[pk]" : "") + (this.isChaveEstrangeira() ? "[fk]" : ""));
	}

}
