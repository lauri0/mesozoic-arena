package com.mesozoic.arena.ui;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Graphical interface for battling dinosaurs.
 */
public class MainWindow extends JFrame {
    private final Battle battle;
    private final Player player;
    private final Player opponent;

    private JLabel playerNameLabel;
    private JLabel playerHealthLabel;
    private JLabel playerStaminaLabel;
    private JLabel playerSpeedLabel;
    private JLabel playerImageLabel;
    private JPanel benchPanel;
    private final JButton[] moveButtons = new JButton[4];

    private JLabel opponentNameLabel;
    private JLabel opponentHealthLabel;
    private JLabel opponentStaminaLabel;
    private JLabel opponentSpeedLabel;
    private JLabel opponentImageLabel;

    private final Random random = new Random();

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
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(createPlayerPanel());
        centerPanel.add(createOpponentPanel());
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createMovesPanel(), BorderLayout.CENTER);
        benchPanel = new JPanel();
        bottomPanel.add(benchPanel, BorderLayout.NORTH);
        JButton exitButton = new JButton("Exit Game");
        exitButton.addActionListener(e -> System.exit(0));
        bottomPanel.add(exitButton, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlayerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        playerImageLabel = new JLabel();
        panel.add(playerImageLabel, BorderLayout.CENTER);
        JPanel stats = new JPanel(new GridLayout(4, 1));
        playerNameLabel = new JLabel();
        stats.add(playerNameLabel);
        playerHealthLabel = new JLabel();
        stats.add(playerHealthLabel);
        playerStaminaLabel = new JLabel();
        stats.add(playerStaminaLabel);
        playerSpeedLabel = new JLabel();
        stats.add(playerSpeedLabel);
        panel.add(stats, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createOpponentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        opponentImageLabel = new JLabel();
        panel.add(opponentImageLabel, BorderLayout.CENTER);
        JPanel stats = new JPanel(new GridLayout(4, 1));
        opponentNameLabel = new JLabel();
        stats.add(opponentNameLabel);
        opponentHealthLabel = new JLabel();
        stats.add(opponentHealthLabel);
        opponentStaminaLabel = new JLabel();
        stats.add(opponentStaminaLabel);
        opponentSpeedLabel = new JLabel();
        stats.add(opponentSpeedLabel);
        panel.add(stats, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMovesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, moveButtons.length));
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
    }

    private void updateDinosaurInfo(Player targetPlayer, JLabel nameLabel,
            JLabel healthLabel, JLabel staminaLabel, JLabel speedLabel,
            JLabel imageLabel) {
        Dinosaur dino = targetPlayer.getActiveDinosaur();
        if (dino == null) {
            nameLabel.setText("None");
            healthLabel.setText("Health: 0");
            staminaLabel.setText("Stamina: 0");
            speedLabel.setText("Speed: 0");
            imageLabel.setIcon(null);
            return;
        }
        nameLabel.setText(dino.getName());
        healthLabel.setText("Health: " + dino.getHealth());
        staminaLabel.setText("Stamina: " + dino.getStamina());
        speedLabel.setText("Speed: " + dino.getSpeed());
        ImageIcon icon = loadIcon(dino.getImagePath());
        imageLabel.setIcon(icon);
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            return new ImageIcon();
        }
        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void updateBench() {
        benchPanel.removeAll();
        Dinosaur active = player.getActiveDinosaur();
        for (Dinosaur dino : player.getDinosaurs()) {
            if (dino.equals(active)) {
                continue;
            }
            JPanel p = new JPanel();
            p.add(new JLabel(dino.getName()));
            JButton switchButton = new JButton("Switch");
            switchButton.addActionListener(e -> {
                player.setActiveDinosaur(dino);
                refreshDisplay();
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
        for (int i = 0; i < moveButtons.length; i++) {
            JButton button = moveButtons[i];
            for (ActionListener l : button.getActionListeners()) {
                button.removeActionListener(l);
            }
            if (i < moves.size()) {
                Move move = moves.get(i);
                button.setText(move.getName());
                button.setEnabled(true);
                button.addActionListener(e -> handlePlayerMove(move));
            } else {
                button.setText("N/A");
                button.setEnabled(false);
            }
        }
    }

    private void handlePlayerMove(Move playerMove) {
        Dinosaur opponentDino = opponent.getActiveDinosaur();
        Move opponentMove = null;
        if (opponentDino != null && !opponentDino.getMoves().isEmpty()) {
            List<Move> moves = opponentDino.getMoves();
            opponentMove = moves.get(random.nextInt(moves.size()));
        }
        battle.executeRound(playerMove, opponentMove);
        refreshDisplay();
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
