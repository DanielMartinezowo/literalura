package com.alurachallenge.LiteraturaChallenge.principal;




import com.alurachallenge.LiteraturaChallenge.modelo.*;
import com.alurachallenge.LiteraturaChallenge.repository.AutorRepository;
import com.alurachallenge.LiteraturaChallenge.repository.LibroRepository;
import com.alurachallenge.LiteraturaChallenge.service.ConsumoAPI;
import com.alurachallenge.LiteraturaChallenge.service.ConvierteDatosJson;

import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatosJson convierteDatosJson = new ConvierteDatosJson();
    private List<Libro> libros;
    private List<Autor> autores;
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;


    public Principal(AutorRepository autorRepository, LibroRepository libroRepository){
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }
                //Menu
    public void muestramenu(){
        int opcion = -1;
            String menu = """
              \n********************************************
                       Bienvenido/a a nuestro Buscador Virtual
              1) Buscar libro por título 
              2) Listar libros registrados
              3) Listar autores registrados
              4) Listar autores vivos en un determinado año
              5) Listar libros por idioma
              
              0) Salir
              ***********************************************      
              """;


        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = teclado.nextInt();
                teclado.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número del 0 al 5.");
                teclado.nextLine();
                continue;
            }
            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresPorYear();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.printf("Opción inválidaPor favor, ingrese un número del 0 al 5.\n");
            }

    }

    }

    private void listarLibrosPorIdioma() {
        String menuIdioma = """
                Ingrese el idioma para la busqueda de los libros: 
                es >> Español
                en >> Ingles
                fr >> Frances 
                pt >> Portugues
                """;

        System.out.println(menuIdioma);
        String idiomaBuscado = teclado.nextLine();
        CategoriaIdioma idioma = null;

        switch (idiomaBuscado){
            case "es":
                idioma = CategoriaIdioma.fromEspanol("Español") ;
                break;
            case "en":
                idioma = CategoriaIdioma.fromEspanol("Ingles") ;
                break;
            case "fr":
                idioma = CategoriaIdioma.fromEspanol("Frances") ;
                break;
            case "pt":
                idioma = CategoriaIdioma.fromEspanol("Portugues");
                break;
            default:
                System.out.println("Entrada incorrecta.");
                return;

        }
        buscarPorIdioma(idioma);

    }

    private void buscarPorIdioma(CategoriaIdioma idioma){
        libros = libroRepository.findLibrosByidioma(idioma);
        if (libros.isEmpty()){
            System.out.println("No se encontraron resultados");
        } else {
            libros.stream().forEach(System.out::println);
        }
    }

    private void listarAutoresPorYear() {


        System.out.println("Ingrese el año vivo de Autore(s) que desea buscar: ");
        try {
            Integer year = teclado.nextInt();
            autores = autorRepository.findAutoresByYear(year);
            if (autores.isEmpty()){
                System.out.println("No hay autores en ese periodo");
            } else {
                autores.stream().forEach(System.out::println);
            }
        } catch (InputMismatchException e) {
            System.out.println("Ingrese un año correcto");
            teclado.nextLine();
        }

    }


    private void listarAutoresRegistrados() {
        autores = autorRepository.findAll();
        autores.stream().forEach(System.out::println);
    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAll();
        libros.stream().forEach(System.out::println);

    }

    private String realizarConsulta (){

        System.out.println("Escribe el nombre del libro que desea buscar: ");
        var nombreLibro = teclado.nextLine();
        String url = URL_BASE + "?search=" + nombreLibro.replace(" ", "%20");
        System.out.println("Esperando la respuesta...");
        String respuesta = consumoAPI.obtenerDatosApi(url);
        return respuesta;
    }

    private void buscarLibroPorTitulo() {
        String respuesta = realizarConsulta();
        DatosConsultaAPI datosConsultaAPI = convierteDatosJson.obtenerDatos(respuesta, DatosConsultaAPI.class);
        if (datosConsultaAPI.numeroLibros() != 0) {
            DatosLibro primerLibro = datosConsultaAPI.resultado().get(0);
            if (primerLibro.autores() != null && !primerLibro.autores().isEmpty()) {
                Autor autorLibro = new Autor(primerLibro.autores().get(0));
                Optional<Libro> libroBase = libroRepository.findLibroBytitulo(primerLibro.titulo());
                if (libroBase.isPresent()) {
                    System.out.println("El libro ya se encuentre registrado, intenta con otro");
                } else {
                    Optional<Autor> autorDeBase = autorRepository.findBynombre(autorLibro.getNombre());
                    autorDeBase.ifPresentOrElse(
                            autor -> autor = autor,
                            () -> autorRepository.save(autorLibro)
                    );

                    Libro libro = new Libro(primerLibro);
                    libro.setAutor(autorLibro);
                    libroRepository.save(libro);
                    System.out.println(libro);
                }
            } else {
                System.out.println("El libro no tiene autores");
            }
        } else {
            System.out.println("Libro no se encuentra, vuelve a intentarlo...");
        }
    }


}
