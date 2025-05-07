package br.com.erbs.integradorhub.utilitarios;

import java.time.LocalTime;

public class DataUtil {

    public static Integer converteHoraParaInteiro(LocalTime horaAlteracao) {
        if (horaAlteracao == null) {
            return 0;
        }

        int horas = horaAlteracao.getHour();
        int minutos = horaAlteracao.getMinute();

        return (horas * 60) + minutos;
    }
}
