# cria alguns alunos
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base aluno --op create --key aaaaa --val joaoa #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base aluno --op create --key bbbbb --val mariaa #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base aluno --op create --key ccccc --val josea #
#
#cria alguns professores
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base professor --op create --key 11111 --val paulo #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base professor --op create --key 22222 --val pedro #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base professor --op create --key 33333 --val rafael #
#
#cria alguns disciplinas com nome e numero de vagas
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base disciplina --op create --key gbc001 --val materia1 1 #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base disciplina --op create --key gbc002 --val materia2 2 #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base disciplina --op create --key gbc003 --val materia3 3 #
#
#lista professores em outro portal administrativo
bash build/install/TrabSd/bin/portal-administrativo-client --port 9002 --base professor --op getall #
#
#edita professor em outro portal administrativo
bash build/install/TrabSd/bin/portal-administrativo-client --port 9002 --base professor --op update --key 33333 --val luiss #
#
#lista professores no primeiro portal administrativo
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base professor --op getall #
#
#adiciona professores a disciplinas no portal de matriculas
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_prof --val gbc001 11111 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_prof --val gbc002 22222 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_prof --val gbc003 33333 #
#
#adiciona alunos a disciplinas no portal de matriculas
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc001 aaaaa #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc001 bbbbb # excede limite da disciplina
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc002 aaaaa #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc002 bbbbb #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc003 aaaaa #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc003 bbbbb #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op add_aluno --val gbc003 ccccc #
#
#imprime relatorios no portal de matriculas
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_disc --val gbc001 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_disc --val gbc002 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_disc --val gbc003 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_prof --val 11111 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_prof --val 22222 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_prof --val 33333 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_aluno --val aaaaa #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_aluno --val bbbbb #
bash build/install/TrabSd/bin/portal-matricula-client --port 8000 --op rel_aluno --val ccccc #
#
#remove 1 aluno e 1 professor no portal administrativo
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base aluno --op delete --key bbbbb #
bash build/install/TrabSd/bin/portal-administrativo-client --port 9001 --base professor --op delete  --key 22222 #
#
#imprime novamente relatorios em outro portal de matriculas
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_disc --val gbc001 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_disc --val gbc002 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_disc --val gbc003 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_prof --val 11111 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_prof --val 22222 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_prof --val 33333 #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_aluno --val aaaaa #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_aluno --val bbbbb #
bash build/install/TrabSd/bin/portal-matricula-client --port 8001 --op rel_aluno --val ccccc #