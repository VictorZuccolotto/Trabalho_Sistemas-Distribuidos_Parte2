Trabalho Sistemas distribuidos
==============================================
O intuito do trabalho é a criação de dois servidores, um matricula e um administrativo.
E dois clientes, um para o servidor matricula e outro para o servidor administrativo.
A comunicação entre cliente e servidor será via gRPC e a comunicação entre servidores será
via MQTT.

O servidor administrativo será responsavel pela criação de alunos, professores e disciplinas.
O servidor matricula será responsável pela alocaçao de alunos e professores em disciplinas.

Um maior detalhamento do projeto pode ser encontrado [aqui](https://paulo-coelho.github.io/ds_notes/projeto/).

## Requisitos

- Java
- Gradle
- Servidor MQTT em localhost na por 1883

## Instruções de compilação

1. Baixe o projeto do git

2. Com o terminal no diretório do projeto:
```
$ ./gradlew installDist
```
Isso criará os arquivos necessários para executar os clientes e servidores

## Executando servidores

### Servidor administrativo
```
$ bash admin-server.sh <port>
```
Defina a porta que será utilizada pelo servidor, caso vazio será 50051

### Servidor matricula
```
$ bash mat-server.sh <port>
```
Defina a porta que será utilizada pelo servidor, caso vazio será 50052

## Executando clientes

### Cliente administrativo
```
$ bash admin-client.sh --port <port> --base <base> --op <operacao> --key <chave> --val <valor> [<valor2>]
```
- port: Porta que se conectará no servidor 0 a 65535
- base: aluno, professor, disciplina
- operacao: create, update, delete, get, getall
- chave: matricula, siape, sigla
- valor: nome de aluno ou professor ou disciplina
- valor2: numero de vagas da disciplina

#### Exemplos
Cria um professor de siape 1111 e nome paulo
```
$ bash admin-client.sh --port 9001 --base professor --op create --key 1111 --val paulo
```

Atualiza um professor de siape 1111 para o nome luis
```
$ bash admin-client.sh --port 9001 --base professor --op update --key 1111 --val luis
```

Deleta um aluno de matricula aaaa
```
$ bash admin-client.sh --port 9001 --base aluno --op delete --key aaaa --val luis
```

Obtem informações de disciplina gbc001
```
$ bash admin-client.sh --port 9001 --base disciplina --op get --key gbc001
```

Obtem informações de  todas as disciplinaa
```
$ bash admin-client.sh --port 9002 --base disciplina --op getall
```

Cria uma disciplina de sigla gbc001, nome materia1 com 1 vaga para alunos
```
$ bash admin-client.sh --port 9001 --base disciplina --op create --key gbc001 --val materia1 1
```

### Cliente matricula
```
$ bash mat-cliente.sh --port <port> --op <operacao> --val <valor> [<valor2>]
```
- port: Porta que se conectará no servidor 0 a 65535
- operacao: add_prof, add_aluno, del_prof, del_aluno, rel_disc, rel_prof, rel_aluno
- valor: matricula, siape, sigla
- valor2: sigla

#### Exemplos
Adiciona professor de siape 1111 á disciplina gbc001
```
$ bash mat-client.sh --port 8000 --op add_prof --val gbc001 1111
```

Remove aluno de matricula aaaa da disciplina gbc001
```
$ bash mat-client.sh --port 8000 --op add_prof --val gbc001 aaaa
```

Obtem informações de disciplina gbc001 sobre professor e alunos
```
$ bash mat-client.sh --port 8000 --op rel_disc --val gbc001
```

Obtem informações de todas as disciplinas que o professor de siape 1111 participa
```
$ bash mat-client.sh --port 8001 --op rel_prof --val 1111
```

Obtem detalhess de todas as disciplinas que o aluno de matricula aaaa participa
```
$ bash mat-client.sh --port 8001 --op rel_aluno --val aaaa
```

### Testando
```
$ bash admin-server.sh 9001
```

```
$ bash admin-server.sh 9002
```

```
$ bash mat-server.sh 8000
```

```
$ bash mat-server.sh 8001
```

```
$ bash test.sh
```
Faz uma sequência de criações de aluno, professores e disciplinas, alocando em disciplinas, listando e apagando-os
