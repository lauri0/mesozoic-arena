package com.mesozoic.arena.ui;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Graphical interface for battling dinosaurs.
 */
public class MainWindow extends JFrame {
    private final Battle battle;
    private final Player player;
    private final Player opponent;

    private static final int STAT_ICON_SIZE = 24;
    private static final int DINO_IMAGE_WIDTH = 400;
    private static final int DINO_IMAGE_HEIGHT = 300;

    private JLabel playerNameLabel;
    private JLabel playerHealthLabel;
    private JLabel playerStaminaLabel;
    private JLabel playerSpeedLabel;
    private JLabel playerImageLabel;
    private JPanel benchPanel;
    private final JButton[] moveButtons = new JButton[4];

    private JTextArea logArea;

    private JLabel opponentNameLabel;
    private JLabel opponentHealthLabel;
    private JLabel opponentStaminaLabel;
    private JLabel opponentSpeedLabel;
    private JLabel opponentImageLabel;

    private void setStatLabel(JLabel label, String iconPath, int value) {
        label.setIcon(loadIcon(iconPath, STAT_ICON_SIZE, STAT_ICON_SIZE));
        label.setText(String.valueOf(value));
    }


    public MainWindow(Battle battle, Player player, Player opponent) {
        this.battle = battle;
        this.player = player;
        this.opponent = opponent;
        setupFrame();
        initComponents();
        refreshDisplay();
    }

    private void setupFrame() {
        setTitle("Mesozoic Arena");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(createPlayerPanel());
        centerPanel.add(createOpponentPanel());
        add(centerPanel, BorderLayout.CENTER);

        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);

        JButton exitButton = new JButton("Exit Game");
        exitButton.addActionListener(e -> System.exit(0));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        bottomPanel.add(exitButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlayerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        playerImageLabel = new JLabel();
        playerImageLabel.setHorizontalAlignment(JLabel.CENTER);
        playerImageLabel.setVerticalAlignment(JLabel.CENTER);
        panel.add(playerImageLabel, BorderLayout.CENTER);
        JPanel lower = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(new GridLayout(4, 1));
        playerNameLabel = new JLabel();
        stats.add(playerNameLabel);
        playerHealthLabel = new JLabel();
        stats.add(playerHealthLabel);
        playerStaminaLabel = new JLabel();
        stats.add(playerStaminaLabel);
        playerSpeedLabel = new JLabel();
        stats.add(playerSpeedLabel);
        lower.add(stats, BorderLayout.NORTH);

        benchPanel = new JPanel();
        lower.add(benchPanel, BorderLayout.CENTER);

        lower.add(createMovesPanel(), BorderLayout.SOUTH);
        panel.add(lower, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createOpponentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        opponentImageLabel = new JLabel();
        opponentImageLabel.setHorizontalAlignment(JLabel.CENTER);
        opponentImageLabel.setVerticalAlignment(JLabel.CENTER);
        panel.add(opponentImageLabel, BorderLayout.CENTER);
        JPanel lower = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(new GridLayout(4, 1));
        opponentNameLabel = new JLabel();
        stats.add(opponentNameLabel);
        opponentHealthLabel = new JLabel();
        stats.add(opponentHealthLabel);
        opponentStaminaLabel = new JLabel();
        stats.add(opponentStaminaLabel);
        opponentSpeedLabel = new JLabel();
        stats.add(opponentSpeedLabel);
        lower.add(stats, BorderLayout.NORTH);
        panel.add(lower, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMovesPanel() {
        JPanel panel = new JPanel(new GridLayout(moveButtons.length, 1));
        for (int i = 0; i < moveButtons.length; i++) {
            moveButtons[i] = new JButton();
            panel.add(moveButtons[i]);
        }
        return panel;
    }

    private void refreshDisplay() {
        updateDinosaurInfo(player, playerNameLabel, playerHealthLabel,
                playerStaminaLabel, playerSpeedLabel, playerImageLabel);
        updateDinosaurInfo(opponent, opponentNameLabel, opponentHealthLabel,
                opponentStaminaLabel, opponentSpeedLabel, opponentImageLabel);
        updateBench();
        updateMoveButtons();
        updateLogArea();
    }

    private void updateDinosaurInfo(Player targetPlayer, JLabel nameLabel,
            JLabel healthLabel, JLabel staminaLabel, JLabel speedLabel,
            JLabel imageLabel) {
        Dinosaur dino = targetPlayer.getActiveDinosaur();
        if (dino == null) {
            nameLabel.setText("None");
            setStatLabel(healthLabel, "assets/icons/health.png", 0);
            setStatLabel(staminaLabel, "assets/icons/stamina.png", 0);
            setStatLabel(speedLabel, "assets/icons/speed.png", 0);
            imageLabel.setIcon(null);
            return;
        }
        nameLabel.setText(dino.getName());
        setStatLabel(healthLabel, "assets/icons/health.png", dino.getHealth());
        setStatLabel(staminaLabel, "assets/icons/stamina.png", dino.getStamina());
        setStatLabel(speedLabel, "assets/icons/speed.png", dino.getSpeed());
        ImageIcon icon = loadIcon(dino.getImagePath(), DINO_IMAGE_WIDTH, DINO_IMAGE_HEIGHT);
        imageLabel.setIcon(icon);
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            return new ImageIcon();
        }
        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private String buildImageHtml(String path, int width, int height) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            return "";
        }
        return "<img src='" + url + "' width='" + width + "' height='" + height + "'>";
    }

    private void updateBench() {
        benchPanel.removeAll();
        Dinosaur active = player.getActiveDinosaur();
        for (Dinosaur dino : player.getDinosaurs()) {
            if (dino.equals(active)) {
                continue;
            }
            JPanel p = new JPanel();
            p.add(new JLabel(dino.getName() + " {"));
            JLabel hp = new JLabel(String.valueOf(dino.getHealth()),
                    loadIcon("assets/icons/health.png", 16, 16), JLabel.LEFT);
            p.add(hp);
            JLabel st = new JLabel(String.valueOf(dino.getStamina()),
                    loadIcon("assets/icons/stamina.png", 16, 16), JLabel.LEFT);
            p.add(st);
            p.add(new JLabel("}"));
            JButton switchButton = new JButton("Switch");
            switchButton.addActionListener(e -> {
                player.queueSwitch(dino);
                handlePlayerMove(null);
            });
            p.add(switchButton);
            benchPanel.add(p);
        }
        benchPanel.revalidate();
        benchPanel.repaint();
    }

    private void updateMoveButtons() {
        Dinosaur dino = player.getActiveDinosaur();
        List<Move> moves = dino == null ? new ArrayList<>() : dino.getMoves();
        int stamina = dino == null ? 0 : dino.getStamina();
        for (int i = 0; i < moveButtons.length; i++) {
            JButton button = moveButtons[i];
            for (ActionListener l : button.getActionListeners()) {
                button.removeActionListener(l);
            }
            if (i < moves.size()) {
                Move move = moves.get(i);
                button.setText(formatMoveLabel(move));
                boolean canUseMove = stamina >= move.getStaminaChange();
                button.setEnabled(canUseMove);
                if (canUseMove) {
                    button.addActionListener(e -> handlePlayerMove(move));
                }
            } else {
                button.setText("N/A");
                button.setEnabled(false);
            }
        }
    }

    private String formatMoveLabel(Move move) {
        String dmgImg = buildImageHtml("assets/icons/damage.png", STAT_ICON_SIZE, STAT_ICON_SIZE);
        String staminaImg = buildImageHtml("assets/icons/stamina.png", STAT_ICON_SIZE, STAT_ICON_SIZE);
        return "<html>" + move.getName() + " (" + move.getDamage() + " " + dmgImg
                + " / " + move.getStaminaChange() + " " + staminaImg + ")</html>";
    }

    private void updateLogArea() {
        StringBuilder text = new StringBuilder();
        for (String entry : battle.getEventLog()) {
            text.append(entry).append("\n");
        }
        logArea.setText(text.toString());
    }

    private void handlePlayerMove(Move playerMove) {
        battle.executeRound(playerMove);
        refreshDisplay();
        updateLogArea();
        checkWinner();
    }

    private void checkWinner() {
        Player winner = battle.getWinner();
        if (winner != null) {
            JOptionPane.showMessageDialog(this, (winner == player ? "Player" : "Opponent") + " wins!");
            for (JButton button : moveButtons) {
                button.setEnabled(false);
            }
        }
    }

    public static void launch(Battle battle, Player player, Player opponent) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow(battle, player, opponent);
            window.setVisible(true);
        });
    }
}
