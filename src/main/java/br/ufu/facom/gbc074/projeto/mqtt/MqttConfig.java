package br.ufu.facom.gbc074.projeto.mqtt;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import br.ufu.facom.gbc074.projeto.bd.Banco;
import br.ufu.facom.gbc074.projeto.bd.model.DisciplinaModel;

public class MqttConfig {

	public MqttClient cliente;
	Gson gson = new Gson();
	String broker = "tcp://127.0.0.1:1883";
	
	public MqttConfig(String clientId) throws MqttException {
		MemoryPersistence persistence = new MemoryPersistence();
		this.cliente = new MqttClient(broker, clientId, persistence);
		
		this.cliente.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) {
            	String payload = new String(message.getPayload());
                System.out.println("Received message: " + payload);
                switchTopicos(topic, message);
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
            }

            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
            }
        });
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker");
        cliente.connect(connOpts);
        System.out.println("Connected");
	}
	
	public void switchTopicos(String topic, MqttMessage message) {
		JsonObject payload = gson.fromJson(new String(message.getPayload()), JsonObject.class);
		String[] topico = topic.split("/");
		String base = topico[0];
		String operacao = topico[1];
        switch(base) {
        case "aluno":
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

	private void handleAlunoPayload(String operacao,JsonObject payload) {
		String matricula = payload.get("matricula").getAsString();
		String nome = payload.get("nome").getAsString();
        switch(operacao) {
        case "create":
            Banco.alunos.put(matricula, nome);
            Banco.alunoDisciplinas.put(matricula, new ArrayList<String>());
            break;
        case "update":
        	Banco.alunos.put(matricula, nome);
        	break;
        case "delete":
			for (String disciplinaID : Banco.alunoDisciplinas.get(matricula)) {
				Banco.disciplinaAlunos.get(disciplinaID).remove(matricula);
			}
			Banco.alunoDisciplinas.remove(matricula);
			Banco.alunos.remove(matricula);
        	break;
        case "alunoDisciplinas":
        	break;
        default:
    		System.out.println("Erro ao receber Mqtt");
            break;
    }
	}
        private void handleProfessorPayload(String operacao,JsonObject payload) {
        	String siape = payload.get("siape").getAsString();
        	String nome = payload.get("nome").getAsString();
        	switch(operacao) {
        	case "create":
        		Banco.professores.put(siape, nome);
        		Banco.professorDisciplinas.put(siape, new ArrayList<String>());
        		break;
        	case "update":
        		Banco.professores.put(siape, nome);
        		break;
        	case "delete":
				for (String disciplinaID : Banco.professorDisciplinas.get(siape)) {
					Banco.disciplinaProfessor.remove(disciplinaID);
				}
				Banco.alunoDisciplinas.remove(siape);
				Banco.professores.remove(siape);
        		break;
        	case "alunoDisciplinas":
        		break;
        	default:
        		System.out.println("Erro ao receber Mqtt");
        		break;
        	}
		
	}
        private void handleDisciplinaPayload(String operacao,JsonObject payload) {
        	String sigla = payload.get("sigla").getAsString();
        	String nome = payload.get("nome").getAsString();
        	int vagas = payload.get("vagas").getAsInt();
        	switch(operacao) {
        	case "create":
			  Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
			  Banco.disciplinaAlunos.put(sigla, new ArrayList<String>());
        		break;
        	case "update":
			  Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
        		break;
        	case "delete":
				for (String alunosID : Banco.disciplinaAlunos.get(sigla)) {
					Banco.alunoDisciplinas.get(alunosID).remove(sigla);
				}
				Banco.professorDisciplinas.get(Banco.disciplinaProfessor.get(sigla)).remove(sigla);
				Banco.disciplinaProfessor.remove(sigla);
				Banco.disciplinas.remove(sigla);
        		break;
        	case "alunoDisciplinas":
        		break;
        	default:
        		System.out.println("Erro ao receber Mqtt");
        		break;
        	}
        	
        }
	
}
