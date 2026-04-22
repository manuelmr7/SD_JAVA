package proyectocliente;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.io.*;
import proyectocomun.*;

public class ClienteBiblioteca {
    
    private static GestorBibliotecaIntf gestor;
    private static int idAdmin = -1;
    private static Scanner scanner = new Scanner(System.in);
    private static int campoOrdenacionActual = 0;
    
    
    private static String Ajustar(String S, int Ancho) {
        byte v[] = S.getBytes();
        int c = 0;
        int len = 0;
        int uin;
        for (int i = 0; i < v.length; i++) {
            uin = Byte.toUnsignedInt(v[i]);
            if (uin > 128) {
                c++;
            }
        }
        len = c / 2;
        for (int i = 0; i < len; i++) {
            S = S + " ";
        }
        return S;
    }
    
    private static void MostrarLibro(TLibro libro, int Pos, boolean Cabecera, boolean esAdmin) {
        if (Cabecera) {
            System.out.println(String.format("%-5s%-58s%-18s%-4s%-4s%-4s", "POS", "TITULO", "ISBN", "DIS", "PRE", "RES"));
            System.out.println(String.format("%-5s%-30s%-28s%-12s", "", "AUTOR", "PAIS (IDIOMA)", "AÑO"));
            for (int i = 0; i < 93; i++) System.out.print("*");
            System.out.println();
        }
        
        String T = Ajustar(String.format("%-58s", libro.getTitulo()), 58);
        String A = Ajustar(String.format("%-30s", libro.getAutor()), 30);
        String PI = Ajustar(String.format("%-28s", libro.getPais() + " (" + libro.getIdioma() + ")"), 28);
        
        if (esAdmin) {
            System.out.println(String.format("%-5d%s%-18s%-4d%-4d%-4d", 
                Pos + 1, T, libro.getIsbn(), 
                libro.getNoLibros(), libro.getNoPrestados(), libro.getNoListaEspera()));
        } else {
            System.out.println(String.format("%-5d%s%-18s%-4d%-4d%-4d", 
                Pos + 1, T, libro.getIsbn(), 
                libro.getNoLibros(), 0, 0));
        }
        System.out.println(String.format("%-5s%s%s%-12d", "", A, PI, libro.getAnio()));
    }
    
    private static void limpiarPantalla() {
        System.out.print("\nPulsa la tecla return para continuar...");
        scanner.nextLine();
        // En NetBeans no se puede limpiar realmente, pero esto sirve como pausa
        for (int i = 0; i < 50; i++) System.out.println();
    }
    
    // Menú Principal
    private static void menuPrincipal() throws Exception {
        int opcion;
        do {
            System.out.println("\nGESTOR BIBLIOTECARIO 2.0 (M. PRINCIPAL)");
            System.out.println("**************************************");
            System.out.println("1.- M. Administración");
            System.out.println("2.- Consulta de libros");
            System.out.println("3.- Préstamo de libros");
            System.out.println("4.- Devolución de libros");
            System.out.println("0.- Salir");
            System.out.print("Elige opción: ");
            
            opcion = Integer.parseInt(scanner.nextLine());
            
            switch (opcion) {
                case 1:
                    menuAdministracion();
                    break;
                case 2:
                    consultaLibros(false);
                    break;
                case 3:
                    prestamoLibros();
                    break;
                case 4:
                    devolucionLibros();
                    break;
                case 0:
                    if (idAdmin != -1) {
                        gestor.Desconexion(idAdmin);
                    }
                    System.out.println("¡Hasta luego!");
                    break;
            }
        } while (opcion != 0);
    }
    
    // Menú de Administración
    private static void menuAdministracion() throws Exception {
        if (idAdmin == -1) {
            System.out.print("Por favor inserte la contraseña de Administración: ");
            String passwd = scanner.nextLine();
            idAdmin = gestor.Conexion(passwd);
            
            if (idAdmin == -1) {
                System.out.println("*** Ya hay un administrador conectado. ***");
                limpiarPantalla();
                return;
            } else if (idAdmin == -2) {
                System.out.println("*** Contraseña incorrecta. ***");
                limpiarPantalla();
                return;
            } else {
                System.out.println("*** Contraseña correcta, puede acceder al menú de Administración. ***");
                limpiarPantalla();
            }
        }
        
        int opcion;
        do {
            System.out.println("\nGESTOR BIBLIOTECARIO 2.0 (M. ADMINISTRACION)");
            System.out.println("********************************************");
            System.out.println("1.- Cargar Repositorio");
            System.out.println("2.- Guardar Repositorio");
            System.out.println("3.- Nuevo libro");
            System.out.println("4.- Comprar libros");
            System.out.println("5.- Retirar libros");
            System.out.println("6.- Ordenar libros");
            System.out.println("7.- Buscar libros");
            System.out.println("8.- Listar libros");
            System.out.println("0.- Salir");
            System.out.print("Elige opción: ");
            
            opcion = Integer.parseInt(scanner.nextLine());
            
            switch (opcion) {
                case 1:
                    cargarRepositorio();
                    break;
                case 2:
                    guardarRepositorio();
                    break;
                case 3:
                    nuevoLibro();
                    break;
                case 4:
                    comprarLibros();
                    break;
                case 5:
                    retirarLibros();
                    break;
                case 6:
                    ordenarLibros();
                    break;
                case 7:
                    consultaLibros(true);
                    break;
                case 8:
                    listarLibros();
                    break;
                case 0:
                    gestor.Desconexion(idAdmin);
                    idAdmin = -1;
                    System.out.println("*** Sesión de administración cerrada. ***");
                    limpiarPantalla();
                    break;
            }
        } while (opcion != 0);
    }
    
    private static void cargarRepositorio() throws Exception {
        System.out.print("Introduce el nombre del fichero de datos: ");
        String fichero = scanner.nextLine();
        
        int resultado = gestor.AbrirRepositorio(idAdmin, fichero);
        
        if (resultado == -1) {
            System.out.println("*** Error: No eres administrador. ***");
        } else if (resultado == -2) {
            System.out.println("*** Ya existe un repositorio cargado con ese nombre. ***");
        } else if (resultado == 0) {
            System.out.println("*** No se ha podido abrir el fichero. ***");
        } else if (resultado == 1) {
            System.out.println("*** El repositorio ha sido cargado. ***");
        }
        limpiarPantalla();
    }
    
    private static void guardarRepositorio() throws Exception {
        // Mostrar lista de repositorios
        int numRepos = gestor.NRepositorios(idAdmin);
        if (numRepos == -1) {
            System.out.println("*** Error: No eres administrador. ***");
            limpiarPantalla();
            return;
        }
        
        if (numRepos == 0) {
            System.out.println("*** No hay repositorios cargados. ***");
            limpiarPantalla();
            return;
        }
        
        System.out.println("\nPOS NOMBRE DIRECCION Nº LIBROS");
        System.out.println("**************************************************************");
        for (int i = 0; i < numRepos; i++) {
            TDatosRepositorio datos = gestor.DatosRepositorio(idAdmin, i);
            System.out.println(String.format("%-3d %-20s %-25s %d", i+1, datos.getNombre(), datos.getDireccion(), datos.getNumLibros()));
        }
        System.out.println("0 Todos los repositorios");
        System.out.print("Elige repositorio: ");
        
        int repo = Integer.parseInt(scanner.nextLine()) - 1;
        
        int resultado = gestor.GuardarRepositorio(idAdmin, repo);
        
        if (resultado == -1) {
            System.out.println("*** Error: No eres administrador. ***");
        } else if (resultado == -2) {
            System.out.println("*** El repositorio no existe. ***");
        } else if (resultado == 0) {
            System.out.println("*** No se ha podido guardar el/los repositorios. ***");
        } else if (resultado == 1) {
            System.out.println("*** Se ha guardado el/los repositorios seleccionados de la biblioteca. ***");
        }
        limpiarPantalla();
    }
    
    private static void nuevoLibro() throws Exception {
        System.out.print("Introduce el Isbn: ");
        String isbn = scanner.nextLine();
        System.out.print("Introduce el Autor: ");
        String autor = scanner.nextLine();
        System.out.print("Introduce el Título: ");
        String titulo = scanner.nextLine();
        System.out.print("Introduce el año: ");
        int anio = Integer.parseInt(scanner.nextLine());
        System.out.print("Introduce el País: ");
        String pais = scanner.nextLine();
        System.out.print("Introduce el Idioma: ");
        String idioma = scanner.nextLine();
        System.out.print("Introduce número de libros inicial: ");
        int numLibros = Integer.parseInt(scanner.nextLine());
        
        // Mostrar repositorios disponibles
        int numRepos = gestor.NRepositorios(idAdmin);
        if (numRepos <= 0) {
            System.out.println("*** No hay repositorios cargados. ***");
            limpiarPantalla();
            return;
        }
        
        System.out.println("\nRepositorios disponibles:");
        for (int i = 0; i < numRepos; i++) {
            TDatosRepositorio datos = gestor.DatosRepositorio(idAdmin, i);
            System.out.println(String.format("%d.- %s (%s)", i+1, datos.getNombre(), datos.getDireccion()));
        }
        System.out.print("Elige repositorio: ");
        int repo = Integer.parseInt(scanner.nextLine()) - 1;
        
        TLibro libro = new TLibro(isbn, titulo, autor, anio, pais, idioma, numLibros);
        int resultado = gestor.NuevoLibro(idAdmin, libro, repo);
        
        if (resultado == -1) {
            System.out.println("*** Error: No eres administrador. ***");
        } else if (resultado == -2) {
            System.out.println("*** El repositorio no existe. ***");
        } else if (resultado == 0) {
            System.out.println("*** Ya existe un libro con ese ISBN. ***");
        } else if (resultado == 1) {
            System.out.println("*** El libro ha sido añadido correctamente. ***");
        }
        limpiarPantalla();
    }
    
    private static void comprarLibros() throws Exception {
        System.out.print("Introduce Isbn a buscar: ");
        String isbn = scanner.nextLine();
        
        int pos = gestor.Buscar(idAdmin, isbn);
        if (pos == -1) {
            System.out.println("*** No se ha encontrado el libro. ***");
            limpiarPantalla();
            return;
        } else if (pos == -2) {
            System.out.println("*** No se ha encontrado ningún libro con ese ISBN. ***");
            limpiarPantalla();
            return;
        }
        
        TLibro libro = gestor.Descargar(idAdmin, -1, pos);
        MostrarLibro(libro, pos, true, true);
        
        System.out.print("¿ Es este el libro del que desea comprar más unidades (s/n) ? ");
        String respuesta = scanner.nextLine();
        
        if (respuesta.equalsIgnoreCase("s")) {
            System.out.print("Introduce número de libros comprados: ");
            int num = Integer.parseInt(scanner.nextLine());
            int resultado = gestor.Comprar(idAdmin, isbn, num);
            
            if (resultado == -1) {
                System.out.println("*** Error: No eres administrador. ***");
            } else if (resultado == 0) {
                System.out.println("*** No se ha encontrado el libro. ***");
            } else if (resultado == 1) {
                System.out.println("*** Se han añadido el número de libros indicados. ***");
            }
        }
        limpiarPantalla();
    }
    
    private static void retirarLibros() throws Exception {
        System.out.print("Introduce Isbn a buscar: ");
        String isbn = scanner.nextLine();
        
        int pos = gestor.Buscar(idAdmin, isbn);
        if (pos < 0) {
            System.out.println("*** No se ha encontrado ningún libro con ese ISBN. ***");
            limpiarPantalla();
            return;
        }
        
        TLibro libro = gestor.Descargar(idAdmin, -1, pos);
        MostrarLibro(libro, pos, true, true);
        
        System.out.print("¿ Es este el libro del que desea retirar unidades (s/n) ? ");
        String respuesta = scanner.nextLine();
        
        if (respuesta.equalsIgnoreCase("s")) {
            System.out.print("Introduce número de unidades a retirar: ");
            int num = Integer.parseInt(scanner.nextLine());
            int resultado = gestor.Retirar(idAdmin, isbn, num);
            
            if (resultado == -1) {
                System.out.println("*** Error: No eres administrador. ***");
            } else if (resultado == 0) {
                System.out.println("*** No se ha encontrado el libro. ***");
            } else if (resultado == 1) {
                System.out.println("*** Se han retirado el número de libros indicados. ***");
            } else if (resultado == 2) {
                System.out.println("*** No hay suficientes ejemplares disponibles. ***");
            }
        }
        limpiarPantalla();
    }
    
    private static void ordenarLibros() throws Exception {
        System.out.println("Código de ordenación");
        System.out.println("0.- Por Isbn");
        System.out.println("1.- Por Título");
        System.out.println("2.- Por Autor");
        System.out.println("3.- Por Año");
        System.out.println("4.- Por País");
        System.out.println("5.- Por Idioma");
        System.out.println("6.- Por nº de libros Disponibles");
        System.out.println("7.- Por nº de libros Prestados");
        System.out.println("8.- Por nº de libros en espera");
        System.out.print("Introduce código: ");
        
        int campo = Integer.parseInt(scanner.nextLine());
        boolean resultado = gestor.Ordenar(idAdmin, campo);
        
        if (resultado) {
            campoOrdenacionActual = campo;
            System.out.println("*** La biblioteca ha sido ordenada correctamente. ***");
        } else {
            System.out.println("*** Error: No eres administrador. ***");
        }
        limpiarPantalla();
    }
    
    private static void consultaLibros(boolean esAdmin) throws Exception {
        System.out.print("Introduce el texto a buscar: ");
        String texto = scanner.nextLine().toLowerCase();
        
        System.out.println("Código de búsqueda");
        System.out.println("I.- Por Isbn");
        System.out.println("T.- Por Título");
        System.out.println("A.- Por Autor");
        System.out.println("P.- Por País");
        System.out.println("D.- Por Idioma");
        System.out.println("*.- Por todos los campos");
        System.out.print("Introduce código: ");
        
        String codigo = scanner.nextLine();
        
        // Mostrar repositorios disponibles
        if (esAdmin) {
            int numRepos = gestor.NRepositorios(idAdmin);
            if (numRepos > 0) {
                System.out.println("\nPOS NOMBRE   \t\t\tDIRECCION \t\tNº LIBROS");
                System.out.println("**************************************************************");
                for (int i = 0; i < numRepos; i++) {
                    TDatosRepositorio datos = gestor.DatosRepositorio(idAdmin, i);
                    System.out.println(String.format("%-3d %-20s %-25s %d", i+1, datos.getNombre(), datos.getDireccion(), datos.getNumLibros()));
                }
                System.out.println("0 Todos los repositorios");
                System.out.print("Elige repositorio: ");
                int repo = Integer.parseInt(scanner.nextLine());
                // Nota: Para simplificar, buscamos en todos (repo 0)
            }
        }
        
        // Obtener todos los libros de la mezcla ordenada
        int totalLibros = gestor.NLibros(-1);
        boolean encontrado = false;
        
        for (int i = 0; i < totalLibros; i++) {
            TLibro libro = gestor.Descargar(idAdmin, -1, i);
            if (libro == null) continue;
            
            boolean coincide = false;
            switch (codigo.toUpperCase()) {
                case "I":
                    coincide = libro.getIsbn().toLowerCase().contains(texto);
                    break;
                case "T":
                    coincide = libro.getTitulo().toLowerCase().contains(texto);
                    break;
                case "A":
                    coincide = libro.getAutor().toLowerCase().contains(texto);
                    break;
                case "P":
                    coincide = libro.getPais().toLowerCase().contains(texto);
                    break;
                case "D":
                    coincide = libro.getIdioma().toLowerCase().contains(texto);
                    break;
                case "*":
                    coincide = libro.getIsbn().toLowerCase().contains(texto) ||
                              libro.getTitulo().toLowerCase().contains(texto) ||
                              libro.getAutor().toLowerCase().contains(texto) ||
                              libro.getPais().toLowerCase().contains(texto) ||
                              libro.getIdioma().toLowerCase().contains(texto);
                    break;
            }
            
            if (coincide) {
                if (!encontrado) {
                    MostrarLibro(libro, i, true, esAdmin);
                    encontrado = true;
                } else {
                    MostrarLibro(libro, i, false, esAdmin);
                }
            }
        }
        
        if (!encontrado) {
            System.out.println("*** No se encontraron libros. ***");
        }
        limpiarPantalla();
    }
    
    private static void listarLibros() throws Exception {
        int totalLibros = gestor.NLibros(-1);
        if (totalLibros == 0) {
            System.out.println("*** No hay libros en la biblioteca. ***");
            limpiarPantalla();
            return;
        }
        
        boolean primera = true;
        for (int i = 0; i < totalLibros; i++) {
            TLibro libro = gestor.Descargar(idAdmin, -1, i);
            if (libro != null) {
                MostrarLibro(libro, i, primera, true);
                primera = false;
            }
        }
        limpiarPantalla();
    }
    
    private static void prestamoLibros() throws Exception {
        System.out.print("Introduce el texto a buscar: ");
        String texto = scanner.nextLine().toLowerCase();
        
        System.out.println("Código de consulta");
        System.out.println("I.- Por Isbn");
        System.out.println("T.- Por Título");
        System.out.println("A.- Por Autor");
        System.out.println("P.- Por País");
        System.out.println("D.- Por Idioma");
        System.out.println("*.- Por todos los campos");
        System.out.print("Introduce código: ");
        
        String codigo = scanner.nextLine();
        
        // Buscar y mostrar libros que coincidan
        int totalLibros = gestor.NLibros(-1);
        java.util.List<Integer> posicionesEncontradas = new ArrayList<>();
        
        for (int i = 0; i < totalLibros; i++) {
            TLibro libro = gestor.Descargar(-1, -1, i);
            if (libro == null) continue;
            
            boolean coincide = false;
            switch (codigo.toUpperCase()) {
                case "I":
                    coincide = libro.getIsbn().toLowerCase().contains(texto);
                    break;
                case "T":
                    coincide = libro.getTitulo().toLowerCase().contains(texto);
                    break;
                case "A":
                    coincide = libro.getAutor().toLowerCase().contains(texto);
                    break;
                case "P":
                    coincide = libro.getPais().toLowerCase().contains(texto);
                    break;
                case "D":
                    coincide = libro.getIdioma().toLowerCase().contains(texto);
                    break;
                case "*":
                    coincide = libro.getIsbn().toLowerCase().contains(texto) ||
                              libro.getTitulo().toLowerCase().contains(texto) ||
                              libro.getAutor().toLowerCase().contains(texto) ||
                              libro.getPais().toLowerCase().contains(texto) ||
                              libro.getIdioma().toLowerCase().contains(texto);
                    break;
            }
            
            if (coincide) {
                posicionesEncontradas.add(i);
                MostrarLibro(libro, i, posicionesEncontradas.size() == 1, false);
            }
        }
        
        if (posicionesEncontradas.isEmpty()) {
            System.out.println("*** No se encontraron libros. ***");
            limpiarPantalla();
            return;
        }
        
        System.out.print("¿ Quieres sacar algún libro de la biblioteca (s/n) ? ");
        String respuesta = scanner.nextLine();
        
        if (respuesta.equalsIgnoreCase("s")) {
            System.out.print("Introduce la posición del libro a solicitar su préstamo: ");
            int pos = Integer.parseInt(scanner.nextLine()) - 1;
            
            int resultado = gestor.Prestar(pos);
            
            if (resultado == -1) {
                System.out.println("*** Posición incorrecta. ***");
            } else if (resultado == 0) {
                System.out.println("*** No hay ejemplares disponibles, se ha añadido a la lista de espera. ***");
            } else if (resultado == 1) {
                System.out.println("*** El préstamo se ha concedido, recoge el libro en el mostrador. ***");
            }
        }
        limpiarPantalla();
    }
    
    private static void devolucionLibros() throws Exception {
        System.out.print("Introduce el Isbn a buscar: ");
        String isbn = scanner.nextLine();
        
        int totalLibros = gestor.NLibros(-1);
        java.util.List<Integer> posicionesEncontradas = new ArrayList<>();
        
        for (int i = 0; i < totalLibros; i++) {
            TLibro libro = gestor.Descargar(-1, -1, i);
            if (libro != null && libro.getIsbn().contains(isbn)) {
                posicionesEncontradas.add(i);
                System.out.println(String.format("%d %-50s %-30s %s", 
                    i+1, libro.getTitulo(), libro.getAutor(), libro.getPais() + " (" + libro.getIdioma() + ")"));
            }
        }
        
        if (posicionesEncontradas.isEmpty()) {
            System.out.println("*** No se encontraron libros con ese ISBN. ***");
            limpiarPantalla();
            return;
        }
        
        System.out.print("¿ Quieres devolver algún libro de la biblioteca (s/n) ? ");
        String respuesta = scanner.nextLine();
        
        if (respuesta.equalsIgnoreCase("s")) {
            System.out.print("Introduce la posición del libro a devolver: ");
            int pos = Integer.parseInt(scanner.nextLine()) - 1;
            
            int resultado = gestor.Devolver(pos);
            
            if (resultado == -1) {
                System.out.println("*** Posición incorrecta. ***");
            } else if (resultado == 0) {
                System.out.println("*** Se ha devuelto el libro y se ha asignado a un usuario en espera. ***");
            } else if (resultado == 1) {
                System.out.println("*** Se ha devuelto el libro y se pondrá en la estantería. ***");
            } else if (resultado == 2) {
                System.out.println("*** El libro no se puede devolver, no hay usuarios en espera ni libros prestados. ***");
            }
        }
        limpiarPantalla();
    }
    
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            gestor = (GestorBibliotecaIntf) registry.lookup("GestorBiblioteca");
            System.out.println("Conectado al servidor RMI de la Biblioteca\n");
            menuPrincipal();
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
