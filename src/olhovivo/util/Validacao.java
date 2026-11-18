package olhovivo.util;

/** Validacoes e normalizacoes usadas pelos objectos de negocio. */
public final class Validacao {
    private Validacao() { }

    /** 840000001 ou 258840000001 passam a +258840000001. */
    public static String normalizarContacto(String c) {
        if (c == null) return "";
        c = c.replaceAll("[\\s-]", "");
        if (c.matches("[0-9]{9}")) return "+258" + c;
        if (c.matches("258[0-9]{9}")) return "+" + c;
        return c;
    }
    public static boolean contactoValido(String c) { return c != null && c.matches("^[+]?[0-9]{9,13}$"); }
    public static boolean latitudeValida(double v) { return v >= -90 && v <= 90; }
    public static boolean longitudeValida(double v) { return v >= -180 && v <= 180; }
    public static boolean vazio(String s) { return s == null || s.trim().isEmpty(); }

    /** Aceita virgula ou ponto decimal. */
    public static double numero(String s, String campo) throws NegocioException {
        try { return Double.parseDouble(s.trim().replace(',', '.')); }
        catch (Exception e) { throw new NegocioException("Valor invalido em \"" + campo + "\"."); }
    }
}
