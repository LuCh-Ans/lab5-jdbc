package lab5.db;

import lab5.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String PG_HOST = "localhost";
    private static final String PG_PORT = "5432";
    private static final String TARGET_DB = "shop_db";
    private static final String POSTGRES_DB = "postgres";

    private Connection conn;
    private String currentUser;

    public void connect(String username, String password) throws SQLException {
        conn = DriverManager.getConnection(buildUrl(TARGET_DB), username, password);
        currentUser = username;
    }

    public void disconnect() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }

    public boolean isConnected() {
        try { return conn != null && !conn.isClosed(); }
        catch (SQLException e) { return false; }
    }

    //CREATE DATABASE
    public void createDatabase(String superUser, String superPassword) throws SQLException {
        try (Connection sysConn = DriverManager.getConnection(
                buildUrl(POSTGRES_DB), superUser, superPassword)) {
            sysConn.setAutoCommit(true);
            try (Statement st = sysConn.createStatement()) {
                st.executeUpdate("CREATE DATABASE " + TARGET_DB);
            }
        }
        try (Connection initConn = DriverManager.getConnection(
                buildUrl(TARGET_DB), superUser, superPassword)) {
            runInitScript(initConn);
        }
    }

    private void runInitScript(Connection c) throws SQLException {
        try {
            var is = getClass().getResourceAsStream("/init.sql");
            if (is == null) throw new SQLException("init.sql не найден в classpath");
            String sql = new String(is.readAllBytes());

            List<String> statements = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean insideDollar = false;

            for (int i = 0; i < sql.length(); i++) {
                char ch = sql.charAt(i);

                if (ch == '$' && i + 1 < sql.length() && sql.charAt(i + 1) == '$') {
                    current.append("$$");
                    i++;
                    insideDollar = !insideDollar;
                    continue;
                }

                if (ch == ';' && !insideDollar) {
                    String stmt = current.toString().trim();
                    if (!stmt.isEmpty() && !stmt.startsWith("--")) {
                        statements.add(stmt);
                    }
                    current.setLength(0);
                } else {
                    current.append(ch);
                }
            }
            String last = current.toString().trim();
            if (!last.isEmpty() && !last.startsWith("--")) {
                statements.add(last);
            }

            for (String stmt : statements) {
                try (Statement st = c.createStatement()) {
                    st.execute(stmt);
                } catch (SQLException e) {
                    if (!e.getSQLState().startsWith("42")) throw e;
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Ошибка выполнения init.sql: " + e.getMessage(), e);
        }
    }

    //DROP DATABASE
    public void dropDatabase(String superUser, String superPassword) throws SQLException {
        disconnect();
        try (Connection sysConn = DriverManager.getConnection(
                buildUrl(POSTGRES_DB), superUser, superPassword)) {
            sysConn.setAutoCommit(true);
            try (Statement st = sysConn.createStatement()) {
                st.executeUpdate(
                    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                    "WHERE datname = '" + TARGET_DB + "' AND pid <> pg_backend_pid()");
                st.executeUpdate("DROP DATABASE IF EXISTS " + TARGET_DB);
            }
        }
    }

    //Хранимые процедуры
    public void createTable() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("CALL sp_create_table()")) {
            ps.execute();
        }
    }

    public void clearTable() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("CALL sp_clear_table()")) {
            ps.execute();
        }
    }

    public void addProduct(Product p) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CALL sp_add_product(?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getCategory());
            ps.setDouble(4, p.getPrice());
            ps.setBoolean(5, p.isInStock());
            ps.execute();
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM fn_get_all_products()");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Product> searchByBrand(String brand) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM fn_search_by_brand(?)")) {
            ps.setString(1, brand);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateProduct(Product p) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CALL sp_update_product(?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getBrand());
            ps.setString(4, p.getCategory());
            ps.setDouble(5, p.getPrice());
            ps.setBoolean(6, p.isInStock());
            ps.execute();
        }
    }

    public void deleteByBrand(String brand) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CALL sp_delete_by_brand(?)")) {
            ps.setString(1, brand);
            ps.execute();
        }
    }

    public void createUser(String username, String password, String role) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CALL sp_create_user(?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.execute();
        }
    }

    private String buildUrl(String db) {
        return "jdbc:postgresql://" + PG_HOST + ":" + PG_PORT + "/" + db;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("brand"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getBoolean("in_stock")
        );
    }

    public String getCurrentUser() { return currentUser; }
}
