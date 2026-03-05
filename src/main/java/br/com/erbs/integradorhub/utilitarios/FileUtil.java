package br.com.erbs.integradorhub.utilitarios;

import br.com.erbs.integradorhub.principal.Principal;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(Principal.class);

    public static boolean verificarOuCriarDiretorio(File diretorio) {
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                logger.info("Diretório criado com sucesso.");
            } else {
                logger.error("Falha ao criar o diretório.");
                return false;
            }
        } else if (!diretorio.isDirectory()) {
            logger.error("O caminho existe, mas não é um diretório.");
            return false;
        }
        return true;
    }
}
