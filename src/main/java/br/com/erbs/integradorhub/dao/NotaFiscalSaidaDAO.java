package br.com.erbs.integradorhub.dao;

import br.com.erbs.integradorhub.conexao.OracleConnection;
import br.com.erbs.integradorhub.utilitarios.DataUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Map;

public class NotaFiscalSaidaDAO {

    public void autorizarNfce(Map<String, String> nfce) {
        String qry = "UPDATE E140IDE\n"
                + "   SET SITDOE = ?,\n"
                + "       NUMPRT = ?,\n"
                + "	   DATAUT = ?,\n"
                + "	   HORAUT = ?\n"
                + " WHERE CHVDOE = ?";

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(nfce.get("DataAutorizacao"));

        LocalDate data = offsetDateTime.toLocalDate();
        LocalTime hora = offsetDateTime.toLocalTime();
        Integer horaInteiro = DataUtil.converteHoraParaInteiro(hora);

        try (Connection con = OracleConnection.openConnection();
                PreparedStatement pst = con.prepareStatement(qry)) {
            pst.setInt(1, 3);
            pst.setString(2, nfce.get("ProtocoloAutorizacao"));
            pst.setDate(3, Date.valueOf(data));
            pst.setInt(4, horaInteiro);
            pst.setString(5, nfce.get("ChaveDocumento"));

            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Erro ao gravar ocorrência: " + ex.getMessage());
        }
    }

}
