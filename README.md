# Mesozoic Arena

## LLM Opponent

The project can use a small language model to drive the opponent AI. The weights
are not bundled with the repository. To enable the LLM based agent:

1. Download the `distilgpt2` model from Hugging Face and extract it to
   `models/gpt2` so that the directory contains `config.json`, `pytorch_model.bin`
   and related files.
2. Edit `data/constants.ini` and set `useLLMAgent=true`.

If the model cannot be loaded, the game falls back to a random strategy.
