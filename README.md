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
mvn exec:java -Dexec.mainClass=com.mesozoic.arena.App -Dexec.classpathScope=runtime
```

## Gameplay

When launched, a window displays your active dinosaur on the left and the
opponent on the right. Four buttons at the bottom correspond to the moves of the
currently active dinosaur. Additional dinosaurs appear in the bench area with a
`Switch` button that swaps them into battle. Use the **Exit Game** button to
close the window. The match ends when one side has no dinosaurs remaining.

## LLM Opponent

The project can use a small language model to drive the opponent AI. The weights
are not bundled with the repository. To enable the LLM based agent:

1. Download the `distilgpt2` model from Hugging Face and extract it to
   `models/gpt2` so that the directory contains `config.json`,
   `pytorch_model.bin` and the related files. A quick option is:

   ```bash
   huggingface-cli download distilgpt2 --local-dir models/gpt2
   ```

   or download the archive from the web interface and extract it manually.
2. Edit `data/constants.ini` and set `useLLMAgent=true`.

If the model cannot be loaded, the game falls back to a random strategy.
