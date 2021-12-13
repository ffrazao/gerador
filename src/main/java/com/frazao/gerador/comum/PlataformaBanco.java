package com.frazao.gerador.comum;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlataformaBanco {

	MYSQL("mysql"), POSTGRES("postgres");

	private String nomePlataforma;

	PlataformaBanco(String nomePlataforma) {
		this.nomePlataforma = nomePlataforma;
	}

	public static PlataformaBanco encontrarPlataforma(String driver) {
		if (driver == null) {
			return null;
		}
		AtomicReference<PlataformaBanco> result = new AtomicReference<>();
		Optional.ofNullable(Stream.of(PlataformaBanco.values())
				.filter(e -> driver.toLowerCase().contains(e.nomePlataforma.toLowerCase()))
				.collect(Collectors.toList())).ifPresent(r -> result.set(r.get(0)));
		return result.get();
	}
}
