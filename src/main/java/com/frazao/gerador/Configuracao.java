package com.frazao.gerador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.frazao.gerador.impl.GerarSistemaImpl;

@Configuration
public class Configuracao {
	
	@Bean
	public GerarSistema geradorJPABean() {
		return new GerarSistemaImpl();
	}

}
