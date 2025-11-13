/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entities.SeguroVehicular;
import entities.Cobertura;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDaoImpl implements GenericDao<SeguroVehicular> {

    @Override
    public void crear(SeguroVehicular s, Connection conn) throws Exception {
        String sql = "INSERT INTO seguro_vehicular (eliminado, aseguradora, nro_poliza, cobertura, vencimiento) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, s.isEliminado());
            ps.setString(2, s.getAseguradora());
            ps.setString(3, s.getNroPoliza());
            ps.setString(4, s.getCobertura().name());
            ps.setDate(5, Date.valueOf(s.getVencimiento()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
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

    @Override
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

    @Override
    public void actualizar(SeguroVehicular s, Connection conn) throws Exception {
        String sql = "UPDATE seguro_vehicular SET aseguradora=?, nro_poliza=?, cobertura=?, vencimiento=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getAseguradora());
            ps.setString(2, s.getNroPoliza());
            ps.setString(3, s.getCobertura().name());
            ps.setDate(4, Date.valueOf(s.getVencimiento()));
            ps.setLong(5, s.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(long id, Connection conn) throws Exception {
        String sql = "UPDATE seguro_vehicular SET eliminado = TRUE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // ? Método auxiliar: convierte una fila del ResultSet en un objeto SeguroVehicular
    private SeguroVehicular mapResultSet(ResultSet rs) throws SQLException {
        return new SeguroVehicular(
                rs.getLong("id"),
                rs.getBoolean("eliminado"),
                rs.getString("aseguradora"),
                rs.getString("nro_poliza"),
                Cobertura.valueOf(rs.getString("cobertura")),
                rs.getDate("vencimiento").toLocalDate()
        );
    }
}

