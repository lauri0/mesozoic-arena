package com.mesozoic.arena;

import com.mesozoic.arena.data.DinosaurLoader;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.ui.MainWindow;
import java.io.IOException;

/**
 * Entry point for the application.
 */
public class App {
    public static void main(String[] args) {
        try {
            DinosaurLoader loader = new DinosaurLoader();
            Player player = loader.createRandomPlayer();
            Player opponent = loader.createRandomPlayer();

            Battle battle = new Battle(player, opponent);
            MainWindow window = new MainWindow(battle, player, opponent);
            window.setVisible(true);

            waitForWinner(window, battle);
            announceWinner(battle, player, opponent);
        } catch (IOException e) {
            System.err.println("Failed to load dinosaurs: " + e.getMessage());
        }
    }

    private static void waitForWinner(MainWindow window, Battle battle) {
        while (window.isDisplayable() && battle.getWinner() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static void announceWinner(Battle battle, Player player, Player opponent) {
        Player winner = battle.getWinner();
        if (winner != null) {
            String label = winner == player ? "Player" : "Opponent";
            System.out.println(label + " wins!");
        }
    }
}
