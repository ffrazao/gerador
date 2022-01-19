package com.frazao.gerador.entidade;

public abstract class Definicao {

	public static String converterCase(String nome, boolean primeiraTambem) {
		String[] words = nome.split("[\\W_]+");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (i == 0 && !primeiraTambem) {
				word = word.isEmpty() ? word : word.toLowerCase();
			} else {
				word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
			}
			result.append(word);
		}
		return result.toString();
	}
	
	public abstract String getNomeJavaPacote();

	public abstract String getNomeJavaClasse();

	public abstract String getNomeJavaObjeto();

	public abstract String getNomeJavaClasseCompleto();	
	
}
