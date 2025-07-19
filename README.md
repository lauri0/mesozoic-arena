# Mesozoic Arena

Battle dinosaurs in a simple turn based arena. The application is a standard
Maven project and only requires a Java 17 runtime.

## Building

Run the following from the project root to compile the sources and package the
game:

```bash
mvn package
```

The command creates `target/mesozoic-arena-1.0-SNAPSHOT.jar` together with all
compiled classes.

## Running

Start the game through Maven so that all dependencies are automatically added to
the class path:

```bash
mvn exec:java
```

## Gameplay

When launched, a window displays your active dinosaur on the left and the
opponent on the right. Four buttons at the bottom correspond to the moves of the
currently active dinosaur. Additional dinosaurs appear in the bench area with a
`Switch` button. Selecting it consumes your turn but the swap happens before any
attacks are performed, so incoming damage hits the new dinosaur. Use the
**Exit Game** button to close the window. The match ends when one side has no
dinosaurs remaining.

## LLM Opponent

The opponent AI can use Google's Gemini Flash model. To enable it:

1. Copy the `gemini.env.example` file to `gemini.env`. Open the `gemini.env` file in the project root and add your API key after
   `API_KEY=`.
2. Ensure `useLLMAgent=true` in `data/constants.ini`.

The application first looks for `gemini.env` on the class path but also falls back to the current working directory. Place the file next to the JAR when running a packaged build.

If the API request fails, the game falls back to a random strategy. The raw
response from the model is appended to the in-game log displayed on the right
side of the window.

When producing a move the model may include reasoning or multiple lines in its
response. The final action should be provided on a line starting with
`Answer:` followed by either the move name or `Switch to <dinosaur>`.

## MCTS Opponent

When the LLM is disabled the game uses a Monte Carlo Tree Search agent. The
search depth can be tuned through the `mctsIterations` option in
`data/constants.ini`. Higher values yield stronger play at the cost of longer
thinking time. The default is `1000` iterations.
