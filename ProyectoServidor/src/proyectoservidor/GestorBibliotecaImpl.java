package proyectoservidor;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import proyectocomun.*;
public class GestorBibliotecaImpl extends UnicastRemoteObject implements GestorBibliotecaIntf {
    
    private static final long serialVersionUID = 1L;
    private static final String ADMIN_PASSWD = "563498";
    
    // Estructura de datos: lista de repositorios
    private List<Repositorio> repositorios;
    private int idAdmin;  // -1 si no hay admin conectado
    private int campoOrdenacion;  // campo por defecto para ordenar
    
    // Clase interna para almacenar un repositorio
    private class Repositorio implements Serializable {
        String nombreFichero;
        String nombre;
        String direccion;
        List<TLibro> libros;
        
        Repositorio(String nombreFichero, String nombre, String direccion) {
            this.nombreFichero = nombreFichero;
            this.nombre = nombre;
            this.direccion = direccion;
            this.libros = new ArrayList<>();
        }
    }
    
    public GestorBibliotecaImpl() throws RemoteException {
        super();
        repositorios = new ArrayList<>();
        idAdmin = -1;
        campoOrdenacion = 0;  // Por Isbn por defecto
    }
    
    // Método auxiliar para ordenar un repositorio según el campo actual
    private void ordenarRepositorio(Repositorio repo) {
        if (repo == null || repo.libros.isEmpty()) return;
        
        repo.libros.sort(new Comparator<TLibro>() {
            @Override
            public int compare(TLibro o1, TLibro o2) {
                switch (campoOrdenacion) {
                    case 0: return o1.getIsbn().compareTo(o2.getIsbn());
                    case 1: return o1.getTitulo().compareTo(o2.getTitulo());
                    case 2: return o1.getAutor().compareTo(o2.getAutor());
                    case 3: return Integer.compare(o1.getAnio(), o2.getAnio());
                    case 4: return o1.getPais().compareTo(o2.getPais());
                    case 5: return o1.getIdioma().compareTo(o2.getIdioma());
                    case 6: return Integer.compare(o1.getNoLibros(), o2.getNoLibros());
                    case 7: return Integer.compare(o1.getNoPrestados(), o2.getNoPrestados());
                    case 8: return Integer.compare(o1.getNoListaEspera(), o2.getNoListaEspera());
                    default: return 0;
                }
            }
        });
    }
    
    // Método auxiliar para ordenar todos los repositorios
    private void ordenarTodosRepositorios() {
        for (Repositorio repo : repositorios) {
            ordenarRepositorio(repo);
        }
    }
    
    // Método auxiliar para obtener la mezcla ordenada de todos los libros
    private List<TLibro> obtenerMezclaOrdenada() {
        List<TLibro> mezcla = new ArrayList<>();
        for (Repositorio repo : repositorios) {
            mezcla.addAll(repo.libros);
        }
        
        mezcla.sort(new Comparator<TLibro>() {
            @Override
            public int compare(TLibro o1, TLibro o2) {
                switch (campoOrdenacion) {
                    case 0: return o1.getIsbn().compareTo(o2.getIsbn());
                    case 1: return o1.getTitulo().compareTo(o2.getTitulo());
                    case 2: return o1.getAutor().compareTo(o2.getAutor());
                    case 3: return Integer.compare(o1.getAnio(), o2.getAnio());
                    case 4: return o1.getPais().compareTo(o2.getPais());
                    case 5: return o1.getIdioma().compareTo(o2.getIdioma());
                    case 6: return Integer.compare(o1.getNoLibros(), o2.getNoLibros());
                    case 7: return Integer.compare(o1.getNoPrestados(), o2.getNoPrestados());
                    case 8: return Integer.compare(o1.getNoListaEspera(), o2.getNoListaEspera());
                    default: return 0;
                }
            }
        });
        
        return mezcla;
    }
    
    // Método auxiliar para verificar si un ISBN ya existe en algún repositorio
    private boolean isbnExiste(String isbn) {
        for (Repositorio repo : repositorios) {
            for (TLibro libro : repo.libros) {
                if (libro.getIsbn().equals(isbn)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Método auxiliar para encontrar un libro por ISBN (devuelve posición en mezcla ordenada)
    private int encontrarPosicionPorIsbn(String isbn) {
        List<TLibro> mezcla = obtenerMezclaOrdenada();
        for (int i = 0; i < mezcla.size(); i++) {
            if (mezcla.get(i).getIsbn().equals(isbn)) {
                return i;
            }
        }
        return -1;
    }
    
    // Método auxiliar para encontrar repositorio y posición de un libro por ISBN
    private int[] encontrarLibroPorIsbn(String isbn) {
        for (int r = 0; r < repositorios.size(); r++) {
            Repositorio repo = repositorios.get(r);
            for (int p = 0; p < repo.libros.size(); p++) {
                if (repo.libros.get(p).getIsbn().equals(isbn)) {
                    return new int[]{r, p};
                }
            }
        }
        return new int[]{-1, -1};
    }
    
    // ==================== IMPLEMENTACIÓN DE MÉTODOS REMOTOS ====================
    
    @Override
    public int Conexion(String Passwd) throws RemoteException {
        if (idAdmin != -1) {
            return -1;  // Ya hay un admin conectado
        }
        if (!ADMIN_PASSWD.equals(Passwd)) {
            return -2;  // Contraseña incorrecta
        }
        idAdmin = 1 + (int)(Math.random() * 1000000);
        return idAdmin;
    }
    
    @Override
    public boolean Desconexion(int Ida) throws RemoteException {
        if (idAdmin != Ida) {
            return false;
        }
        idAdmin = -1;
        return true;
    }
    
    @Override
    public int AbrirRepositorio(int Ida, String NomFichero) throws RemoteException {
        if (idAdmin != Ida) return -1;
        
        // Verificar si ya existe un repositorio con el mismo nombre
        for (Repositorio repo : repositorios) {
            if (repo.nombreFichero.equals(NomFichero)) {
                return -2;
            }
        }
        
        try (DataInputStream entrada = new DataInputStream(new FileInputStream(NomFichero))) {
            int numLibros = entrada.readInt();
            String nombre = entrada.readUTF();
            String direccion = entrada.readUTF();
            
            Repositorio nuevoRepo = new Repositorio(NomFichero, nombre, direccion);
            
            for (int i = 0; i < numLibros; i++) {
                TLibro libro = new TLibro();
                libro.setIsbn(entrada.readUTF());
                libro.setTitulo(entrada.readUTF());
                libro.setAutor(entrada.readUTF());
                libro.setAnio(entrada.readInt());
                libro.setPais(entrada.readUTF());
                libro.setIdioma(entrada.readUTF());
                libro.setNoLibros(entrada.readInt());
                libro.setNoPrestados(entrada.readInt());
                libro.setNoListaEspera(entrada.readInt());
                nuevoRepo.libros.add(libro);
            }
            
            repositorios.add(nuevoRepo);
            ordenarRepositorio(nuevoRepo);
            return 1;
            
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }
    }
    
    @Override
    public int GuardarRepositorio(int Ida, int Repo) throws RemoteException {
        if (idAdmin != Ida) return -1;
        
        if (Repo == -1) {
            // Guardar todos los repositorios
            boolean todosOk = true;
            for (Repositorio repo : repositorios) {
                if (!guardarUnRepositorio(repo)) {
                    todosOk = false;
                }
            }
            return todosOk ? 1 : 0;
        } else {
            if (Repo < 0 || Repo >= repositorios.size()) return -2;
            return guardarUnRepositorio(repositorios.get(Repo)) ? 1 : 0;
        }
    }
    
    private boolean guardarUnRepositorio(Repositorio repo) {
        try (DataOutputStream salida = new DataOutputStream(new FileOutputStream(repo.nombreFichero))) {
            salida.writeInt(repo.libros.size());
            salida.writeUTF(repo.nombre);
            salida.writeUTF(repo.direccion);
            
            for (TLibro libro : repo.libros) {
                salida.writeUTF(libro.getIsbn());
                salida.writeUTF(libro.getTitulo());
                salida.writeUTF(libro.getAutor());
                salida.writeInt(libro.getAnio());
                salida.writeUTF(libro.getPais());
                salida.writeUTF(libro.getIdioma());
                salida.writeInt(libro.getNoLibros());
                salida.writeInt(libro.getNoPrestados());
                salida.writeInt(libro.getNoListaEspera());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public int NRepositorios(int Ida) throws RemoteException {
        if (idAdmin != Ida) return -1;
        return repositorios.size();
    }
    
    @Override
    public TDatosRepositorio DatosRepositorio(int Ida, int Repo) throws RemoteException {
        if (idAdmin != Ida) return null;
        
        if (Repo < 0 || Repo >= repositorios.size()) {
            return new TDatosRepositorio("???", "???", 0);
        }
        
        Repositorio repo = repositorios.get(Repo);
        return new TDatosRepositorio(repo.nombre, repo.direccion, repo.libros.size());
    }
    
    @Override
    public int NuevoLibro(int Ida, TLibro Libro, int Repo) throws RemoteException {
        if (idAdmin != Ida) return -1;
        if (Repo < 0 || Repo >= repositorios.size()) return -2;
        if (isbnExiste(Libro.getIsbn())) return 0;
        
        repositorios.get(Repo).libros.add(Libro);
        ordenarRepositorio(repositorios.get(Repo));
        return 1;
    }
    
    @Override
    public int Comprar(int Ida, String Isbn, int NoLibros) throws RemoteException {
        if (idAdmin != Ida) return -1;
        
        int[] pos = encontrarLibroPorIsbn(Isbn);
        if (pos[0] == -1) return 0;
        
        TLibro libro = repositorios.get(pos[0]).libros.get(pos[1]);
        libro.setNoLibros(libro.getNoLibros() + NoLibros);
        
        // Atender lista de espera
        while (libro.getNoListaEspera() > 0 && libro.getNoLibros() > 0) {
            libro.setNoListaEspera(libro.getNoListaEspera() - 1);
            libro.setNoLibros(libro.getNoLibros() - 1);
            libro.setNoPrestados(libro.getNoPrestados() + 1);
        }
        
        ordenarRepositorio(repositorios.get(pos[0]));
        return 1;
    }
    
    @Override
    public int Retirar(int Ida, String Isbn, int NoLibros) throws RemoteException {
        if (idAdmin != Ida) return -1;
        
        int[] pos = encontrarLibroPorIsbn(Isbn);
        if (pos[0] == -1) return 0;
        
        TLibro libro = repositorios.get(pos[0]).libros.get(pos[1]);
        if (libro.getNoLibros() < NoLibros) return 2;
        
        libro.setNoLibros(libro.getNoLibros() - NoLibros);
        ordenarRepositorio(repositorios.get(pos[0]));
        return 1;
    }
    
    @Override
    public boolean Ordenar(int Ida, int Campo) throws RemoteException {
        if (idAdmin != Ida) return false;
        
        this.campoOrdenacion = Campo;
        ordenarTodosRepositorios();
        return true;
    }
    
    @Override
    public int NLibros(int Repo) throws RemoteException {
        if (Repo == -1) {
            int total = 0;
            for (Repositorio repo : repositorios) {
                total += repo.libros.size();
            }
            return total;
        } else {
            if (Repo < 0 || Repo >= repositorios.size()) return -1;
            return repositorios.get(Repo).libros.size();
        }
    }
    
    @Override
    public int Buscar(int Ida, String Isbn) throws RemoteException {
        if (idAdmin != Ida) return -1;
        
        int pos = encontrarPosicionPorIsbn(Isbn);
        return pos;
    }
    
    @Override
    public TLibro Descargar(int Ida, int Repo, int Pos) throws RemoteException {
        TLibro resultado = new TLibro();
        
        if (Repo == -1) {
            List<TLibro> mezcla = obtenerMezclaOrdenada();
            if (Pos < 0 || Pos >= mezcla.size()) return null;
            resultado = mezcla.get(Pos);
        } else {
            if (Repo < 0 || Repo >= repositorios.size()) return null;
            if (Pos < 0 || Pos >= repositorios.get(Repo).libros.size()) return null;
            resultado = repositorios.get(Repo).libros.get(Pos);
        }
        
        // Si el Ida no coincide, ocultar datos sensibles
        if (idAdmin != Ida) {
            resultado.setNoPrestados(0);
            resultado.setNoListaEspera(0);
        }
        
        return resultado;
    }
    
    @Override
    public int Prestar(int Pos) throws RemoteException {
        List<TLibro> mezcla = obtenerMezclaOrdenada();
        if (Pos < 0 || Pos >= mezcla.size()) return -1;
        
        TLibro libro = mezcla.get(Pos);
        
        if (libro.getNoLibros() > 0) {
            libro.setNoLibros(libro.getNoLibros() - 1);
            libro.setNoPrestados(libro.getNoPrestados() + 1);
            
            // Encontrar el repositorio que contiene este libro y ordenarlo
            for (Repositorio repo : repositorios) {
                if (repo.libros.contains(libro)) {
                    ordenarRepositorio(repo);
                    break;
                }
            }
            return 1;
        } else {
            libro.setNoListaEspera(libro.getNoListaEspera() + 1);
            
            for (Repositorio repo : repositorios) {
                if (repo.libros.contains(libro)) {
                    ordenarRepositorio(repo);
                    break;
                }
            }
            return 0;
        }
    }
    
    @Override
    public int Devolver(int Pos) throws RemoteException {
        List<TLibro> mezcla = obtenerMezclaOrdenada();
        if (Pos < 0 || Pos >= mezcla.size()) return -1;
        
        TLibro libro = mezcla.get(Pos);
        
        if (libro.getNoListaEspera() > 0) {
            libro.setNoListaEspera(libro.getNoListaEspera() - 1);
            
            for (Repositorio repo : repositorios) {
                if (repo.libros.contains(libro)) {
                    ordenarRepositorio(repo);
                    break;
                }
            }
            return 0;
        } else if (libro.getNoPrestados() > 0) {
            libro.setNoPrestados(libro.getNoPrestados() - 1);
            libro.setNoLibros(libro.getNoLibros() + 1);
            
            for (Repositorio repo : repositorios) {
                if (repo.libros.contains(libro)) {
                    ordenarRepositorio(repo);
                    break;
                }
            }
            return 1;
        } else {
            return 2;
        }
    }
}