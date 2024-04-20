 package br.ufu.facom.gbc074.projeto.matricula;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import br.ufu.facom.gbc074.projeto.bd.Banco;
import br.ufu.facom.gbc074.projeto.mqtt.MqttConfig;
import br.ufu.facom.gbc074.projeto.ratis.RatisClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class PortalMatriculaServer {
	  private static final Logger logger = Logger.getLogger(PortalMatriculaServer.class.getName());

	  private Server server;
	  
	  public static RatisClient ratisClient = new RatisClient();
	  
	  public static Gson gson = new Gson();
	  
	  public static MqttConfig mqtt;
	  
	  private void isPortInUse(int port) throws Exception {
		    try {
		        (new ServerSocket(port)).close();
		        return;
		    } catch (IOException e) {
		        throw new Exception();
		    }
	  }

	  private void start(int port) throws IOException {
	    /* The port on which the server should run */
	    server = ServerBuilder.forPort(port)
	        .addService(new PortalMatriculaImpl())
	        .build()
	        .start();
	    logger.info("Server started, listening on " + port);
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	      @Override
	      public void run() {
	        // Use stderr here since the logger may have been reset by its JVM shutdown
	        // hook.
	        System.err.println("*** shutting down gRPC server since JVM is shutting down");
	        try {
	        	PortalMatriculaServer.this.stop();
	        } catch (InterruptedException e) {
	          e.printStackTrace(System.err);
	        }
	        System.err.println("*** server shut down");
	      }
	    });
	  }

	  private void stop() throws InterruptedException {
	    if (server != null) {
	      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
	    }
	  }

	  /**
	   * Await termination on the main thread since the grpc library uses daemon
	   * threads.
	   */
	  private void blockUntilShutdown() throws InterruptedException {
	    if (server != null) {
	      server.awaitTermination();
	    }
	  }

	  /**
	   * Main launches the server from the command line.
	   */
	  public static void main(String[] args) throws IOException, InterruptedException {
		  int port =50052;
		  if(args.length != 0 ) {
			  port = Integer.parseInt(args[0]);
		  }
		  try {
			PortalMatriculaServer.mqtt = new MqttConfig(String.valueOf(port));
			PortalMatriculaServer.mqtt.cliente.subscribe("aluno/#");
			PortalMatriculaServer.mqtt.cliente.subscribe("disciplina/#");
			PortalMatriculaServer.mqtt.cliente.subscribe("professor/#");
		  } catch (MqttException e) {
			System.err.println("Nao foi possivel conectar ao servidor MQTT");
			System.exit(1);
		  }
		  final PortalMatriculaServer server = new PortalMatriculaServer();
		    try {
				server.isPortInUse(port);
			} catch (Exception e) {
				System.out.println("Esta porta já está em uso");
				System.exit(1);
			}
		  server.start(port);
		  server.blockUntilShutdown();
    }

	  static class PortalMatriculaImpl extends PortalMatriculaGrpc.PortalMatriculaImplBase {
		  
	    @Override
	    public void adicionaProfessor(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
			String professorID = req.getIdPessoa();
			String disciplinaID = req.getDisciplina();
			int code = 1;
			String errorMsg = "";
			//Validacao
			 try {
				if(!Banco.professores.containsKey(professorID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + professorID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se professor nao existe
				 	code = 1;
				 	errorMsg = "Professor nao cadastrado";
				 }else if(!Banco.disciplinas.containsKey(disciplinaID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se disciplina nao existe
				 	code = 1;
				 	errorMsg = "Disciplina nao cadastrada";
				 }else if(Banco.disciplinaProfessor.containsKey(disciplinaID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinaProfessor:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Caso disciplina já tenha um professor
					 code = 1;
					 errorMsg = "Disciplina ja possui um professor associado";
				}else{
					code = 0;
					//Json
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("siape", professorID);
					jsonObject.addProperty("sigla", disciplinaID);
					//Ratis
					RaftClientReply getValue;
					getValue = ratisClient.clusters.get(professorID.hashCode()%2).io().send(Message.valueOf("professor:add:"+jsonObject.toString()));
					String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
					System.out.println("Resposta:" + response);
					//Mqtt
					try {
						PortalMatriculaServer.mqtt.cliente.publish("professor/add", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }

	    @Override
	    public void removeProfessor(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
			String professorID = req.getIdPessoa();
			String disciplinaID = req.getDisciplina();
			int code = 1;
			String errorMsg = "";
			//Validacao
			 try {
				if(!Banco.professores.containsKey(professorID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + professorID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se professor nao existe
				 	code = 1;
				 	errorMsg = "Professor nao cadastrado";
				 }else if(!Banco.disciplinas.containsKey(disciplinaID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se disciplina nao existe
				 	code = 1;
				 	errorMsg = "Disciplina nao cadastrada";
				 }else if(!Banco.disciplinaProfessor.containsKey(disciplinaID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinaProfessor:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Caso disciplina não tenha um professor
					 code = 1;
					 errorMsg = "Disciplina nao possui um professor";
				 }else if(!Banco.disciplinaProfessor.get(disciplinaID).equals(professorID) && ratisClient.clusters.get(professorID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinaProfessor:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).equals(professorID)){
					 code = 1;
					 errorMsg = "Este professor nao esta associado a esta disciplina";
				 }else{
					code = 0;
					//Json
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("siape", professorID);
					jsonObject.addProperty("sigla", disciplinaID);
					//Ratis
					RaftClientReply getValue;
					getValue = ratisClient.clusters.get(professorID.hashCode()%2).io().send(Message.valueOf("professor:remove:"+jsonObject.toString()));
					String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
					System.out.println("Resposta:" + response);
					//Mqtt
					try {
						PortalMatriculaServer.mqtt.cliente.publish("professor/remove", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }
	    @Override
	    public void adicionaAluno(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
	    	String alunoID = req.getIdPessoa();
	    	String disciplinaID = req.getDisciplina();
	    	int code = 1;
	    	String errorMsg = "";
	    	//Validacao
	    	try {
				if(!Banco.alunos.containsKey(alunoID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + alunoID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se alunoo nao existe
					code = 1;
					errorMsg = "Aluno nao cadastrado";
				}else if(!Banco.disciplinas.containsKey(disciplinaID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se disciplina nao existe
					code = 1;
					errorMsg = "Disciplina nao cadastrada";
				}else if(Banco.disciplinaAlunos.get(disciplinaID).contains(alunoID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinaAlunos:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).contains(alunoID)){ //aluno ja faz parte da disciplina?
					code = 1;
					errorMsg = "Aluno ja matriculado na disciplina";
				}else if(!(Banco.disciplinaAlunos.get(disciplinaID).size() < Banco.disciplinas.get(disciplinaID).getVagas())) { // Se cabe mais aluno na disciplina
					code = 1;
					errorMsg = "Disciplina "+Banco.disciplinas.get(disciplinaID).getNome()+" ja atingiu a capacidade maxima de "+Banco.disciplinas.get(disciplinaID).getVagas();	    	
				}else{
					code = 0;
//	    		Banco.disciplinaAlunos.get(disciplinaID).add(alunoID);
//	    		Banco.alunoDisciplinas.get(alunoID).add(disciplinaID);
					//Json
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("matricula", alunoID);
					jsonObject.addProperty("sigla", disciplinaID);
					//Ratis
					RaftClientReply getValue;
					getValue = ratisClient.clusters.get(alunoID.hashCode()%2).io().send(Message.valueOf("aluno:add:"+jsonObject.toString()));
					String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
					System.out.println("Resposta:" + response);
					//Mqtt
					try {
						PortalMatriculaServer.mqtt.cliente.publish("aluno/add", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	    	responseObserver.onNext(status);
	    	responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void removeAluno(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
	    	String alunoID = req.getIdPessoa();
	    	String disciplinaID = req.getDisciplina();
	    	int code = 1;
	    	String errorMsg = "";
	    	//Validacao
	    	try {
				if(!Banco.alunos.containsKey(alunoID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + alunoID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se aluno nao existe
					code = 1;
					errorMsg = "Aluno nao cadastrado";
				}else if(!Banco.disciplinas.containsKey(disciplinaID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se disciplina nao existe
					code = 1;
					errorMsg = "Disciplina nao cadastrada";
				}else if(!Banco.disciplinaAlunos.get(disciplinaID).contains(alunoID) && ratisClient.clusters.get(alunoID.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinaAlunos:get:" + disciplinaID)).getMessage().getContent().toString(Charset.defaultCharset()).contains(alunoID)){ //aluno nao faz parte da disciplina
					code = 1;
					errorMsg = "Aluno nao matriculado na disciplina";
				}else{
					code = 0;
//	    		Banco.disciplinaAlunos.get(disciplinaID).remove(alunoID);
//	    		Banco.alunoDisciplinas.get(alunoID).remove(disciplinaID);
					//Json
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("matricula", alunoID);
					jsonObject.addProperty("sigla", disciplinaID);
					//Ratis
					RaftClientReply getValue;
					getValue = ratisClient.clusters.get(alunoID.hashCode()%2).io().send(Message.valueOf("aluno:remove:"+jsonObject.toString()));
					String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
					System.out.println("Resposta:" + response);
					//Mqtt
					try {
						PortalMatriculaServer.mqtt.cliente.publish("aluno/remove", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	    	responseObserver.onNext(status);
	    	responseObserver.onCompleted();
	    }
	    @Override
	    public void detalhaDisciplina(Identificador req, StreamObserver<RelatorioDisciplina> responseObserver) {
	    	String disciplinaID = req.getId();
	    	Disciplina disciplina;
	    	Professor professor;
	    	ArrayList<Aluno> alunos = new ArrayList<Aluno>();
	    	RelatorioDisciplina response;
	    	if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
	    		disciplina = Disciplina.newBuilder().setSigla(" ").setNome(" ").setVagas(0).build();
	    		professor = Professor.newBuilder().setNome(" ").setSiape(" ").build();
	    		alunos.add(Aluno.newBuilder().setMatricula(" ").setNome(" ").build());
	    	}else {
	    		disciplina = Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).setVagas(Banco.disciplinas.get(disciplinaID).getVagas()).build();
	    		if(Banco.disciplinaAlunos.get(disciplinaID).size() > 0) {//Disciplina tem alunos
	    			for (String alunoID : Banco.disciplinaAlunos.get(disciplinaID)) {
	    				alunos.add(Aluno.newBuilder().setMatricula(alunoID).setNome(Banco.alunos.get(alunoID)).build());
					}
	    		}else {
	    			alunos.add(Aluno.newBuilder().setMatricula(" ").setNome(" ").build());
	    		}
	    		if(Banco.disciplinaProfessor.containsKey(disciplinaID)) {//Disciplina tem professor
	    			String siape = Banco.disciplinaProfessor.get(disciplinaID);
	    			String nome = Banco.professores.get(siape);
	    			professor = Professor.newBuilder().setNome(nome).setSiape(siape).build();
	    		}else {
	    			professor = Professor.newBuilder().setNome(" ").setSiape(" ").build();
	    		}
	    	}
	    	response = RelatorioDisciplina.newBuilder().setDisciplina(disciplina).setProfessor(professor).addAllAlunos(alunos).build();
	    	responseObserver.onNext(response);
	    	responseObserver.onCompleted();
	    }

	    @Override
	    public void obtemDisciplinasProfessor(Identificador req, StreamObserver<RelatorioDisciplina> responseObserver) {
	    	String professorID = req.getId();
	    	if(!Banco.professores.containsKey(professorID)) {//if professor nao existe
//	    		responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").setVagas(0).build())
//	    		.addAlunos(Aluno.newBuilder().setMatricula(" ").setNome(" "))
//	    		.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build()).build());
	    	}else if(Banco.professorDisciplinas.get(professorID).size() == 0) {//professor nao participa de alguma disciplina
	    		responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").setVagas(0).build())
	    		.addAlunos(Aluno.newBuilder().setMatricula(" ").setNome(" "))
	    		.setProfessor(Professor.newBuilder().setSiape(professorID).setNome(Banco.professores.get(professorID)).build()).build());
	    	}else {
	    		for (String disciplinaID : Banco.professorDisciplinas.get(professorID)) {
	    			ArrayList<Aluno> alunos = new ArrayList<Aluno>();
	    			if(Banco.disciplinaAlunos.get(disciplinaID).size() == 0) { //disciplina nao tem alunos
	    				alunos.add(Aluno.newBuilder().setMatricula(" ").setNome(" ").build());
	    			}else {
	    				for (String alunoID : Banco.disciplinaAlunos.get(disciplinaID)) {
	    					alunos.add(Aluno.newBuilder().setMatricula(alunoID).setNome(Banco.alunos.get(alunoID)).build());
	    				}
	    			}
	    			responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).setVagas(Banco.disciplinas.get(disciplinaID).getVagas()).build())
	    															.addAllAlunos(alunos)
	    															.setProfessor(Professor.newBuilder().setSiape(professorID).setNome(Banco.professores.get(professorID)).build()).build());
	    		}
	    	}
	    	responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void obtemDisciplinasAluno(Identificador req, StreamObserver<ResumoDisciplina> responseObserver) {
	    	String alunoID = req.getId();
	    	if(!Banco.alunos.containsKey(alunoID) || Banco.alunoDisciplinas.get(alunoID).size() == 0) { //se aluno nao existe ou numero de disciplinas do aluno é 0
//				responseObserver.onNext(ResumoDisciplina.newBuilder()
//				.setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").setVagas(0).build())
//				.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build())
//				.setTotalAlunos(0).build());
	    	}else {
	    		for (String disciplinaID : Banco.alunoDisciplinas.get(alunoID)) {
	    			if(!Banco.disciplinaProfessor.containsKey(disciplinaID)){//Disciplina nao tem professor
	    				responseObserver.onNext(ResumoDisciplina.newBuilder()
	    						.setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).setVagas(Banco.disciplinas.get(disciplinaID).getVagas()).build())
	    						.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build())
	    						.setTotalAlunos(Banco.disciplinaAlunos.get(disciplinaID).size()).build());
	    			}else {
	    				responseObserver.onNext(ResumoDisciplina.newBuilder()
	    						.setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).setVagas(Banco.disciplinas.get(disciplinaID).getVagas()).build())
	    						.setProfessor(Professor.newBuilder().setSiape(Banco.disciplinaProfessor.get(disciplinaID)).setNome(Banco.professores.get(Banco.disciplinaProfessor.get(disciplinaID))).build())
	    						.setTotalAlunos(Banco.disciplinaAlunos.get(disciplinaID).size()).build());
	    			}
				}
	    	}
	    	responseObserver.onCompleted();
	  }
	 }
}

