import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class LevelDBTeste {

	public static void main(String[] args) {
		
		  File databaseDir = new File("src\\main\\resources\\leveldb\\"+args[0]);

			// Configurações para abrir o banco de dados
			Options options = new Options();
			options.createIfMissing(true);

			// Cria o banco de dados
			DBFactory factory = new Iq80DBFactory();
			try {
				DB db = factory.open(databaseDir, options);
				byte[] b = db.get("testeChave".getBytes());
				System.out.println(new String(b));
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

}
