package com.mesozoic.arena.ui;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static final String HEALTH_ICON_PATH  = "assets/icons/health.png";
    private static final String STAMINA_ICON_PATH = "assets/icons/energy.png";
    private static final String SPEED_ICON_PATH   = "assets/icons/speed.png";

    private final Battle   battle;
    private final Player   player, opponent;

    private final DinoPanel playerPanel;
    private final DinoPanel opponentPanel;
    private final JTextArea logArea = new JTextArea(10,20);

    public MainWindow(Battle battle, Player player, Player opponent) {
        super("Mesozoic Arena");
        this.battle   = battle;
        this.player   = player;
        this.opponent = opponent;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 900);

        playerPanel   = new DinoPanel(true);
        opponentPanel = new DinoPanel(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                playerPanel, opponentPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        logArea.setEditable(false);
        JButton exit = new JButton("Exit Game");
        exit.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel(new BorderLayout(5,5));
        bottom.add(new JScrollPane(logArea), BorderLayout.CENTER);
        bottom.add(exit, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refreshDisplay();
        setVisible(true);
    }

    private void refreshDisplay() {
        updateDinoPanel(playerPanel,   player);
        updateDinoPanel(opponentPanel, opponent);
        logArea.setText(String.join("\n", battle.getEventLog()));
    }

    private void updateDinoPanel(DinoPanel panel, Player who) {
        Dinosaur d = who.getActiveDinosaur();

        // Name
        panel.name.setText(d == null ? "None" : d.getName());

        // Stats with icons
        setStatLabel(panel.health,  HEALTH_ICON_PATH,  d == null ? 0 : d.getHealth());
        setStatLabel(panel.stamina, STAMINA_ICON_PATH, d == null ? 0 : d.getStamina());
        setStatLabel(panel.speed,   SPEED_ICON_PATH,   d == null ? 0 : d.getSpeed());

        // Image
        if (d != null) {
            panel.image.setIcon(loadIcon(d.getImagePath(), 400, 300));
        } else {
            panel.image.setIcon(null);
        }

        // Only player side shows bench & moves
        if (panel.isPlayerSide) {
            panel.bench.removeAll();
            for (Dinosaur dd : player.getDinosaurs()) {
                if (dd.equals(d)) continue;
                panel.bench.add(createBenchItem(dd));
            }

            panel.moves.removeAll();
            if (d != null) {
                for (Move m : d.getMoves()) {
                    JButton mb = new JButton(
                            m.getName()
                                    + " (" + (m.getDamage()*d.getAttack())
                                    + " / " + m.getStaminaChange() + ")"
                    );
                    mb.setEnabled(d.canUse(m));
                    if (d.canUse(m)) {
                        mb.addActionListener(e -> doRound(m));
                    }
                    panel.moves.add(mb);
                }
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    private void doRound(Move playerMove) {
        battle.executeRound(playerMove);
        refreshDisplay();
        if (battle.getWinner() != null) {
            String msg = (battle.getWinner()==player ? "You win!" : "You lose!");
            JOptionPane.showMessageDialog(this, msg);
        }
    }

    /**
     * Helper to put a colored icon + number into a JLabel
     */
    private void setStatLabel(JLabel label, String iconPath, int value) {
        label.setIcon(loadIcon(iconPath, 24, 24));
        label.setText(String.valueOf(value));
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    /**
     * Creates the detailed bench entry for the given dinosaur.
     */
    private JPanel createBenchItem(Dinosaur dino) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel img = new JLabel(loadIcon(dino.getImagePath(), 120, 90));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel n = new JLabel(dino.getName());
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        n.setFont(n.getFont().deriveFont(Font.BOLD, 14f));

        JLabel hp = new JLabel();
        setStatLabel(hp, HEALTH_ICON_PATH, dino.getHealth());
        JLabel sp = new JLabel();
        setStatLabel(sp, STAMINA_ICON_PATH, dino.getStamina());
        JPanel stats = new JPanel(new GridLayout(1,2,5,0));
        stats.add(hp);
        stats.add(sp);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton sw = new JButton("Switch");
        sw.setAlignmentX(Component.CENTER_ALIGNMENT);
        sw.addActionListener(e -> {
            player.queueSwitch(dino);
            doRound(null);
        });

        column.add(img);
        column.add(n);
        column.add(stats);
        column.add(sw);
        return column;
    }

    public static void launch(Battle b, Player p, Player o) {
        SwingUtilities.invokeLater(() -> new MainWindow(b,p,o));
    }

    /**
     * Encapsulates one side's dinosaur image, stats, (and optional) controls.
     */
    private static class DinoPanel extends JPanel {
        final boolean isPlayerSide;
        final JLabel image   = new JLabel();
        final JLabel name    = new JLabel();
        final JLabel health  = new JLabel();
        final JLabel stamina = new JLabel();
        final JLabel speed   = new JLabel();
        final JPanel bench   = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,5));
        final JPanel moves   = new JPanel(new GridLayout(0,1,5,5));

        DinoPanel(boolean isPlayer) {
            this.isPlayerSide = isPlayer;
            setLayout(new BorderLayout(10,10));
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // Top: dino picture
            image.setHorizontalAlignment(JLabel.CENTER);
            add(image, BorderLayout.NORTH);

            name.setHorizontalAlignment(JLabel.CENTER);
            name.setFont(name.getFont().deriveFont(Font.BOLD, 24f));

            // Center: stats + (maybe) bench & moves
            Box info = Box.createVerticalBox();
            info.add(name);
            JPanel statsRow = new JPanel(new GridLayout(1,3,5,0));
            statsRow.add(health);
            statsRow.add(stamina);
            statsRow.add(speed);
            info.add(statsRow);

            if (isPlayerSide) {
                info.add(Box.createVerticalStrut(10));
                info.add(new JLabel("Bench:"));
                info.add(bench);
                info.add(Box.createVerticalStrut(10));
                info.add(new JLabel("Moves:"));
                info.add(moves);
            } else {
                // placeholders to keep heights aligned
                bench.setPreferredSize(new Dimension(0, 50));
                moves.setPreferredSize(new Dimension(0, 200));
                info.add(Box.createVerticalStrut(260));
            }

            add(info, BorderLayout.CENTER);
        }

    }
}
