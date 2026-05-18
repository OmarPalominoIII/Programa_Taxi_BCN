package database;

import project_models.*;
import project_models.Driver;

import java.sql.*;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:taxi_bcn.db";
    private Connection conexion;

    // ─────────────────────────────────────────────────────────────
    // CONEXIÓN
    // ─────────────────────────────────────────────────────────────

    public void conectar() throws SQLException {
        conexion = DriverManager.getConnection(URL);
        conexion.setAutoCommit(true);

        // SQLite no aplica FK por defecto — hay que activarlas en cada conexión
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        System.out.println("[DB] Conectado a taxi_bcn.db");
    }

    public void desconectar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("[DB] Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al cerrar: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE TABLE  (se crean al arrancar, solo si no existen)
    // ─────────────────────────────────────────────────────────────

    public void crearTablas() throws SQLException {
        try (Statement stmt = conexion.createStatement()) {

            // Tabla CONDUCTORES
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS conductores (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre        TEXT    NOT NULL,
                    apellido      TEXT    NOT NULL,
                    edad          INTEGER NOT NULL,
                    dni           TEXT    NOT NULL UNIQUE,
                    licencia_taxi TEXT    NOT NULL
                )
            """);

            // Tabla CLIENTES
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clientes (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre   TEXT    NOT NULL,
                    apellido TEXT    NOT NULL,
                    edad     INTEGER NOT NULL,
                    dni      TEXT    NOT NULL UNIQUE,
                    telefono TEXT    NOT NULL
                )
            """);

            // Tabla TAXIS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS taxis (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    matricula    TEXT    NOT NULL UNIQUE,
                    color        TEXT    NOT NULL,
                    capacidad    INTEGER NOT NULL,
                    id_conductor INTEGER,
                    fila         INTEGER NOT NULL DEFAULT 0,
                    columna      INTEGER NOT NULL DEFAULT 0,
                    tipo         TEXT    NOT NULL,
                    estado       TEXT    NOT NULL DEFAULT 'AVAILABLE',
                    FOREIGN KEY (id_conductor) REFERENCES conductores(id)
                )
            """);

            // Tabla SERVICIOS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS servicios (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo         INTEGER NOT NULL UNIQUE,
                    id_cliente     INTEGER,
                    id_taxi        INTEGER,
                    fila_recogida  INTEGER NOT NULL,
                    col_recogida   INTEGER NOT NULL,
                    tipo_taxi      TEXT    NOT NULL,
                    estado         TEXT    NOT NULL DEFAULT 'PENDING',
                    hora_solicitud TEXT    NOT NULL,
                    FOREIGN KEY (id_cliente) REFERENCES clientes(id),
                    FOREIGN KEY (id_taxi)    REFERENCES taxis(id)
                )
            """);

            System.out.println("[DB] Tablas creadas correctamente.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT — conductores
    // ─────────────────────────────────────────────────────────────

    public int insertarConductor(Driver conductor) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO conductores (nombre, apellido, edad, dni, licencia_taxi)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, conductor.getFirstName());
            ps.setString(2, conductor.getLastName());
            ps.setInt   (3, conductor.getAge());
            ps.setString(4, conductor.getNationalId());
            ps.setString(5, conductor.getTaxiLicense());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return buscarIdConductorPorDni(conductor.getNationalId());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT — clientes
    // ─────────────────────────────────────────────────────────────

    public int insertarCliente(Customer cliente) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO clientes (nombre, apellido, edad, dni, telefono)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getFirstName());
            ps.setString(2, cliente.getLastName());
            ps.setInt   (3, cliente.getAge());
            ps.setString(4, cliente.getNationalId());
            ps.setString(5, cliente.getPhoneNumber());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return buscarIdClientePorDni(cliente.getNationalId());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT — taxis
    // ─────────────────────────────────────────────────────────────

    public int insertarTaxi(Taxi taxi) throws SQLException {
        int idConductor = (taxi.getDriver() != null) ? insertarConductor(taxi.getDriver()) : -1;

        String sql = """
            INSERT OR IGNORE INTO taxis (matricula, color, capacidad, id_conductor, fila, columna, tipo, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, taxi.getLicensePlate());
            ps.setString(2, taxi.getColor());
            ps.setInt   (3, taxi.getCapacity());
            if (idConductor > 0) ps.setInt(4, idConductor); else ps.setNull(4, Types.INTEGER);
            ps.setInt   (5, taxi.getPosition().getRow());
            ps.setInt   (6, taxi.getPosition().getColumn());
            ps.setString(7, taxi.getType().name());
            ps.setString(8, taxi.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return buscarIdTaxiPorMatricula(taxi.getLicensePlate());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT — servicios
    // ─────────────────────────────────────────────────────────────

    public int insertarServicio(ServiceRequest servicio) throws SQLException {
        int idCliente = insertarCliente(servicio.getCustomer());
        int idTaxi    = (servicio.getTaxi() != null) ? insertarTaxi(servicio.getTaxi()) : -1;

        String sql = """
            INSERT OR IGNORE INTO servicios
                (codigo, id_cliente, id_taxi, fila_recogida, col_recogida, tipo_taxi, estado, hora_solicitud)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, servicio.getServiceCode());
            ps.setInt   (2, idCliente);
            if (idTaxi > 0) ps.setInt(3, idTaxi); else ps.setNull(3, Types.INTEGER);
            ps.setInt   (4, servicio.getCustomerPosition().getRow());
            ps.setInt   (5, servicio.getCustomerPosition().getColumn());
            ps.setString(6, servicio.getTaxirequired().name());
            ps.setString(7, servicio.getServiceStatus().name());
            ps.setString(8, servicio.getRequestTime().toString());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Búsquedas auxiliares
    // ─────────────────────────────────────────────────────────────

    private int buscarIdConductorPorDni(String dni) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id FROM conductores WHERE dni = ?")) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private int buscarIdClientePorDni(String dni) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id FROM clientes WHERE dni = ?")) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private int buscarIdTaxiPorMatricula(String matricula) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id FROM taxis WHERE matricula = ?")) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
}
