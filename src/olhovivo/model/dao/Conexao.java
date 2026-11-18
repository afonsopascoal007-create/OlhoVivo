package olhovivo.model.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de ligacoes JDBC (passos 1 a 3 da aula de JDBC: carregar o driver,
 * definir o URL e estabelecer a ligacao). Le a configuracao de db.properties.
 */
public final class Conexao {
    private static final Properties CFG = new Properties();
    private static String url, utilizador, senha;

    static {
        try (FileInputStream in = new FileInputStream("db.properties")) {
            CFG.load(in);
        } catch (IOException e) {
            System.err.println("Aviso: db.properties nao encontrado; a usar valores por omissao.");
        }
        utilizador = CFG.getProperty("user", "root");
        senha = CFG.getProperty("password", "");
        url = CFG.getProperty("url", "").trim();
        boolean mariadb = existe("org.mariadb.jdbc.Driver");
        boolean mysql = existe("com.mysql.cj.jdbc.Driver") || existe("com.mysql.jdbc.Driver");
        if (url.isEmpty()) {
            String host = CFG.getProperty("host", "localhost");
            String porta = CFG.getProperty("port", "3306");
            String bd = CFG.getProperty("database", "olho_vivo");
            if (mariadb) url = "jdbc:mariadb://" + host + ":" + porta + "/" + bd;
            else url = "jdbc:mysql://" + host + ":" + porta + "/" + bd
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
        if (!mariadb && !mysql)
            System.err.println("Aviso: nenhum driver JDBC encontrado na pasta lib/ (mariadb-java-client ou mysql-connector-j).");
    }

    private Conexao() { }

    private static boolean existe(String classe) {
        try { Class.forName(classe); return true; } catch (ClassNotFoundException e) { return false; }
    }

    /** Devolve uma nova ligacao; quem a pede deve fecha-la (try-with-resources). */
    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(url, utilizador, senha);
    }

    public static String getUrl() { return url; }
}
