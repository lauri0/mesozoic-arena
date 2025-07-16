package com.mesozoic.arena.ui;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.Player;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static final String HEALTH_ICON_PATH  = "assets/icons/health.png";
    private static final String SPEED_ICON_PATH   = "assets/icons/speed.png";
    private static final String ATTACK_ICON_PATH  = "assets/icons/attack.png";
    private static final String BLEED_ICON_PATH   = "assets/icons/bleed.png";

    private static final int   BASE_STAT_ICON_SIZE   = 24;
    private static final int   BASE_STAT_FONT_SIZE   = 16;
    private static final float STAT_LARGE_ICON_SIZE_MULT = 1.5f;

    private final Battle   battle;
    private final Player   player, opponent;

    private final DinoPanel playerPanel;
    private final DinoPanel opponentPanel;
    private final JTextArea logArea = new JTextArea(10,20);
    private final JTextArea npcArea = new JTextArea(10,20);

    public MainWindow(Battle battle, Player player, Player opponent) {
        super("Mesozoic Arena");
        this.battle   = battle;
        this.player   = player;
        this.opponent = opponent;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 900);

        playerPanel   = new DinoPanel(true);
        opponentPanel = new DinoPanel(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                playerPanel, opponentPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        npcArea.setEditable(false);
        npcArea.setLineWrap(true);
        npcArea.setWrapStyleWord(true);
        JPanel npcPanel = new JPanel(new BorderLayout());
        JLabel npcLabel = new JLabel("NPC's thoughts", JLabel.CENTER);
        npcPanel.add(npcLabel, BorderLayout.NORTH);
        npcPanel.add(new JScrollPane(npcArea), BorderLayout.CENTER);
        npcPanel.setPreferredSize(new Dimension(350, 100));
        add(npcPanel, BorderLayout.EAST);

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
        npcArea.setText(String.join("\n", battle.getAiLog()));
    }

    private void updateDinoPanel(DinoPanel panel, Player who) {
        Dinosaur d = who.getActiveDinosaur();

        // Name with stage icons
        if (d == null) {
            panel.name.setText("None");
        } else {
            StringBuilder nameText = new StringBuilder(d.getName());
            nameText.append(stageFragment(d.getAttackStage(), ATTACK_ICON_PATH));
            nameText.append(stageFragment(d.getSpeedStage(), SPEED_ICON_PATH));
            nameText.append(ailmentFragment(d.hasAilment("Bleeding"), BLEED_ICON_PATH));
            panel.name.setText("<html>" + nameText + "</html>");
        }

        // Stats with icons
        setStatLabel(panel.health,  HEALTH_ICON_PATH,  d == null ? 0 : d.getHealth(),  true);
        setStatLabel(panel.speed,   SPEED_ICON_PATH,   d == null ? 0 : d.getSpeed(),   true);

        // Image
        if (d != null) {
            panel.image.setIcon(loadIcon(d.getImagePath(), 384, 256));
        } else {
            panel.image.setIcon(null);
        }

        panel.bench.removeAll();
        for (Dinosaur dd : who.getDinosaurs()) {
            if (dd.equals(d)) {
                continue;
            }
            panel.bench.add(createBenchItem(dd, panel.isPlayerSide));
        }

        panel.moves.removeAll();
        if (d != null) {
            for (Move m : d.getMoves()) {
                panel.moves.add(createMoveButton(d, m, panel.isPlayerSide));
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
    private void setStatLabel(JLabel label, String iconPath, int value, boolean large) {
        int iconSize = large ? Math.round(BASE_STAT_ICON_SIZE * STAT_LARGE_ICON_SIZE_MULT) : BASE_STAT_ICON_SIZE;
        label.setIcon(loadIcon(iconPath, iconSize, iconSize));
        label.setText(String.valueOf(value));
        float fontSize = label.getFont().getSize2D();
        if (large) {
            fontSize = BASE_STAT_FONT_SIZE;
        }
        label.setFont(label.getFont().deriveFont(fontSize));
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private String iconHtml(String iconPath) {
        java.net.URL url = getClass().getClassLoader().getResource(iconPath);
        if (url == null) {
            return "";
        }
        int size = Math.round(BASE_STAT_ICON_SIZE * 0.75f);
        return "<img src='" + url + "' width='" + size + "' height='" + size + "'/>";
    }

    private String stageFragment(int stage, String iconPath) {
        if (stage == 0) {
            return "";
        }
        String img = iconHtml(iconPath);
        if (img.isEmpty()) {
            return " " + stage;
        }
        return " " + stage + img;
    }

    private String ailmentFragment(boolean show, String iconPath) {
        if (!show) {
            return "";
        }
        String img = iconHtml(iconPath);
        return img.isEmpty() ? "" : " " + img;
    }

    /**
     * Creates the detailed bench entry for the given dinosaur.
     */
    private JPanel createBenchItem(Dinosaur dino, boolean enableSwitch) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel img = new JLabel(loadIcon(dino.getImagePath(), 120, 80));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel n = new JLabel(dino.getName());
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        n.setFont(n.getFont().deriveFont(Font.BOLD, 14f));

        JLabel hp = new JLabel();
        setStatLabel(hp, HEALTH_ICON_PATH, dino.getHealth(), false);
        JPanel stats = new JPanel(new GridLayout(1,1,5,0));
        stats.add(hp);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton sw = new JButton("Switch");
        sw.setAlignmentX(Component.CENTER_ALIGNMENT);
        sw.setEnabled(enableSwitch);
        if (enableSwitch) {
            sw.addActionListener(e -> {
                player.queueSwitch(dino);
                doRound(null);
            });
        }

        column.add(img);
        column.add(n);
        column.add(stats);
        column.add(sw);
        return column;
    }

    private JButton createMoveButton(Dinosaur dino, Move move, boolean playerSide) {
        double attackValue = move.getType() == MoveType.HEAD
                ? dino.getEffectiveHeadAttack()
                : dino.getEffectiveBodyAttack();
        int dmg = Math.toIntExact(Math.round(move.getDamage() * attackValue));
        JButton button = new JButton(move.getName() + " (" + dmg + ")");
        if (playerSide) {
            button.addActionListener(e -> doRound(move));
        } else {
            button.setEnabled(false);
        }
        return button;
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
        final JLabel speed   = new JLabel();
        final JPanel bench   = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,5));
        final JPanel moves   = new JPanel(new GridLayout(0,2,5,5));

        DinoPanel(boolean isPlayer) {
            this.isPlayerSide = isPlayer;
            setLayout(new BorderLayout(10,10));
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // Top: dino picture
            image.setHorizontalAlignment(JLabel.CENTER);
            add(image, BorderLayout.NORTH);

            name.setHorizontalAlignment(JLabel.CENTER);
            name.setFont(name.getFont().deriveFont(Font.BOLD, 24f));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Center: stats + bench & moves
            Box info = Box.createVerticalBox();
            info.add(name);
            JPanel statsRow = new JPanel(new GridLayout(1,2,5,0));
            statsRow.add(health);
            statsRow.add(speed);
            statsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(statsRow);
            info.add(Box.createVerticalStrut(10));
            info.add(new JLabel("Moves:"));
            info.add(moves);
            info.add(Box.createVerticalStrut(10));
            info.add(new JLabel("Bench:"));
            info.add(bench);

            add(info, BorderLayout.CENTER);
        }

    }
}
