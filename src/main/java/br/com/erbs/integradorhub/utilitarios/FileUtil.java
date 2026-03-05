package br.com.erbs.integradorhub.utilitarios;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static boolean verificarOuCriarDiretorio(File diretorio) {
        if (diretorio == null) {
            logger.error("Diretório é nulo");
            return false;
        }
        
        logger.debug("Verificando diretório: {}", diretorio.getAbsolutePath());
        
        if (!diretorio.exists()) {
            logger.info("Diretório não existe, tentando criar: {}", diretorio.getAbsolutePath());
            if (diretorio.mkdirs()) {
                logger.info("Diretório criado com sucesso: {}", diretorio.getAbsolutePath());
            } else {
                logger.error("Falha ao criar o diretório: {}", diretorio.getAbsolutePath());
                logger.error("Verifique permissões e espaço em disco");
                return false;
            }
        } else if (!diretorio.isDirectory()) {
            logger.error("O caminho existe, mas não é um diretório: {}", diretorio.getAbsolutePath());
            return false;
        } else {
            logger.debug("Diretório já existe: {}", diretorio.getAbsolutePath());
        }
        return true;
    }
}
