package br.com.erbs.integradorhub.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties properties = new Properties();
    private static boolean carregado = false;

    public static void carregar() {
        if (carregado) {
            return; // Já carregado
        }
        
        String caminhoArquivo = System.getProperty("user.dir") + "/integrador_config.properties";

        try (FileInputStream input = new FileInputStream(caminhoArquivo)) {
            // Usar encoding UTF-8 e BufferedReader para melhor leitura
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(input, "UTF-8"))) {
                properties.load(reader);
                carregado = true;
                System.out.println("Configurações carregadas de: " + caminhoArquivo);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar o arquivo de configuração: " + caminhoArquivo, e);
        }
    }

    public static String get(String chave) {
        if (!carregado) {
            carregar();
        }
        
        String ambiente = properties.getProperty("ambiente");
        if (ambiente == null) {
            ambiente = "1";
        }
        
        // Se não for ambiente 1, adiciona sufixo
        String chaveCompleta = chave;
        if (!"1".equals(ambiente)) {
            chaveCompleta = chave + ambiente;
        }

        String valor = properties.getProperty(chaveCompleta);
        if (valor == null) {
            throw new RuntimeException("Propriedade não encontrada: " + chaveCompleta + " (ambiente: " + ambiente + ")");
        }
        
        return valor;
    }
    
    public static String getAmbiente() {
        if (!carregado) {
            carregar();
        }
        return properties.getProperty("ambiente", "1");
    }
}
