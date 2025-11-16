package dao;

import entities.Vehiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDaoImpl implements GenericDao<Vehiculo> {

    @Override
    public void crear(Vehiculo v, Connection conn) throws Exception {
        // CORREGIDO: Se quitó 'seguro_id' del INSERT, no existe en la tabla 'vehiculo'.
        String sql = "INSERT INTO vehiculo (eliminado, dominio, marca, modelo, anio, nroChasis) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, v.getEliminado());
            ps.setString(2, v.getDominio());
            ps.setString(3, v.getMarca());
            ps.setString(4, v.getModelo());
            ps.setObject(5, v.getAnio(), Types.INTEGER); // Usar setObject para Integers anulables
            ps.setString(6, v.getNroChasis());

            ps.executeUpdate();

            // Esto es VITAL: Poner el ID generado en el objeto.
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Vehiculo leer(long id, Connection conn) throws Exception {
        // CORREGIDO: Se quitó el JOIN. El Service orquestará la búsqueda del seguro.
        String sql = "SELECT * FROM vehiculo WHERE id = ? AND eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null; // No se encontró
    }

    @Override
    public List<Vehiculo> leerTodos(Connection conn) throws Exception {
        List<Vehiculo> lista = new ArrayList<>();
        // CORREGIDO: Se quitó el JOIN.
        String sql = "SELECT * FROM vehiculo WHERE eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    @Override
    public void actualizar(Vehiculo v, Connection conn) throws Exception {
        // CORREGIDO: Se quitó 'seguro_id' del UPDATE.
        String sql = "UPDATE vehiculo SET dominio=?, marca=?, modelo=?, anio=?, nroChasis=? "
                   + "WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setObject(4, v.getAnio(), Types.INTEGER);
            ps.setString(5, v.getNroChasis());
            ps.setLong(6, v.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(long id, Connection conn) throws Exception {
        String sql = "UPDATE vehiculo SET eliminado = TRUE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // --- MÉTODOS NUEVOS (Útiles para tu Service) ---

    public Vehiculo leerPorDominio(String dominio, Connection conn) throws Exception {
        String sql = "SELECT * FROM vehiculo WHERE dominio = ? AND eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dominio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // --- MAPEO ---
    
    private Vehiculo mapResultSet(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getLong("id"));
        v.setEliminado(rs.getBoolean("eliminado"));
        v.setDominio(rs.getString("dominio"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio((Integer) rs.getObject("anio"));
        v.setNroChasis(rs.getString("nroChasis"));
        // El seguro (v.setSeguro) se asigna en la capa de SERVICIO.
        return v;
    }
}