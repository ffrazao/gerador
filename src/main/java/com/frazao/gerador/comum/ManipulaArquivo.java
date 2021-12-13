package com.frazao.gerador.comum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class ManipulaArquivo {

	protected static final String DIRETORIO_FONTE_JAVA = "src" + File.separator + "main" + File.separator + "java"
			+ File.separator;

	protected static final String DIRETORIO_FONTE_RESOUCES = "src" + File.separator + "main" + File.separator
			+ "resources" + File.separator;

	protected String localSaida;

	protected String pacotePadrao;

	protected void copiaDiretorio(File diretorioOrigem, File diretorioDestino) throws Exception {
		if (!diretorioDestino.exists()) {
			diretorioDestino.mkdirs();
		}
		// copiar todos os diretórios
		for (String f : diretorioOrigem.list()) {
			copiarDiretorioCompativel(new File(diretorioOrigem, f), new File(diretorioDestino, f));
		}
	}

	private void copiarDiretorioCompativel(File source, File destination) throws Exception {
		if (source.isDirectory()) {
			copiaDiretorio(source, destination);
		} else {
			copiarArquivo(source, destination);
		}
	}

	protected void copiarArquivo(File sourceFile, File destinationFile) throws Exception {
		try (InputStream in = new FileInputStream(sourceFile);
				OutputStream out = new FileOutputStream(destinationFile)) {
			byte[] buf = new byte[1024];
			int length;
			while ((length = in.read(buf)) > 0) {
				out.write(buf, 0, length);
			}
		}

		isArquivoJava(destinationFile);
	}

	public abstract void executar() throws Exception;

	protected Optional<String> getExtensaoArquivo(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	private void isArquivoJava(File destinationFile) throws Exception {
		getExtensaoArquivo(destinationFile.getName()).ifPresent(f -> {
			if (f.endsWith("java")) {
				try {
					modificarPacote(destinationFile);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		});
	}

	private void modificarPacote(File destinationFile) throws Exception {
		// /saida/src/main/java/br/gov/df/seagri/migracao/entidade/Entidade.java
		String pacote = pegaNomePacote(destinationFile);
		Charset charset = StandardCharsets.UTF_8;

		// abrir o arquivo
		Path path = Paths.get(destinationFile.getPath());
		
		String content = new String(Files.readAllBytes(path), charset);
		
		content = content.replaceAll("package(.?)*;", String.format("package %s;", pacote));

		// atualizar o arquivo
		Files.write(path, content.getBytes(charset));
	}

	private String pegaNomePacote(File destinationFile) {
		String result = destinationFile.getPath()
				.substring(destinationFile.getPath().indexOf(DIRETORIO_FONTE_JAVA) + DIRETORIO_FONTE_JAVA.length(),
						destinationFile.getPath().lastIndexOf(destinationFile.getName()))
				.replaceAll(File.separator, ".");
		result = result.endsWith(".") ? result.substring(0, result.length() - 1) : result;

		return result;
	}

	protected void salvarArquivoJava(File localBase, String nomePacote, String nomeClasse, String conteudoArquivoJava)
			throws Exception {
		Charset charset = StandardCharsets.UTF_8;

		// abrir o arquivo
		Path path = Paths.get(localBase.getPath() + (localBase.getPath().endsWith(File.separator) ? "" : File.separator)
				+ nomePacote.replaceAll("\\.", File.separator));
		if (!path.toFile().exists()) {
			path.toFile().mkdirs();
		}

		File java = new File(path.toFile(), nomeClasse + ".java");

		// atualizar o arquivo
		Files.write(java.toPath(), conteudoArquivoJava.getBytes(charset));
	}

}
