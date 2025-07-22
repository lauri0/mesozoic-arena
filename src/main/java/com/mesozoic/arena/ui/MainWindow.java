package com.mesozoic.arena.ui;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.DinoType;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.DamageCalculator;
import com.mesozoic.arena.model.Ability;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    private static final String HEALTH_ICON_PATH  = "assets/icons/health.png";
    private static final String SPEED_ICON_PATH   = "assets/icons/speed.png";
    private static final String ATTACK_ICON_PATH  = "assets/icons/attack.png";
    private static final String ACCURACY_ICON_PATH = "assets/icons/accuracy.png";
    private static final String BLEED_ICON_PATH   = "assets/icons/bleed.png";
    private static final String HEAD_ICON_PATH    = "assets/icons/head.png";
    private static final String BODY_ICON_PATH    = "assets/icons/tail.png";

    private static final int   BASE_STAT_ICON_SIZE   = 32;
    private static final int   BASE_STAT_FONT_SIZE   = 16;
    private static final float STAT_LARGE_ICON_SIZE_MULT = 1.5f;
    private static final int   TYPE_BOX_SIZE = 12;
    private static final int   ACTIVE_IMG_WIDTH  = 384;
    private static final int   ACTIVE_IMG_HEIGHT = 256;
    private static final int   BENCH_IMG_WIDTH   = 120;
    private static final int   BENCH_IMG_HEIGHT  = 80;

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
        setSize(1800, 950);

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
        JButton typeChartButton = new JButton("Type Chart");
        typeChartButton.addActionListener(e -> openTypeChart());
        Box npcHeader = Box.createVerticalBox();
        npcHeader.add(typeChartButton);
        npcHeader.add(npcLabel);
        npcPanel.add(npcHeader, BorderLayout.NORTH);
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
            panel.abilityHeading.setVisible(false);
            panel.ability.setText("");
        } else {
            panel.name.setText("<html>" + formatDinoName(d, true, ACTIVE_IMG_WIDTH) + "</html>");
            Ability ability = d.getAbility();
            if (ability == null) {
                panel.abilityHeading.setVisible(false);
                panel.ability.setText("");
            } else {
                panel.abilityHeading.setVisible(true);
                panel.ability.setText(ability.getName() + " - " + ability.getDescription());
            }
        }

        // Stats with icons
        if (d == null) {
            setStatLabel(panel.health,  HEALTH_ICON_PATH, "0", true);
            setStatLabel(panel.headAttack, HEAD_ICON_PATH, "0", true);
            setStatLabel(panel.bodyAttack, BODY_ICON_PATH, "0", true);
            setStatLabel(panel.speed,   SPEED_ICON_PATH,  "0", true);
        } else {
            String hpText = String.valueOf(d.getHealth());
            setStatLabel(panel.health, HEALTH_ICON_PATH, hpText, true);
            setStatLabel(panel.headAttack, HEAD_ICON_PATH,
                    String.format("%.2f", d.getHeadAttack()), true);
            setStatLabel(panel.bodyAttack, BODY_ICON_PATH,
                    String.format("%.2f", d.getBodyAttack()), true);
            setStatLabel(panel.speed, SPEED_ICON_PATH, d.getSpeed(), true);
        }

        // Image
        if (d != null) {
            panel.image.setIcon(loadIcon(d.getImagePath(), ACTIVE_IMG_WIDTH, ACTIVE_IMG_HEIGHT));
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
     * Helper to put an icon and value text into a JLabel.
     */
    private void setStatLabel(JLabel label, String iconPath, String text, boolean large) {
        int iconSize = large ? Math.round(BASE_STAT_ICON_SIZE * STAT_LARGE_ICON_SIZE_MULT) : BASE_STAT_ICON_SIZE;
        label.setIcon(loadIcon(iconPath, iconSize, iconSize));
        label.setText(text);
        float fontSize = label.getFont().getSize2D();
        if (large) {
            fontSize = BASE_STAT_FONT_SIZE;
        }
        label.setFont(label.getFont().deriveFont(fontSize));
    }

    private void setStatLabel(JLabel label, String iconPath, int value, boolean large) {
        setStatLabel(label, iconPath, String.valueOf(value), large);
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        return url == null ? new ImageIcon() : new ImageIcon(url);
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        ImageIcon base = loadIcon(path);
        if (base.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        Image img = base.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void openTypeChart() {
        DinoType[] types = DinoType.values();
        StringBuilder table = new StringBuilder();
        table.append("<html><body><table border='1' cellpadding='4' cellspacing='0'>");
        table.append("<tr><th>Atk/Def</th>");
        for (DinoType defend : types) {
            table.append(typeHeaderCellHtml(defend));
        }
        table.append("</tr>");
        for (DinoType attack : types) {
            table.append("<tr>").append(typeHeaderCellHtml(attack));
            for (DinoType defend : types) {
                double multiplier = defend.getMultiplierFrom(attack);
                String color;
                if (multiplier == 2.0) {
                    color = "#ccffcc"; // green
                } else if (multiplier == 0.5) {
                    color = "#ffcccc"; // red
                } else {
                    color = "#ffffff"; // white
                }
                table.append("<td style='background-color:")
                        .append(color)
                        .append(";text-align:center;'>");
                if (multiplier == 2.0) {
                    table.append("2x");
                } else if (multiplier == 0.5) {
                    table.append("0.5x");
                } else {
                    table.append("1x");
                }
                table.append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></body></html>");

        JLabel label = new JLabel(table.toString());
        JFrame frame = new JFrame("Type Chart");
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(label), BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(e -> frame.dispose());
        frame.add(close, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private String iconHtml(String iconPath) {
        java.net.URL url = getClass().getClassLoader().getResource(iconPath);
        if (url == null) {
            return "";
        }
        int size = Math.round(BASE_STAT_ICON_SIZE * 0.75f);
        return "<img src='" + url + "' width='" + size + "' height='" + size + "'/>";
    }

    private String typeIconHtml(DinoType type) {
        if (type == null) {
            return "";
        }
        String path = "assets/icons/" + type.name().toLowerCase() + ".png";
        return iconHtml(path);
    }

    private static final Map<DinoType, Color> TYPE_COLORS = Map.ofEntries(
            Map.entry(DinoType.BITER, new Color(255, 165, 0)),      // orange
            Map.entry(DinoType.BLEEDER, new Color(255, 0, 0)),      // red
            Map.entry(DinoType.CHARGER, new Color(135, 92, 52)),    // brown
            Map.entry(DinoType.CRUSHER, new Color(139, 0, 0)),      // darkred
            Map.entry(DinoType.DEFENDER, new Color(192, 192, 192)), // silver
            Map.entry(DinoType.GRAZER, new Color(50, 205, 50)),     // limegreen
            Map.entry(DinoType.IMPALER, new Color(0, 100, 0)),      // darkgreen
            Map.entry(DinoType.LANDSCAPER, new Color(0, 0, 255)),   // blue
            Map.entry(DinoType.RUNNER, new Color(0, 255, 255)),     // cyan
            Map.entry(DinoType.SLASHER, new Color(255, 255, 0)),    // yellow
            Map.entry(DinoType.SWIMMER, new Color(128, 0, 128))     // purple
    );


    private static String colorHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String typeBoxHtml(DinoType type) {
        Color color = TYPE_COLORS.getOrDefault(type, Color.LIGHT_GRAY);
        return "<span style='display:inline-block;width:" + TYPE_BOX_SIZE + "px;" +
                "height:" + TYPE_BOX_SIZE + "px;background-color:" +
                colorHex(color) + ";'>&nbsp;</span>";
    }

    private String typeHeaderCellHtml(DinoType type) {
        Color color = TYPE_COLORS.getOrDefault(type, Color.LIGHT_GRAY);
        return "<th style='background-color:" + colorHex(color) + ";'>"
                + type.name().substring(0, 3) + "</th>";
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

    private String typeBadgesHtml(java.util.List<DinoType> types) {
        StringBuilder sb = new StringBuilder();
        for (DinoType type : types) {
            Color color = TYPE_COLORS.getOrDefault(type, Color.LIGHT_GRAY);
            sb.append("<span style='background-color:")
                    .append(colorHex(color))
                    .append(";padding:2px 4px;margin-right:2px;font-weight:bold;'>")
                    .append(type.name().substring(0, 3))
                    .append("</span>");
        }
        return sb.toString();
    }

    private String formatDinoName(Dinosaur dino, boolean includeStatus, int imgWidth) {
        java.util.List<DinoType> types = dino.getTypes();
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='width:").append(imgWidth).append("px;text-align:center;'>");
        sb.append(typeBadgesHtml(types)).append(dino.getName());
        if (includeStatus) {
            sb.append(stageFragment(dino.getHeadAttackStage(), HEAD_ICON_PATH));
            sb.append(stageFragment(dino.getBodyAttackStage(), BODY_ICON_PATH));
            sb.append(stageFragment(dino.getSpeedStage(), SPEED_ICON_PATH));
            sb.append(ailmentFragment(dino.hasAilment("Bleeding"), BLEED_ICON_PATH));
        }
        sb.append("</div>");
        return sb.toString();
    }


    /**
     * Creates the detailed bench entry for the given dinosaur.
     */
    private JPanel createBenchItem(Dinosaur dino, boolean enableSwitch) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel img = new JLabel(loadIcon(dino.getImagePath(), BENCH_IMG_WIDTH, BENCH_IMG_HEIGHT));
        img.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel n = new JLabel("<html>" + formatDinoName(dino, false, BENCH_IMG_WIDTH) + "</html>");
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        n.setHorizontalAlignment(JLabel.CENTER);
        n.setFont(n.getFont().deriveFont(Font.BOLD, 14f));

        Ability dinoAbility = dino.getAbility();
        JLabel abilityLabel = new JLabel();
        abilityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        abilityLabel.setFont(new JButton().getFont());
        if (dinoAbility != null) {
            abilityLabel.setText(dinoAbility.getName());
        }

        JLabel hp = new JLabel();
        setStatLabel(hp, HEALTH_ICON_PATH, dino.getHealth(), false);
        JLabel sp = new JLabel();
        setStatLabel(sp, SPEED_ICON_PATH, dino.getSpeed(), false);
        JPanel stats = new JPanel(new GridLayout(1,2,5,0));
        stats.add(hp);
        stats.add(sp);
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
        column.add(abilityLabel);
        column.add(stats);
        column.add(sw);
        return column;
    }

    private JButton createMoveButton(Dinosaur dino, Move move, boolean playerSide) {
        Dinosaur target = playerSide ? opponent.getActiveDinosaur() : player.getActiveDinosaur();
        int damage = DamageCalculator.calculate(dino, target, move);
        String attackImg = iconHtml(ATTACK_ICON_PATH);
        String accuracyImg = iconHtml(ACCURACY_ICON_PATH);
        String nameWithType = typeBoxHtml(move.getType()) + " " + move.getName();
        String label = String.format("<html>%s %s %d %s %.0f%s</html>",
                nameWithType, attackImg, damage, accuracyImg, move.getAccuracy() * 100, "%");
        JButton button = new JButton(label);
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
        final JLabel abilityHeading = new JLabel("Ability:");
        final JLabel ability = new JLabel();
        final JLabel health      = new JLabel();
        final JLabel headAttack  = new JLabel();
        final JLabel bodyAttack  = new JLabel();
        final JLabel speed       = new JLabel();
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
            JPanel statsRow = new JPanel(new GridLayout(1,4,5,0));
            statsRow.add(health);
            statsRow.add(headAttack);
            statsRow.add(bodyAttack);
            statsRow.add(speed);
            statsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(statsRow);
            info.add(Box.createVerticalStrut(10));
            ability.setAlignmentX(Component.CENTER_ALIGNMENT);
            ability.setFont(new JButton().getFont());
            info.add(abilityHeading);
            info.add(ability);
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
