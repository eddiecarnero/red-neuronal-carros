package com.proyecto.carro.gui;

import com.proyecto.carro.model.Car;
import com.proyecto.carro.model.EvolutionManager;
import com.proyecto.carro.model.Track;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private EvolutionManager manager;
    private Car humanCar;
    private boolean humanMode = false;

    // Simulation loop timer
    private Timer gameTimer;

    // Key flags for manual play
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    // GUI Components
    private GamePanel gamePanel;

    // Stats Labels
    private JLabel lblGenA, lblGenB;
    private JLabel lblAliveA, lblAliveB;
    private JLabel lblScoreA, lblScoreB;
    private JLabel lblHighScoreA, lblHighScoreB;
    private JLabel lblBestFitnessA, lblBestFitnessB;

    // Controls
    private JRadioButton rbtnAI;
    private JRadioButton rbtnHuman;
    private JRadioButton rbtnShowAll;
    private JRadioButton rbtnShowBest;
    private JSlider speedSlider;

    // Sensor Configuration
    private JSpinner spinSensors;
    private JComboBox<String> comboTrack;
    private JButton btnReconfigure;
    private JButton btnEditTrack;
    private boolean editingTrack = false;

    // Fast Evolution
    private JButton btnEvolve10;
    private JButton btnEvolve15;
    private JButton btnEvolve25;
    private JProgressBar progressEvolve;

    // Theme Colors (Catppuccin Mocha)
    private final Color bgDark = new Color(30, 30, 46);
    private final Color bgCard = new Color(37, 37, 56);
    private final Color textMain = new Color(205, 214, 244);
    private final Color textMuted = new Color(166, 173, 200);

    private final Color btnBgNormal = new Color(49, 50, 68);
    private final Color btnBgAccent = new Color(137, 180, 250); // Blue
    private final Color btnBgSuccess = new Color(166, 227, 161); // Green

    public MainFrame() {
        int initialSensors = 5;
        manager = new EvolutionManager(100, initialSensors); // 100 cars
        humanCar = new Car(manager.getTrack().getStartPoint().x, manager.getTrack().getStartPoint().y, manager.getTrack().getStartAngle(), initialSensors);

        setTitle("IA Conducción Autónoma - Red Neuronal y Algoritmo Genético");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 560);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgDark);

        initComponents();
        setupEvents();
        setupInputHandlers();

        // Start loop
        setupSimulationTimer();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // 1. LEFT PANEL - Game Arena
        JPanel panelLeft = new JPanel(new BorderLayout(10, 10));
        panelLeft.setBackground(bgDark);
        panelLeft.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 5));

        JLabel lblTitle = new JLabel("Lienzo de Simulación (Circuito Wavy 450x450)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(textMain);
        panelLeft.add(lblTitle, BorderLayout.NORTH);

        gamePanel = new GamePanel(manager);
        gamePanel.setHumanCar(humanCar);
        panelLeft.add(gamePanel, BorderLayout.CENTER);

        add(panelLeft, BorderLayout.CENTER);

        // 2. RIGHT PANEL - Sidebar Controls
        JPanel panelRight = new JPanel(new GridBagLayout());
        panelRight.setBackground(bgDark);
        panelRight.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 15));
        panelRight.setPreferredSize(new Dimension(320, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Card 1: Statistics Comparison Grid
        JPanel panelStats = createCardPanel("Comparador de Algoritmos");
        panelStats.setLayout(new GridLayout(7, 3, 5, 5));

        lblGenA = new JLabel("1"); styleStatLabel(lblGenA, new Color(137, 180, 250));
        lblGenB = new JLabel("1"); styleStatLabel(lblGenB, new Color(243, 139, 168));

        lblAliveA = new JLabel("50 / 50"); styleStatLabel(lblAliveA, new Color(137, 180, 250));
        lblAliveB = new JLabel("50 / 50"); styleStatLabel(lblAliveB, new Color(243, 139, 168));

        lblScoreA = new JLabel("0"); styleStatLabel(lblScoreA, new Color(137, 180, 250));
        lblScoreB = new JLabel("0"); styleStatLabel(lblScoreB, new Color(243, 139, 168));

        lblHighScoreA = new JLabel("0"); styleStatLabel(lblHighScoreA, new Color(137, 180, 250));
        lblHighScoreB = new JLabel("0"); styleStatLabel(lblHighScoreB, new Color(243, 139, 168));

        lblBestFitnessA = new JLabel("0"); styleStatLabel(lblBestFitnessA, new Color(137, 180, 250));
        lblBestFitnessB = new JLabel("0"); styleStatLabel(lblBestFitnessB, new Color(243, 139, 168));

        // Column Headers
        JLabel lblHeaderMetric = createMutedLabel("Métrica");
        JLabel lblHeaderA = new JLabel("A: Genético");
        lblHeaderA.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeaderA.setForeground(new Color(137, 180, 250));
        lblHeaderA.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblHeaderB = new JLabel("B: Mutación");
        lblHeaderB.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeaderB.setForeground(new Color(243, 139, 168));
        lblHeaderB.setHorizontalAlignment(SwingConstants.RIGHT);

        panelStats.add(lblHeaderMetric);
        panelStats.add(lblHeaderA);
        panelStats.add(lblHeaderB);

        // Row 2: Generation
        panelStats.add(createMutedLabel("Generación:"));
        panelStats.add(lblGenA);
        panelStats.add(lblGenB);

        // Row 3: Alive Count
        panelStats.add(createMutedLabel("Carros Vivos:"));
        panelStats.add(lblAliveA);
        panelStats.add(lblAliveB);

        // Row 4: Generation Best Checkpoint Score
        panelStats.add(createMutedLabel("Score (Gen):"));
        panelStats.add(lblScoreA);
        panelStats.add(lblScoreB);

        // Row 5: All-time High Checkpoint Score
        panelStats.add(createMutedLabel("Récord Abs:"));
        panelStats.add(lblHighScoreA);
        panelStats.add(lblHighScoreB);

        // Row 6: Max Fitness
        panelStats.add(createMutedLabel("Aptitud Máx:"));
        panelStats.add(lblBestFitnessA);
        panelStats.add(lblBestFitnessB);

        // Row 7: Algorithmic Complexity Formula (Big-O)
        panelStats.add(createMutedLabel("Complejidad:"));
        
        JLabel lblCompA = new JLabel("O(N log N + N*G)");
        lblCompA.setFont(new Font("Consolas", Font.BOLD, 9));
        lblCompA.setForeground(new Color(166, 173, 200));
        lblCompA.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblCompB = new JLabel("O(N * G)");
        lblCompB.setFont(new Font("Consolas", Font.BOLD, 9));
        lblCompB.setForeground(new Color(166, 173, 200));
        lblCompB.setHorizontalAlignment(SwingConstants.RIGHT);

        panelStats.add(lblCompA);
        panelStats.add(lblCompB);

        gbc.gridy = 0;
        panelRight.add(panelStats, gbc);

        // Card 2: Interactive Controls
        JPanel panelControls = createCardPanel("Controles");
        panelControls.setLayout(new GridBagLayout());
        GridBagConstraints gbcCtrl = new GridBagConstraints();
        gbcCtrl.fill = GridBagConstraints.HORIZONTAL;
        gbcCtrl.insets = new Insets(4, 4, 4, 4);
        gbcCtrl.weightx = 1.0;

        // Mode switch
        JLabel lblMode = new JLabel("Modo de Juego:");
        lblMode.setForeground(textMuted);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 0; gbcCtrl.gridwidth = 1;
        panelControls.add(lblMode, gbcCtrl);

        rbtnAI = new JRadioButton("IA (Evolución)", true);
        rbtnHuman = new JRadioButton("Manual (Tú)", false);
        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbtnAI); bgMode.add(rbtnHuman);
        styleRadioButton(rbtnAI); styleRadioButton(rbtnHuman);

        JPanel panelModes = new JPanel(new GridLayout(1, 2));
        panelModes.setBackground(bgCard);
        panelModes.add(rbtnAI); panelModes.add(rbtnHuman);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 1; gbcCtrl.gridwidth = 2;
        panelControls.add(panelModes, gbcCtrl);

        // View Mode
        JLabel lblView = new JLabel("Visualización (IA):");
        lblView.setForeground(textMuted);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 2; gbcCtrl.gridwidth = 1;
        panelControls.add(lblView, gbcCtrl);

        rbtnShowAll = new JRadioButton("Ver Todos", true);
        rbtnShowBest = new JRadioButton("Ver Mejor", false);
        ButtonGroup bgView = new ButtonGroup();
        bgView.add(rbtnShowAll); bgView.add(rbtnShowBest);
        styleRadioButton(rbtnShowAll); styleRadioButton(rbtnShowBest);

        JPanel panelViews = new JPanel(new GridLayout(1, 2));
        panelViews.setBackground(bgCard);
        panelViews.add(rbtnShowAll); panelViews.add(rbtnShowBest);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 3; gbcCtrl.gridwidth = 2;
        panelControls.add(panelViews, gbcCtrl);

        // Speed Slider
        JLabel lblSpeed = new JLabel("Velocidad de Simulación:");
        lblSpeed.setForeground(textMuted);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 4; gbcCtrl.gridwidth = 2;
        panelControls.add(lblSpeed, gbcCtrl);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 3, 2); // 0: Hyper, 1: Fast, 2: Normal, 3: Slow
        speedSlider.setBackground(bgCard);
        speedSlider.setFocusable(false);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 5; gbcCtrl.gridwidth = 2;
        panelControls.add(speedSlider, gbcCtrl);

        gbc.gridy = 1;
        panelRight.add(panelControls, gbc);

        // Card 3: Dynamic Sensors Configuration (Varies Input size)
        JPanel panelSensors = createCardPanel("Escalabilidad de Entradas (LIDAR)");
        panelSensors.setLayout(new GridBagLayout());
        GridBagConstraints gbcSens = new GridBagConstraints();
        gbcSens.fill = GridBagConstraints.HORIZONTAL;
        gbcSens.insets = new Insets(4, 4, 4, 4);
        gbcSens.weightx = 1.0;

        JLabel lblSens = new JLabel("Número de Sensores (Rayos):");
        lblSens.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSens.setForeground(textMuted);
        gbcSens.gridx = 0; gbcSens.gridy = 0;
        panelSensors.add(lblSens, gbcSens);

        // Spinner allowing 1 to 20 sensors (Varies input neurons dynamically)
        spinSensors = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        spinSensors.setBackground(bgCard);
        spinSensors.setForeground(textMain);
        spinSensors.setFocusable(false);
        gbcSens.gridx = 1; gbcSens.gridy = 0;
        panelSensors.add(spinSensors, gbcSens);

        JLabel lblTrack = new JLabel("Circuito de Carrera:");
        lblTrack.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrack.setForeground(textMuted);
        gbcSens.gridx = 0; gbcSens.gridy = 1;
        panelSensors.add(lblTrack, gbcSens);

        String[] trackOptions = {"Óvalo Clásico (Fácil)", "Circuito Sinuoso (Medio)", "Grand Prix F1 (Difícil)", "El Laberinto (Extremo)", "Circuito Personalizado"};
        comboTrack = new JComboBox<>(trackOptions);
        comboTrack.setBackground(bgCard);
        comboTrack.setForeground(textMain);
        comboTrack.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        comboTrack.setFocusable(false);
        comboTrack.setSelectedIndex(0); // Starts on Oval
        gbcSens.gridx = 1; gbcSens.gridy = 1;
        panelSensors.add(comboTrack, gbcSens);

        btnReconfigure = new JButton("Reconfigurar IA y Reiniciar");
        styleButton(btnReconfigure, btnBgAccent, Color.BLACK);
        gbcSens.gridx = 0; gbcSens.gridy = 2; gbcSens.gridwidth = 2;
        gbcSens.insets = new Insets(8, 4, 4, 4);
        panelSensors.add(btnReconfigure, gbcSens);

        btnEditTrack = new JButton("Editar Pista Personalizada");
        styleButton(btnEditTrack, btnBgNormal, textMain);
        gbcSens.gridx = 0; gbcSens.gridy = 3; gbcSens.gridwidth = 2;
        gbcSens.insets = new Insets(4, 4, 4, 4);
        panelSensors.add(btnEditTrack, gbcSens);

        gbc.gridy = 2;
        panelRight.add(panelSensors, gbc);

        // Card 4: Instant Fast Evolution
        JPanel panelFastEvolve = createCardPanel("Evolución Rápida de Fondo");
        panelFastEvolve.setLayout(new GridBagLayout());
        GridBagConstraints gbcFast = new GridBagConstraints();
        gbcFast.fill = GridBagConstraints.HORIZONTAL;
        gbcFast.insets = new Insets(4, 4, 4, 4);
        gbcFast.weightx = 1.0;

        btnEvolve10 = new JButton("10 Gens");
        btnEvolve15 = new JButton("15 Gens");
        btnEvolve25 = new JButton("25 Gens");
        styleButton(btnEvolve10, btnBgNormal, textMain);
        styleButton(btnEvolve15, btnBgNormal, textMain);
        styleButton(btnEvolve25, btnBgNormal, textMain);

        JPanel panelFastButtons = new JPanel(new GridLayout(1, 3, 6, 0));
        panelFastButtons.setBackground(bgCard);
        panelFastButtons.add(btnEvolve10);
        panelFastButtons.add(btnEvolve15);
        panelFastButtons.add(btnEvolve25);
        gbcFast.gridx = 0; gbcFast.gridy = 0;
        panelFastEvolve.add(panelFastButtons, gbcFast);

        progressEvolve = new JProgressBar(0, 100);
        progressEvolve.setForeground(btnBgSuccess);
        progressEvolve.setBackground(bgDark);
        progressEvolve.setBorderPainted(false);
        progressEvolve.setVisible(false);
        gbcFast.gridy = 1;
        panelFastEvolve.add(progressEvolve, gbcFast);

        gbc.gridy = 3; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelRight.add(panelFastEvolve, gbc);

        add(panelRight, BorderLayout.EAST);
    }

    private void setupEvents() {
        rbtnShowAll.addActionListener(e -> gamePanel.setShowAll(true));
        rbtnShowBest.addActionListener(e -> gamePanel.setShowAll(false));

        speedSlider.addChangeListener(e -> adjustTimerSpeed());

        rbtnHuman.addActionListener(e -> {
            humanMode = true;
            gamePanel.setHumanMode(true);
            resetHumanGame();

            lblGenA.setText("N/A"); lblGenB.setText("N/A");
            lblAliveA.setText("1 / 1"); lblAliveB.setText("0 / 0");
            lblScoreA.setText("0"); lblScoreB.setText("0");
            lblHighScoreA.setText("N/A"); lblHighScoreB.setText("N/A");
            lblBestFitnessA.setText("N/A"); lblBestFitnessB.setText("N/A");

            gameTimer.start();
            gamePanel.requestFocusInWindow();
        });

        rbtnAI.addActionListener(e -> {
            humanMode = false;
            gamePanel.setHumanMode(false);
            gameTimer.start();
            updateLiveStats();
        });

        // Reconfigures the AI network inputs
        btnReconfigure.addActionListener(e -> reconfigureSensors());

        // Toggles track editor mode
        btnEditTrack.addActionListener(e -> toggleTrackEditor());

        // Fast Evolution Action Listeners (10, 15, 25 generations)
        btnEvolve10.addActionListener(e -> runBackgroundEvolution(10));
        btnEvolve15.addActionListener(e -> runBackgroundEvolution(15));
        btnEvolve25.addActionListener(e -> runBackgroundEvolution(25));
    }

    private void setupInputHandlers() {
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!humanMode) return;
                toggleKeys(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!humanMode) return;
                toggleKeys(e.getKeyCode(), false);
            }
        });
    }

    private void toggleKeys(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) upPressed = pressed;
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) downPressed = pressed;
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) leftPressed = pressed;
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) rightPressed = pressed;
    }

    private void setupSimulationTimer() {
        gameTimer = new Timer(getTimerDelay(), e -> gameTick());
        gameTimer.start();
    }

    private int getTimerDelay() {
        int val = speedSlider.getValue();
        switch (val) {
            case 0: return 3;   // Hyper speed (updates 15 updates per loop)
            case 1: return 8;   // Fast
            case 2: return 20;  // Normal
            case 3: return 60;  // Slow
            default: return 20;
        }
    }

    private void adjustTimerSpeed() {
        gameTimer.setDelay(getTimerDelay());
    }

    private void gameTick() {
        Track track = manager.getTrack();

        if (humanMode) {
            if (humanCar != null) {
                double steer = 0.0;
                if (leftPressed) steer = -1.0;
                if (rightPressed) steer = 1.0;

                double accel = 0.0;
                if (upPressed) accel = 1.0;
                if (downPressed) accel = -1.0;

                humanCar.updateManual(steer, accel, track.getWallSegments());
                humanCar.checkCollisions(track.getWallSegments());
                humanCar.checkCheckpoint(track.getCheckpoints());

                lblScoreA.setText(String.valueOf(humanCar.getScore()));
                if (humanCar.getScore() > manager.getHighScoreA()) {
                    lblHighScoreA.setText(String.valueOf(humanCar.getScore()));
                }

                if (!humanCar.isAlive()) {
                    gameTimer.stop();
                    JOptionPane.showMessageDialog(this, 
                        "¡Colisión! Has chocado contra las paredes de la pista.\nCheckpoints cruzados: " + humanCar.getScore(), 
                        "Fin de Partida", JOptionPane.INFORMATION_MESSAGE);
                    resetHumanGame();
                    gameTimer.start();
                }
            }
        } else {
            // AI Mode
            int updatesPerTick = 1;
            if (speedSlider.getValue() == 0) {
                updatesPerTick = 15;
            }

            for (int i = 0; i < updatesPerTick; i++) {
                manager.update();

                if (manager.isGenerationOverA()) {
                    manager.evolveA();
                }
                if (manager.isGenerationOverB()) {
                    manager.evolveB();
                }
            }
        }

        gamePanel.repaint();
        updateLiveStats();
    }

    private void resetHumanGame() {
        int sensors = (int) spinSensors.getValue();
        double startX = manager.getTrack().getStartPoint().x;
        double startY = manager.getTrack().getStartPoint().y;
        double startAngle = manager.getTrack().getStartAngle();
        if (humanCar.getNumSensors() != sensors) {
            humanCar = new Car(startX, startY, startAngle, sensors);
            gamePanel.setHumanCar(humanCar);
        } else {
            humanCar.reset(startX, startY, startAngle);
        }
        
        // Reset key flags
        upPressed = false; downPressed = false;
        leftPressed = false; rightPressed = false;
    }

    private void reconfigureSensors() {
        gameTimer.stop();
        int sensors = (int) spinSensors.getValue();
        int trackType = comboTrack.getSelectedIndex();
        
        manager.resetSimulation(sensors, trackType);
        gamePanel.setManager(manager);
        
        resetHumanGame();
        
        updateLiveStats();
        gamePanel.repaint();
        gameTimer.start();

        JOptionPane.showMessageDialog(this, 
            "IA reconfigurada correctamente.\nSe cargó el circuito seleccionado con " + sensors + " sensores.", 
            "Reconfiguración Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLiveStats() {
        if (humanMode) return;

        // Algoritmo A (Genetic Algorithm)
        lblGenA.setText(String.valueOf(manager.getGenerationA()));
        int aliveA = 0;
        int maxScoreA = 0;
        for (Car car : manager.getPopulationA()) {
            if (car.isAlive()) {
                aliveA++;
                if (car.getScore() > maxScoreA) {
                    maxScoreA = car.getScore();
                }
            }
        }
        lblAliveA.setText(aliveA + " / 50");
        lblScoreA.setText(String.valueOf(maxScoreA));
        lblHighScoreA.setText(String.valueOf(manager.getHighScoreA()));
        lblBestFitnessA.setText(String.format("%.0f", manager.getBestFitnessA()));

        // Algoritmo B (Hill Climbing / Mutation)
        lblGenB.setText(String.valueOf(manager.getGenerationB()));
        int aliveB = 0;
        int maxScoreB = 0;
        for (Car car : manager.getPopulationB()) {
            if (car.isAlive()) {
                aliveB++;
                if (car.getScore() > maxScoreB) {
                    maxScoreB = car.getScore();
                }
            }
        }
        lblAliveB.setText(aliveB + " / 50");
        lblScoreB.setText(String.valueOf(maxScoreB));
        lblHighScoreB.setText(String.valueOf(manager.getHighScoreB()));
        lblBestFitnessB.setText(String.format("%.0f", manager.getBestFitnessB()));
    }

    private void runBackgroundEvolution(int gens) {
        if (humanMode) return;

        gameTimer.stop();
        setControlsEnabled(false);

        progressEvolve.setValue(0);
        progressEvolve.setVisible(true);
        progressEvolve.setIndeterminate(false);

        final int startGenA = manager.getGenerationA();
        final int startGenB = manager.getGenerationB();

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                manager.evolveMultipleGenerations(gens, () -> {
                    int completed = (manager.getGenerationA() - startGenA) + (manager.getGenerationB() - startGenB);
                    int percent = (int) (((double) completed / (gens * 2)) * 100);
                    publish(percent);
                });
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int percent = chunks.get(chunks.size() - 1);
                progressEvolve.setValue(Math.min(100, percent));
                updateLiveStats();
            }

            @Override
            protected void done() {
                progressEvolve.setVisible(false);
                setControlsEnabled(true);
                updateLiveStats();
                gameTimer.start();
                gamePanel.repaint();
                JOptionPane.showMessageDialog(MainFrame.this, 
                    "Simulación rápida completada.\nAmbos algoritmos avanzaron " + gens + " generaciones con " + manager.getNumSensors() + " sensores.", 
                    "Evolución Terminada", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        btnEvolve10.setEnabled(enabled);
        btnEvolve15.setEnabled(enabled);
        btnEvolve25.setEnabled(enabled);
        btnReconfigure.setEnabled(enabled);
        spinSensors.setEnabled(enabled);
        comboTrack.setEnabled(enabled);
        rbtnHuman.setEnabled(enabled);
        rbtnAI.setEnabled(enabled);
        speedSlider.setEnabled(enabled);
        btnEditTrack.setEnabled(enabled);
    }

    private void toggleTrackEditor() {
        if (!editingTrack) {
            editingTrack = true;
            gameTimer.stop();

            // If not already on Custom track, load its waypoints in memory so they have a starting point
            int currentTrack = comboTrack.getSelectedIndex();
            if (currentTrack != 4) {
                int sensors = (int) spinSensors.getValue();
                manager.resetSimulation(sensors, 4);
                gamePanel.setManager(manager);
            }

            gamePanel.setEditorMode(true);
            btnEditTrack.setText("Guardar Pista y Entrenar");
            styleButton(btnEditTrack, btnBgSuccess, Color.BLACK);

            // Disable other controls while editing
            setControlsEnabled(false);
            btnEditTrack.setEnabled(true); // Keep itself enabled!

            JOptionPane.showMessageDialog(this,
                "Modo Editor Activado:\n" +
                "- ARRASTRA los puntos naranjas con click izquierdo para mover curvas.\n" +
                "- DOBLE CLICK en una zona libre para añadir un nuevo punto de control.\n" +
                "- CLICK DERECHO sobre un punto para eliminarlo (mínimo 4).\n" +
                "- Haz clic en 'Guardar Pista y Entrenar' cuando termines.",
                "Editor de Pista", JOptionPane.INFORMATION_MESSAGE);
        } else {
            editingTrack = false;

            // Save waypoints to file
            java.util.List<java.awt.geom.Point2D.Double> pts = manager.getTrack().getBasePoints();
            com.proyecto.carro.model.Track.saveCustomWaypoints(pts);

            gamePanel.setEditorMode(false);
            btnEditTrack.setText("Editar Pista Personalizada");
            styleButton(btnEditTrack, btnBgNormal, textMain);

            // Select "Circuito Personalizado" in combo
            comboTrack.setSelectedIndex(4);

            // Enable controls and reconfigure
            setControlsEnabled(true);
            reconfigureSensors();
        }
    }

    // Card Helper
    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setBackground(bgCard);
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(bgDark.brighter(), 1, true),
            title,
            TitledBorder.LEADING,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            textMain
        ));
        return card;
    }

    private void styleStatLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(btnBgAccent);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private void styleStatLabel(JLabel lbl, Color fgColor) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(fgColor);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private JLabel createMutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(textMuted);
        return lbl;
    }

    private void styleRadioButton(JRadioButton rbtn) {
        rbtn.setBackground(bgCard);
        rbtn.setForeground(textMain);
        rbtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbtn.setFocusPainted(false);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
