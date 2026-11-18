package olhovivo.util;

/** Excepcao de regra de negocio ou de base de dados, com mensagem pronta a mostrar ao utilizador. */
public class NegocioException extends Exception {
    public NegocioException(String msg) { super(msg); }
    public NegocioException(String msg, Throwable causa) { super(msg, causa); }

    /** Traduz SQLException: SIGNAL dos triggers (45000) mostra a mensagem; o resto e generico. */
    public static NegocioException de(java.sql.SQLException e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if ("45000".equals(e.getSQLState())) return new NegocioException(m.replaceFirst("^\\(conn=\\d+\\)\\s*", ""), e);
        if (e.getErrorCode() == 1062) return new NegocioException("Ja existe um registo com esse valor (contacto, email, nome ou matricula).", e);
        if (e.getErrorCode() == 1451) return new NegocioException("Nao e possivel apagar: existem registos dependentes.", e);
        if (e.getErrorCode() == 3819 || e.getErrorCode() == 4025) return new NegocioException("Valor rejeitado por uma restricao CHECK da base de dados.", e);
        if (e.getSQLState() != null && e.getSQLState().startsWith("08"))
            return new NegocioException("Sem ligacao a base de dados. Verifique se o MySQL/MariaDB esta iniciado e o db.properties.", e);
        if (e.getErrorCode() == 1049) return new NegocioException("A base de dados olho_vivo nao existe. Execute primeiro sql/olho_vivo.sql.", e);
        if (e.getErrorCode() == 1045) return new NegocioException("Utilizador ou palavra-passe da base de dados incorrectos (db.properties).", e);
        return new NegocioException("Erro de base de dados: " + m, e);
    }
}
