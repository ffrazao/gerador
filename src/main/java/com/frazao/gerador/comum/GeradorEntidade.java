package com.frazao.gerador.comum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.frazao.gerador.GerarSistema;

public class GeradorEntidade extends ManipulaArquivo {
	
	private static final String NOME_PACOTE_FUNCIONALIDADE = "entidade"; 

	private List<DefinicaoTabela> definicaoTabelaList;
	private Set<String> importacaoBasica = new TreeSet<>();

	private Set<String> importacaoEntidade;
	private InformacaoConexao informacaoConexao;
	
	
	protected String nomeSistema;

	protected String pacoteOrganizador;


	public GeradorEntidade(GerarSistema gerarSistema) {
		super(gerarSistema);
		this.nomeSistema = nomeSistema;
		this.pacoteOrganizador = pacoteOrganizador;

		this.informacaoConexao = informacaoConexao;
		this.definicaoTabelaList = definicaoTabelaList;

		this.importacaoBasica.add("import java.util.*;");
		this.importacaoBasica.add("import javax.persistence.*;");
		this.importacaoBasica.add("import lombok.*;");
		
		this.importacaoBasica.add(String.format("import %s.%s.EntidadeBase;", this.pacotePadrao, NOME_PACOTE_FUNCIONALIDADE));
	}

	@Override
	public void executar() throws Exception {
		List<File> arquivoAtualizado = new ArrayList<File>();

		for (DefinicaoTabela dt : this.definicaoTabelaList) {
			String nomePacote = String.format("%s.%s.%s", this.pacotePadrao, NOME_PACOTE_FUNCIONALIDADE, dt.getNomePacoteJava());
			String nomeClasse = dt.getNomeClasseJava();

			importacaoEntidade = new TreeSet<>();
			importacaoEntidade.addAll(importacaoBasica);
			StringBuilder declaracaoClasse = new StringBuilder();
			declaracaoClasse.append(String.format("@Data")).append("\n");
			declaracaoClasse.append(String.format("@Entity")).append("\n");
			declaracaoClasse.append(String.format("@Table(%s = \"%s\", name = \"%s\")",
					this.informacaoConexao.getPlataformaBanco() == PlataformaBanco.POSTGRES ? "catalog" : "schema",
					dt.getNomeEsquema(), dt.getNomeTabela())).append("\n");
			declaracaoClasse.append(
					String.format("public class %s %s %s {", dt.getNomeClasseJava(), "implements", "EntidadeBase"))
					.append("\n");

			// excrever campos
			// chave primaria
			StringBuilder id = new StringBuilder();
			for (DefinicaoEstruturaDados ded : dt.getChavePrimariaList()) {
				id.append("").append("\n");
				id.append("\t@Id").append("\n");
				id.append("\t@GeneratedValue(strategy = GenerationType.SEQUENCE)").append("\n");
				if (ded.isChaveEstrangeira()) {
					id.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
					id.append(String.format("\tprivate %s %s;", ded.getReferencia().getNomeClasseJava(),
							ded.getNomePropriedade())).append("\n");
					id.append("").append("\n");

					id.append(String.format("\t@OneToOne(mappedBy = \"%s\", cascade = CascadeType.ALL)",
							ded.getReferencia().getNomePropriedade())).append("\n");
					id.append(String.format("\t@PrimaryKeyJoinColumn")).append("\n");
					id.append(String.format("\tprivate %s %s;",
							this.pacotePadrao + "." + NOME_PACOTE_FUNCIONALIDADE + "." + ded.getReferencia().getNomeClasseCompletoJava(),
							ded.getReferencia().getNomePropriedade())).append("\n");
					importacaoEntidade.add(String.format("import %s.%s.%s;", this.pacotePadrao, NOME_PACOTE_FUNCIONALIDADE,
							ded.getReferencia().getNomeClasseCompletoJava()));
				} else {
					id.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
					id.append(String.format("\tprivate %s %s;", ded.getTipoPropriedade(), ded.getNomePropriedade()))
							.append("\n");
				}
			}
			// demais campos
			StringBuilder demais = new StringBuilder();
			for (DefinicaoEstruturaDados ded : dt.getDemaisCamposList()) {
				demais.append("").append("\n");
				// se for uma foreign key
				if (ded.isChaveEstrangeira()) {
					demais.append(String.format("\t@ManyToOne")).append("\n");
					demais.append(String.format("\t@JoinColumn(name = \"%s\")", ded.getColuna())).append("\n");
					demais.append(String.format("\tprivate %s %s;", ded.getNomeClasseJava(), ded.getNomePropriedade()))
							.append("\n");

					importacaoEntidade
							.add(String.format("import %s.%s.%s;", this.pacotePadrao, NOME_PACOTE_FUNCIONALIDADE, ded.getNomeClasseCompletoJava()));
				} else {
					// se for um campo da própria tabela
					demais.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
					demais.append(String.format("\tprivate %s %s;", ded.getTipoPropriedade(), ded.getNomePropriedade()))
							.append("\n");
				}
			}
			// acessorios
			StringBuffer java = new StringBuffer();
			java.append("package ").append(nomePacote).append(";").append("\n").append("\n");
			java.append(importacaoBasica.stream().map(Object::toString).collect(Collectors.joining("\n"))).append("\n")
					.append("\n");
			java.append(declaracaoClasse).append("\n");
			java.append(id).append("\n");
			java.append(demais).append("\n").append("\n");
			java.append("}").append("\n").append("\n");

			salvarArquivoJava(new File(this.localSaida
					+ (this.localSaida.endsWith(File.separator) ? "" : File.separator) + DIRETORIO_FONTE_JAVA),
					nomePacote, nomeClasse, java.toString());

			System.out.println(java);
			// arquivoAtualizado.add(dt.salvar(this.localSaida, this.pacotePadrao));
		}
		// excluir os arquivos q não foram atualizados
	}

}
