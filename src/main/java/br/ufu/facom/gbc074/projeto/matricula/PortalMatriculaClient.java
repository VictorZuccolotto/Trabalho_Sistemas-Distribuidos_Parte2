package br.ufu.facom.gbc074.projeto.matricula;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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
	  
	  private int mutex = 0;

	  public PortalMatriculaClient(Channel channel) {
	    // Passing Channels to code makes code easier to test and makes it easier to
	    // reuse Channels.
	    blockingStub = PortalMatriculaGrpc.newBlockingStub(channel);
	  }

	  public void addProfessor(String disciplinaID, String pessoaID) {
		mutex = 1;
	    logger.info("Adicionando professor "+ "<"+ pessoaID+"> a disciplina" + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.adicionaProfessor(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	    mutex = 0;
	  }
	  
	  public void removeProfessor(String disciplinaID, String pessoaID) {
		mutex = 1;
	    logger.info("Removendo professor "+ "<"+ pessoaID+"> da disciplina" + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.removeProfessor(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	    mutex = 0;
	  }
	  
	  public void addAluno(String disciplinaID, String pessoaID) {
		mutex = 1;
	    logger.info("Adicionando aluno "+ "<"+ pessoaID+"> a disciplina" + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.adicionaAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	    mutex = 0;
	  }
	  
	  public void removeAluno(String disciplinaID, String pessoaID) {
		mutex = 1;
	    logger.info("Removendo aluno "+ "<"+ pessoaID+"> a disciplina" + disciplinaID);
	    DisciplinaPessoa request= DisciplinaPessoa.newBuilder().setDisciplina(disciplinaID).setIdPessoa(pessoaID).build();
	    Status response;
	    try {
	      response = blockingStub.removeAluno(request);
	    } catch (StatusRuntimeException e) {
	      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
	      return;
	    }
	    logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
	    mutex = 0;
	  }
	  
	  public void detalhaDisciplina(String id) {
		  mutex = 1;
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
		  mutex = 0;
	  }

	  public void obtemDisciplinasProfessor(String id) {
		  mutex = 1;
		  logger.info("Obtendo disciplinas do professor "+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Iterator<RelatorioDisciplina> response;
		  try {
			  response = blockingStub.obtemDisciplinasProfessor(request);
			  while(response.hasNext()) {
				  System.out.println(response.next());
			  }
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
		  mutex = 0;
	  }
	  
	  public void obtemDisciplinasAluno(String id) {
		  mutex = 1;
		  logger.info("Obtendo disciplinas do aluno"+ "<"+ id+"> ");
		  Identificador request= Identificador.newBuilder().setId(id).build();
		  Iterator<ResumoDisciplina> response;
		  try {
			  response = blockingStub.obtemDisciplinasAluno(request);
			  while(response.hasNext()) {
				  System.out.println(response.next());
			  }
		  } catch (StatusRuntimeException e) {
			  logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			  return;
		  }
//		  logger.info("Status: " + "Code= "+ response.getStatus() + " Msg= "+ response.getMsg() );
		  mutex = 0;
	  }


	  public static void main(String[] args) throws Exception {
	    String user = "Dedin";
	    List<String> users = Arrays.asList("world", "sistemas", "distribuidos", "paulo");
	    String target = "localhost:50052";

	    if (args.length > 0) {
	      if ("--help".equals(args[0])) {
	        System.err.println("Usage: [name name name name]");
	        System.err.println("");
	        System.err.println("  name    The name(s) you wish to be greeted by. Defaults to " + user + " and " + users);
	        System.exit(1);
	      }
	      user = args[0];
	      users = Arrays.asList(args);
	    }

	    // Create a communication channel to the server, known as a Channel.
	    // Channels are thread-safe and reusable.
	    // It is common to create channels at the beginning of your application and
	    // reuse
	    // them until the application shuts down.
	    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
	        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
	        // to avoid needing certificates.
	        .usePlaintext().build();
	    Scanner scan = new Scanner(System.in);
	    try {
	    	PortalMatriculaClient client = new PortalMatriculaClient(channel);
	    	int opcao = 0;
	    	do {
	    		client.printpainelCliente();
	    		opcao = scan.nextInt();
	    		client.executaPortal(opcao,scan);
	    	}while(opcao != 0);
	      logger.info("Finalizando painel");
	    } finally {
	    	scan.close();
	      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	    }
	  }
	  
	  
	  
	  public void executaPortal(int opcao,Scanner scan) {
		  switch (opcao) {
		case 0:
			break;
		case 1:
			System.out.println("Digite o siape do professor:");
			String siape = scan.next();
			System.out.println("Digite a sigla da disciplina:");
			String sigla = scan.next();
			this.addProfessor(siape, sigla);
			while(mutex != 0) {}
			break;
		case 2:
			System.out.println("Digite o siape do professor:");
			siape = scan.next();
			System.out.println("Digite a sigla da disciplina:");
			sigla = scan.next();
			this.removeProfessor(siape,sigla);
			while(mutex != 0) {}
			break;
		case 3:
			System.out.println("Digite a matricula do aluno:");
			String matricula = scan.next();
			System.out.println("Digite a sigla da disciplina:");
			sigla = scan.next();
			this.addProfessor(matricula, sigla);
			while(mutex != 0) {}
			break;
		case 4:
			System.out.println("Digite a matricula do aluno:");
			matricula = scan.next();
			System.out.println("Digite a sigla da disciplina:");
			sigla = scan.next();
			this.removeProfessor(matricula,sigla);
			while(mutex != 0) {}
			break;
		case 5:
			System.out.println("Digite a sigla da disciplina:");
			sigla = scan.next();
			this.detalhaDisciplina(sigla);
			while(mutex != 0) {}
			break;
		case 6:
			System.out.println("Digite o siape do professor:");
			sigla = scan.next();
			this.obtemDisciplinasProfessor(sigla);
			while(mutex != 0) {}
			break;
		case 7:
			System.out.println("Digite a matricula do aluno:");
			sigla = scan.next();
			this.obtemDisciplinasAluno(sigla);
			while(mutex != 0) {}
			break;
		default:
			System.out.println("Digite uma opção válida");
			break;
		}
	  }
	  
	  public void printpainelCliente() {
		  System.out.println("==========Painel Cliente==========");
		  System.out.println("[1]  Adiciona professor a disciplina");
		  System.out.println("[2]  Remove professor de disciplina");
		  System.out.println("[3]  Adiciona aluno a disciplina");
		  System.out.println("[4]  Remove aluno de disciplina");
		  System.out.println("[5]  Detalha disciplina");
		  System.out.println("[6]  Obtem disciplinas de professor");
		  System.out.println("[7]  Obtem disciplinas de aluno");
		  System.out.println("[0]  Sair");
		  System.out.println("================//================");
	  }
	}
