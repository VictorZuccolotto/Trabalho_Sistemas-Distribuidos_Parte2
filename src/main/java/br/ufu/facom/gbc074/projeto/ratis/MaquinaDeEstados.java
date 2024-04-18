package br.ufu.facom.gbc074.projeto.ratis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ratis.proto.*;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import br.ufu.facom.gbc074.projeto.bd.Banco;
import br.ufu.facom.gbc074.projeto.bd.model.DisciplinaModel;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

//import static org.iq80.leveldb.impl.Iq80DBFactory.*;



public class MaquinaDeEstados extends BaseStateMachine {
  private final Map<String, String> key2values = new ConcurrentHashMap<>();
  private DB db;

  public MaquinaDeEstados(String bd) {
	  File databaseDir = new File("src\\main\\resources\\leveldb\\"+bd);

		// Configurações para abrir o banco de dados
		Options options = new Options();
		options.createIfMissing(true);

		// Cria o banco de dados
		DBFactory factory = new Iq80DBFactory();
		try {
			this.db = factory.open(databaseDir, options);
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
  @Override
  public CompletableFuture<Message> query(Message request) {
    final String[] opKey = request.getContent().toString(Charset.defaultCharset()).split(":");
    final String result = opKey[0] + ":" + key2values.get(opKey[1]);
//    LOG.debug("{}: {} = {}", opKey[0], opKey[1], result);
    System.out.println("É aqui??" + opKey[0]);
    return CompletableFuture.completedFuture(Message.valueOf(result));
  }

  @Override
  public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
    final RaftProtos.LogEntryProto entry = trx.getLogEntry();
    final String[] opKeyValue = entry.getStateMachineLogEntry().getLogData().toString(Charset.defaultCharset())
        .split(":");
    
    
    final String op = opKeyValue[0];
    String result = op + ":";

    String key = opKeyValue.length < 2 ? "" : opKeyValue[1];
    String value = opKeyValue.length < 3 ? "" : opKeyValue[2];
    switch (op) {
      case "add":
    	  System.out.println("Esta passando pelo add");
    	this.db.put("testeChave".getBytes(), "testeValorVapovapo".getBytes());
    	String[] payload = {"bcc01", "Dedin"};
    	handleAlunoPayload("create",payload);
        result += key2values.put(key, value);
        break;
      case "del":
        result += key2values.remove(key);
        break;
      case "clear":
        key2values.clear();
        result += ":ok";
        break;
      default:
        result += "invalid-op";
    }
    final CompletableFuture<Message> f = CompletableFuture.completedFuture(Message.valueOf(result));

    final RaftProtos.RaftPeerRole role = trx.getServerRole();
//    LOG.info("{}:{} {} {}={}", role, getId(), op, key, value);

    return f;
  }
  
	public void switchBase(String[] message) {
		String base = message[0];
		String operacao = message[1];
		String[] payload = Arrays.copyOfRange(message, 2, message.length);
        switch(base) {
        case "aluno":
            handleAlunoPayload(operacao,payload);
            break;
        case "professor":
        	handleProfessorPayload(operacao, payload);
        	break;
        case "disciplina":
//        	handleDisciplinaPayload(operacao, payload);//TODO
        	break;
        default:
            break;
        }
	}
	
	private void handleProfessorPayload(String operacao,String[] payload) {
	    String siape = payload[0];
	    String nome = payload[1];
	    String sigla = payload.length >= 3? payload[2]:null;
    	switch(operacao) {
    	case "create":
        	this.db.put(("professores:"+siape).getBytes(), nome.getBytes());
        	String listaJson = new Gson().toJson(new ArrayList<String>());
        	this.db.put(("professorDisciplinas:"+siape).getBytes(), listaJson.getBytes());
    		//
//    		Banco.professores.put(siape, nome);
//    		Banco.professorDisciplinas.put(siape, new ArrayList<String>());
    		break;
    	case "update":
    		this.db.put(("professores:"+siape).getBytes(), nome.getBytes());
    		//
//    		Banco.professores.put(siape, nome);
    		break;
    	case "delete":
        	String professorDisciplinas = new String(this.db.get(("professorDisciplinas:"+siape).getBytes()));
        	List<String> professorDisciplinasList = new Gson().fromJson(professorDisciplinas, new TypeToken<List<String>>(){}.getType());
        	for (String disciplinaID : professorDisciplinasList) {
        		this.db.delete(("disciplinaProfessor:"+disciplinaID).getBytes());
			}
        	this.db.delete(("professorDisciplinas:"+siape).getBytes());
        	this.db.delete(("professores:"+siape).getBytes());
        	//
//			for (String disciplinaID : Banco.professorDisciplinas.get(siape)) {
//				Banco.disciplinaProfessor.remove(disciplinaID);
//			}
//			Banco.professorDisciplinas.remove(siape);
//			Banco.professores.remove(siape);
    		break;
        case "add":
        	//Add professor na disciplina
        	this.db.put(("disciplinaProfessor:"+sigla).getBytes(), siape.getBytes());
        	//Add disciplina para professor
        	professorDisciplinas = new String(this.db.get(("professorDisciplinas:"+siape).getBytes()));
        	professorDisciplinasList = new Gson().fromJson(professorDisciplinas, new TypeToken<List<String>>(){}.getType());
        	professorDisciplinasList.add(sigla);
        	this.db.put(("professorDisciplinas:"+siape).getBytes(), new Gson().toJson(professorDisciplinasList).getBytes());
        	//
//			Banco.disciplinaProfessor.put(sigla,siape);
//			Banco.professorDisciplinas.get(siape).add(sigla);
        	break;
        case "remove":
        	//remove professor da disciplina
        	this.db.delete(("disciplinaProfessor:"+sigla).getBytes());
        	//remove disciplina do professor
        	professorDisciplinas = new String(this.db.get(("professorDisciplinas:"+siape).getBytes()));
        	professorDisciplinasList = new Gson().fromJson(professorDisciplinas, new TypeToken<List<String>>(){}.getType());
        	professorDisciplinasList.remove(sigla);
        	this.db.put(("professorDisciplinas:"+siape).getBytes(), new Gson().toJson(professorDisciplinasList).getBytes());
        	//
//			Banco.disciplinaProfessor.remove(sigla);
//			Banco.professorDisciplinas.get(siape).remove(sigla);
        	break;
    	default:
    		System.out.println("Erro");
    		break;
    	}
	
	}
    
	private void handleAlunoPayload(String operacao,String[] payload) {
	    String matricula = payload[0];
	    String nome = payload[1];
	    String sigla = payload.length >= 3? payload[2]:null;
        switch(operacao) {
        case "create":
        	this.db.put(("alunos:"+matricula).getBytes(), nome.getBytes());
        	String listaJson = new Gson().toJson(new ArrayList<String>());
        	this.db.put(("alunoDisciplinas:"+matricula).getBytes(), listaJson.getBytes());
//            Banco.alunos.put(matricula, nome);
//            Banco.alunoDisciplinas.put(matricula, new ArrayList<String>());
            break;
        case "update":
        	this.db.put(("alunos:"+matricula).getBytes(), nome.getBytes());
//        	Banco.alunos.put(matricula, nome);
        	break;
        case "delete":
        	String alunoDisciplinas = new String(this.db.get(("alunoDisciplinas:"+matricula).getBytes()));
        	List<String> alunoDisciplinasList = new Gson().fromJson(alunoDisciplinas, new TypeToken<List<String>>(){}.getType());
        	for (String disciplinaID : alunoDisciplinasList) {
                String disciplinaAlunos = new String(this.db.get(("disciplinaAlunos:"+disciplinaID).getBytes()));
                List<String> disciplinaAlunosList = new Gson().fromJson(disciplinaAlunos, new TypeToken<List<String>>(){}.getType());
                disciplinaAlunosList.remove(matricula);
                this.db.put(("disciplinaAlunos:"+disciplinaID).getBytes(), new Gson().toJson(disciplinaAlunosList).getBytes());
			}
        	this.db.delete(("alunoDisciplinas:"+matricula).getBytes());
        	this.db.delete(("alunos:"+matricula).getBytes());
        	//
//			for (String disciplinaID : Banco.alunoDisciplinas.get(matricula)) {
//				Banco.disciplinaAlunos.get(disciplinaID).remove(matricula);
//			}
//			Banco.alunoDisciplinas.remove(matricula);
//			Banco.alunos.remove(matricula);
        	break;
        case "add":
        	//Add aluno na disciplina
            String disciplinaAlunos = new String(this.db.get(("disciplinaAlunos:"+sigla).getBytes()));
            List<String> disciplinaAlunosList = new Gson().fromJson(disciplinaAlunos, new TypeToken<List<String>>(){}.getType());
            disciplinaAlunosList.add(matricula);
        	this.db.put(("disciplinaAlunos:"+sigla).getBytes(), new Gson().toJson(disciplinaAlunosList).getBytes());
        	//Add disciplina para aluno
        	alunoDisciplinas = new String(this.db.get(("alunoDisciplinas:"+matricula).getBytes()));
        	alunoDisciplinasList = new Gson().fromJson(alunoDisciplinas, new TypeToken<List<String>>(){}.getType());
        	alunoDisciplinasList.add(sigla);
        	this.db.put(("alunoDisciplinas:"+matricula).getBytes(), new Gson().toJson(alunoDisciplinasList).getBytes());
        	//
//    		Banco.disciplinaAlunos.get(sigla).add(matricula);
//    		Banco.alunoDisciplinas.get(matricula).add(sigla);
        	break;
        case "remove":
        	//remove aluno da disciplina
            disciplinaAlunos = new String(this.db.get(("disciplinaAlunos:"+sigla).getBytes()));
            disciplinaAlunosList = new Gson().fromJson(disciplinaAlunos, new TypeToken<List<String>>(){}.getType());
            disciplinaAlunosList.remove(matricula);
        	this.db.put(("disciplinaAlunos:"+sigla).getBytes(), new Gson().toJson(disciplinaAlunosList).getBytes());
        	//remove disciplina de aluno
        	alunoDisciplinas = new String(this.db.get(("alunoDisciplinas:"+matricula).getBytes()));
        	alunoDisciplinasList = new Gson().fromJson(alunoDisciplinas, new TypeToken<List<String>>(){}.getType());
        	alunoDisciplinasList.remove(sigla);
        	this.db.put(("alunoDisciplinas:"+matricula).getBytes(), new Gson().toJson(alunoDisciplinasList).getBytes());
        	//
//    		Banco.disciplinaAlunos.get(sigla).remove(matricula);
//    		Banco.alunoDisciplinas.get(matricula).remove(sigla);
        	break;
        default:
    		System.out.println("Erro");
            break;
        }
	}

//    private void handleDisciplinaPayload(String operacao,String[] payload) {
//	    String matricula = payload[0];
//	    String nome = payload[1];
//	    String vagas = payload.length >= 3? payload[2]:null;
////		String nome  = payload.has("nome") ? payload.get("nome").getAsString() : null;
////		String sigla = payload.has("sigla") ? payload.get("sigla").getAsString() : null;
////    	int vagas = payload.has("vagas") ? payload.get("vagas").getAsInt() : null;
//    	switch(operacao) {
//    	case "create":
//    		Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
//    		Banco.disciplinaAlunos.put(sigla, new ArrayList<String>());
//    		break;
//    	case "update":
//    		Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
//    		break;
//    	case "delete":
//			for (String alunosID : Banco.disciplinaAlunos.get(sigla)) {
//				Banco.alunoDisciplinas.get(alunosID).remove(sigla);
//			}
//			Banco.professorDisciplinas.get(Banco.disciplinaProfessor.get(sigla)).remove(sigla);
//			Banco.disciplinaProfessor.remove(sigla);
//			Banco.disciplinas.remove(sigla);
//    		break;
//    	default:
//    		System.out.println("Erro ao receber Mqtt");
//    		break;
//    	}
//    	
//    }

}

