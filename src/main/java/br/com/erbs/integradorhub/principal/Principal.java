package br.com.erbs.integradorhub.principal;

import br.com.erbs.integradorhub.processador.ProcessadorXml;
import br.com.erbs.integradorhub.util.ConfigLoader;
import br.com.erbs.integradorhub.utilitarios.FileUtil;
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
    private static final String LOG_DIR = System.getProperty("user.dir") + "/logs/";
    private String logFileNameAtual = "";

    // caminhos de diretório
    private String xmlDir;
    private String processarDir;
    private String autorizarDir;
    private String autorizadoDir;
    private String rejeitadoDir;

    private static final long EXEC_INTERVAL_MS = 10_000L; // intervalo de execução (milissegundos)    

    private Timer execTimer;
    private SwingWorker<Void, Void> worker;

    public Principal() {
        initComponents();

        iniciarTailer();

        carregaParametros();

    }

    private void carregaParametros() {
        
        ConfigLoader.carregar();

        xmlDir = ConfigLoader.get("xmlDir");
        processarDir = xmlDir + "Processar\\";
        autorizarDir = xmlDir + "Autorizar\\";
        autorizadoDir = xmlDir + "Autorizado\\";
        rejeitadoDir = xmlDir + "Rejeitado\\";
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
        File diretorio = new File(processarDir);

        if (!FileUtil.verificarOuCriarDiretorio(diretorio)) {
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
                ProcessadorXml.processarXml(xml.getAbsolutePath(), autorizarDir + xml.getName());
            } catch (Exception e) {
                logger.error("Erro ao processar " + xml.getName() + ": " + e.getMessage());
            }
        }
    }

    public void autorizarArquivosXml() {
        File diretorio = new File(autorizarDir);

        if (!FileUtil.verificarOuCriarDiretorio(diretorio)) {
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
                ProcessadorXml.autorizarXml(xml.getAbsolutePath(), autorizadoDir + xml.getName(), rejeitadoDir + xml.getName());
            } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
                logger.error("Erro ao processar " + xml.getName() + ": " + e.getMessage());
            }
        }
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

                    jTextPaneLogs.setText(""); //Limpa a tela na virada do dia para não fica muito longo
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
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButtonIniciar = new javax.swing.JButton();
        jButton2Parar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Integrador Hub ");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(0, 0, 0));

        jTextPaneLogs.setEditable(false);
        jTextPaneLogs.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane1.setViewportView(jTextPaneLogs);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("Integrador Hub");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(388, 388, 388)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/robot.png"))); // NOI18N

        jLabelStatus.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabelStatus.setForeground(new java.awt.Color(255, 0, 0));
        jLabelStatus.setText("Parado");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jLabel1)
                .addGap(48, 48, 48)
                .addComponent(jLabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(121, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabelStatus))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(356, 356, 356)
                .addComponent(jButtonIniciar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2Parar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonIniciar)
                    .addComponent(jButton2Parar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIniciarActionPerformed
        // TODO add your handling code here:
        agendarProcessamento();
        jButtonIniciar.setEnabled(false);
        jButton2Parar.setEnabled(true);
        jLabelStatus.setText("Executando...");
        jLabelStatus.setForeground(Color.GREEN);
    }//GEN-LAST:event_jButtonIniciarActionPerformed

    private void jButton2PararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2PararActionPerformed
        // TODO add your handling code here:
        pararProcessamento();
        jButtonIniciar.setEnabled(true);
        jButton2Parar.setEnabled(false);
        jLabelStatus.setText("Parado");
        jLabelStatus.setForeground(Color.RED);
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPaneLogs;
    // End of variables declaration//GEN-END:variables
}
