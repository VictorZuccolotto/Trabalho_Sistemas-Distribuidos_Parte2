 package br.ufu.facom.gbc074.projeto.matricula;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import br.ufu.facom.gbc074.projeto.bd.Banco;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class PortalMatriculaServer {
	  private static final Logger logger = Logger.getLogger(PortalMatriculaServer.class.getName());

	  private Server server;

	  private void start() throws IOException {
	    /* The port on which the server should run */
	    int port = 50052;
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
	    final PortalMatriculaServer server = new PortalMatriculaServer();
	    server.start();
	    server.blockUntilShutdown();
	  }

	  static class PortalMatriculaImpl extends PortalMatriculaGrpc.PortalMatriculaImplBase {
		  
	    @Override
	    public void adicionaProfessor(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
			String professorID = req.getIdPessoa();
			String disciplinaID = req.getDisciplina();
			int code;
			String errorMsg = "";
			//Validacao
			 if(!Banco.professores.containsKey(professorID)){ //Se professor nao existe
			 	code = 1;
			 	errorMsg = "Professor não cadastrado";
			 }else if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
			 	code = 1;
			 	errorMsg = "Disciplina não cadastrada";
			 }else if(Banco.disciplinaProfessor.containsKey(disciplinaID)){ //Caso disciplina já tenha um professor
				 code = 1;
				 errorMsg = "Disciplina já possui um professor associado";
			}else{
				code = 0;
				Banco.disciplinaProfessor.put(disciplinaID,professorID);
				Banco.professorDisciplinas.get(professorID).add(disciplinaID);
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }

	    @Override
	    public void removeProfessor(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
			String professorID = req.getIdPessoa();
			String disciplinaID = req.getDisciplina();
			int code;
			String errorMsg = "";
			//Validacao
			 if(!Banco.professores.containsKey(professorID)){ //Se professor nao existe
			 	code = 1;
			 	errorMsg = "Professor não cadastrado";
			 }else if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
			 	code = 1;
			 	errorMsg = "Disciplina não cadastrada";
			 }else if(!Banco.disciplinaProfessor.containsKey(disciplinaID)){ //Caso disciplina não tenha um professor
				 code = 1;
				 errorMsg = "Disciplina não possui um professor";
			 }else if(!Banco.disciplinaProfessor.get(disciplinaID).equals(professorID)){
				 code = 1;
				 errorMsg = "Este professor não está associado a esta disciplina";
			 }else{
				code = 0;
				Banco.disciplinaProfessor.put(disciplinaID,professorID);
				Banco.professorDisciplinas.get(professorID).remove(disciplinaID);
			}
			Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	      responseObserver.onNext(status);
	      responseObserver.onCompleted();
	    }
	    @Override
	    public void adicionaAluno(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
	    	String alunoID = req.getIdPessoa();
	    	String disciplinaID = req.getDisciplina();
	    	int code;
	    	String errorMsg = "";
	    	//Validacao
	    	if(!Banco.alunos.containsKey(alunoID)){ //Se alunoo nao existe
	    		code = 1;
	    		errorMsg = "Aluno não cadastrado";
	    	}else if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
	    		code = 1;
	    		errorMsg = "Disciplina não cadastrada";
	    	}else if(Banco.disciplinaAlunos.get(disciplinaID).contains(alunoID)){ //aluno ja faz parte da disciplina?
	    		code = 1;
	    		errorMsg = "Aluno já matriculado na disciplina";
	    	}else if(!(Banco.disciplinaAlunos.get(disciplinaID).size() < Banco.disciplinas.get(disciplinaID).getVagas())) { // Se cabe mais aluno na disciplina
	    		code = 1;
	    		errorMsg = "Disciplina está com as vagas esgotadas";	    	
	    	}else{
	    		code = 0;
	    		Banco.disciplinaAlunos.get(disciplinaID).add(alunoID);
	    		Banco.alunoDisciplinas.get(alunoID).add(disciplinaID);
	    	}
	    	Status status = Status.newBuilder().setStatus(code).setMsg(errorMsg).build();
	    	responseObserver.onNext(status);
	    	responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void removeAluno(DisciplinaPessoa req, StreamObserver<Status> responseObserver) {
	    	String alunoID = req.getIdPessoa();
	    	String disciplinaID = req.getDisciplina();
	    	int code;
	    	String errorMsg = "";
	    	//Validacao
	    	if(!Banco.alunos.containsKey(alunoID)){ //Se aluno nao existe
	    		code = 1;
	    		errorMsg = "Aluno não cadastrado";
	    	}else if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
	    		code = 1;
	    		errorMsg = "Disciplina não cadastrada";
	    	}else if(!Banco.disciplinaAlunos.get(disciplinaID).contains(alunoID)){ //aluno nao faz parte da disciplina
	    		code = 1;
	    		errorMsg = "Aluno não matriculado na disciplina";
	    	}else{
	    		code = 0;
	    		Banco.disciplinaAlunos.get(disciplinaID).remove(alunoID);
	    		Banco.alunoDisciplinas.get(alunoID).remove(disciplinaID);
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
	    	
	    	if(!Banco.disciplinas.containsKey(disciplinaID)){ //Se disciplina nao existe
	    		disciplina = Disciplina.newBuilder().setSigla(" ").setNome(Banco.disciplinas.get(" ").getNome()).build();
	    		professor = Professor.newBuilder().setNome(" ").setSiape(" ").build();
	    		alunos.add(Aluno.newBuilder().setMatricula(" ").setNome(" ").build());
	    	}else {
	    		disciplina = Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).build();
	    		if(Banco.disciplinaAlunos.get(disciplinaID).size() > 0) {//Disciplina tem alunos
	    			for (String alunoID : Banco.disciplinaAlunos.get(disciplinaID)) {
	    				alunos.add(Aluno.newBuilder().setMatricula(alunoID).setNome(Banco.alunos.get(alunoID)).setNome(" ").build());
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
	    		RelatorioDisciplina response = RelatorioDisciplina.newBuilder().setDisciplina(disciplina).setProfessor(professor).addAllAlunos(alunos).build();
	    		responseObserver.onNext(response);
	    	}
	    	responseObserver.onCompleted();
	    }

	    @Override
	    public void obtemDisciplinasProfessor(Identificador req, StreamObserver<RelatorioDisciplina> responseObserver) {
	    	String professorID = req.getId();
	    	if(!Banco.professores.containsKey(professorID)) {//if professor naoo existe
	    		responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").build())
	    		.setAlunos(0, Aluno.newBuilder().setMatricula(" ").setNome(" "))
	    		.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build()).build());
	    	}else if(Banco.professorDisciplinas.get(professorID).size() == 0) {//professor nao participa de alguma disciplina
	    		responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").build())
	    		.setAlunos(0, Aluno.newBuilder().setMatricula(" ").setNome(" "))
	    		.setProfessor(Professor.newBuilder().setSiape(professorID).setNome(Banco.professores.get(professorID)).build()).build());
	    	}else {
	    		for (String disciplinaID : Banco.professorDisciplinas.get(professorID)) {
	    			ArrayList<Aluno> alunos = new ArrayList<Aluno>();
	    			if(Banco.disciplinaAlunos.get(disciplinaID).size() == 0) {
	    				alunos.add(Aluno.newBuilder().setMatricula(" ").setNome(" ").build());
	    			}else {
	    				for (String alunoID : Banco.disciplinaAlunos.get(disciplinaID)) {
	    					alunos.add(Aluno.newBuilder().setMatricula(alunoID).setNome(Banco.alunos.get(alunoID)).build());
	    				}
	    			}
	    			responseObserver.onNext(RelatorioDisciplina.newBuilder().setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).build())
	    															.addAllAlunos(alunos)
	    															.setProfessor(Professor.newBuilder().setSiape(professorID).setNome(Banco.professores.get(professorID)).build()).build());
	    		}
	    		responseObserver.onCompleted();
	    	}
	    }
	    
	    @Override
	    public void obtemDisciplinasAluno(Identificador req, StreamObserver<ResumoDisciplina> responseObserver) {
	    	String alunoID = req.getId();
	    	if(!Banco.alunos.containsKey(alunoID) || Banco.alunoDisciplinas.get(alunoID).size() == 0) {
				responseObserver.onNext(ResumoDisciplina.newBuilder()
				.setDisciplina(Disciplina.newBuilder().setSigla(" ").setNome(" ").build())
				.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build())
				.setTotalAlunos(0).build());
	    	}else {
	    		for (String disciplinaID : Banco.alunoDisciplinas.get(alunoID)) {
	    			if(!Banco.disciplinaProfessor.containsKey(disciplinaID)){//Disciplina nao tem professor
	    				responseObserver.onNext(ResumoDisciplina.newBuilder()
	    				.setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).build())
	    				.setProfessor(Professor.newBuilder().setSiape(Banco.disciplinaProfessor.get(disciplinaID)).setNome(Banco.professores.get(Banco.disciplinaProfessor.get(disciplinaID))).build())
	    				.setTotalAlunos(Banco.disciplinaAlunos.get(disciplinaID).size()).build());
	    			}else {
	    				responseObserver.onNext(ResumoDisciplina.newBuilder()
	    				.setDisciplina(Disciplina.newBuilder().setSigla(disciplinaID).setNome(Banco.disciplinas.get(disciplinaID).getNome()).build())
	    				.setProfessor(Professor.newBuilder().setSiape(" ").setNome(" ").build())
	    				.setTotalAlunos(Banco.disciplinaAlunos.get(disciplinaID).size()).build());
	    			}
				}
	    	}
	    
	  }
	 }
}

