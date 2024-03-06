package br.ufu.facom.gbc074.projeto.matricula;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;


public class PortalMatriculaClient {
	  private static final Logger logger = Logger.getLogger(PortalMatriculaClient.class.getName());

	  private final PortalMatriculaGrpc.PortalMatriculaBlockingStub blockingStub;
	  

	  public PortalMatriculaClient(Channel channel) {
	    // Passing Channels to code makes code easier to test and makes it easier to
	    // reuse Channels.
	    blockingStub = PortalMatriculaGrpc.newBlockingStub(channel);
	  }

	  public void addProfessor(String disciplinaID, String pessoaID) {
	    logger.info("Adicionando professor "+ "<"+ pessoaID+"> a disciplina " + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.adicionaProfessor(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void removeProfessor(String disciplinaID, String pessoaID) {
	    logger.info("Removendo professor "+ "<"+ pessoaID+"> da disciplina " + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.removeProfessor(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void addAluno(String disciplinaID, String pessoaID) {
	    logger.info("Adicionando aluno "+ "<"+ pessoaID +"> a disciplina " + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.adicionaAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void removeAluno(String disciplinaID, String pessoaID) {
	    logger.info("Removendo aluno "+ "<"+ pessoaID+"> a disciplina " + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.removeAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void detalhaDisciplina(String id) {
		  logger.info("Detalhando disciplina "+ id);
		  Identificador request = Identificador.newBuilder().setId(id).build();
		  RelatorioDisciplina response;
		  try {
			  response = blockingStub.detalhaDisciplina(request);
			  System.out.println(response);
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }

	  public void obtemDisciplinasProfessor(String id) {
		  logger.info("Obtendo disciplinas do professor "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Iterator<RelatorioDisciplina> response;
		  try {
			  response = blockingStub.obtemDisciplinasProfessor(request);
			  System.out.print("[");
			  while(response.hasNext()) {
				  System.out.print(response.next());
				  if(response.hasNext()) {
					  System.out.print(", ");
				  }
					  
			  }
			  System.out.println("]");
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }
	  
	  public void obtemDisciplinasAluno(String id) {
		  logger.info("Obtendo disciplinas do aluno "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Iterator<ResumoDisciplina> response;
		  try {
			  response = blockingStub.obtemDisciplinasAluno(request);
			  System.out.print("[");
			  while(response.hasNext()) {
				  System.out.print(response.next());
				  if(response.hasNext()) {
					  System.out.print(", ");
				  }
					  
			  }
			  System.out.println("]");
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	  }


	  public static void main(String[] args) throws Exception {
	        if (args.length < 6 && args.length > 8) {
	            System.out.println("Usage: --port <port> --op <op> --val <val1> [<val2>]");
	            return;
	        }

	        String port = null;
	        String op = null;
	        String val1 = null;
	        String val2 = null;
	        HashMap<String, String> options = new HashMap<String, String>();
	        
	        for (int i = 0; i < args.length; i += 2) {
	            switch (args[i]) {
	                case "--port":
	                    port = args[i + 1];
	                    break;
	                case "--op":
	                    op = args[i + 1];
	                    break;
	                case "--val":
	                    val1 = args[i + 1];
	                    if (i + 2 < args.length) {
	                        val2 = args[i + 2];
	                    	i++;
	                    }
	                    break;
	                default:
	                    System.out.println("Invalid argument: " + args[i]);
	                    return;
	            }
	        }
	        
	        if (port == null || op == null || val1 == null) {
	            System.out.println("Missing required argument.");
	            return;
	        }
	        
	        if(val2 == null && (op.contains("add") || op.contains("del"))) {
	        	System.out.println("Precisa de mais argumentos para essa operação");
	        	return;
	        }

	        options.put("op", op);
	        options.put("val1", val1);
	        options.put("val2", val2);
	        
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
	    	PortalMatriculaClient client = new PortalMatriculaClient(channel);
    		client.executaPortal(options);
	    } finally {
	      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	    }
	  }
	  
	  
	  
	  public void executaPortal(HashMap<String,String> options) {
		  String opcao = options.get("op");
		  String val1 = options.get("val1");
		  String val2 = options.get("val2");
		  switch (opcao) {
			case "add_prof":
				this.addProfessor(val1,val2);
				break;
			case "add_aluno":
				this.addAluno(val1,val2);
				break;
			case "del_prof":
				this.removeProfessor(val1,val2);
				break;
			case "del_aluno":
				this.removeAluno(val1,val2);
				break;
			case "rel_disc":
				this.detalhaDisciplina(val1);
				break;
			case "rel_prof":
				this.obtemDisciplinasProfessor(val1);
				break;
			case "rel_aluno":
				this.obtemDisciplinasAluno(val1);
				break;
			default:
				System.out.println("Digite uma opção válida");
				System.out.println("add_prof  add_aluno  del_prof  del_aluno  rel_disc  rel_prof  rel_aluno");
				break;
			}
	  }
	  
	}
