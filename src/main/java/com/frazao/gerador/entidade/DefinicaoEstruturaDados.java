package com.frazao.gerador.entidade;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DefinicaoEstruturaDados extends Definicao implements Comparable<DefinicaoEstruturaDados> {

	@Setter(AccessLevel.NONE)
	@EqualsAndHashCode.Include
	private String coluna;

	@Setter(AccessLevel.NONE)
	@EqualsAndHashCode.Include
	private String esquema;

	@Setter(AccessLevel.NONE)
	private Map<String, Object> propriedadeList = new HashMap<>();

	private DefinicaoEstruturaDados referencia;

	@Setter(AccessLevel.NONE)
	@EqualsAndHashCode.Include
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
		return Comparator.comparing(DefinicaoEstruturaDados::getEsquema)
				.thenComparing(DefinicaoEstruturaDados::getTabela).thenComparing(DefinicaoEstruturaDados::getColuna)
				.compare(this, o);
	}

	public String getNomeJavaClasseCompleto() {
		return String.format("%s.%s", this.getNomeJavaPacote(), this.getNomeJavaClasse());
	}

	public String getNomeJavaClasse() {
		return converterCase(this.getTabela(), true);
	}

	public String getNomeJavaObjeto() {
		return converterCase(this.getTabela(), false);
	}

	public String getNomeJavaPacote() {
		switch (this.getEsquema().toLowerCase()) {
		case "public":
			return "_public";
		default:
			return this.getEsquema().toLowerCase();
		}
	}

	public String getNomeJavaPropriedade() {
		switch (converterCase(this.coluna, false)) {
		case "class":
			return "_class";
		case "default":
			return "_default";
		default:
			return converterCase(this.coluna, false);
		}
	}

	public Object getPropriedade(String nome) {
		return this.propriedadeList.get(nome);
	}

	public String getTipoPropriedade() {
		if (isChaveEstrangeira()) {
			return String.format("%s", this.getReferencia().getNomeJavaClasseCompleto());
		} else {
			String tipo = (String) this.getPropriedade("TYPE_NAME");
			if (tipo == null) {
				throw new RuntimeException("erro");
			}
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
			case "\"core\".\"id\"": 
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
		return this.getReferencia() != null;
	}

	public boolean isChavePrimaria() {
		return this.getPropriedade("PK") != null;
	}

	@Override
	public String toString() {
		return String.format("%s.%s.%s\n", this.esquema, this.tabela,
				this.coluna + (this.isChavePrimaria() ? "[pk]" : "") + (this.isChaveEstrangeira() ? "[fk]" : ""));
	}

}
