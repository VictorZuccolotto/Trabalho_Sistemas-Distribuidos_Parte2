package br.ufu.facom.gbc074.projeto.bd.model;


public class DisciplinaModel {

	private String nome;
	
	private int vagas;
	
	public DisciplinaModel(String nome, int vagas) {
		this.nome = nome;
		this.vagas = vagas;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getVagas() {
		return vagas;
	}
	public void setVagas(int qntAlunos) {
		this.vagas = qntAlunos;
	}
}
