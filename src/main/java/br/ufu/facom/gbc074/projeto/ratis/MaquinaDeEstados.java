package br.ufu.facom.gbc074.projeto.ratis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import com.google.gson.JsonParser;
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
  
  //get e getall
  @Override
  public CompletableFuture<Message> query(Message request) {
    final String req = request.getContent().toString(Charset.defaultCharset());
    String[] opKey = req.split(":");
    System.out.println("Recebendo query: "+ req);
    String result = "";
    result += readSwitch(opKey);
//    final String result = opKey[0] + ":" + key2values.get(opKey[1]);
//    LOG.debug("{}: {} = {}", opKey[0], opKey[1], result);
//    System.out.println("É aqui??" + opKey[0]);
//    CompletableFuture<Message> f = CompletableFuture.completedFuture(Message.valueOf(result));
    return CompletableFuture.completedFuture(Message.valueOf(result));
  }

  //operacoes crud sem read
  @Override
  public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
    final RaftProtos.LogEntryProto entry = trx.getLogEntry();
    final String opKeyValue = entry.getStateMachineLogEntry().getLogData().toString(Charset.defaultCharset());
    System.out.println("Recebendo insert: "+opKeyValue);
    // Encontrar a posição do segundo ":"
    int segundoDoisPontos = opKeyValue.indexOf(":", opKeyValue.indexOf(":") + 1);

    // Obter as partes separadas
    String base = opKeyValue.substring(0, opKeyValue.indexOf(":"));
    String operacao = opKeyValue.substring(opKeyValue.indexOf(":") + 1, segundoDoisPontos);
    String json = opKeyValue.substring(segundoDoisPontos + 1);
    String[] comando = {base,operacao,json};
    switchBase(comando);
  
    final String op = opKeyValue;
    String result = op + ":";
//
//    String key = opKeyValue.length < 2 ? "" : opKeyValue[1];
//    String value = opKeyValue.length < 3 ? "" : opKeyValue[2];
//    switch (op) {
//      case "add":
////    	  System.out.println("Esta passando pelo add");
////    	this.db.put("testeChave".getBytes(), "testeValorVapovapo".getBytes());
////    	String[] payload = {"bcc01", "Dedin"};
////    	handleAlunoPayload("create",payload);
//        result += key2values.put(key, value);
//        break;
//      case "del":
//        result += key2values.remove(key);
//        break;
//      case "clear":
//        key2values.clear();
//        result += ":ok";
//        break;
//      default:
//        result += "invalid-op";
//    }
    final CompletableFuture<Message> f = CompletableFuture.completedFuture(Message.valueOf(result));

//    final RaftProtos.RaftPeerRole role = trx.getServerRole();
//    LOG.info("{}:{} {} {}={}", role, getId(), op, key, value);

    return f;
  }
  
  	public String readSwitch(String[] op) {
  		String base = op[0];
  		String operacao = op[1];
  		String id = op.length > 2 ? op[2] : null;
  		String response = "";
  		switch(operacao) {
        case "get":
        	System.out.println(operacao+":"+id);
        	byte[] b = this.db.get((base+":"+id).getBytes());
        	if (b != null){
        		String str = new String(b);
        		System.out.println(str);
        		return str;
        	}
        	return "";	
        case "getsize":
        	System.out.println("chega aqui? "+base);
            String prefixo = base+":";
            int tamanhoSubbanco = 0;
            try (DBIterator iterator = db.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    Map.Entry<byte[], byte[]> entry = iterator.peekNext();
                    String chave = new String(entry.getKey(), StandardCharsets.UTF_8);
                    if (chave.startsWith(prefixo)) {
                        tamanhoSubbanco ++;
                    }
                }
            } catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println(tamanhoSubbanco);
        	return String.valueOf(tamanhoSubbanco);
        case "getall": //[base:chave=valor]
            prefixo = base+":";
            List<Map.Entry<String, String>> entries = new ArrayList<>();
            try (DBIterator iterator = db.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    Map.Entry<byte[], byte[]> entry = iterator.peekNext();
                    String chave = new String(entry.getKey(), StandardCharsets.UTF_8);
                    if (chave.startsWith(prefixo)) {
                        String valor = new String(entry.getValue(), StandardCharsets.UTF_8);
                        entries.add(Map.entry(chave, valor));
                    }
                }
            } catch (IOException e) {
				e.printStackTrace();
			}
            String mapSemcolchete = entries.toString().substring(1, entries.toString().length() -1);
        	return mapSemcolchete;
        default:
        	return null;
        }
  	}
  
	public void switchBase(String[] message) {
		String base = message[0];
		String operacao = message[1];
		JsonObject payload = JsonParser.parseString(message[2]).getAsJsonObject();
        switch(base) {
        case "aluno":
        	System.out.println(operacao+":"+message[2]);
            handleAlunoPayload(operacao,payload);
            break;
        case "professor":
        	handleProfessorPayload(operacao, payload);
        	break;
        case "disciplina":
        	handleDisciplinaPayload(operacao, payload);
        	break;
        default:
            break;
        }
	}
	
	private void handleProfessorPayload(String operacao,JsonObject payload) {
		String siape  = payload.has("siape") ? payload.get("siape").getAsString() : null;
	    String nome  = payload.has("nome") ? payload.get("nome").getAsString() : null;
	    String sigla  = payload.has("sigla") ? payload.get("sigla").getAsString() : null;
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
    
	private void handleAlunoPayload(String operacao,JsonObject payload) {
		String matricula  = payload.has("matricula") ? payload.get("matricula").getAsString() : null;
	    String nome  = payload.has("nome") ? payload.get("nome").getAsString() : null;
	    String sigla  = payload.has("sigla") ? payload.get("sigla").getAsString() : null;
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

    private void handleDisciplinaPayload(String operacao,JsonObject payload) {
		String nome  = payload.has("nome") ? payload.get("nome").getAsString() : null;
		String sigla = payload.has("sigla") ? payload.get("sigla").getAsString() : null;
    	int vagas = payload.has("vagas") ? payload.get("vagas").getAsInt() : null;
    	switch(operacao) {
    	case "create":
        	this.db.put(("disciplinas:"+sigla).getBytes(), (nome+":"+vagas).getBytes());
        	String listaJson = new Gson().toJson(new ArrayList<String>());
        	this.db.put(("disciplinaAlunos:"+sigla).getBytes(), listaJson.getBytes());
    		//
//    		Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
//    		Banco.disciplinaAlunos.put(sigla, new ArrayList<String>());
    		break;
    	case "update":
    		this.db.put(("disciplinas:"+sigla).getBytes(), (nome+":"+vagas).getBytes());
    		break;
    	case "delete":
        	String disciplinaAlunos = new String(this.db.get(("disciplinaAlunos:"+sigla).getBytes()));
        	List<String> disciplinaAlunosList = new Gson().fromJson(disciplinaAlunos, new TypeToken<List<String>>(){}.getType());
        	for (String alunoID : disciplinaAlunosList) {
                String alunoDisciplinas = new String(this.db.get(("alunoDisciplinas:"+alunoID).getBytes()));
                List<String> alunoDisciplinasList = new Gson().fromJson(alunoDisciplinas, new TypeToken<List<String>>(){}.getType());
                alunoDisciplinasList.remove(sigla);
                this.db.put(("alunoDisciplinas:"+alunoID).getBytes(), new Gson().toJson(alunoDisciplinasList).getBytes());
			}
        	String siape = new String(this.db.get(("disciplinaProfessor:"+sigla).getBytes()));
        	
        	String professorDisciplinas = new String(this.db.get(("professorDisciplinas:"+siape).getBytes()));
        	List<String> professorDisciplinasList = new Gson().fromJson(professorDisciplinas, new TypeToken<List<String>>(){}.getType());
        	professorDisciplinasList.remove(sigla);
        	this.db.put(("professorDisciplinas:"+siape).getBytes(), new Gson().toJson(professorDisciplinasList).getBytes());
        	this.db.delete(("disciplinaProfessor:"+sigla).getBytes());
        	this.db.delete(("disciplinas:"+sigla).getBytes());
        	//
//			for (String alunosID : Banco.disciplinaAlunos.get(sigla)) {
//				Banco.alunoDisciplinas.get(alunosID).remove(sigla);
//			}
//			Banco.professorDisciplinas.get(Banco.disciplinaProfessor.get(sigla)).remove(sigla);
//			Banco.disciplinaProfessor.remove(sigla);
//			Banco.disciplinas.remove(sigla);
    		break;
    	default:
    		System.out.println("Erro");
    		break;
    	}
    	
    }

}

