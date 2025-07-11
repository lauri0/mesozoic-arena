package com.mesozoic.arena;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.ui.MainWindow;
import com.mesozoic.arena.util.DinosaurLoader;
import java.util.List;

/**
 * Entry point for the application.
 */
public class App {
    public static void main(String[] args) {
        List<Dinosaur> allDinosaurs = DinosaurLoader.load();
        if (allDinosaurs.size() < 2) {
            System.err.println("Not enough dinosaurs defined");
            return;
        }
        Player player = new Player(List.of(allDinosaurs.get(0)));
        Player opponent = new Player(List.of(allDinosaurs.get(1)));
        Battle battle = new Battle(player, opponent);
        MainWindow.launch(battle, player, opponent);
    }
}
