package br.ufu.facom.gbc074.projeto.cadastro;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import br.ufu.facom.gbc074.projeto.bd.Banco;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class PortalAdministrativoServer {
	  private static final Logger logger = Logger.getLogger(PortalAdministrativoServer.class.getName());

	  private Server server;

	  private void start() throws IOException {
	    /* The port on which the server should run */
	    int port = 50051;
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
	    final PortalAdministrativoServer server = new PortalAdministrativoServer();
	    server.start();
	    server.blockUntilShutdown();
	  }

	  static class PortalAdministrativoImpl extends PortalAdministrativoGrpc.PortalAdministrativoImplBase {
		  
	    @Override
	    public void novoAluno(Aluno req, StreamObserver<Status> responseObserver) {
			String nome = req.getNome();
			String matricula = req.getMatricula();
			int code;
			String errorMsg = "";
			//Validacao
			 if(Banco.alunos.containsKey(matricula)){ //Se aluno existe
			 	code = 1;
			 	errorMsg = "Aluno já cadastrado";
			 } else 
			if(nome.length() > 4 && matricula.length() > 4){ //Caso contrario e tenha nome e matricula maior que 4
				//Salvar no bd
				Banco.alunos.put(matricula,nome);
				code = 0;
			}else{
				code = 1;
				errorMsg = "Nome ou matricula menor que 4";
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
//	      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void editaAluno(Aluno req, StreamObserver<Status> responseObserver) {
			String nome = req.getNome();
			String matricula = req.getMatricula();
			int code;
			String errorMsg = "";
			//Validacao
			 if(!Banco.alunos.containsKey(matricula)){ //Se aluno não existe
			 	code = 1;
			 	errorMsg = "Aluno não cadastrado";
			 } else 
			if(nome.length() > 4){ //Caso contrario e tenha nome e matricula maior que 4
				//Salvar no bd
				Banco.alunos.put(matricula,nome);
				code = 0;
			}else{
				code = 1;
				errorMsg = "Nome menor que 4";
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
//	      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }

	    @Override
	    public void removeAluno(Identificador req, StreamObserver<Status> responseObserver) {
			String matricula = req.getId();
			int code;
			String errorMsg = "";
			//Validacao
			 if(!Banco.alunos.containsKey(matricula)){ //Se aluno não existe
			 	code = 1;
			 	errorMsg = "Aluno não cadastrado";
			}else{
				Banco.alunos.remove(matricula);
				code = 0;
				errorMsg = "";
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			responseObserver.onNext(status);
			responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void obtemAluno(Identificador req, StreamObserver<Aluno> responseObserver) {
			String matricula = req.getId();
			Aluno alunoResponse;
			//Validacao
			 if(!Banco.alunos.containsKey(matricula)){ //Se aluno não existe
				 alunoResponse = Aluno.newBuilder().setMatricula(" ").setNome(" ").build();
			}else{
				alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(Banco.alunos.get(matricula)).build();
			}
			responseObserver.onNext(alunoResponse);
	    	responseObserver.onCompleted();
	    }
	    @Override
	    public void obtemTodosAlunos(Vazia req, StreamObserver<Aluno> responseObserver) {
	    	if(Banco.alunos.size() == 0) {
	    		Aluno alunoResponse = Aluno.newBuilder().setMatricula(" ").setNome(" ").build();
	    		responseObserver.onNext(alunoResponse);
	    	}else {
		        for (String matricula : Banco.alunos.keySet()) {
		            Aluno alunoResponse = Aluno.newBuilder().setMatricula(matricula).setNome(Banco.alunos.get(matricula)).build();
		            responseObserver.onNext(alunoResponse);
		          }
	    	}
		      responseObserver.onCompleted();
	    }
	  }
	}

