package main;

import entities.Cobertura;
import entities.SeguroVehicular;
import entities.Vehiculo;
import service.ServiceException;
import service.VehiculoService;
import service.VehiculoServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class AppMenu {

    // El Menú SÓLO habla con la capa de Servicio
    private VehiculoService vehiculoService = new VehiculoServiceImpl();
    private Scanner scanner = new Scanner(System.in);

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                switch (opcion) {
                    case 1: crearVehiculo(); break;
                    case 2: buscarPorId(); break;
                    case 3: buscarPorDominio(); break;
                    case 4: listarVehiculos(); break;
                    case 5: actualizarVehiculo(); break;
                    case 6: eliminarVehiculo(); break;
                    case 0: System.out.println("Saliendo de la aplicación..."); break;
                    default: System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.err.println("❌ Error: Debe ingresar un número.");
                scanner.nextLine(); // Limpiar buffer de entrada
                opcion = -1; // Resetear opción para continuar bucle
            } catch (ServiceException e) {
                // Captura TODOS los errores de negocio y transacciones
                System.err.println("❌ Error de Operación: " + e.getMessage());
            } catch (Exception e) {
                // Captura genérica para errores inesperados
                System.err.println("❌ Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("--- Presione Enter para continuar ---");
            scanner.nextLine();
        }
        scanner.close();
    }

    private void mostrarMenu() {
        System.out.println("\n--- GESTIÓN DE VEHÍCULOS Y SEGUROS (TFI) ---");
        System.out.println("1. Crear Vehículo (con su seguro)");
        System.out.println("2. Buscar Vehículo por ID");
        System.out.println("3. Buscar Vehículo por Dominio");
        System.out.println("4. Listar todos los Vehículos");
        System.out.println("5. Actualizar Vehículo (y su seguro)");
        System.out.println("6. Eliminar Vehículo (baja lógica)");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private void crearVehiculo() throws ServiceException {
        System.out.println("\n--- 1. Nuevo Vehículo ---");
        
        System.out.print("Dominio (Patente): ");
        String dominio = scanner.nextLine().toUpperCase();
        System.out.print("Marca: ");
        String marca = scanner.nextLine();
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        System.out.print("Año (ej: 2023): ");
        int anio = leerEntero();
        System.out.print("Nro de Chasis: ");
        String nroChasis = scanner.nextLine();

        System.out.println("\n--- Datos del Seguro Asociado (Obligatorio) ---");
        System.out.print("Aseguradora: ");
        String aseguradora = scanner.nextLine();
        System.out.print("Nro de Póliza: ");
        String nroPoliza = scanner.nextLine();
        System.out.print("Cobertura (RC, TERCEROS, TODO_RIESGO): ");
        Cobertura cobertura = leerCobertura();
        System.out.print("Fecha de Vencimiento (AAAA-MM-DD): ");
        LocalDate vencimiento = leerFecha();

        // Crear objetos
        SeguroVehicular seguro = new SeguroVehicular(null, false, aseguradora, nroPoliza, cobertura, vencimiento);
        Vehiculo vehiculo = new Vehiculo(null, false, dominio, marca, modelo, anio, nroChasis, seguro);

        // Enviar a la capa de Servicio (aquí ocurre la transacción)
        vehiculoService.insertar(vehiculo);
        System.out.println("✅ ¡Vehículo y seguro creados exitosamente!");
        System.out.println("ID Asignado al Vehículo: " + vehiculo.getId());
    }

    private void buscarPorId() throws ServiceException {
        System.out.println("\n--- 2. Buscar por ID ---");
        System.out.print("Ingrese el ID del vehículo a buscar: ");
        long id = leerLong();
        
        Vehiculo v = vehiculoService.getById(id);
        
        if (v != null) {
            System.out.println("Vehículo encontrado:");
            System.out.println(v); // Llama al toString() de Vehiculo
            System.out.println("Seguro asociado: " + v.getSeguro()); // Llama al toString() de Seguro
        } else {
            System.out.println("No se encontró ningún vehículo con ID: " + id);
        }
    }

    private void buscarPorDominio() throws ServiceException {
        System.out.println("\n--- 3. Buscar por Dominio ---");
        System.out.print("Ingrese el Dominio (Patente) a buscar: ");
        String dominio = scanner.nextLine().toUpperCase();
        
        Vehiculo v = vehiculoService.getByDominio(dominio);
        
        if (v != null) {
            System.out.println("Vehículo encontrado:");
            System.out.println(v);
            System.out.println("Seguro asociado: " + v.getSeguro());
        } else {
            System.out.println("No se encontró ningún vehículo con dominio: " + dominio);
        }
    }

    private void listarVehiculos() throws ServiceException {
        System.out.println("\n--- 4. Listado de Vehículos ---");
        List<Vehiculo> vehiculos = vehiculoService.getAll();
        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos registrados.");
            return;
        }
        
        for (Vehiculo v : vehiculos) {
            System.out.println(v);
            System.out.println("   Seguro: " + v.getSeguro());
            System.out.println("--------------------");
        }
    }

    private void actualizarVehiculo() throws ServiceException {
        System.out.println("\n--- 5. Actualizar Vehículo ---");
        System.out.print("Ingrese el ID del vehículo a actualizar: ");
        long id = leerLong();
        
        Vehiculo v = vehiculoService.getById(id);
        if (v == null) {
            throw new ServiceException("Vehículo no encontrado. ID: " + id);
        }
        
        System.out.println("Datos actuales del Vehículo: " + v);
        System.out.println("Datos actuales del Seguro: " + v.getSeguro());
        System.out.println("Ingrese los nuevos datos (deje en blanco para no cambiar):");
        
        // Pedir nuevos datos (lógica de "dejar en blanco")
        System.out.print("Nuevo Dominio (" + v.getDominio() + "): ");
        String dominio = scanner.nextLine().toUpperCase();
        if (!dominio.isEmpty()) v.setDominio(dominio);

        System.out.print("Nueva Marca (" + v.getMarca() + "): ");
        String marca = scanner.nextLine();
        if (!marca.isEmpty()) v.setMarca(marca);
        
        // ... (Repetir para modelo, anio, nroChasis) ...
        
        System.out.print("Nueva Aseguradora (" + v.getSeguro().getAseguradora() + "): ");
        String aseguradora = scanner.nextLine();
        if (!aseguradora.isEmpty()) v.getSeguro().setAseguradora(aseguradora);

        // ... (Repetir para nroPoliza, cobertura, vencimiento) ...
        
        vehiculoService.actualizar(v);
        System.out.println("✅ Vehículo y seguro actualizados correctamente.");
    }

    private void eliminarVehiculo() throws ServiceException {
        System.out.println("\n--- 6. Eliminar Vehículo (Baja Lógica) ---");
        System.out.print("Ingrese el ID del vehículo a eliminar: ");
        long id = leerLong();
        
        // Confirmación
        System.out.print("¿Está seguro que desea dar de baja este vehículo y su seguro? (S/N): ");
        String confirmacion = scanner.nextLine().toUpperCase();
        
        if (confirmacion.equals("S")) {
            vehiculoService.eliminar(id);
            System.out.println("✅ Vehículo y seguro dados de baja lógicamente.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // --- Métodos de ayuda para lectura robusta ---

    private int leerEntero() {
        while (true) {
            try {
                int valor = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer
                return valor;
            } catch (InputMismatchException e) {
                System.err.print("Error: Ingrese un número entero. Intente de nuevo: ");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }
    
    private long leerLong() {
        while (true) {
            try {
                long valor = scanner.nextLong();
                scanner.nextLine(); // Limpiar buffer
                return valor;
            } catch (InputMismatchException e) {
                System.err.print("Error: Ingrese un número largo. Intente de nuevo: ");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    private LocalDate leerFecha() {
        while (true) {
            try {
                String fechaStr = scanner.nextLine();
                return LocalDate.parse(fechaStr); // Formato AAAA-MM-DD
            } catch (DateTimeParseException e) {
                System.err.print("Error: Formato de fecha incorrecto (debe ser AAAA-MM-DD). Intente de nuevo: ");
            }
        }
    }

    private Cobertura leerCobertura() {
        while (true) {
            try {
                String coberturaStr = scanner.nextLine().toUpperCase();
                return Cobertura.valueOf(coberturaStr); // Ej: "TODO_RIESGO"
            } catch (IllegalArgumentException e) {
                System.err.print("Error: Cobertura inválida (RC, TERCEROS, TODO_RIESGO). Intente de nuevo: ");
            }
        }
    }
}