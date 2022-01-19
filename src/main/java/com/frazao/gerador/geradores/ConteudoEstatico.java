package com.frazao.gerador.geradores;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.frazao.gerador.GerarSistema;
import com.frazao.gerador.util.ManipulaArquivo;

public class ConteudoEstatico extends ManipulaArquivo {

	public ConteudoEstatico(GerarSistema gerarSistema) {
		super(gerarSistema);
	}

	@Override
	public void executar() throws Exception {
		String origem = null;
		String destino = null;

		URL url = null;

		// copiar dados do projeto
		origem = "projeto";
		destino = String.format("%s", this.gerarSistema.getLocalSaida());
		url = this.getClass().getClassLoader().getResource(origem);
		copiaDiretorio(new File(url.toURI()), new File(destino));

		// copiar conteúdo estático
		origem = "estatico";
		destino = String.format("%s%s%s.%s.%s.%s",
				this.gerarSistema.getLocalSaida().endsWith(File.separator) ? "" : File.separator,
				GerarSistema.DIRETORIO_FONTE_JAVA, this.gerarSistema.getPacotePadrao(),
				this.gerarSistema.getNomeSistema(), GerarSistema.NOME_PACOTE_FUNCIONALIDADE_BANCO_DADOS,
				GerarSistema.NOME_PACOTE_COMUM).replaceAll("\\.", File.separator);
		destino = this.gerarSistema.getLocalSaida() + destino;
		url = this.getClass().getClassLoader().getResource(origem);
		copiaDiretorio(new File(url.toURI()), new File(destino));

		// criar arquivo de inicialização
		StringBuilder arq = new StringBuilder();
		arq.append("package %s.%s;").append("\n");
		arq.append("").append("\n");
		arq.append("import java.util.Arrays;").append("\n");
		arq.append("").append("\n");
		arq.append("import org.springframework.boot.CommandLineRunner;").append("\n");
		arq.append("import org.springframework.boot.SpringApplication;").append("\n");
		arq.append("import org.springframework.boot.autoconfigure.SpringBootApplication;").append("\n");
		arq.append("import org.springframework.context.ApplicationContext;").append("\n");
		arq.append("import org.springframework.context.annotation.Bean;").append("\n");
		arq.append("").append("\n");
		arq.append("@SpringBootApplication").append("\n");
		arq.append("public class Application {").append("\n");
		arq.append("").append("\n");
		arq.append("\tpublic static void main(String[] args) {").append("\n");
		arq.append("\t\tSpringApplication app = new SpringApplication(Application.class);").append("\n");
		arq.append("\t\tapp.setLazyInitialization(true);").append("\n");
		arq.append("\t\tapp.run(args);").append("\n");		
		arq.append("\t}").append("\n");
		arq.append("").append("\n");
		arq.append("\t@Bean").append("\n");
		arq.append("\tpublic CommandLineRunner commandLineRunner(ApplicationContext ctx) {").append("\n");
		arq.append("\t\treturn args -> {").append("\n");
		arq.append("").append("\n");
		arq.append("\t\t\tSystem.out.println(\"Let\'s inspect the beans provided by Spring Boot:\");").append("\n");
		arq.append("").append("\n");
		arq.append("\t\t\tString[] beanNames = ctx.getBeanDefinitionNames();").append("\n");
		arq.append("\t\t\tArrays.sort(beanNames);").append("\n");
		arq.append("\t\t\tfor (String beanName : beanNames) {").append("\n");
		arq.append("\t\t\t\tSystem.out.println(beanName);").append("\n");
		arq.append("\t\t\t}").append("\n");
		arq.append("").append("\n");
		arq.append("\t\t};").append("\n");
		arq.append("\t}").append("\n");
		arq.append("").append("\n");
		arq.append("}").append("\n");

		arq = new StringBuilder(
				String.format(arq.toString(), this.gerarSistema.getPacotePadrao(), this.gerarSistema.getNomeSistema()));

		String arquivoPrincipal = this.gerarSistema.getLocalSaida() + String.format("%s%s%s.%s",
				this.gerarSistema.getLocalSaida().endsWith(File.separator) ? "" : File.separator,
				GerarSistema.DIRETORIO_FONTE_JAVA, this.gerarSistema.getPacotePadrao(),
				this.gerarSistema.getNomeSistema()).replaceAll("\\.", File.separator).concat(File.separator).concat("Application.java");

		Files.write(
				new File(arquivoPrincipal).toPath(),
				arq.toString().getBytes(StandardCharsets.UTF_8));
	}

}
