package br.com.erbs.integradorhub.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties properties = new Properties();

    public static void carregar() {
        String caminhoArquivo = System.getProperty("user.dir") + "/integrador_config.properties";

        try (FileInputStream input = new FileInputStream(caminhoArquivo)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar o arquivo de configuração: " + caminhoArquivo, e);
        }
    }

    public static String get(String chave) {
        String ambiente = properties.getProperty("ambiente");
        chave += ambiente.equals("1") ? "" : ambiente;

        return properties.getProperty(chave);
    }
}
