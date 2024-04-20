package br.ufu.facom.gbc074.projeto.teste;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class LevelDBTeste {

	public static void main(String[] args) {
		  File databaseDir = new File("src\\main\\resources\\leveldb\\1-0");

			// Configurações para abrir o banco de dados
			Options options = new Options();
			options.createIfMissing(true);

			// Cria o banco de dados
			DBFactory factory = new Iq80DBFactory();
			try {
				DB db = factory.open(databaseDir, options);
	            // Prefixo do subbanco
	            String prefixo = "alunos:";

	            // Iterar sobre as chaves e calcular o tamanho do subbanco
	            int tamanhoSubbanco = 0;
	            try (DBIterator iterator = db.iterator()) {
	                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
	                    Map.Entry<byte[], byte[]> entry = iterator.peekNext();
	                    String chave = new String(entry.getKey(), StandardCharsets.UTF_8);
	                    if (chave.startsWith(prefixo)) {
	                        tamanhoSubbanco ++;
	                    }
	                }
	            }
	            System.out.println(tamanhoSubbanco);
	            
	            
	            // Prefixo do subbanco
	            prefixo = "alunos:";

	            // Lista para armazenar chaves e valores
	            List<Map.Entry<String, String>> entries = new ArrayList<>();

	            // Iterar sobre as chaves e adicionar à lista
	            try (DBIterator iterator = db.iterator()) {
	                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
	                    Map.Entry<byte[], byte[]> entry = iterator.peekNext();
	                    String chave = new String(entry.getKey(), StandardCharsets.UTF_8);
	                    if (chave.startsWith(prefixo)) {
	                        String valor = new String(entry.getValue(), StandardCharsets.UTF_8);
	                        entries.add(Map.entry(chave, valor));
	                    }
	                }
	            }
	            String b = entries.toString().substring(1, entries.toString().length() -1);
	            System.out.println(b);
	            // Exibir as chaves e valores
	            for (Map.Entry<String, String> entry : entries) {
	                System.out.println("Chave: " + entry.getKey() + ", Valor: " + entry.getValue());
	            }
				
				byte[] ba = db.get(("alunos:aaaa").getBytes());
				db.put(("alunos:aaaa").getBytes(), "dedo".getBytes());
				System.out.println("eh null?");
				System.out.println(new String(ba));
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

}
