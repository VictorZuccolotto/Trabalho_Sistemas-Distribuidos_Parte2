package br.ufu.facom.gbc074.projeto.cadastro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import br.ufu.facom.gbc074.projeto.bd.Banco;
import br.ufu.facom.gbc074.projeto.bd.model.DisciplinaModel;
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
				Banco.alunoDisciplinas.put(matricula, new ArrayList<String>());
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
				for (String disciplinaID : Banco.alunoDisciplinas.get(matricula)) {
					Banco.disciplinaAlunos.get(disciplinaID).remove(matricula);
				}
				Banco.alunoDisciplinas.remove(matricula);
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
	    
		  @Override
		  public void novoProfessor(Professor req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  String siape = req.getSiape();
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(Banco.professores.containsKey(siape)){ //Se professor existe
				  code = 1;
				  errorMsg = "Professor já cadastrado";
			  } else 
				  if(nome.length() > 4 && siape.length() > 4){ //Caso contrario e tenha nome e siape maior que 4
					  //Salvar no bd
					  Banco.professores.put(siape,nome);
					  Banco.professorDisciplinas.put(siape, new ArrayList<String>());
					  code = 0;
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
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(!Banco.professores.containsKey(siape)){ //Se professor não existe
				  code = 1;
				  errorMsg = "Aluno não cadastrado";
			  } else 
				  if(nome.length() > 4){ //Caso contrario e tenha nome e matricula maior que 4
					  //Salvar no bd
					  Banco.professores.put(siape,nome);
					  code = 0;
				  }else{
					  code = 1;
					  errorMsg = "Nome menor que 4";
				  }
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void removeProfessor(Identificador req, StreamObserver<Status> responseObserver) {
			  String siape = req.getId();
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(!Banco.professores.containsKey(siape)){ //Se professor não existe
				  code = 1;
				  errorMsg = "Professor não cadastrado";
			  }else{
					for (String disciplinaID : Banco.professorDisciplinas.get(siape)) {
						Banco.disciplinaProfessor.remove(disciplinaID);
					}
					Banco.alunoDisciplinas.remove(siape);
					Banco.professores.remove(siape);
					code = 0;
					errorMsg = "";
			  }
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemProfessor(Identificador req, StreamObserver<Professor> responseObserver) {
			  String siape = req.getId();
			  Professor professorResponse;
			  //Validacao
			  if(!Banco.professores.containsKey(siape)){ //Se aluno não existe
				  professorResponse = Professor.newBuilder().setSiape(" ").setNome(" ").build();
			  }else{
				  professorResponse = Professor.newBuilder().setSiape(siape).setNome(Banco.professores.get(siape)).build();
			  }
			  responseObserver.onNext(professorResponse);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemTodosProfessores(Vazia req, StreamObserver<Professor> responseObserver) {
			  if(Banco.professores.size() == 0) {
				  Professor professorResponse = Professor.newBuilder().setSiape(" ").setNome(" ").build();
				  responseObserver.onNext(professorResponse);
			  }else {
				  for (String siape : Banco.professores.keySet()) {
					  Professor professorResponse = Professor.newBuilder().setSiape(siape).setNome(Banco.professores.get(siape)).build();
					  responseObserver.onNext(professorResponse);
				  }
			  }
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void novaDisciplina(Disciplina req, StreamObserver<Status> responseObserver) {
			  String nome = req.getNome();
			  int vagas = req.getVagas();
			  String sigla = req.getSigla();
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(Banco.disciplinas.containsKey(sigla)){ //Se disciplina existe
				  code = 1;
				  errorMsg = "Disciplina já cadastrado";
			  } else 
				  if(nome.length() > 4 && sigla.length() > 4){ //Caso contrario e tenha nome e sigla maior que 4
					  //Salvar no bd
					  Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
					  Banco.disciplinaAlunos.put(sigla, new ArrayList<String>());
					  code = 0;
				  }else{
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
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(!Banco.disciplinas.containsKey(sigla)){ //Se disciplina não existe
				  code = 1;
				  errorMsg = "Disciplina não cadastrado";
			  } else 
				  if(nome.length() > 4){ //Caso contrario e tenha nome e matricula maior que 4
					  //Salvar no bd
					  Banco.disciplinas.put(sigla,new DisciplinaModel(nome,vagas));
					  code = 0;
				  }else{
					  code = 1;
					  errorMsg = "Nome menor que 4";
				  }
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void removeDisciplina(Identificador req, StreamObserver<Status> responseObserver) {
			  String sigla = req.getId();
			  int code;
			  String errorMsg = "";
			  //Validacao
			  if(!Banco.disciplinas.containsKey(sigla)){ //Se disciplina não existe
				  code = 1;
				  errorMsg = "Disciplina não cadastrado";
			  }else{
					for (String alunosID : Banco.disciplinaAlunos.get(sigla)) {
						Banco.alunoDisciplinas.get(alunosID).remove(sigla);
					}
					Banco.professorDisciplinas.get(Banco.disciplinaProfessor.get(sigla)).remove(sigla);
					Banco.disciplinaProfessor.remove(sigla);
					Banco.disciplinas.remove(sigla);
				  code = 0;
				  errorMsg = "";
			  }
			  Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
			  responseObserver.onNext(status);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemDisciplina(Identificador req, StreamObserver<Disciplina> responseObserver) {
			  String sigla = req.getId();
			  Disciplina disciplinaResponse;
			  //Validacao
			  if(!Banco.disciplinas.containsKey(sigla)){ //Se aluno não existe
				  disciplinaResponse = Disciplina.newBuilder().setSigla(" ").setNome(" ").build();
			  }else{
				  DisciplinaModel disciplina = Banco.disciplinas.get(sigla);
				  disciplinaResponse = Disciplina.newBuilder().setSigla(sigla).setNome(disciplina.getNome()).setVagas(disciplina.getVagas()).build();
			  }
			  responseObserver.onNext(disciplinaResponse);
			  responseObserver.onCompleted();
		  }
		  
		  @Override
		  public void obtemTodasDisciplinas(Vazia req, StreamObserver<Disciplina> responseObserver) {
			  if(Banco.disciplinas.size() == 0) {
				  Disciplina disciplinaResponse = Disciplina.newBuilder().setSigla(" ").setNome(" ").build();
				  responseObserver.onNext(disciplinaResponse);
			  }else {
				  for (String sigla : Banco.disciplinas.keySet()) {
					  DisciplinaModel disciplina = Banco.disciplinas.get(sigla);
					  Disciplina professorResponse = Disciplina.newBuilder().setSigla(sigla).setNome(disciplina.getNome()).setVagas(disciplina.getVagas()).build();
					  responseObserver.onNext(professorResponse);
				  }
			  }
			  responseObserver.onCompleted();
		  }

	  }
	  
	}

