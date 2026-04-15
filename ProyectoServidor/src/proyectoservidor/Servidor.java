package proyectoservidor;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Servidor {
    public static void main(String[] args) {
        try {
            // Crear el registro RMI en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Crear la instancia del servidor
            GestorBibliotecaImpl servidor = new GestorBibliotecaImpl();
            
            // Registrar el objeto remoto
            registry.rebind("GestorBiblioteca", servidor);
            
            System.out.println("Servidor RMI de la Biblioteca iniciado...");
            System.out.println("Registro en puerto 1099");
            System.out.println("Esperando peticiones...");
            
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}