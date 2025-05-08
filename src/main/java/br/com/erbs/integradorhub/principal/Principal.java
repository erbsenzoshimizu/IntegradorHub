package br.com.erbs.integradorhub.principal;

import br.com.erbs.integradorhub.modelos.LogEntry;
import br.com.erbs.integradorhub.processador.ProcessadorXml;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public final class Principal extends javax.swing.JFrame {

    // caminhos de diretório
    private static final String BASE_DIR = "\\\\192.168.2.40\\xml\\NFCe\\";
    private static final String PROCESSAR_DIR = BASE_DIR + "Processar\\";
    private static final String AUTORIZAR_DIR = BASE_DIR + "Autorizar\\";
    private static final String AUTORIZADO_DIR = BASE_DIR + "Autorizado\\";
    private static final String REJEITADO_DIR = BASE_DIR + "Rejeitado\\";

    private static final long EXEC_INTERVAL_MS = 10_000L; // intervalo de execução (milissegundos)    

    private Timer execTimer;
    private SwingWorker<Void, LogEntry> worker;

    public Principal() {
        initComponents();

        agendarProcessamento();
    }

    public void agendarProcessamento() {
        if (execTimer == null) {
            execTimer = new Timer((int) EXEC_INTERVAL_MS, e -> {
                try {
                    processarArquivos();
                } catch (Exception ex) {
                    System.err.println("Erro na execução: " + ex.getMessage());
                }
            });

            execTimer.start();
        }
    }

    public void processarArquivos() {
        if (worker != null && !worker.isDone()) {
            return;
        }

        worker = new SwingWorker<Void, LogEntry>() {
            @Override
            protected Void doInBackground() throws Exception {
                processarArquivosXml();
                autorizarArquivosXml();

                return null;
            }

            @Override
            protected void done() {
                worker = null;
            }
        };
        worker.execute();
    }

    public void processarArquivosXml() {
        File diretorio = new File(PROCESSAR_DIR);

        if (!verificarOuCriarDiretorio(diretorio)) {
            return;
        }

        FilenameFilter filtroXML = (dir, nome) -> nome.toLowerCase().endsWith(".xml");
        File[] arquivosXML = diretorio.listFiles(filtroXML);

        if (arquivosXML == null || arquivosXML.length == 0) {
            return;
        }

        // Processa os arquivos
        for (File xml : arquivosXML) {
            if (!xml.isFile()) {
                adicionarLog("Arquivo inválido: " + xml.getName(), "erro");
                return;
            }

            adicionarLog("Processando: " + xml.getAbsolutePath(), null);

            try {
                ProcessadorXml.processarXml(xml.getAbsolutePath(), AUTORIZAR_DIR + xml.getName());
            } catch (Exception e) {
                adicionarLog("Erro ao processar " + xml.getName() + ": " + e.getMessage(), "erro");
            }
        }
    }

    public void autorizarArquivosXml() {
        File diretorio = new File(AUTORIZAR_DIR);

        if (!verificarOuCriarDiretorio(diretorio)) {
            return;
        }

        FilenameFilter filtroXML = (dir, nome) -> nome.toLowerCase().endsWith(".xml");
        File[] arquivosXML = diretorio.listFiles(filtroXML);

        if (arquivosXML == null || arquivosXML.length == 0) {
            return;
        }

        // Processa os arquivos
        for (File xml : arquivosXML) {
            if (!xml.isFile()) {
                adicionarLog("Arquivo inválido: " + xml.getName(), "erro");
                return;
            }

            adicionarLog("Autorizando: " + xml.getAbsolutePath(), null);

            try {
                ProcessadorXml.autorizarXml(xml.getAbsolutePath(), AUTORIZADO_DIR + xml.getName(), REJEITADO_DIR + xml.getName());
            } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
                adicionarLog("Erro ao processar " + xml.getName() + ": " + e.getMessage(), "erro");
            }
        }
    }

    private boolean verificarOuCriarDiretorio(File diretorio) {
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                adicionarLog("Diretório criado com sucesso.", null);
            } else {
                adicionarLog("Falha ao criar o diretório.", "erro");
                return false;
            }
        } else if (!diretorio.isDirectory()) {
            adicionarLog("O caminho existe, mas não é um diretório.", "erro");
            return false;
        }
        return true;
    }

    public void adicionarLog(String mensagem, String nivel) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        mensagem = "[" + timestamp + "] " + mensagem + "\n";

        System.out.println(mensagem);

        StyledDocument doc = jTextPaneLogs.getStyledDocument();

        Style estiloPadrao = jTextPaneLogs.addStyle("padrao", null);
        StyleConstants.setForeground(estiloPadrao, Color.black);

        Style estiloErro = jTextPaneLogs.addStyle("erro", null);
        StyleConstants.setForeground(estiloErro, Color.red);

        Style estilo = estiloPadrao;

        if ("erro".equalsIgnoreCase(nivel)) {
            estilo = estiloErro;
        }

        try {
            doc.insertString(doc.getLength(), mensagem, estilo);
        } catch (BadLocationException ex) {
            System.out.println(ex.getMessage());
        }

        jTextPaneLogs.setCaretPosition(jTextPaneLogs.getDocument().getLength());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneLogs = new javax.swing.JTextPane();
        txtLog = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Integrador Hub ");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(0, 0, 0));

        jTextPaneLogs.setEditable(false);
        jScrollPane1.setViewportView(jTextPaneLogs);

        txtLog.setEditable(false);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(109, 109, 109)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 835, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtLog, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtLog, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.out.println("Tema não suportado: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            new Principal().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextPane jTextPaneLogs;
    private static javax.swing.JTextPane txtLog;
    // End of variables declaration//GEN-END:variables
}
