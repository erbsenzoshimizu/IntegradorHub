package br.com.erbs.integradorhub.modelos;

public class LogEntry {

    private final String mensagem;
    private final String nivel;

    public LogEntry(String mensagem, String nivel) {
        this.mensagem = mensagem;
        this.nivel = nivel;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getNivel() {
        return nivel;
    }

}
