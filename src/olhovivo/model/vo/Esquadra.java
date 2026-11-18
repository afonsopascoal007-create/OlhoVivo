package olhovivo.model.vo;

/** Value Object: esquadra da PRM. */
public class Esquadra {
    private int id;
    private String nome, contacto, endereco, area;
    private double latitude, longitude, distanciaKm;

    public Esquadra() { }
    public Esquadra(int id, String nome, String contacto, String endereco, String area, double lat, double lon) {
        this.id = id; this.nome = nome; this.contacto = contacto; this.endereco = endereco;
        this.area = area; this.latitude = lat; this.longitude = lon;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
    @Override public String toString() { return nome; }
}
