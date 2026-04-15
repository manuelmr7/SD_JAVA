package proyectocomun;
import java.io.Serializable;

public class TDatosRepositorio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String Nombre;
    private String Direccion;
    private int NumLibros;
    
    public TDatosRepositorio() {}
    
    public TDatosRepositorio(String Nombre, String Direccion, int NumLibros) {
        this.Nombre = Nombre;
        this.Direccion = Direccion;
        this.NumLibros = NumLibros;
    }
    
    public String getNombre() { return Nombre; }
    public void setNombre(String Nombre) { this.Nombre = Nombre; }
    
    public String getDireccion() { return Direccion; }
    public void setDireccion(String Direccion) { this.Direccion = Direccion; }
    
    public int getNumLibros() { return NumLibros; }
    public void setNumLibros(int NumLibros) { this.NumLibros = NumLibros; }
}