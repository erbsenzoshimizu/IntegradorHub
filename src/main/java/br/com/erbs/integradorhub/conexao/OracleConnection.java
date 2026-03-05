package br.com.erbs.integradorhub.conexao;

import br.com.erbs.integradorhub.util.ConfigLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleConnection {

    private static final Logger logger = LoggerFactory.getLogger(OracleConnection.class);

    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    public static Connection openConnection() throws SQLException {
        try {
            Class.forName(ORACLE_DRIVER);

            String url = ConfigLoader.get("urlDB");
            String user = ConfigLoader.get("userDB");
            String password = ConfigLoader.get("passwordDB");

            Properties dbCredentials = new Properties();
            dbCredentials.put("user", user);
            dbCredentials.put("password", password);

            return DriverManager.getConnection(url, dbCredentials);

        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC não encontrado: " + e.getMessage(), e);
            throw new SQLException("Driver JDBC não encontrado", e);
        }
    }

}
