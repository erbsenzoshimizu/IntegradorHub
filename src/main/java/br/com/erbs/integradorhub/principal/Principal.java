package br.com.erbs.integradorhub.principal;

import br.com.erbs.integradorhub.processador.ProcessadorXml;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class Principal extends javax.swing.JFrame {

    private static final Logger logger = LoggerFactory.getLogger(Principal.class);
    private RandomAccessFile logReader;
    private long logFilePointer = 0;
    private Timer tailTimer;
    private static final String LOG_DIR = "c:/temp/logs/";
    private String logFileNameAtual = "";

    // caminhos de diretório
    private static final String BASE_DIR = "\\\\192.168.2.40\\xml\\NFCe\\";
    private static final String PROCESSAR_DIR = BASE_DIR + "Processar\\";
    private static final String AUTORIZAR_DIR = BASE_DIR + "Autorizar\\";
    private static final String AUTORIZADO_DIR = BASE_DIR + "Autorizado\\";
    private static final String REJEITADO_DIR = BASE_DIR + "Rejeitado\\";

    private static final long EXEC_INTERVAL_MS = 10_000L; // intervalo de execução (milissegundos)    

    private Timer execTimer;
    private SwingWorker<Void, Void> worker;

    public Principal() {
        initComponents();

        iniciarTailer();
    }

    public void agendarProcessamento() {
        logger.info("Integração iniciada pelo usuário.");

        if (execTimer == null) {
            execTimer = new Timer((int) EXEC_INTERVAL_MS, e -> {
                try {
                    processarArquivos();
                } catch (Exception ex) {
                    logger.error("Erro na execução: " + ex.getMessage());
                }
            });

            execTimer.start();
        }
    }

    public void pararProcessamento() {
        if (execTimer != null && execTimer.isRunning()) {
            execTimer.stop();
            logger.info("Integração parado pelo usuário.");
        }
    }

    public void processarArquivos() {
        if (worker != null && !worker.isDone()) {
            return;
        }

        worker = new SwingWorker<Void, Void>() {
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
                logger.error("Arquivo inválido: " + xml.getName());
                return;
            }

            logger.info("Processando: " + xml.getAbsolutePath());

            try {
                ProcessadorXml.processarXml(xml.getAbsolutePath(), AUTORIZAR_DIR + xml.getName());
            } catch (Exception e) {
                logger.error("Erro ao processar " + xml.getName() + ": " + e.getMessage());
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
                logger.error("Arquivo inválido: " + xml.getName());
                return;
            }

            logger.info("Autorizando: " + xml.getAbsolutePath());

            try {
                ProcessadorXml.autorizarXml(xml.getAbsolutePath(), AUTORIZADO_DIR + xml.getName(), REJEITADO_DIR + xml.getName());
            } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
                logger.error("Erro ao processar " + xml.getName() + ": " + e.getMessage());
            }
        }
    }

    private boolean verificarOuCriarDiretorio(File diretorio) {
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

    public void adicionarLog(String mensagem, String nivel) {
        StyledDocument doc = jTextPaneLogs.getStyledDocument();

        Style estiloPadrao = jTextPaneLogs.addStyle("info", null);
        StyleConstants.setForeground(estiloPadrao, Color.black);

        Style estiloErro = jTextPaneLogs.addStyle("error", null);
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

    private String getLogFileName() {
        String hoje = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return LOG_DIR + "integradorhub-" + hoje + ".log";
    }

    private void iniciarTailer() {
        tailTimer = new Timer(1000, e -> {
            try {
                String novoLogFileName = getLogFileName(); // ex: integradorhub-2025-05-08.log

                // Se o nome mudou (nova data), reabra o arquivo
                if (!novoLogFileName.equals(logFileNameAtual)) {
                    if (logReader != null) {
                        logReader.close();
                    }
                    File logFile = new File(novoLogFileName);
                    if (!logFile.exists()) {
                        logFile.getParentFile().mkdirs();
                        logFile.createNewFile();
                    }
                    logReader = new RandomAccessFile(logFile, "r");
                    logFilePointer = logFile.length(); // começa do fim
                    logFileNameAtual = novoLogFileName;
                }

                long fileLength = logReader.length();
                if (fileLength > logFilePointer) {
                    logReader.seek(logFilePointer);
                    String line;
                    while ((line = logReader.readLine()) != null) {
                        String decoded = new String(line.getBytes("ISO-8859-1"), "UTF-8");

                        // Extrai o nível de log usando regex
                        String nivel = "info"; // valor padrão
                        Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} ([A-Z]{4,5})").matcher(decoded);
                        if (matcher.find()) {
                            nivel = matcher.group(1).toLowerCase(); // ex: "error", "info", etc.
                        }

                        adicionarLog(decoded + "\n", nivel);

                    }
                    logFilePointer = logReader.getFilePointer();
                }
            } catch (IOException ex) {
                logger.error("Erro ao ler log: " + ex.getMessage());
            }
        });
        tailTimer.start();
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
        jButtonIniciar = new javax.swing.JButton();
        jButton2Parar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Integrador Hub ");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(0, 0, 0));

        jTextPaneLogs.setEditable(false);
        jScrollPane1.setViewportView(jTextPaneLogs);

        jButtonIniciar.setText("Iniciar");
        jButtonIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIniciarActionPerformed(evt);
            }
        });

        jButton2Parar.setText("Parar");
        jButton2Parar.setEnabled(false);
        jButton2Parar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2PararActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/customer_service_robot_lg_wm.gif"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonIniciar)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2Parar)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 665, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonIniciar)
                    .addComponent(jButton2Parar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIniciarActionPerformed
        // TODO add your handling code here:
        agendarProcessamento();
        jButtonIniciar.setEnabled(false);
        jButton2Parar.setEnabled(true);
    }//GEN-LAST:event_jButtonIniciarActionPerformed

    private void jButton2PararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2PararActionPerformed
        // TODO add your handling code here:
        pararProcessamento();
        jButtonIniciar.setEnabled(true);
        jButton2Parar.setEnabled(false);
    }//GEN-LAST:event_jButton2PararActionPerformed

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
    private javax.swing.JButton jButton2Parar;
    private javax.swing.JButton jButtonIniciar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPaneLogs;
    // End of variables declaration//GEN-END:variables
}
