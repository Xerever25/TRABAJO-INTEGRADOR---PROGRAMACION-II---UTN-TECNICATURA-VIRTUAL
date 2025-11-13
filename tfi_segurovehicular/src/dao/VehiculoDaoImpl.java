package dao;

import entities.Vehiculo;
import entities.SeguroVehicular;
import entities.Cobertura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDaoImpl implements GenericDao<Vehiculo> {

    @Override
    public void crear(Vehiculo v, Connection conn) throws Exception {
        String sql = "INSERT INTO vehiculo (eliminado, dominio, marca, modelo, anio, nro_chasis, seguro_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBoolean(1, v.getEliminado());
            ps.setString(2, v.getDominio());
            ps.setString(3, v.getMarca());
            ps.setString(4, v.getModelo());
            ps.setObject(5, v.getAnio(), Types.INTEGER);
            ps.setString(6, v.getNroChasis());

            if (v.getSeguro() != null) {
                ps.setLong(7, v.getSeguro().getId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) v.setId(rs.getLong(1));
            }
        }
    }

    @Override
    public Vehiculo leer(long id, Connection conn) throws Exception {
        String sql =
            "SELECT v.*, s.* " +
            "FROM vehiculo v " +
            "LEFT JOIN seguro_vehicular s ON v.seguro_id = s.id " +
            "WHERE v.id = ? AND v.eliminado = FALSE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Vehiculo> leerTodos(Connection conn) throws Exception {
        List<Vehiculo> lista = new ArrayList<>();

        String sql =
            "SELECT v.*, s.* " +
            "FROM vehiculo v " +
            "LEFT JOIN seguro_vehicular s ON v.seguro_id = s.id " +
            "WHERE v.eliminado = FALSE";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapResultSet(rs));
        }

        return lista;
    }

    @Override
    public void actualizar(Vehiculo v, Connection conn) throws Exception {
        String sql =
            "UPDATE vehiculo SET dominio=?, marca=?, modelo=?, anio=?, nro_chasis=?, seguro_id=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setObject(4, v.getAnio(), Types.INTEGER);
            ps.setString(5, v.getNroChasis());

            if (v.getSeguro() != null) {
                ps.setLong(6, v.getSeguro().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setLong(7, v.getId());

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

    // --------------------------------------------------------------
    //  MAPEO: ResultSet ? Vehiculo (y ? SeguroVehicular si existe)
    // --------------------------------------------------------------
    private Vehiculo mapResultSet(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo(
                rs.getLong("id"),
                rs.getBoolean("eliminado"),
                rs.getString("dominio"),
                rs.getString("marca"),
                rs.getString("modelo"),
                (Integer) rs.getObject("anio"),
                rs.getString("nro_chasis"),
                null // lo cargamos abajo
        );

        Long seguroId = rs.getLong("seguro_id");
        if (!rs.wasNull()) {
            SeguroVehicular s = new SeguroVehicular(
                    seguroId,
                    rs.getBoolean("eliminado"),
                    rs.getString("aseguradora"),
                    rs.getString("nro_poliza"),
                    Cobertura.valueOf(rs.getString("cobertura")),
                    rs.getDate("vencimiento").toLocalDate()
            );
            v.setSeguro(s);
        }

        return v;
    }
}
