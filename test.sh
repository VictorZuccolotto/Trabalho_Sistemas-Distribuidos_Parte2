# cria alguns alunos
bash admin-client.sh --port 9001 --base aluno --op create --key aaaa --val joao #
bash admin-client.sh --port 9001 --base aluno --op create --key bbbb --val maria #
bash admin-client.sh --port 9001 --base aluno --op create --key cccc --val jose #
#
#cria alguns professores
bash admin-client.sh --port 9001 --base professor --op create --key 1111 --val paulo #
bash admin-client.sh --port 9001 --base professor --op create --key 2222 --val pedro #
bash admin-client.sh --port 9001 --base professor --op create --key 3333 --val rafael #
#
#cria alguns disciplinas com nome e numero de vagas
bash admin-client.sh --port 9001 --base disciplina --op create --key gbc001 --val materia1 1 #
bash admin-client.sh --port 9001 --base disciplina --op create --key gbc002 --val materia2 2 #
bash admin-client.sh --port 9001 --base disciplina --op create --key gbc003 --val materia3 3 #
#
#lista professores em outro portal administrativo
bash admin-client.sh --port 9002 --base professor --op getall #
#
#edita professor em outro portal administrativo
bash admin-client.sh --port 9002 --base professor --op update --key 3333 --val luis #
#
#lista professores no primeiro portal administrativo
bash admin-client.sh --port 9001 --base professor --op getall #
#
#adiciona professores a disciplinas no portal de matriculas
bash mat-client.sh --port 8000 --op add_prof --val gbc001 1111 #
bash mat-client.sh --port 8000 --op add_prof --val gbc002 2222 #
bash mat-client.sh --port 8000 --op add_prof --val gbc003 3333 #
#
#adiciona alunos a disciplinas no portal de matriculas
bash mat-client.sh --port 8000 --op add_aluno --val gbc001 aaaa #
bash mat-client.sh --port 8000 --op add_aluno --val gbc001 bbbb # excede limite da disciplina
bash mat-client.sh --port 8000 --op add_aluno --val gbc002 aaaa #
bash mat-client.sh --port 8000 --op add_aluno --val gbc002 bbbb #
bash mat-client.sh --port 8000 --op add_aluno --val gbc003 aaaa #
bash mat-client.sh --port 8000 --op add_aluno --val gbc003 bbbb #
bash mat-client.sh --port 8000 --op add_aluno --val gbc003 cccc #
#
#imprime relatorios no portal de matriculas
bash mat-client.sh --port 8000 --op rel_disc --val gbc001 #
bash mat-client.sh --port 8000 --op rel_disc --val gbc002 #
bash mat-client.sh --port 8000 --op rel_disc --val gbc003 #
bash mat-client.sh --port 8000 --op rel_prof --val 1111 #
bash mat-client.sh --port 8000 --op rel_prof --val 2222 #
bash mat-client.sh --port 8000 --op rel_prof --val 3333 #
bash mat-client.sh --port 8000 --op rel_aluno --val aaaa #
bash mat-client.sh --port 8000 --op rel_aluno --val bbbb #
bash mat-client.sh --port 8000 --op rel_aluno --val cccc #
#
#remove 1 aluno e 1 professor no portal administrativo
bash admin-client.sh --port 9001 --base aluno --op delete --key bbbb #
bash admin-client.sh --port 9001 --base professor --op delete  --key 2222 #
#
#imprime novamente relatorios em outro portal de matriculas
bash mat-client.sh --port 8001 --op rel_disc --val gbc001 #
bash mat-client.sh --port 8001 --op rel_disc --val gbc002 #
bash mat-client.sh --port 8001 --op rel_disc --val gbc003 #
bash mat-client.sh --port 8001 --op rel_prof --val 1111 #
bash mat-client.sh --port 8001 --op rel_prof --val 2222 #
bash mat-client.sh --port 8001 --op rel_prof --val 3333 #
bash mat-client.sh --port 8001 --op rel_aluno --val aaaa #
bash mat-client.sh --port 8001 --op rel_aluno --val bbbb #
bash mat-client.sh --port 8001 --op rel_aluno --val cccc #