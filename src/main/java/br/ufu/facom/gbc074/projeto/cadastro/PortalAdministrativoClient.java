package br.ufu.facom.gbc074.projeto.cadastro;

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


public class PortalAdministrativoClient {
	  private static final Logger logger = Logger.getLogger(PortalAdministrativoClient.class.getName());

	  private final PortalAdministrativoGrpc.PortalAdministrativoBlockingStub blockingStub;
	  
	  private int mutex = 0;

	  public PortalAdministrativoClient(Channel channel) {
	    // Passing Channels to code makes code easier to test and makes it easier to
	    // reuse Channels.
	    blockingStub = PortalAdministrativoGrpc.newBlockingStub(channel);
	  }

	  public void createAluno(String nome, String matricula) {
		  mutex = 1;
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
	    mutex = 0;
	  }
	  
	  public void updateAluno(String nome, String matricula) {
		  mutex = 1;
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
	    mutex = 0;
	  }
	  
	  public void deleteAluno(String id) {
		  mutex = 1;
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
		  mutex = 0;
	  }
	  
	  public void readAluno(String id) {
		  mutex = 1;
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
		  mutex = 0;
	  }
	  
	  public void readAllAlunos() {
		  mutex = 1;
		  logger.info("Obtendo alunos");
		  Vazia request = Vazia.newBuilder().build();
		  Iterator<Aluno> response;
		  StringBuilder dados = null;
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
		  mutex = 0;
	  }


	  public static void main(String[] args) throws Exception {
	    String user = "Dedin";
	    String matricula = "11811BCC034";
	    List<String> users = Arrays.asList("world", "sistemas", "distribuidos", "paulo");
	    String target = "localhost:50051";

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
	    	PortalAdministrativoClient client = new PortalAdministrativoClient(channel);
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
			System.out.println("Digite o nome do aluno:");
			String nome = scan.next();
			System.out.println("Digite a matricula do aluno:");
			String matricula = scan.next();
			this.createAluno(nome,matricula);
			while(mutex != 0) {}
			break;
		case 2:
			System.out.println("Digite o nome do aluno:");
			nome = scan.next();
			System.out.println("Digite a matricula do aluno:");
			matricula = scan.next();
			this.updateAluno(nome,matricula);
			while(mutex != 0) {}
			break;
		case 3:
			System.out.println("Digite a matricula do aluno:");
			matricula = scan.next();
			this.deleteAluno(matricula);
			while(mutex != 0) {}
			break;
		case 4:
			System.out.println("Digite a matricula do aluno:");
			matricula = scan.next();
			this.readAluno(matricula);
			while(mutex != 0) {}
			break;
		case 5:
			this.readAllAlunos();
			break;
		default:
			System.out.println("Digite uma opção válida");
			break;
		}
	  }
	  
	  public void printpainelCliente() {
		  System.out.println("==========Painel Cliente==========");
		  System.out.println("[1]  Novo aluno");
		  System.out.println("[2]  Editar aluno");
		  System.out.println("[3]  Remover aluno");
		  System.out.println("[4]  Obter aluno");
		  System.out.println("[5]  Obter todos alunos");
		  System.out.println("[6]  Novo professor");
		  System.out.println("[7]  Editar professor");
		  System.out.println("[8]  Remover professor");
		  System.out.println("[9]  Obter professor");
		  System.out.println("[10] Obter todos professores");
		  System.out.println("[11] Nova disciplina");
		  System.out.println("[12] Editar disciplina");
		  System.out.println("[13] Remover disciplina");
		  System.out.println("[14] Obter disciplina");
		  System.out.println("[15] Obter todas disciplinas");
		  System.out.println("[0]  Sair");
		  System.out.println("================//================");
	  }
	}
