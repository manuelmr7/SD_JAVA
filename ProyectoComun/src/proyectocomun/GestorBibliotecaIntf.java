package proyectocomun;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GestorBibliotecaIntf extends Remote {
    
    int Conexion(String Passwd) throws RemoteException;
    boolean Desconexion(int Ida) throws RemoteException;
    
    int AbrirRepositorio(int Ida, String NomFichero) throws RemoteException;
    int GuardarRepositorio(int Ida, int Repo) throws RemoteException;
    int NRepositorios(int Ida) throws RemoteException;
    TDatosRepositorio DatosRepositorio(int Ida, int Repo) throws RemoteException;
    
    int NuevoLibro(int Ida, TLibro Libro, int Repo) throws RemoteException;
    int Comprar(int Ida, String Isbn, int NoLibros) throws RemoteException;
    int Retirar(int Ida, String Isbn, int NoLibros) throws RemoteException;
    boolean Ordenar(int Ida, int Campo) throws RemoteException;
    
    int NLibros(int Repo) throws RemoteException;
    int Buscar(int Ida, String Isbn) throws RemoteException;
    TLibro Descargar(int Ida, int Repo, int Pos) throws RemoteException;
    int Prestar(int Pos) throws RemoteException;
    int Devolver(int Pos) throws RemoteException;
}