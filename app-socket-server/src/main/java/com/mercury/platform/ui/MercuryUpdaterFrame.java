package com.mercury.platform.ui;

import com.mercury.platform.config.MercuryServerConfig;
import com.mercury.platform.holder.UpdateHolder;
import com.mercury.platform.server.bus.UpdaterServerAsyncEventBus;
import com.mercury.platform.server.bus.handlers.ClientActiveEventHandler;
import com.mercury.platform.server.core.UpdaterServer;
import com.mercury.platform.server.main.listeners.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Frost on 25.01.2017.
 */
public class MercuryUpdaterFrame extends JFrame {

    private static final Logger LOGGER = LogManager.getLogger(MercuryUpdaterFrame.class.getSimpleName());

    public static final Dimension DEFAULT_FRAME_SIZE = new Dimension(500, 170);

    private volatile JLabel onlineCountLabel;
    private volatile JLabel updateCount;
    private volatile JTextField versionField;
    private volatile JButton startUpdate;
    private UpdaterServer server;
    private Path path;

    public MercuryUpdaterFrame(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setPreferredSize(DEFAULT_FRAME_SIZE);
        this.add(getTopPanel(),BorderLayout.PAGE_START);
        this.initServer();
        this.add(getLabelsPanel(),BorderLayout.CENTER);
        this.add(getBottomPanel(),BorderLayout.PAGE_END);
        this.initEvents();
        this.pack();

    }

    private void initServer() {
        MercuryServerConfig serverConfig = MercuryServerConfig.getInstance();
        this.server = new UpdaterServer(serverConfig.getPort());
    }

    private void initEvents() {
        UpdaterServerAsyncEventBus asyncEventBus = UpdaterServerAsyncEventBus.getInstance();

        asyncEventBus.register((ClientActiveEventHandler) event ->
                LOGGER.info("Client connected, IP = {}" , event.getIpAddress())
        );

        asyncEventBus.register(new UIClientActiveListener(onlineCountLabel));
        asyncEventBus.register(new UIClientUnregisteredListener(onlineCountLabel));
        asyncEventBus.register(new UIClientUpdatedHandler(updateCount));
    }

    private JPanel getTopPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField jarPathField = new JTextField();
        jarPathField.setEditable(false);
        jarPathField.setPreferredSize(new Dimension(220,24));


        FileDialog dialog = new FileDialog(this,"Choose jar", FileDialog.LOAD);
        dialog.setDirectory("C:\\");

        JButton pickJarButton = new JButton("Open");
        pickJarButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.setVisible(true);
                path = Paths.get(dialog.getDirectory(), dialog.getFile());
                jarPathField.setText(dialog.getDirectory() + dialog.getFile());
                startUpdate.setEnabled(true);
            }
        });

        versionField = new JTextField();
        versionField.setPreferredSize(new Dimension(120,24));


        panel.add(jarPathField);
        panel.add(pickJarButton);
        panel.add(new JLabel("Version: "));
        versionField.setText("1.0.0");
        panel.add(versionField);
        return panel;
    }
    private JPanel getLabelsPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        JPanel onlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel onlineLabel = new JLabel("Online: ");
        //
        onlineCountLabel = new JLabel("0");

        onlinePanel.add(onlineLabel);
        onlinePanel.add(onlineCountLabel);

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel updateLabel = new JLabel("Update count: ");
        //
        updateCount = new JLabel("0");

        updatePanel.add(updateLabel);
        updatePanel.add(updateCount);

        panel.add(onlinePanel);
        panel.add(updatePanel);
        return panel;
    }
    private JPanel getBottomPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        startUpdate = new JButton("Start");
        startUpdate.addMouseListener(new StartServerButtonListener(this,server));
        startUpdate.setEnabled(false);
        JButton shutdownUpdate = new JButton("Shut down");
        shutdownUpdate.addMouseListener(new ShutdownServerButtonListener(server));

        panel.add(startUpdate);
        panel.add(shutdownUpdate);
        return panel;
    }

    public Path getPath() {
        return path;
    }
    public String getVersion() {
        return versionField.getText();
    }
}
