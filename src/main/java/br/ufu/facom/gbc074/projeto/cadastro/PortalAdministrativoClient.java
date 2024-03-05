package br.ufu.facom.gbc074.projeto.cadastro;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;


public class PortalAdministrativoClient {
	  private static final Logger logger = Logger.getLogger(PortalAdministrativoClient.class.getName());

	  private final PortalAdministrativoGrpc.PortalAdministrativoBlockingStub blockingStub;
	  
	  public PortalAdministrativoClient(Channel channel) {
	    // Passing Channels to code makes code easier to test and makes it easier to
	    // reuse Channels.
	    blockingStub = PortalAdministrativoGrpc.newBlockingStub(channel);
	  }

	  public void createAluno(String nome, String matricula) {
	    logger.info("Criando aluno "+ "<"+ matricula+"> " + nome);
	    Aluno request= Aluno.newBuilder().setNome(nome).setMatricula(matricula).build();
	    Status response;
	    try {
	      response = blockingStub.novoAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void updateAluno(String nome, String matricula) {
	    logger.info("Editando aluno "+ "<"+ matricula+"> " + nome);
	    Aluno request= Aluno.newBuilder().setNome(nome).setMatricula(matricula).build();
	    Status response;
	    try {
	      response = blockingStub.editaAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void deleteAluno(String id) {
		  logger.info("Deletando aluno "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Status response;
		  try {
			  response = blockingStub.removeAluno(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void readAluno(String id) {
		  logger.info("Obtendo aluno "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Aluno response;
		  try {
			  response = blockingStub.obtemAluno(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
		  System.out.println(response);
	  }
	  
	  public void readAllAlunos() {
		  logger.info("Obtendo alunos");
		  Vazia request = Vazia.newBuilder().build();
		  Iterator<Aluno> response;
		  try {
			  response = blockingStub.obtemTodosAlunos(request);
			  while(response.hasNext()) {
				  System.out.println(response.next());
			  }
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }

	  public void createProfessor(String nome, String siape) {
		  logger.info("Criando professor "+ "<"+ siape+"> " + nome);
		  Professor request= Professor.newBuilder().setNome(nome).setSiape(siape).build();
		  Status response;
		  try {
			  response = blockingStub.novoProfessor(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void updateProfessor(String nome, String siape) {
		  logger.info("Editando professor "+ "<"+ siape+"> " + nome);
		  Professor request= Professor.newBuilder().setNome(nome).setSiape(siape).build();
		  Status response;
		  try {
			  response = blockingStub.editaProfessor(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void deleteProfessor(String id) {
		  logger.info("Deletando professor "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Status response;
		  try {
			  response = blockingStub.removeProfessor(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void readProfessor(String id) {
		  logger.info("Obtendo professor "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Professor response;
		  try {
			  response = blockingStub.obtemProfessor(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  System.out.println(response);
	  }
	  
	  public void readAllProfessores() {
		  logger.info("Obtendo professores");
		  Vazia request = Vazia.newBuilder().build();
		  Iterator<Professor> response;
		  try {
			  response = blockingStub.obtemTodosProfessores(request);
			  while(response.hasNext()) {
				  System.out.println(response.next());
			  }
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
	  }
	  
	  public void createDisciplina(String nome, String sigla, int vagas) {
		  logger.info("Criando disciplina "+ "<"+ sigla+"> " + nome);
		  Disciplina request= Disciplina.newBuilder().setNome(nome).setSigla(sigla).setVagas(vagas).build();
		  Status response;
		  try {
			  response = blockingStub.novaDisciplina(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void updateDisciplina(String nome, String sigla, int vagas) {
		  logger.info("Editando disciplina "+ "<"+ sigla+"> " + nome);
		  Disciplina request= Disciplina.newBuilder().setNome(nome).setSigla(sigla).setVagas(vagas).build();
		  Status response;
		  try {
			  response = blockingStub.editaDisciplina(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void deleteDisciplina(String id) {
		  logger.info("Deletando disciplina "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Status response;
		  try {
			  response = blockingStub.removeDisciplina(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void readDisciplina(String id) {
		  logger.info("Obtendo disciplina "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Disciplina response;
		  try {
			  response = blockingStub.obtemDisciplina(request);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
		  System.out.println(response);
	  }
	  
	  public void readAllDisciplinas() {
		  logger.info("Obtendo disciplinas");
		  Vazia request = Vazia.newBuilder().build();
		  Iterator<Disciplina> response;
		  try {
			  response = blockingStub.obtemTodasDisciplinas(request);
			  while(response.hasNext()) {
				  System.out.println(response.next());
			  }
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
	  }
	  

	  public static void main(String[] args) throws Exception {
	        if (args.length < 6 || args.length > 12) {
	            System.out.println("Usage: --port <port> --base <base> --op <op> --key <key> --val <val1> [<val2>]");
	            return;
	        }

	        String port = null;
	        String base = null;
	        String op = null;
	        String key = null;
	        String val1 = null;
	        String val2 = null;
	        HashMap<String, String> options = new HashMap<String, String>();

	        for (int i = 0; i < args.length; i += 2) {
	            switch (args[i]) {
	                case "--port":
	                    port = args[i + 1];
	                    break;
	                case "--base":
	                    base = args[i + 1];
	                    break;
	                case "--op":
	                    op = args[i + 1];
	                    break;
	                case "--key":
	                    key = args[i + 1];
	                    break;
	                case "--val":
	                    val1 = args[i + 1];
	                    if (i + 2 < args.length && !args[i + 2].startsWith("--")) {
	                        val2 = args[i + 2];
	                        i++; // skip the next argument
	                    }
	                    break;
	                default:
	                    System.out.println("Invalid argument: " + args[i]);
	                    return;
	            }
	        }

	        if (port == null || base == null || op == null) {
	            System.out.println("Missing required argument.");
	            return;
	        }
	        
	        if(!op.equals("getall") && val1 == null && key == null) {
	        	return;
	        }
	        
	        options.put("base", base);
	        options.put("op", op);
	        options.put("key", key);
	        options.put("val1", val1);
	        if (val2 != null) {
	        	options.put("val2", val2);
	        }
	        if(base == "disciplina" && (op == "create" || op == "update")) {
	        	try {
	        		Integer.parseInt(val2);
	        	}catch (Exception e) {
					System.out.println("Digite um valor inteiro para quantidade de vagas");
				}

	        }
	        
	    String target = "localhost:" + port;

	    // Create a communication channel to the server, known as a Channel.
	    // Channels are thread-safe and reusable.
	    // It is common to create channels at the beginning of your application and
	    // reuse
	    // them until the application shuts down.
	    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
	        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
	        // to avoid needing certificates.
	        .usePlaintext().build();
	    try {
	    	PortalAdministrativoClient client = new PortalAdministrativoClient(channel);
    		client.executaPortal(options);
	    } finally {
	      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	    }
	  }
	  
	  
	  
	  public void executaPortal(HashMap<String,String> options) {
		  String opcao = options.get("op");
		  String base = options.get("base");
		  String key = options.get("key");
		  String val = options.get("val1");
		  switch (opcao) {
			case "create":
				switch(base) {
					case "aluno":
						this.createAluno(val,key);
						break;
					case "professor":
						this.createProfessor(val,key);
						break;
					case "disciplina":
						Integer val2 = Integer.valueOf(options.get("val2"));
						this.createDisciplina(val,key, val2);
						break;
					default:
						break;
					}
					break;
			case "update":
				switch(base) {
				case "aluno":
					this.updateAluno(val,key);
					break;
				case "professor":
					this.updateProfessor(val,key);
					break;
				case "disciplina":
					Integer val2 = Integer.valueOf(options.get("val2"));
					this.updateDisciplina(val,key, val2);
					break;
				default:
					break;
				}
				break;
			case "delete":
				switch(base) {
				case "aluno":
					this.deleteAluno(key);
					break;
				case "professor":
					this.deleteProfessor(key);
					break;
				case "disciplina":
					this.deleteDisciplina(key);
					break;
				default:
					break;
				}
				break;
			case "get":
				switch(base) {
				case "aluno":
					this.readAluno(key);
					break;
				case "professor":
					this.readProfessor(key);
					break;
				case "disciplina":
					this.readDisciplina(key);
					break;
				default:
					break;
				}
				break;
			case "getall":
				switch(base) {
				case "aluno":
					this.readAllAlunos();
					break;
				case "professor":
					this.readAllProfessores();
					break;
				case "disciplina":
					this.readAllDisciplinas();
					break;
				default:
					break;
				}
				break;
			default:
				System.out.println("Digite uma opção válida");
				System.out.println("create  update  delete  get  getall");
				break;
			}
	  }
	  
	}
