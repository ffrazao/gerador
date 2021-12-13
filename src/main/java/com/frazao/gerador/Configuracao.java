package com.frazao.gerador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.frazao.gerador.impl.GerarManipuladorBancoDadosImpl;

@Configuration
public class Configuracao {
	
	@Bean
	public GerarManipuladorBancoDados geradorJPABean() {
		return new GerarManipuladorBancoDadosImpl();
	}

}
