package com.frazao.gerador.comum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.frazao.gerador.GerarSistema;

public class GeradorEntidade extends ManipulaArquivo {

	private Set<String> importacaoBasica = new TreeSet<>();

	private String pacoteInicial;

	public GeradorEntidade(GerarSistema gerarSistema) {
		super(gerarSistema);

		this.importacaoBasica.add("import java.util.*;");
		this.importacaoBasica.add("import javax.persistence.*;");
		this.importacaoBasica.add("import lombok.*;");

		pacoteInicial = String.format("%s.%s", this.gerarSistema.getPacotePadrao(), this.gerarSistema.getNomeSistema());

		this.importacaoBasica.add(String.format("import %s.%s.EntidadeBase;", pacoteInicial,
				String.format(GerarSistema.NOME_PACOTE_ENTIDADE, GerarSistema.NOME_PACOTE_COMUM)));
	}

	@Override
	public void executar() throws Exception {
		List<File> arquivoAtualizado = new ArrayList<File>();

		for (Entry<InformacaoConexao, List<DefinicaoTabela>> informacaoConexaoMapa : this.gerarSistema.getInformacaoConexaoMap().entrySet()) {

			String nomePacoteBase = String.format("%s.%s", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_ENTIDADE, informacaoConexaoMapa.getKey().getNomeBanco()));

			for (DefinicaoTabela dt : informacaoConexaoMapa.getValue()) {

				String nomePacote = String.format("%s.%s", nomePacoteBase, dt.getNomeJavaPacote());

				String nomeClasse = dt.getNomeJavaClasse();
				
				Set<String> importacaoEntidade = new TreeSet<>(importacaoBasica);
				
				StringBuilder declaracaoClasse = new StringBuilder();
				declaracaoClasse.append(String.format("@Data")).append("\n");
				declaracaoClasse.append(String.format("@Entity%s", nomeEntidadeDuplicada(dt))).append("\n");
				//declaracaoClasse.append(String.format("@Table(%s = \"%s\", name = \"%s\")", informacaoConexaoMapa.getKey().getPlataformaBanco() == PlataformaBanco.POSTGRES ? "catalog" : "schema", dt.getEsquema(), dt.getTabela())).append("\n");
				declaracaoClasse.append(String.format("@Table(%s = \"%s\", name = \"%s\")", "schema", dt.getEsquema(), dt.getTabela())).append("\n");
				declaracaoClasse.append(String.format("public class %s %s %s {", dt.getNomeJavaClasse(), "implements", "EntidadeBase")).append("\n");

				// escrever campos
				
				// chave primaria
				StringBuilder id = new StringBuilder();

				// se não tem chave primária ignorar tabela
				if (dt.isChavePrimariaComposta()) {
					// CHAVE COMPOSTA
					StringBuilder emb = new StringBuilder();
					emb.append("@Embeddable").append("\n");
					emb.append("@Data").append("\n");
					emb.append(String.format("public class %sId implements Serializable {", dt.getNomeJavaClasse())).append("\n");
					
					emb.append("").append("\n");
					emb.append("\tprivate static final long serialVersionUID = 1L;").append("\n");
					
					Set<String> embImportacaoEntidade = new TreeSet<>(importacaoBasica);
					
					embImportacaoEntidade.add("import java.io.Serializable;");
					
					// chaves externas
					Set<DefinicaoTabela> tabRefSet = new HashSet<>();

					for (DefinicaoEstruturaDados ded : dt.getChavePrimariaList()) {
						emb.append("").append("\n");
						if (ded.isChaveEstrangeira()) {
							
							// captar informacao da tabela referida
							DefinicaoTabela tabRef = this.getDefnicaoTabela(informacaoConexaoMapa.getValue(), ded.getReferencia().getEsquema(), ded.getReferencia().getTabela());
							if (!tabRefSet.add(tabRef) && tabRef.isChavePrimariaComposta()) {
								// ignorar caso o campo já foi contabilizado 
								continue;
							};
							
							if (tabRef.isChavePrimariaComposta()) {
								// se a tabela referida tiver uma chave composta então é para usar o embedable dela 
								emb.append(String.format("\t@Embedded", ded.getReferencia().getNomeJavaPropriedade())).append("\n");
								emb.append(String.format("\tprivate %sId %s;", ded.getReferencia().getNomeJavaClasse(), ded.getNomeJavaPropriedade())).append("\n");
							} else {
								emb.append(String.format("\t@OneToOne", ded.getReferencia().getNomeJavaPropriedade())).append("\n");
								emb.append(String.format("\t@JoinColumn(name = \"%s\")", ded.getColuna())).append("\n");
								// se não, usar ela de forma simples mesmo. 
								emb.append(String.format("\tprivate %s %s;", ded.getReferencia().getNomeJavaClasse(), ded.getNomeJavaPropriedade())).append("\n");
							}

//							emb.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
//							emb.append(String.format("\tprivate %s %s;", ded.getReferencia().getNomeJavaClasse(), ded.getNomePropriedade())).append("\n");
//							emb.append("").append("\n");(mappedBy = \"%s\", cascade = CascadeType.ALL)

							embImportacaoEntidade.add(String.format("import %s.%s;", nomePacoteBase, ded.getReferencia().getNomeJavaClasseCompleto()));
						} else {
							emb.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
							emb.append(String.format("\tprivate %s %s;", ded.getTipoPropriedade(), ded.getNomeJavaPropriedade())).append("\n");
						}
					}
					emb.append("}").append("\n");
					
					StringBuilder embC = new StringBuilder();
					embC.append(String.format("package %s;", nomePacote)).append("\n").append("\n");
					embC.append(embImportacaoEntidade.stream().sorted().map(Object::toString).collect(Collectors.joining("\n"))).append("\n").append("\n");
					embC.append(emb).append("\n");
					
					salvarArquivoJava(new File(this.gerarSistema.getLocalSaida()
							+ (this.gerarSistema.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
							+ GerarSistema.DIRETORIO_FONTE_JAVA), nomePacote, nomeClasse + "Id", embC.toString());

					id.append("").append("\n");
					id.append("\t@EmbeddedId").append("\n");
					id.append(String.format("\tprivate %sId id;", dt.getNomeJavaClasse())).append("\n");
				} else {
					// CHAVE SIMPLES
					DefinicaoEstruturaDados primeiraDed = dt.getChavePrimariaList().stream().findFirst().get();
					id.append("").append("\n");
					id.append("\t@Id").append("\n");
					if (primeiraDed.isChaveEstrangeira()) {
						if (primeiraDed.getReferencia().getEsquema().equals(dt.getEsquema()) && primeiraDed.getReferencia().getTabela().equals(dt.getTabela())) {
							id.append("\t@GeneratedValue(strategy = GenerationType.SEQUENCE)").append("\n");
							id.append(String.format("\t@Column(name = \"%s\")", primeiraDed.getColuna())).append("\n");
							id.append(String.format("\tprivate %s %s;", "Long"/*primeiraDed.getTipoPropriedade()*/, primeiraDed.getNomeJavaPropriedade())).append("\n");
						} else {
							if (dt.getTabela().equals("diaria_realizada")) {
								System.out.println("aqui");
							}
							id.append(String.format("\t@Column(name = \"%s\")", primeiraDed.getColuna())).append("\n");
							DefinicaoTabela tabRef = null;
							String esquemaTmp = primeiraDed.getReferencia().getEsquema();
							String tabelaTmp = primeiraDed.getReferencia().getTabela();
							String tipoId = null;
							
							while (true) {
								tabRef = getDefnicaoTabela(informacaoConexaoMapa.getValue(), esquemaTmp, tabelaTmp);
								tipoId = tabRef.getChavePrimariaList().stream().findFirst().get().getTipoPropriedade();
								if (tipoId.contains(".")) {
									esquemaTmp = tabRef.getChavePrimariaList().stream().findFirst().get().getReferencia().getEsquema();
									tabelaTmp = tabRef.getChavePrimariaList().stream().findFirst().get().getReferencia().getTabela();
								} else {
									break;
								}
							}
							id.append(String.format("\tprivate %s %s;", tipoId, "_id")).append("\n");
							id.append("").append("\n");
							id.append(String.format("\t@OneToOne", primeiraDed.getReferencia().getNomeJavaPropriedade())).append("\n");
							id.append(String.format("\t@MapsId", primeiraDed.getReferencia().getNomeJavaPropriedade())).append("\n");
							id.append(String.format("\t@JoinColumn(name = \"%s\")", primeiraDed.getColuna())).append("\n");
							id.append(String.format("\tprivate %s %s;", primeiraDed.getReferencia().getNomeJavaClasse(), primeiraDed.getReferencia().getNomeJavaPropriedade())).append("\n");
							importacaoEntidade.add(String.format("import %s.%s;", nomePacoteBase, primeiraDed.getReferencia().getNomeJavaClasseCompleto()));
						}
					} else {
						id.append("\t@GeneratedValue(strategy = GenerationType.SEQUENCE)").append("\n");
						id.append(String.format("\t@Column(name = \"%s\")", primeiraDed.getColuna())).append("\n");
						id.append(String.format("\tprivate %s %s;", primeiraDed.getTipoPropriedade(), primeiraDed.getNomeJavaPropriedade())).append("\n");
					}
				}

				// demais campos
				StringBuilder demais = new StringBuilder();
				
				// chaves externas
				Set<DefinicaoTabela> tabRefSet = new HashSet<>();
				
				for (DefinicaoEstruturaDados ded : dt.getDemaisCamposList()) {
					demais.append("").append("\n");
					// se for uma foreign key
					if (ded.isChaveEstrangeira()) {
						// captar informação da tabela referida
						DefinicaoTabela tabRef = this.getDefnicaoTabela(informacaoConexaoMapa.getValue(), ded.getReferencia().getEsquema(), ded.getReferencia().getTabela());
						if (!tabRefSet.add(tabRef) && tabRef.isChavePrimariaComposta()) {
							// ignorar caso o campo já foi contabilizado 
							continue;
						}
						if (tabRef.isChavePrimariaComposta()) {
							// se a tabela referida tiver uma chave composta então é para usar o embedable dela 
							demais.append(String.format("\t@ManyToOne")).append("\n");
							demais.append(String.format("\tprivate %sId %s;", ded.getReferencia().getNomeJavaClasse(), ded.getNomeJavaPropriedade())).append("\n");
						} else {
							// se não, usar ela de forma simples mesmo. 
							demais.append(String.format("\t@ManyToOne")).append("\n");
							demais.append(String.format("\t@JoinColumn(name = \"%s\")", ded.getColuna())).append("\n");
							demais.append(String.format("\tprivate %s %s;", ded.getReferencia().getNomeJavaClasse(), ded.getNomeJavaPropriedade())).append("\n");
						}
						importacaoEntidade.add(String.format("import %s.%s;", nomePacoteBase, ded.getReferencia().getNomeJavaClasseCompleto()));
					} else {
						// se for um campo da própria tabela
						demais.append(String.format("\t@Column(name = \"%s\")", ded.getColuna())).append("\n");
						demais.append(String.format("\tprivate %s %s;", ded.getTipoPropriedade(), ded.getNomeJavaPropriedade())).append("\n");
					}
				}
				// acessorios
				StringBuffer java = new StringBuffer();
				java.append("package ").append(nomePacote).append(";").append("\n").append("\n");
				java.append(importacaoEntidade.stream().sorted().map(Object::toString).collect(Collectors.joining("\n"))).append("\n").append("\n");
				java.append(declaracaoClasse).append("\n");
				java.append("\tprivate static final long serialVersionUID = 1L;").append("\n");
				java.append(id).append("\n");
				java.append(demais).append("\n").append("\n");
				java.append("}").append("\n").append("\n");

				salvarArquivoJava(new File(this.gerarSistema.getLocalSaida()
						+ (this.gerarSistema.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
						+ GerarSistema.DIRETORIO_FONTE_JAVA), nomePacote, nomeClasse, java.toString());

				System.out.println(java);
				// arquivoAtualizado.add(dt.salvar(this.localSaida, this.pacotePadrao));
			}
			// excluir os arquivos q não foram atualizados
		}
	}

	private DefinicaoTabela getDefnicaoTabela(List<DefinicaoTabela> dtList, String esquema, String tabela) {
		return dtList.stream().filter(v1 -> {
			return v1.getEsquema().equals(esquema) && v1.getTabela().equals(tabela);
		}).findFirst().get();
	}

	private String nomeEntidadeDuplicada(DefinicaoTabela dt) {
		boolean existe = this.gerarSistema.getInformacaoConexaoMap().values().stream().filter(v1 -> {
			return v1.stream().filter(v2 -> {
				return !v2.getEsquema().equals(dt.getEsquema()) && v2.getTabela().equals(dt.getTabela());
			}).count() > 0;
		}).count() > 0;
		
		if (existe) {
			System.out.println("Entidade duplicada " + dt);
		}

		return existe ? String.format("(name = \"%s\")", String.format("%s%s", dt.getNomeJavaPacote(), dt.getNomeJavaClasse())) : "";
	}

}
