package com.frazao.gerador.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.frazao.gerador.GerarSistema;
import com.frazao.gerador.comum.BancoDados;
import com.frazao.gerador.comum.ConteudoEstatico;
import com.frazao.gerador.comum.Definicao;
import com.frazao.gerador.comum.DefinicaoEstruturaDados;
import com.frazao.gerador.comum.DefinicaoTabela;
import com.frazao.gerador.comum.GeradorEntidade;
import com.frazao.gerador.comum.InformacaoConexao;
import com.frazao.gerador.comum.ManipulaArquivo;

import lombok.Data;
import lombok.Getter;

@Data
public class GerarSistemaImpl implements GerarSistema {

	@Getter
	private Map<InformacaoConexao, List<DefinicaoTabela>> informacaoConexaoMap = new HashMap<>();

	private String localSaida;

	private String nomeSistema;

	private String pacotePadrao;

	@Override
	public GerarSistema carregarDefinicaoTabelaList() throws Exception {
		for (Entry<InformacaoConexao, List<DefinicaoTabela>> informacaoConexao : this.informacaoConexaoMap.entrySet()) {
			BancoDados bd = new BancoDados(informacaoConexao.getKey());
			informacaoConexao.setValue(bd.getDefinicaoBancoDados());
		}
		return this;
	}

	@Override
	public GerarSistema construirApplication_Yml() throws Exception {
		StringBuilder applicationYml = new StringBuilder();

		String pacoteInicial = String.format("%s.%s", this.getPacotePadrao(), this.getNomeSistema());

		int cont = 0;
		for (Entry<InformacaoConexao, List<DefinicaoTabela>> informacaoConexaoEntry : this.informacaoConexaoMap
				.entrySet()) {
			cont++;
			InformacaoConexao informacaoConexao = informacaoConexaoEntry.getKey();

			String nomeBanco = informacaoConexao.getNomeBanco();
			String nomePacoteLocal = String.format("%s.%s.%s", pacoteInicial,
					GerarSistema.NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS, nomeBanco);
			String nomePacoteEntidade = String.format("%s.%s", pacoteInicial,
					String.format(NOME_PACOTE_ENTIDADE, nomeBanco));
			String nomePacoteRepositorio = String.format("%s.%s", pacoteInicial,
					String.format(NOME_PACOTE_DAO, nomeBanco));
			String nomeClasse = String.format("%sConfigDb", Definicao.converterCase(nomeBanco, true));
			String nomeMetodoDataSource = String.format("%sDataSource", Definicao.converterCase(nomeBanco, false));
			String nomeMetodoEntityManager = String.format("%sEntityManager",
					Definicao.converterCase(nomeBanco, false));

			applicationYml.append(nomeBanco).append(":").append("\n");
			applicationYml.append(" datasource:").append("\n");
			applicationYml.append("  lazy-initialization: true").append("\n");
			applicationYml.append("  lazyInitialization: true").append("\n");
			applicationYml.append("  initialization-mode: \"never\"").append("\n");
			applicationYml.append("  initializationMode: \"never\"").append("\n");
			applicationYml.append("  continueOnError: true").append("\n");
		    applicationYml.append("  initialize: false").append("\n");
		    applicationYml.append("  initialSize: 0").append("\n");
		    applicationYml.append("  timeBetweenEvictionRunsMillis: 5000").append("\n");
		    applicationYml.append("  minEvictableIdleTimeMillis: 5000").append("\n");
		    applicationYml.append("  minIdle: 0").append("\n");
			applicationYml.append("  driverClassName: \"").append(informacaoConexao.getDriver()).append("\"\n");
			applicationYml.append("  jdbcUrl: \"").append(informacaoConexao.getUrl()).append("\"\n");
			applicationYml.append("  username: \"").append(informacaoConexao.getUsername()).append("\"\n");
			applicationYml.append("  password: \"").append(informacaoConexao.getPassword()).append("\"\n");
			applicationYml.append("  jpa:").append("\n");
			switch (informacaoConexao.getPlataformaBanco()) {
			case POSTGRES:
				applicationYml.append("   dialect: \"org.hibernate.dialect.PostgreSQL10Dialect\"").append("\n");
				applicationYml.append("   databasePlatform: \"org.hibernate.dialect.PostgreSQL10Dialect\"").append("\n");
				break;
			case MYSQL:
				applicationYml.append("   dialect: \"org.hibernate.dialect.MySQL8Dialect\"").append("\n");
				applicationYml.append("   databasePlatform: \"org.hibernate.dialect.MySQL8Dialect\"").append("\n");
				break;
			}
			applicationYml.append("   show-sql: true").append("\n");
			applicationYml.append("   use_sql_comments: true").append("\n");
			applicationYml.append("   hibernate:").append("\n");
			applicationYml.append("    ddl-auto: \"none\"").append("\n");
			applicationYml.append("    useSqlComments: true").append("\n");
			applicationYml.append("    formatSql: true").append("\n");
			applicationYml.append("\n");

			StringBuilder configDb = new StringBuilder();
			configDb.append("package ").append(nomePacoteLocal).append(";\n");
			configDb.append("").append("\n");
			configDb.append("import javax.sql.DataSource;").append("\n");
			configDb.append("").append("\n");
			configDb.append("import org.springframework.beans.factory.annotation.Qualifier;").append("\n");
			configDb.append("import org.springframework.boot.context.properties.ConfigurationProperties;").append("\n");
			configDb.append("import org.springframework.boot.jdbc.DataSourceBuilder;").append("\n");
			configDb.append("import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;").append("\n");
			configDb.append("import org.springframework.context.annotation.Bean;").append("\n");
			configDb.append("import org.springframework.context.annotation.Configuration;").append("\n");
			configDb.append("import org.springframework.context.annotation.Primary;").append("\n");
			configDb.append("import org.springframework.data.jpa.repository.config.EnableJpaRepositories;")
					.append("\n");
			configDb.append("import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;").append("\n");
			configDb.append("").append("\n");
			configDb.append("@Configuration").append("\n");
			configDb.append("@EnableJpaRepositories(entityManagerFactoryRef = \"").append(nomeMetodoEntityManager)
					.append("\", basePackages = \"").append(nomePacoteRepositorio).append("\")\n");
			configDb.append("public class ").append(nomeClasse).append(" {").append("\n");
			configDb.append("").append("\n");
			configDb.append("\t@Bean").append("\n");
			if (cont == 1) {				
				configDb.append("\t@Primary").append("\n");
			}
			configDb.append("\t@ConfigurationProperties(prefix = \"").append(nomeBanco).append(".datasource\")")
					.append("\n");
			configDb.append("\tpublic DataSource ").append(nomeMetodoDataSource).append("() {").append("\n");
			configDb.append("\t\treturn DataSourceBuilder.create().build();").append("\n");
			configDb.append("\t}").append("\n");
			configDb.append("").append("\n");
			configDb.append("\t@Bean").append("\n");
			if (cont == 1) {				
				configDb.append("\t@Primary").append("\n");
			}
			configDb.append("\tpublic LocalContainerEntityManagerFactoryBean ").append(nomeMetodoEntityManager)
					.append("(EntityManagerFactoryBuilder builder,").append("\n");
			configDb.append("\t\t\t@Qualifier(\"").append(nomeMetodoDataSource).append("\") DataSource dataSource) {")
					.append("\n");
			configDb.append("\t\treturn builder.dataSource(dataSource).packages(\"").append(nomePacoteEntidade)
					.append("\").build();").append("\n");
			configDb.append("\t}").append("\n");
			configDb.append("").append("\n");
			configDb.append("}").append("\n");

			ManipulaArquivo.salvarArquivoJava(
					new File(
							this.getLocalSaida() + (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
									+ GerarSistema.DIRETORIO_FONTE_JAVA),
					nomePacoteLocal, nomeClasse, configDb.toString());
		}

		applicationYml.append("spring:").append("\n");
		applicationYml.append(" datasource:").append("\n");		
		applicationYml.append("  initialization-mode: \"never\"").append("\n");
		applicationYml.append("  initializationMode: \"never\"").append("\n");
		applicationYml.append(" main:").append("\n");
		applicationYml.append("  lazy-initialization: true").append("\n");
		applicationYml.append("  lazyInitialization: true").append("\n");
		applicationYml.append("  continueOnError: true").append("\n");
	    applicationYml.append("  initialize: false").append("\n");
	    applicationYml.append("  initialSize: 0").append("\n");
	    applicationYml.append("  timeBetweenEvictionRunsMillis: 5000").append("\n");
	    applicationYml.append("  minEvictableIdleTimeMillis: 5000").append("\n");
	    applicationYml.append("  minIdle: 0").append("\n");
		applicationYml.append("  allow-bean-definition-overriding: true").append("\n");
		applicationYml.append(" jpa:").append("\n");
		applicationYml.append("  generate-ddl: false").append("\n");
		applicationYml.append("  show-sql: true").append("\n");
		applicationYml.append("  hibernate:").append("\n");
		applicationYml.append("   ddl-auto: \"none\"").append("\n");
		applicationYml.append("   hbm2ddl:").append("\n");
		applicationYml.append("    auto: \"none\"").append("\n");
		applicationYml.append("   temp:").append("\n");
        applicationYml.append("    use_jdbc_metadata_defaults: false").append("\n");
		applicationYml.append("  properties:").append("\n");
		applicationYml.append("   hibernate:").append("\n");
		applicationYml.append("    useSqlComments: true").append("\n");
		applicationYml.append("    formatSql: true").append("\n");
		applicationYml.append("").append("\n");
		
		applicationYml.append("logging:").append("\n");
		applicationYml.append(" level:").append("\n");
		applicationYml.append("  root: \"ERROR\"").append("\n");
		applicationYml.append(String.format("  %s: \"DEBUG\"", this.pacotePadrao)).append("\n");
			   
		ManipulaArquivo.salvarArquivo(
				new File(this.getLocalSaida() + (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
						+ GerarSistema.DIRETORIO_FONTE_RESOUCES),
				"application.yml", applicationYml.toString());

		return this;
	}

	@Override
	public GerarSistema construirEntidade() throws Exception {
		ManipulaArquivo ma = new GeradorEntidade(this);
		ma.executar();
		return this;
	}

	@Override
	public GerarSistema construirNovoProjeto() throws Exception {
		ManipulaArquivo ma = new ConteudoEstatico(this);
		ma.executar();
		return this;
	}

	@Override
	public GerarSistema construirRepositorio() throws Exception {
		
		String pacoteInicial = String.format("%s.%s", this.getPacotePadrao(), this.getNomeSistema());

		construirBancoDadosConfig(pacoteInicial);

		for (Entry<InformacaoConexao, List<DefinicaoTabela>> informacaoConexaoEntry : this.informacaoConexaoMap.entrySet()) {
			InformacaoConexao informacaoConexao = informacaoConexaoEntry.getKey();

			for (DefinicaoTabela dt : informacaoConexaoEntry.getValue()) {
				
				Set<String> importacaoBasica = new TreeSet<>();
				String nomeBanco = informacaoConexao.getNomeBanco();
				String nomeEntidade = dt.getNomeJavaClasse();
				String nomeClasse = String.format("%sDAO", nomeEntidade);
				String nomePacoteEntidade = String.format("%s.%s", pacoteInicial, String.format(NOME_PACOTE_ENTIDADE, nomeBanco));
				String nomePacoteRepositorio = String.format("%s.%s.%s", pacoteInicial, String.format(NOME_PACOTE_DAO, nomeBanco), dt.getNomeJavaPacote());
				
				importacaoBasica.add("import org.springframework.data.jpa.repository.JpaRepository;");
				importacaoBasica.add(String.format("import %s.%s;", nomePacoteEntidade, dt.getNomeJavaClasseCompleto()));
				
				String tipoId = null;
				
				if (dt.isChavePrimariaComposta()) {
					tipoId = String.format("%sId", dt.getNomeJavaClasse());
					importacaoBasica.add(String.format("import %s.%sId;", nomePacoteEntidade, dt.getNomeJavaClasseCompleto()));
				} else {
					DefinicaoEstruturaDados ded = dt.getChavePrimariaList().stream().findFirst().get();
					if (ded.isChaveEstrangeira()) {
						importacaoBasica.add(String.format("import %s.%s;", nomePacoteEntidade, ded.getReferencia().getNomeJavaClasseCompleto()));
						tipoId = ded.getReferencia().getNomeJavaClasse();
					} else {
						tipoId = ded.getTipoPropriedade();
					}
				}

				// REPOSITORIO PRINCIPAL
				StringBuilder java = new StringBuilder();
				java.append("package ").append(nomePacoteRepositorio).append(";\n");
				java.append("").append("\n");
				java.append(importacaoBasica.stream().sorted().map(Object::toString).collect(Collectors.joining("\n"))).append("\n").append("\n");
				java.append("").append("\n");
				java.append("public interface ").append(nomeClasse).append(String.format(" extends JpaRepository<%s, %s>, %s {", nomeEntidade, tipoId, String.format("%sDAOFiltro", nomeEntidade))).append("\n");
				java.append("").append("\n");
				java.append("}").append("\n");

				ManipulaArquivo.salvarArquivoJava(
						new File(this.getLocalSaida()
								+ (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
								+ GerarSistema.DIRETORIO_FONTE_JAVA),
						nomePacoteRepositorio, nomeClasse, java.toString());
				
				// CONSULTA PERSONALIZADA
				nomeClasse = String.format("%sDAOFiltro", nomeEntidade);
				
				java = new StringBuilder();
				java.append("package ").append(nomePacoteRepositorio).append(";\n");
				java.append("").append("\n");
				java.append("import java.util.Collection;").append("\n");
				java.append("").append("\n");
//				import br.gov.df.seagri.migracao.banco_dados._comum.dao.DAOFiltro;
//				import br.gov.df.seagri.migracao.banco_dados._comum.dto.DTOFiltro;
				
				java.append(String.format("import %s.%s.DAOFiltro;", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_DAO, GerarSistema.NOME_PACOTE_COMUM))).append("\n");
				java.append(String.format("import %s.%s.DTOFiltro;", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_DTO, GerarSistema.NOME_PACOTE_COMUM))).append("\n");
				java.append("").append("\n");

				java.append(String.format("import %s.%s;", nomePacoteEntidade, dt.getNomeJavaClasseCompleto())).append("\n");
				java.append("").append("\n");
				java.append("public interface ").append(nomeClasse)
						.append(String.format(" extends DAOFiltro<%s, %s> {", nomeEntidade, "DTOFiltro"))
						.append("\n");
				java.append("").append("\n");
				java.append(String.format("\tCollection<%s> filtrar(DTOFiltro f);", nomeEntidade)).append("\n");
				java.append("").append("\n");
				java.append("}").append("\n");
				
				ManipulaArquivo.salvarArquivoJava(
						new File(this.getLocalSaida()
								+ (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
								+ GerarSistema.DIRETORIO_FONTE_JAVA),
						nomePacoteRepositorio, nomeClasse, java.toString());
				
				// IMPLEMENTAÇÃO DA CONSULTA PERSONALIZADA
				nomeClasse = String.format("%sDAOFiltroImpl", nomeEntidade);
				
				
				java = new StringBuilder();
				java.append("package ").append(nomePacoteRepositorio + ".impl").append(";\n");
				java.append("").append("\n");
				java.append("import java.util.Collection;").append("\n");
				java.append("").append("\n");
				java.append(String.format("import %s.%sDAOFiltro;", nomePacoteRepositorio, dt.getNomeJavaClasse())).append("\n");
				java.append(String.format("import %s.%s.DTOFiltro;", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_DTO, GerarSistema.NOME_PACOTE_COMUM))).append("\n");
				java.append("").append("\n");
				java.append(String.format("import %s.%s;", nomePacoteEntidade, dt.getNomeJavaClasseCompleto())).append("\n");
				java.append("").append("\n");
				java.append("public class ").append(nomeClasse).append(String.format(" implements %sDAOFiltro {", dt.getNomeJavaClasse())) .append("\n");
				java.append("").append("\n");
				java.append("\t@Override").append("\n");
				
				java.append(String.format("\tpublic Collection<%s> filtrar(DTOFiltro f) {return null;}", nomeEntidade)).append("\n");
				java.append("").append("\n");
				java.append("}").append("\n");
				
				ManipulaArquivo.salvarArquivoJava(
						new File(this.getLocalSaida()
								+ (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
								+ GerarSistema.DIRETORIO_FONTE_JAVA),
						nomePacoteRepositorio + ".impl", nomeClasse, java.toString());

			}

		}
		
		// ajustar a importação do arquivo DTOFiltro no arquiv DAOFiltro
		
		// /saida/src/main/java/br/gov/df/seagri/migracao/entidade/Entidade.java
		String classeDTOFiltro = String.format("%s.%s.DTOFiltro", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_DTO, GerarSistema.NOME_PACOTE_COMUM));
		String classeDAOFiltro = String.format("%s.%s.DAOFiltro", pacoteInicial, String.format(GerarSistema.NOME_PACOTE_DAO, GerarSistema.NOME_PACOTE_COMUM));
		File daoFiltroFile = new File(this.getLocalSaida()
				+ (this.getLocalSaida().endsWith(File.separator) ? "" : File.separator)
				+ GerarSistema.DIRETORIO_FONTE_JAVA + File.separator +
				classeDAOFiltro.replaceAll("\\.", File.separator) + ".java");
		Charset charset = StandardCharsets.UTF_8;
		Path path = Paths.get(daoFiltroFile.getPath());
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("IMPORT_AQUI", String.format("import %s;", classeDTOFiltro));
		Files.write(path, content.getBytes(charset));

		return this;
	}

	private void construirBancoDadosConfig(String pacoteInicial) throws IOException {

		// construir o arquivo de configuração do banco de dados

		// definição dos arquivos
		String nomePacoteBancoDados = String.format("%s.%s", pacoteInicial, NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS);
		String nomeClasseBancoDados = "BancoDadosConfig";
		File diretorioBancoDados = new File(String.format("%s%s%s%s%s", this.localSaida,
				this.getLocalSaida().endsWith(File.separator) ? "" : File.separator, DIRETORIO_FONTE_JAVA,
				File.separator, nomePacoteBancoDados.replaceAll("\\.", File.separator)));
		File arquivoBancoDados = new File(diretorioBancoDados, String.format("%s.java", nomeClasseBancoDados));

		if (!arquivoBancoDados.exists()) {
			if (!diretorioBancoDados.exists()) {
				diretorioBancoDados.mkdirs();
			}
			StringBuilder conteudo = new StringBuilder();
			conteudo.append(String.format("package %s;", nomePacoteBancoDados)).append("\n");
			conteudo.append("").append("\n");
			conteudo.append("import org.springframework.context.annotation.Bean;").append("\n");
			conteudo.append("import org.springframework.context.annotation.Configuration;").append("\n");
			conteudo.append("import org.springframework.transaction.PlatformTransactionManager;").append("\n");
			conteudo.append("import org.springframework.transaction.annotation.EnableTransactionManagement;")
					.append("\n");
			conteudo.append("import org.springframework.transaction.jta.JtaTransactionManager;").append("\n");
			conteudo.append("").append("\n");
			conteudo.append("@EnableTransactionManagement").append("\n");
			conteudo.append("@Configuration").append("\n");
			conteudo.append(String.format("public class %s {", nomeClasseBancoDados)).append("\n");
			conteudo.append("").append("\n");
			conteudo.append("\t@Bean").append("\n");
			conteudo.append("\tpublic PlatformTransactionManager platformTransactionManager() {").append("\n");
			conteudo.append("\t\treturn new JtaTransactionManager();").append("\n");
			conteudo.append("\t}").append("\n");
			conteudo.append("").append("\n");
			conteudo.append("}").append("\n");
			conteudo.append("").append("\n");

			// salvar o conteúdo do arquivo
			Files.write(Paths.get(arquivoBancoDados.getPath()), conteudo.toString().getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public void addInformacaoConexao(InformacaoConexao informacaoConexao) {
		this.informacaoConexaoMap.put(informacaoConexao, null);
	}

	@Override
	public void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password) {
		this.addInformacaoConexao(new InformacaoConexao(nomeBanco, driver, url, username, password));
	}

	@Override
	public void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList) {
		this.addInformacaoConexao(
				new InformacaoConexao(nomeBanco, driver, url, username, password, adicionaFiltroList));
	}

	@Override
	public void addInformacaoConexao(String nomeBanco, String driver, String url, String username, String password,
			List<String> adicionaFiltroList, List<String> excluiFiltroList) {
		this.addInformacaoConexao(new InformacaoConexao(nomeBanco, driver, url, username, password, adicionaFiltroList,
				excluiFiltroList));
	}

}
