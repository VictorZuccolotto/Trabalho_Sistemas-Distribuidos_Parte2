package br.ufu.facom.gbc074.projeto.teste;

import com.google.gson.JsonObject;

public class GsonTeste {
	public static void main(String[] args) {
	    // Exemplo de valores
	    String sigla = "ABC";
	    String nome = "Curso ABC";
	    int vagas = 20;
	
	    // Criar um objeto JsonObject
	    JsonObject jsonObject = new JsonObject();
	    jsonObject.addProperty("sigla", sigla);
	    jsonObject.addProperty("nome", nome);
	    jsonObject.addProperty("vagas", vagas);
	
	    // Transformar o JsonObject em uma string JSON
	    String jsonString = jsonObject.toString();
	
	    // Imprimir a string JSON
	    System.out.println(jsonString);
	}
}
