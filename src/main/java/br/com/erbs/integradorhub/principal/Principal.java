package br.com.erbs.integradorhub.principal;

import br.com.erbs.integradorhub.processador.ProcessadorXml;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
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
    private SwingWorker<Void, Void> worker;

    public Principal() {
        initComponents();

        adicionarLog("Iniciando...");

        agendarProcessamento();
    }

    public void agendarProcessamento() {
        if (execTimer == null) {
            execTimer = new Timer((int) EXEC_INTERVAL_MS, e -> {
                try {
                    processarArquivos();
                } catch (Exception ex) {
                    adicionarLog("Erro na execução: " + ex.getMessage());
                }
            });
            execTimer.start();
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
                adicionarLog("Arquivo inválido: " + xml.getName());
                return;
            }

            adicionarLog("Processando: " + xml.getAbsolutePath());

            try {
                ProcessadorXml.processarXml(xml.getAbsolutePath(), AUTORIZAR_DIR + xml.getName());
            } catch (Exception e) {
                adicionarLog("Erro ao processar " + xml.getName() + ": " + e.getMessage());
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
                adicionarLog("Arquivo inválido: " + xml.getName());
                return;
            }

            adicionarLog("Autorizando: " + xml.getAbsolutePath());

            try {
                ProcessadorXml.autorizarXml(xml.getAbsolutePath(), AUTORIZADO_DIR + xml.getName(), REJEITADO_DIR + xml.getName());
            } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
                adicionarLog("Erro ao processar " + xml.getName() + ": " + e.getMessage());
            }
        }
    }

    private boolean verificarOuCriarDiretorio(File diretorio) {
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                adicionarLog("Diretório criado com sucesso.");
            } else {
                adicionarLog("Falha ao criar o diretório.");
                return false;
            }
        } else if (!diretorio.isDirectory()) {
            adicionarLog("O caminho existe, mas não é um diretório.");
            return false;
        }
        return true;
    }

    public void adicionarLog(String mensagem) {
        System.out.println(mensagem);

        StyledDocument doc = jTextPaneLogs.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), mensagem, null);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Integrador Hub ");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(0, 0, 0));

        jTextPaneLogs.setEditable(false);
        jScrollPane1.setViewportView(jTextPaneLogs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(273, 273, 273)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JTextPane jTextPaneLogs;
    // End of variables declaration//GEN-END:variables
}
