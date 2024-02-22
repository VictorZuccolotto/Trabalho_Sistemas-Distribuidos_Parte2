package br.ufu.facom.gbc074.projeto.bd;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufu.facom.gbc074.projeto.bd.model.DisciplinaModel;

public class Banco {
	
	public static HashMap<String, String> alunos = new HashMap<String, String>();
	
	public static HashMap<String, String> professores = new HashMap<String, String>();
	
	public static HashMap<String, DisciplinaModel> disciplinas = new HashMap<String, DisciplinaModel>();
	
	public static HashMap<String, ArrayList<String>> disciplinaAlunos = new HashMap<String, ArrayList<String>>();
	
	public static HashMap<String, ArrayList<String>> alunoDisciplinas = new HashMap<String, ArrayList<String>>();

	public static HashMap<String, String> disciplinaProfessor = new HashMap<String, String>();

	public static HashMap<String, ArrayList<String>> professorDisciplinas = new HashMap<String, ArrayList<String>>();
	
}
