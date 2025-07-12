package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.util.Config;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.ModelParameters;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaOutput;

import java.util.stream.Collectors;

/**
 * Opponent powered by a local llama.cpp model via java-llama.cpp v4.1.0
 */
public class LLMAgent implements OpponentAgent, AutoCloseable {

    private final LlamaModel model;
    private String lastResponse;

    public LLMAgent() {
        String modelPath = Config.llmModelDir() + "/Ministral-3b-instruct.Q4_K_M.gguf";

        System.out.println("Loading llama.cpp model (CPU-only) from " + modelPath);
        ModelParameters params = new ModelParameters()
                .setModel(modelPath)
                .setGpuLayers(0); // CPU-only inference
        model = new LlamaModel(params);
        System.out.println("Model loaded successfully");
    }

    @Override
    public Move chooseMove(Dinosaur self, Dinosaur enemy) {
        if (self == null || enemy == null) return null;

        String prompt = buildPrompt(self, enemy);
        InferenceParameters ip = new InferenceParameters(prompt)
                .setTemperature(0.4f)
                .setStopStrings("\n");

        try {
            StringBuilder sb = new StringBuilder();
            for (LlamaOutput out : model.generate(ip)) {
                sb.append(out);
            }
            String output = sb.toString();
            lastResponse = output;
            return parseMove(output, self);
        } catch (Exception e) {
            e.printStackTrace();
            lastResponse = "";
            return new RandomOpponent().chooseMove(self, enemy);
        }
    }

    private Move parseMove(String output, Dinosaur self) {
        String lc = output.toLowerCase();
        for (Move move : self.getMoves()) {
            if (lc.contains(move.getName().toLowerCase()) &&
                    self.getStamina() >= move.getStaminaCost()) {
                return move;
            }
        }
        return new RandomOpponent().chooseMove(self, null);
    }

    private String buildPrompt(Dinosaur self, Dinosaur enemy) {
        String moveList = self.getMoves().stream()
                .map(Move::getName)
                .collect(Collectors.joining(", "));
        return "Choose the best move for " + self.getName() +
                " against " + enemy.getName() + ". Available moves: " +
                moveList + ".\nMove:";
    }

    public String getLastResponse() {
        return lastResponse;
    }

    @Override
    public void close() {
        model.close();
    }
}
