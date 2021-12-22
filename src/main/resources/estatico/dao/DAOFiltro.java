package com.frazao.adubacaodescomplicada.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;

IMPORT_AQUI

public interface DAOFiltro<T, F extends DTOFiltro> {

	default String adWhere(final StringBuilder where) {
		return where.length() == 0 ? "WHERE  " : "AND    ";
	}

	default String adOr(final StringBuilder or) {
		return or.length() == 0 ? "              " : "        OR    ";
	}

	Collection<T> filtrar(F f);

	default String in(final Collection<?> itens) {
		return itens != null
				? String.format("(%s)", itens.stream().map(i -> " ?").collect(Collectors.joining(",")).trim())
				: null;
	}

	default String like(final String arg) {
		return arg != null ? String.format("%%%s%%", arg.replaceAll(" ", "%")) : null;
	}

	default Integer[] idSim(String[] id) {
		List<Integer> result = new ArrayList<>();
		if (!ObjectUtils.isEmpty(id)) {
			result = Stream.of(id).filter(v -> v.matches("^(\\d)+$")).map(v -> NumberUtils.toInt(v))
					.collect(Collectors.toList());
		}
		return (Integer[]) result.toArray(new Integer[result.size()]);
	}

	default Integer[] idNao(String[] id) {
		List<Integer> result = new ArrayList<>();
		if (!ObjectUtils.isEmpty(id)) {
			result = Stream.of(id).filter(v -> v.matches("^(\\!)(\\d)+$"))
					.map(v -> NumberUtils.toInt(v.replaceAll("\\!", ""))).collect(Collectors.toList());
		}
		return (Integer[]) result.toArray(new Integer[result.size()]);
	}

}
