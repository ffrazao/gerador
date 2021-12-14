package com.frazao.gerador.comum;

import java.io.File;
import java.net.URL;

import com.frazao.gerador.GerarSistema;

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
		destino = String.format("%s", this.localSaida);
		url = this.getClass().getClassLoader().getResource(origem);
		copiaDiretorio(new File(url.toURI()), new File(destino));

		// copiar conteúdo estático
		origem = "estatico";
		destino = String.format("%s%s%s%s.%s.%s", this.localSaida,
				this.localSaida.endsWith(File.separator) ? "" : File.separator, DIRETORIO_FONTE_JAVA,
				this.pacotePadrao.replaceAll("\\.", File.separator), this.gerarSistema.getNomeSistema(), this.gerarSistema.getPacoteOrganizador());
		url = this.getClass().getClassLoader().getResource(origem);
		copiaDiretorio(new File(url.toURI()), new File(destino));
	}

}
