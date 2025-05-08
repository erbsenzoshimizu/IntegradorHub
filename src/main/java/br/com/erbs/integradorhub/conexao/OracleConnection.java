package br.com.erbs.integradorhub.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleConnection {

    private static final Logger logger = LoggerFactory.getLogger(OracleConnection.class);

    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    private static final String ORACLE_URL = "jdbc:oracle:thin:@192.168.2.4:1521/dbsenior";
    private static final String USER = "sapiensteste3";
    private static final String PASSWORD = "sbresapiensteste3";

    /*
    private static final String ORACLE_URL = "jdbc:oracle:thin:@192.168.2.3:1521/dbsenior";
    private static final String USER = "sapiens";
    private static final String PASSWORD = "sbresapiens";
     */
    public static Connection openConnection() {
        Connection dbconn = null;

        try {
            if (dbconn == null) {
                Class.forName(ORACLE_DRIVER);
            }

            Properties dbCredentials = new Properties();
            dbCredentials.put("user", USER);
            dbCredentials.put("password", PASSWORD);

            dbconn = DriverManager.getConnection(ORACLE_URL, dbCredentials);

        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC não encontrado: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            logger.error("Erro ao conectar ao Banco de Dados: " + e.getMessage());
            return null;
        }

        return dbconn;
    }

}
