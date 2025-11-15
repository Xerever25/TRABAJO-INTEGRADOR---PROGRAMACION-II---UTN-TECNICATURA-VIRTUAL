package dao;

import entities.SeguroVehicular;
import entities.Cobertura;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDaoImpl {

    public void crear(SeguroVehicular s, Long vehiculoId, Connection conn) throws Exception {
        String sql = "INSERT INTO seguro_vehicular (eliminado, aseguradora, nroPoliza, cobertura, vencimiento, vehiculo_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, s.isEliminado());
            ps.setString(2, s.getAseguradora());
            ps.setString(3, s.getNroPoliza());
            ps.setString(4, s.getCobertura().name());
            ps.setDate(5, Date.valueOf(s.getVencimiento()));
            ps.setLong(6, vehiculoId);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getLong(1));
                }
            }
        }
    }

    public SeguroVehicular leer(long id, Connection conn) throws Exception {
        String sql = "SELECT * FROM seguro_vehicular WHERE id = ? AND eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public SeguroVehicular leerPorVehiculoId(long vehiculoId, Connection conn) throws Exception {
        String sql = "SELECT * FROM seguro_vehicular WHERE vehiculo_id = ? AND eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehiculoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<SeguroVehicular> leerTodos(Connection conn) throws Exception {
        List<SeguroVehicular> lista = new ArrayList<>();
        String sql = "SELECT * FROM seguro_vehicular WHERE eliminado = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        }
        return lista;
    }

    public void actualizar(SeguroVehicular s, Connection conn) throws Exception {
        String sql = "UPDATE seguro_vehicular SET aseguradora=?, nroPoliza=?, cobertura=?, vencimiento=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getAseguradora());
            ps.setString(2, s.getNroPoliza());
            ps.setString(3, s.getCobertura().name());
            ps.setDate(4, Date.valueOf(s.getVencimiento()));
            ps.setLong(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id, Connection conn) throws Exception {
        String sql = "UPDATE seguro_vehicular SET eliminado = TRUE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private SeguroVehicular mapResultSet(ResultSet rs) throws SQLException {
        return new SeguroVehicular(
                rs.getLong("id"),
                rs.getBoolean("eliminado"),
                rs.getString("aseguradora"),
                rs.getString("nroPoliza"),
                Cobertura.valueOf(rs.getString("cobertura")),
                rs.getDate("vencimiento").toLocalDate()
        );
    }
}
