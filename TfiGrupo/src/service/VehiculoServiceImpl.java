package service;

import config.DatabaseConnection;
import dao.SeguroVehicularDaoImpl;
import dao.VehiculoDaoImpl;
import entities.SeguroVehicular;
import entities.Vehiculo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class VehiculoServiceImpl implements VehiculoService {

    // Instancias de los DAOs (corregidos) que este servicio va a orquestar.
    private VehiculoDaoImpl vehiculoDao = new VehiculoDaoImpl();
    private SeguroVehicularDaoImpl seguroDao = new SeguroVehicularDaoImpl();

    /**
     * Inserta un Vehículo (A) y su Seguro (B) de forma atómica.
     * Si falla el seguro, se revierte la creación del vehículo.
     */
    @Override
    public void insertar(Vehiculo vehiculo) throws ServiceException {
        // Validaciones de negocio (Regla 1-a-1)
        if (vehiculo.getSeguro() == null) {
            throw new ServiceException("Error: No se puede crear un vehículo sin un seguro asociado.");
        }
        validarCampos(vehiculo); // Validar campos obligatorios

        Connection conn = null;
        try {
            // 1. Obtener Conexión e Iniciar Transacción
            conn = DatabaseConnection.getConnection(); //
            conn.setAutoCommit(false); // ¡INICIO DE TRANSACCIÓN!

            // 2. Orquestar DAOs
            // Paso A: Crear el Vehículo (A). El DAO le pondrá el ID.
            vehiculoDao.crear(vehiculo, conn);
            
            // Paso B: Crear el Seguro (B) usando el ID de A como FK.
            seguroDao.crear(vehiculo.getSeguro(), vehiculo.getId(), conn);

            // 3. Si todo OK, confirmar
            conn.commit(); // ¡COMMIT!

        } catch (Exception e) {
            // 4. Si algo falla, revertir
            if (conn != null) {
                try {
                    conn.rollback(); // ¡ROLLBACK!
                } catch (SQLException ex) {
                    throw new ServiceException("Error crítico: Falla en el rollback.", ex);
                }
            }
            throw new ServiceException("Error al insertar el vehículo: " + e.getMessage(), e);
        
        } finally {
            // 5. Devolver la conexión al estado normal
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // No cerramos la conexión porque Naza usó un patrón Singleton estático.
            }
        }
    }

    /**
     * Orquesta la lectura de un Vehículo (A) y su Seguro (B).
     */
    @Override
    public Vehiculo getById(long id) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Vehiculo vehiculo = vehiculoDao.leer(id, conn);
            if (vehiculo != null) {
                // Si encontramos Vehículo, buscamos su Seguro asociado
                SeguroVehicular seguro = seguroDao.leerPorVehiculoId(vehiculo.getId(), conn);
                vehiculo.setSeguro(seguro); // Asignamos el seguro al vehículo
            }
            return vehiculo; // Puede ser null si no se encontró
        } catch (Exception e) {
            throw new ServiceException("Error al leer por ID: " + e.getMessage(), e);
        }
    }

    /**
     * Orquesta la lectura de TODOS los Vehículos (A) y sus Seguros (B).
     */
    @Override
    public List<Vehiculo> getAll() throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Vehiculo> vehiculos = vehiculoDao.leerTodos(conn);
            // "Hidratar" cada vehículo con su seguro
            for (Vehiculo v : vehiculos) {
                SeguroVehicular seguro = seguroDao.leerPorVehiculoId(v.getId(), conn);
                v.setSeguro(seguro);
            }
            return vehiculos;
        } catch (Exception e) {
            throw new ServiceException("Error al listar vehículos: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un Vehículo (A) y su Seguro (B) de forma atómica.
     */
    @Override
    public void actualizar(Vehiculo vehiculo) throws ServiceException {
        validarCampos(vehiculo);
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            vehiculoDao.actualizar(vehiculo, conn);
            if (vehiculo.getSeguro() != null) {
                seguroDao.actualizar(vehiculo.getSeguro(), conn);
            }

            conn.commit(); // COMMIT

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /*...*/ }
            }
            throw new ServiceException("Error al actualizar: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /*...*/ }
            }
        }
    }

    /**
     * Elimina lógicamente un Vehículo (A) y su Seguro (B) de forma atómica.
     */
    @Override
    public void eliminar(long id) throws ServiceException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            // Necesitamos el Vehiculo para saber el ID del seguro
            Vehiculo vehiculo = this.getById(id); // Reusamos nuestro método
            if (vehiculo == null || vehiculo.getSeguro() == null) {
                throw new ServiceException("El vehiculo o su seguro no existen (ID: " + id + ").");
            }

            // Damos de baja B (Seguro)
            seguroDao.eliminar(vehiculo.getSeguro().getId(), conn);
            // Damos de baja A (Vehiculo)
            vehiculoDao.eliminar(id, conn);

            conn.commit(); // COMMIT

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /*...*/ }
            }
            throw new ServiceException("Error al eliminar: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /*...*/ }
            }
        }
    }

    @Override
    public Vehiculo getByDominio(String dominio) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Vehiculo vehiculo = vehiculoDao.leerPorDominio(dominio, conn);
            if (vehiculo != null) {
                // Si encontramos Vehículo, buscamos su Seguro asociado
                SeguroVehicular seguro = seguroDao.leerPorVehiculoId(vehiculo.getId(), conn);
                vehiculo.setSeguro(seguro);
            }
            return vehiculo;
        } catch (Exception e) {
            throw new ServiceException("Error al buscar por dominio: " + e.getMessage(), e);
        }
    }
    
    // Método privado para validaciones de negocio
    private void validarCampos(Vehiculo v) throws ServiceException {
        if (v == null) throw new ServiceException("El vehiculo no puede ser nulo.");
        if (v.getDominio() == null || v.getDominio().trim().isEmpty() || v.getDominio().length() > 10) {
            throw new ServiceException("El dominio es obligatorio y debe tener máx. 10 caracteres.");
        }
        if (v.getMarca() == null || v.getMarca().trim().isEmpty()) {
            throw new ServiceException("La marca es obligatoria.");
        }
        if (v.getNroChasis() == null || v.getNroChasis().trim().isEmpty()) {
            throw new ServiceException("El Nro. de Chasis es obligatorio.");
        }
        if (v.getSeguro().getAseguradora() == null || v.getSeguro().getAseguradora().trim().isEmpty()) {
            throw new ServiceException("La aseguradora es obligatoria.");
        }
        if (v.getSeguro().getNroPoliza() == null || v.getSeguro().getNroPoliza().trim().isEmpty()) {
            throw new ServiceException("El Nro. de Póliza es obligatorio.");
        }
        if (v.getSeguro().getVencimiento() == null) {
            throw new ServiceException("La fecha de vencimiento es obligatoria.");
        }
    }
}
