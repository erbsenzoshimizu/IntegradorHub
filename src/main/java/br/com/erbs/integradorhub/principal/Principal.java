package br.com.erbs.integradorhub.principal;

import br.com.erbs.integradorhub.processador.ProcessadorXml;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public final class Principal extends javax.swing.JFrame {

    // caminhos de diretório
    private static final String BASE_DIR = "\\\\192.168.2.40\\xml\\NFCe\\";
    private static final String PROCESSAR_DIR = BASE_DIR + "Processar\\";
    private static final String AUTORIZAR_DIR = BASE_DIR + "Autorizar\\";
    private static final String AUTORIZADO_DIR = BASE_DIR + "Autorizado\\";
    private static final String REJEITADO_DIR = BASE_DIR + "Rejeitado\\";

    private static final long EXEC_INTERVAL_MS = 10_000L; // intervalo de execução (milissegundos)    
    private static final String TIME_PATTERN = "HH:mm:ss"; // formato de hora usado em SimpleDateFormat

    private Timer horaAtualTimer;
    private Timer execTimer;
    private SwingWorker<Void, Void> worker;

    public Principal() throws ParseException {
        initComponents();
        setupWindow();

        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);

        txtMsg.setText("Iniciando o integrador.");

        atualizarRelogio();

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

    public void processarArquivos() throws SQLException, Exception {
        if (worker != null && !worker.isDone()) {
            return;
        }

        barra.setVisible(true);
        barra.setIndeterminate(true);
        barra.setStringPainted(true);
        barra.setString("Executando integração de notas");

        final SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN);
        worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                lblHora1.setText(format.format(new Date()));

                // Processamento e autorização em sequência
                processarArquivosXml();
                autorizarArquivosXml();

                return null;
            }

            @Override
            protected void done() {
                barra.setIndeterminate(false);
                barra.setString("Integração finalizada");
                txtMsg.setText("Aguardando XML");
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
            lblMsg.setText("Nenhum arquivo .xml encontrado.");
            return;
        }

        // Processa os arquivos
        for (File xml : arquivosXML) {
            if (!xml.isFile()) {
                lblMsg.setText("Arquivo inválido: " + xml.getName());
                System.out.println("Arquivo inválido: " + xml.getName());
                return;
            }

            txtMsg.setText("Integrando o XML");
            System.out.println("Processando: " + xml.getAbsolutePath());
            lblMsg.setText(xml.getName());

            try {
                ProcessadorXml.processarXml(xml.getAbsolutePath(), AUTORIZAR_DIR + xml.getName());
            } catch (Exception e) {
                lblMsg.setText("Erro ao processar " + xml.getName() + ": " + e.getMessage());
                System.err.println("Erro ao processar " + xml.getName() + ": " + e.getMessage());
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
            lblMsg.setText("Nenhum arquivo .xml encontrado.");
            return;
        }

        // Processa os arquivos
        for (File xml : arquivosXML) {
            if (!xml.isFile()) {
                lblMsg.setText("Arquivo inválido: " + xml.getName());
                System.out.println("Arquivo inválido: " + xml.getName());
                return;
            }

            txtMsg.setText("Integrando o XML");
            System.out.println("Autorizando: " + xml.getAbsolutePath());
            lblMsg.setText(xml.getName());

            try {
                ProcessadorXml.autorizarXml(xml.getAbsolutePath(), AUTORIZADO_DIR + xml.getName(), REJEITADO_DIR + xml.getName());
            } catch (Exception e) {
                lblMsg.setText("Erro ao processar " + xml.getName() + ": " + e.getMessage());
                System.err.println("Erro ao processar " + xml.getName() + ": " + e.getMessage());
            }
        }
    }

    public final void atualizarRelogio() {
        if (horaAtualTimer == null) {
            final SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN);
            horaAtualTimer = new javax.swing.Timer(1000, e -> {
                try {
                    lblHora1.setText(format.format(new Date().getTime()));
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            });
            horaAtualTimer.start();
            lblHoraFim.setText(format.format(new Date().getTime() + EXEC_INTERVAL_MS));
        }
    }

    public static void deletarArquivoXml(String diretorio) {
        File file = new File(diretorio);
        file.delete();
    }

    private boolean verificarOuCriarDiretorio(File diretorio) {
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                lblMsg.setText("Diretório criado com sucesso.");
                System.out.println("Diretório criado com sucesso.");
            } else {
                lblMsg.setText("Falha ao criar o diretório.");
                System.out.println("Falha ao criar o diretório.");
                return false;
            }
        } else if (!diretorio.isDirectory()) {
            lblMsg.setText("O caminho existe, mas não é um diretório.");
            System.out.println("O caminho existe, mas não é um diretório.");
            return false;
        }
        return true;
    }

    private void setupWindow() throws ParseException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setAlwaysOnTop(false);
        setBackground(Color.RED);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblMsg = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblHora = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtMsg = new javax.swing.JTextArea();
        barra = new javax.swing.JProgressBar();
        lblInfo = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblHora1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblHoraFim = new javax.swing.JLabel();
        lblQtdReg = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setTitle("Integrador Hub ");
        setBackground(new java.awt.Color(50, 77, 173));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setForeground(new java.awt.Color(0, 0, 0));

        jPanel1.setBackground(new java.awt.Color(50, 77, 173));
        jPanel1.setForeground(new java.awt.Color(51, 51, 51));
        jPanel1.setPreferredSize(new java.awt.Dimension(800, 623));

        lblMsg.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblMsg.setForeground(new java.awt.Color(255, 255, 255));
        lblMsg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMsg.setText("Mensagens Automáticas de Hubs");

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        lblHora.setFont(new java.awt.Font("Calibri", 0, 18)); // NOI18N
        lblHora.setForeground(new java.awt.Color(255, 255, 255));
        lblHora.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHora.setText("Canal ");
        lblHora.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        txtMsg.setColumns(20);
        txtMsg.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtMsg.setRows(5);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        txtMsg.setEnabled(false);
        jScrollPane2.setViewportView(txtMsg);

        lblInfo.setForeground(new java.awt.Color(255, 255, 255));
        lblInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/customer_service_robot_lg_wm.gif"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblHora, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(barra, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lblHora, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane2)
                .addGap(2, 2, 2)
                .addComponent(lblInfo)
                .addGap(2, 2, 2)
                .addComponent(barra, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
        );

        jLabel7.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Integrador Erbs Hub");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/nfce.png"))); // NOI18N
        jLabel3.setText("jLabel3");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        lblHora1.setForeground(new java.awt.Color(255, 255, 255));
        lblHora1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHora1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chave.png"))); // NOI18N
        lblHora1.setText(" Pedidos");
        lblHora1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblHora1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHora1MouseClicked(evt);
            }
        });

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sair.png"))); // NOI18N
        jLabel5.setText(" Sair");
        jLabel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        lblHoraFim.setForeground(new java.awt.Color(255, 255, 255));
        lblHoraFim.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHoraFim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chave.png"))); // NOI18N
        lblHoraFim.setText("Manual");
        lblHoraFim.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblHoraFim.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHoraFimMouseClicked(evt);
            }
        });

        lblQtdReg.setForeground(new java.awt.Color(255, 255, 255));
        lblQtdReg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQtdReg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chave.png"))); // NOI18N
        lblQtdReg.setText("Automatico");
        lblQtdReg.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblQtdReg.setMaximumSize(new java.awt.Dimension(71, 28));
        lblQtdReg.setMinimumSize(new java.awt.Dimension(71, 28));
        lblQtdReg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblQtdRegMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(lblHora1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblHoraFim, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblQtdReg, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                        .addGap(268, 268, 268)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(lblMsg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMsg)
                    .addComponent(jLabel7))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHora1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHoraFim, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblQtdReg, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 1, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(465, 465, 465)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblHora1, lblHoraFim, lblQtdReg});

        jLabel1.setBackground(new java.awt.Color(50, 77, 173));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("SISTEMA DE HUB");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 997, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 528, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_jLabel5MouseClicked

    private void lblQtdRegMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblQtdRegMouseClicked
        //   enviarMensagemPedido();
    }//GEN-LAST:event_lblQtdRegMouseClicked

    private void lblHoraFimMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHoraFimMouseClicked
        //
    }//GEN-LAST:event_lblHoraFimMouseClicked

    private void lblHora1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHora1MouseClicked
        //
    }//GEN-LAST:event_lblHora1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Principal().setVisible(true);
                } catch (ParseException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar barra;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblHora1;
    private javax.swing.JLabel lblHoraFim;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblMsg;
    private javax.swing.JLabel lblQtdReg;
    private javax.swing.JTextArea txtMsg;
    // End of variables declaration//GEN-END:variables
}
