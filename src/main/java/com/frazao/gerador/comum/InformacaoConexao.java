package com.frazao.gerador.comum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacaoConexao {
	private String driver;
	private String password;
	private String url;
	private String username;
	
	public PlataformaBanco getPlataformaBanco() {
		return PlataformaBanco.encontrarPlataforma(this.driver);
	}
}
