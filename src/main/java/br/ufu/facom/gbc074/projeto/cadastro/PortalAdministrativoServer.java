package br.ufu.facom.gbc074.projeto.cadastro;

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
import br.ufu.facom.gbc074.projeto.bd.model.DisciplinaModel;
import br.ufu.facom.gbc074.projeto.mqtt.MqttConfig;
import br.ufu.facom.gbc074.projeto.ratis.RatisClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class PortalAdministrativoServer {
	  private static final Logger logger = Logger.getLogger(PortalAdministrativoServer.class.getName());

	  private Server server;
	  
	  public static MqttConfig mqtt;
	  
	  public static RatisClient ratisClient = new RatisClient();

	  public static Gson gson = new Gson();
	  
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
	        .addService(new PortalAdministrativoImpl())
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
	        	PortalAdministrativoServer.this.stop();
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
		  int port =50051;
		  if(args.length != 0 ) {
			  port = Integer.parseInt(args[0]);
		}
		
	    final PortalAdministrativoServer server = new PortalAdministrativoServer();
	    try {
			server.isPortInUse(port);
		} catch (Exception e) {
			System.out.println("Esta porta já está em uso");
			System.exit(1);
		}
	    try {
			mqtt = new MqttConfig(String.valueOf(port));
			mqtt.cliente.subscribe("aluno/#");
			mqtt.cliente.subscribe("disciplina/#");
			mqtt.cliente.subscribe("professor/#");
		} catch (MqttException e) {
			System.err.println("Nao foi possivel conectar ao servidor MQTT");
			System.exit(1);
		}
	    server.start(port);
	    server.blockUntilShutdown();
	  }

	  static class PortalAdministrativoImpl extends PortalAdministrativoGrpc.PortalAdministrativoImplBase {
		  
	    @Override
	    public void novoAluno(Aluno req, StreamObserver<Status> responseObserver) {
			String nome = req.getNome();
			String matricula = req.getMatricula();
			int code = 1;
			String errorMsg = "";
			//Validacao
			 if(Banco.alunos.containsKey(matricula)){ //Se aluno existe
			 	code = 1;
			 	errorMsg = "Aluno ja cadastrado";
			 } else 
			if(nome.length() >= 4 && matricula.length() >= 4){ //Caso contrario e tenha nome e matricula maior que 4 matricula.hashCode()%2
				try {
					String responseGet = ratisClient.clusters.get(matricula.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + matricula)).getMessage().getContent().toString(Charset.defaultCharset());
					if(responseGet.isEmpty()) {
						System.out.println("Nao existe no cluster, tentando inserir");
//				if(nao existe no leveldb) else errorMsg = "Aluno ja cadastrado"; e colocar no cache
						code = 0;
						//Json
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty("matricula", matricula);
						jsonObject.addProperty("nome", nome);
						//Ratis
							RaftClientReply getValue;
							getValue = ratisClient.clusters.get(matricula.hashCode()%2).io().send(Message.valueOf("aluno:create:"+jsonObject.toString()));
							String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
							System.out.println("Resposta:" + response);
						//Mqtt
						try {
							PortalAdministrativoServer.mqtt.cliente.publish("aluno/create", new MqttMessage(gson.toJson(jsonObject).getBytes()));
						} catch (MqttPersistenceException e) {
							e.printStackTrace();
						} catch (MqttException e) {
							e.printStackTrace();
						}	
					}else {
					 	code = 1;
					 	errorMsg = "Aluno ja cadastrado";
//					 	colocar no cache
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				code = 1;
				errorMsg = "Nome ou matricula menor que 4";
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void editaAluno(Aluno req, StreamObserver<Status> responseObserver) {
			String nome = req.getNome();
			String matricula = req.getMatricula();
			int code = 1;
			String errorMsg = "";
			//Validacao
			 try {
				if(Banco.alunos.containsKey(matricula) || !ratisClient.clusters.get(matricula.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + matricula)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //inLevelDB
					 if(nome.length() >= 4){ //Caso contrario e tenha nome e matricula maior que 4
						code = 0;
						//Json
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty("matricula", matricula);
						jsonObject.addProperty("nome", nome);
						//Ratis
						RaftClientReply getValue;
						getValue = ratisClient.clusters.get(matricula.hashCode()%2).io().send(Message.valueOf("aluno:update:"+jsonObject.toString()));
						String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
						System.out.println("Resposta:" + response);
						//Mqtt
						try {
							PortalAdministrativoServer.mqtt.cliente.publish("aluno/update", new MqttMessage(gson.toJson(jsonObject).getBytes()));
						} catch (MqttPersistenceException e) {
							e.printStackTrace();
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}else{
						code = 1;
						errorMsg = "Nome menor que 4";
					}
				 }else {
					 code = 1;
					 errorMsg = "Aluno nao cadastrado";				 					 
				 }
			} catch (IOException e) {
				e.printStackTrace();
			}
			 Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			 responseObserver.onNext(status);
			 responseObserver.onCompleted();
			 
	    }

	    @Override
	    public void removeAluno(Identificador req, StreamObserver<Status> responseObserver) {
			String matricula = req.getId();
			int code = 1;
			String errorMsg = "";
			//Validacao
				try {
					if(Banco.alunos.containsKey(matricula) || !ratisClient.clusters.get(matricula.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + matricula)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()) {
						code = 0;
						errorMsg = "";
						//Json
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty("matricula", matricula);
						//Ratis
						RaftClientReply getValue;
						getValue = ratisClient.clusters.get(matricula.hashCode()%2).io().send(Message.valueOf("aluno:delete:"+jsonObject.toString()));
						String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
						System.out.println("Resposta:" + response);
						//Mqtt
							try {
								PortalAdministrativoServer.mqtt.cliente.publish("aluno/delete", new MqttMessage(gson.toJson(jsonObject).getBytes()));
							} catch (MqttPersistenceException e) {
								e.printStackTrace();
							} catch (MqttException e) {
								e.printStackTrace();
							}
					 }else{
						code = 1;
						errorMsg = "Aluno nao cadastrado";
							 }
						} catch (IOException e) {
							e.printStackTrace();
						}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			responseObserver.onNext(status);
			responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void obtemAluno(Identificador req, StreamObserver<Aluno> responseObserver) {
			String matricula = req.getId();
			Aluno alunoResponse = null;
			//Validacao
			 if(Banco.alunos.containsKey(matricula)){ //Se aluno existe
				 alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(Banco.alunos.get(matricula)).build();
			 } else
				try {
					String nome = ratisClient.clusters.get(matricula.hashCode()%2).io().sendReadOnly(Message.valueOf("alunos:get:" + matricula)).getMessage().getContent().toString(Charset.defaultCharset()); 
					if (!nome.isEmpty()){ //Se aluno não existe) { //LevelDB
						alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(nome).build();
					}else{
						alunoResponse = Aluno.newBuilder().setMatricula(" ").setNome(" ").build();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			responseObserver.onNext(alunoResponse);
	    	responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void obtemTodosAlunos(Vazia req, StreamObserver<Aluno> responseObserver) {
//	    	String sizeC0 = "0";
//	    	String sizeC1 = "0";
	    	try {
				if(Banco.alunos.size() != 0 &&
						(Integer.valueOf(ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("alunos:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
								+Integer.valueOf(ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("alunos:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
								== Banco.alunos.size()) ) {
//					System.out.println((Integer.valueOf(sizeC0) + Integer.valueOf(sizeC1)));
					for (String matricula : Banco.alunos.keySet()) {
						Aluno alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(Banco.alunos.get(matricula)).build();
						responseObserver.onNext(alunoResponse);
					}
				}else {
						String responseC0 =ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("alunos:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
						String responseC1 =ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("alunos:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
						String[] alunos = (responseC0+","+responseC1).split(",");
						for (String aluno : alunos) {// alunos:bcc01=brito
							String[] al = aluno.split("=");
							String nome = al[1];
							String matricula = al[0].split(":")[1];
							Aluno alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(nome).build();
							responseObserver.onNext(alunoResponse);
						}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		      responseObserver.onCompleted();
	    }
	    
		  @Override
		  public void novoProfessor(Professor req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  String siape = req.getSiape();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
			  if(Banco.professores.containsKey(siape)){ //Se professor existe
				  code = 1;
				  errorMsg = "Professor ja cadastrado";
			  } else 
				  if(nome.length() >= 4 && siape.length() >= 4){ //Caso contrario e tenha nome e siape maior que 4
					  try {
						String responseGet = ratisClient.clusters.get(siape.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + siape)).getMessage().getContent().toString(Charset.defaultCharset());
						if(responseGet.isEmpty()) {
						  code = 0;
						  //Json
						  JsonObject jsonObject = new JsonObject();
						  jsonObject.addProperty("siape", siape);
						  jsonObject.addProperty("nome", nome);
						//Ratis
						RaftClientReply getValue;
						getValue = ratisClient.clusters.get(siape.hashCode()%2).io().send(Message.valueOf("professor:create:"+jsonObject.toString()));
						String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
						System.out.println("Resposta:" + response);
						  //Mqtt
						  try {
							PortalAdministrativoServer.mqtt.cliente.publish("professor/create", new MqttMessage(gson.toJson(jsonObject).getBytes()));
						  } catch (MqttPersistenceException e) {
							e.printStackTrace();
						  } catch (MqttException e) {
							e.printStackTrace();
						  }
						}else{
							code = 1;
							errorMsg = "Professor ja cadastrado";
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				  }else{
					  code = 1;
					  errorMsg = "Nome ou siape menor que 4";
				  }
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void editaProfessor(Professor req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  String siape = req.getSiape();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
			  try {
				if(Banco.professores.containsKey(siape) || !ratisClient.clusters.get(siape.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + siape)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se professor existe
					  if(nome.length() >= 4){ //Caso contrario e tenha nome e matricula maior que 4
						  code = 0;
						  //Json
						  JsonObject jsonObject = new JsonObject();
						  jsonObject.addProperty("siape", siape);
						  jsonObject.addProperty("nome", nome);
							//Ratis
							RaftClientReply getValue;
							getValue = ratisClient.clusters.get(siape.hashCode()%2).io().send(Message.valueOf("professor:update:"+jsonObject.toString()));
							String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
							System.out.println("Resposta:" + response);
						  //Mqtt
						  try {
							  PortalAdministrativoServer.mqtt.cliente.publish("professor/update", new MqttMessage(gson.toJson(jsonObject).getBytes()));
						  } catch (MqttPersistenceException e) {
							  e.printStackTrace();
						  } catch (MqttException e) {
							  e.printStackTrace();
						  }
					  }else{
						  code = 1;
						  errorMsg = "Nome menor que 4";
					  }
				  } else {
					code = 1;
				  	errorMsg = "Professor nao cadastrado";
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void removeProfessor(Identificador req, StreamObserver<Status> responseObserver) {
			  String siape = req.getId();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
			  try {
				if(Banco.professores.containsKey(siape) || !ratisClient.clusters.get(siape.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + siape)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){ //Se professor não existe
					code = 0;
					errorMsg = "";
					//Json
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("siape", siape);
					//Ratis
					RaftClientReply getValue;
					getValue = ratisClient.clusters.get(siape.hashCode()%2).io().send(Message.valueOf("professor:delete:"+jsonObject.toString()));
					String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
					System.out.println("Resposta:" + response);
					//Mqtt
					try {
						PortalAdministrativoServer.mqtt.cliente.publish("professor/delete", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					} catch (MqttPersistenceException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				  }else{
					  code = 1;
					  errorMsg = "Professor nao cadastrado";
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemProfessor(Identificador req, StreamObserver<Professor> responseObserver) {
			  String siape = req.getId();
			  Professor professorResponse = null;
			  //Validacao
			  if(Banco.professores.containsKey(siape)){ //Se professor existe
				  professorResponse = Professor.newBuilder().setSiape(siape).setNome(Banco.professores.get(siape)).build();
			  }else{
				  try {
						String nome = ratisClient.clusters.get(siape.hashCode()%2).io().sendReadOnly(Message.valueOf("professores:get:" + siape)).getMessage().getContent().toString(Charset.defaultCharset()); 
						if (!nome.isEmpty()){ //Se professor existe) { //LevelDB
							professorResponse = Professor.newBuilder().setSiape(siape).setNome(nome).build();
						}else {
							professorResponse = Professor.newBuilder().setSiape(" ").setNome(" ").build();
						}
				  } catch (IOException e) {
					  e.printStackTrace();
				  }
						}
			  responseObserver.onNext(professorResponse);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemTodosProfessores(Vazia req, StreamObserver<Professor> responseObserver) {
			  try {
				if(Banco.professores.size() != 0 &&
						(Integer.valueOf(ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("professores:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
								+Integer.valueOf(ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("professores:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
								== Banco.professores.size()) ) {
				  for (String siape : Banco.professores.keySet()) {
					  Professor professorResponse = Professor.newBuilder().setSiape(siape).setNome(Banco.professores.get(siape)).build();
					  responseObserver.onNext(professorResponse);
				  }
				}else {
					String responseC0 =ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("professores:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
					String responseC1 =ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("professores:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
					String[] professores = (responseC0+","+responseC1).split(",");
					for (String professor : professores) {
						String[] prof = professor.split("=");
						String nome = prof[1];
						String siape = prof[0].split(":")[1];
						Professor professorResponse = Professor.newBuilder().setSiape(siape).setNome(nome).build();
						responseObserver.onNext(professorResponse);	
					}
				}
			  } catch (NumberFormatException e) {
				e.printStackTrace();
			  } catch (IOException e) {
				e.printStackTrace();
			  }
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void novaDisciplina(Disciplina req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  int vagas = req.getVagas();
			  String sigla = req.getSigla();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
				if (Banco.disciplinas.containsKey(sigla)) { // Se disciplina existe
					code = 1;
					errorMsg = "Disciplina ja cadastrado";
				} else if (nome.length() > 4 && sigla.length() > 4) { // Caso contrario e tenha nome e sigla maior que 4
					try {
						String responseGet = ratisClient.clusters.get(sigla.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + sigla)).getMessage().getContent().toString(Charset.defaultCharset());
						if (responseGet.isEmpty()) {
							code = 0;
							// Json
							JsonObject jsonObject = new JsonObject();
							jsonObject.addProperty("sigla", sigla);
							jsonObject.addProperty("nome", nome);
							jsonObject.addProperty("vagas", vagas);
							//Ratis
							RaftClientReply getValue;
							getValue = ratisClient.clusters.get(sigla.hashCode()%2).io().send(Message.valueOf("disciplina:create:"+jsonObject.toString()));
							String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
							System.out.println("Resposta:" + response);
							// Mqtt
							try {
								PortalAdministrativoServer.mqtt.cliente.publish("disciplina/create",new MqttMessage(gson.toJson(jsonObject).getBytes()));
							} catch (MqttPersistenceException e) {
								e.printStackTrace();
							} catch (MqttException e) {
								e.printStackTrace();
							}
						} else {
							code = 1;
							errorMsg = "Disciplina ja cadastrado";
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					code = 1;
					errorMsg = "Nome ou sigla menor que 4";
				}
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void editaDisciplina(Disciplina req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  int vagas = req.getVagas();
			  String sigla = req.getSigla();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
			  try {
				if(Banco.disciplinas.containsKey(sigla) || !ratisClient.clusters.get(sigla.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + sigla)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){//Se disciplina existe
					  if(nome.length() > 4){ //Caso contrario e tenha nome e matricula maior que 4
						  code = 0;
						  //Json
						  JsonObject jsonObject = new JsonObject();
						  jsonObject.addProperty("sigla", sigla);
						  jsonObject.addProperty("nome", nome);
						  jsonObject.addProperty("vagas", vagas);
							//Ratis
							RaftClientReply getValue;
							getValue = ratisClient.clusters.get(sigla.hashCode()%2).io().send(Message.valueOf("disciplina:update:"+jsonObject.toString()));
							String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
							System.out.println("Resposta:" + response);
						  //Mqtt
						  try {
							  PortalAdministrativoServer.mqtt.cliente.publish("disciplina/update", new MqttMessage(gson.toJson(jsonObject).getBytes()));
						  } catch (MqttPersistenceException e) {
							  e.printStackTrace();
						  } catch (MqttException e) {
							  e.printStackTrace();
						  }
					  }else{
						  code = 1;
						  errorMsg = "Nome menor que 4";
					  }
				  } else { 
					  code = 1;
					  errorMsg = "Disciplina nao cadastrado";
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void removeDisciplina(Identificador req, StreamObserver<Status> responseObserver) {
			  String sigla = req.getId();
			  int code = 1;
			  String errorMsg = "";
			  //Validacao
			  try {
				if(Banco.disciplinas.containsKey(sigla) || !ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("disciplinas:get:" + sigla)).getMessage().getContent().toString(Charset.defaultCharset()).isEmpty()){//Se disciplina existe
					  code = 0;
					  errorMsg = "";
					  //Json
					  JsonObject jsonObject = new JsonObject();
					  jsonObject.addProperty("sigla", sigla);
						//Ratis
						RaftClientReply getValue;
						getValue = ratisClient.clusters.get(sigla.hashCode()%2).io().send(Message.valueOf("disciplina:delete:"+jsonObject.toString()));
						String response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
						System.out.println("Resposta:" + response);
					  //Mqtt
					  try {
						  PortalAdministrativoServer.mqtt.cliente.publish("disciplina/delete", new MqttMessage(gson.toJson(jsonObject).getBytes()));
					  } catch (MqttPersistenceException e) {
						  e.printStackTrace();
					  } catch (MqttException e) {
						  e.printStackTrace();
					  }
				  }else{
					  code = 1;
					  errorMsg = "Disciplina nao cadastrado";
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemDisciplina(Identificador req, StreamObserver<Disciplina> responseObserver) {
			  String sigla = req.getId();
			  Disciplina disciplinaResponse = null;
			  //Validacao
			  if(Banco.disciplinas.containsKey(sigla)){ //Se aluno não existe
				  DisciplinaModel disciplina = Banco.disciplinas.get(sigla);
				  disciplinaResponse = Disciplina.newBuilder().setSigla(sigla).setNome(disciplina.getNome()).setVagas(disciplina.getVagas()).build();
			  }else { 
				  try {
					String nome = ratisClient.clusters.get(sigla.hashCode()%2).io().sendReadOnly(Message.valueOf("disciplinas:get:" + sigla)).getMessage().getContent().toString(Charset.defaultCharset()); 
					if (!nome.isEmpty()){ //Se professor existe) { //LevelDB
						String[] valores = nome.split(":");
						disciplinaResponse = Disciplina.newBuilder().setSigla(sigla).setNome(valores[0]).setVagas(Integer.valueOf(valores[1])).build();
						
			  		}else{
					  disciplinaResponse = Disciplina.newBuilder().setSigla(" ").setNome(" ").build();
			  		}
				  } catch (IOException e) {
					  e.printStackTrace();
				  }	
			  }
			  responseObserver.onNext(disciplinaResponse);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemTodasDisciplinas(Vazia req, StreamObserver<Disciplina> responseObserver) {
			  try {
				  if(Banco.disciplinas.size() != 0 &&
							(Integer.valueOf(ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("disciplinas:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
									+Integer.valueOf(ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("disciplinas:getsize:")).getMessage().getContent().toString(Charset.defaultCharset()))
									== Banco.disciplinas.size()) ) {
					  for (String sigla : Banco.disciplinas.keySet()) {
						  DisciplinaModel disciplina = Banco.disciplinas.get(sigla);
						  Disciplina disciplinaResponse = Disciplina.newBuilder().setSigla(sigla).setNome(disciplina.getNome()).setVagas(disciplina.getVagas()).build();
						  responseObserver.onNext(disciplinaResponse);
					  }
				  }else {
						String responseC0 =ratisClient.clusters.get(0).io().sendReadOnly(Message.valueOf("disciplinas:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
						String responseC1 =ratisClient.clusters.get(1).io().sendReadOnly(Message.valueOf("disciplinas:getall:")).getMessage().getContent().toString(Charset.defaultCharset());
						String[] disciplinas = (responseC0+","+responseC1).split(",");
						for (String disciplina : disciplinas) {
							String[] disc = disciplina.split("=");
							String[] valores = disc[1].split(":");
							String nome = valores[0];
							int vagas = Integer.valueOf(valores[1]);
							String sigla = disc[0].split(":")[1];
							Disciplina disciplinaResponse = Disciplina.newBuilder().setSigla(sigla).setNome(nome).setVagas(vagas).build();
							responseObserver.onNext(disciplinaResponse);	
						}
					}
			  } catch (NumberFormatException e) {
				e.printStackTrace();
			  } catch (IOException e) {
				e.printStackTrace();
			  }
			  if(Banco.disciplinas.size() == 0) {
//				  Disciplina disciplinaResponse = Disciplina.newBuilder().setSigla(" ").setNome(" ").build();
//				  responseObserver.onNext(disciplinaResponse);
			  }else {
			  }
			  responseObserver.onCompleted();
		  }

	  }
	  
	}

