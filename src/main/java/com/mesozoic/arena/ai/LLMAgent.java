package com.mesozoic.arena.ai;

import ai.djl.ModelException;
import ai.djl.modality.nlp.translator.SimpleText2TextTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.util.Config;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Opponent powered by a local language model.
 */
public class LLMAgent implements OpponentAgent {
    private static final String MODEL_DIR = Config.llmModelDir();
    private final ZooModel<String, String> model;

    /**
     * Loads the model from the local {@code models} directory.
     */
    public LLMAgent() throws IOException, ModelException {
        model = loadModel();
    }

    private static ZooModel<String, String> loadModel()
            throws IOException, ModelException {
        System.out.println("Loading LLM model from " + MODEL_DIR);
        var translator = new SimpleText2TextTranslator();
        Criteria<String, String> criteria = Criteria.builder()
                .setTypes(String.class, String.class)
                .optModelPath(Paths.get(MODEL_DIR))
                .optTranslator(translator)
                .build();
        ZooModel<String, String> loadedModel = criteria.loadModel();
        System.out.println("LLM model loaded successfully");
        return loadedModel;
    }

    @Override
    public Move chooseMove(Dinosaur self, Dinosaur enemy) {
        if (self == null || enemy == null) {
            return null;
        }
        String prompt = buildPrompt(self, enemy);
        try (var predictor = model.newPredictor()) {
            String output = predictor.predict(prompt);
            return parseMove(output, self);
        } catch (TranslateException e) {
            return new RandomOpponent().chooseMove(self, enemy);
        }
    }

    private Move parseMove(String output, Dinosaur self) {
        for (Move move : self.getMoves()) {
            if (output.toLowerCase().contains(move.getName().toLowerCase())
                    && self.getStamina() >= move.getStaminaCost()) {
                return move;
            }
        }
        return new RandomOpponent().chooseMove(self, null);
    }

    private String buildPrompt(Dinosaur self, Dinosaur enemy) {
        List<String> moveNames = self.getMoves().stream()
                .map(Move::getName)
                .collect(Collectors.toList());
        return "Choose the best move for " + self.getName()
                + " against " + enemy.getName() + ". Available moves: "
                + String.join(", ", moveNames) + ".";
    }
}
