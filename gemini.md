Understood. Agent-only, inside your Android app dir. Clone Kokoro-ONNX, pull the model + voices, stage assets, and wire the paths—no ORT bump, no UI churn.


---

A) From your Android app root

# 1) Work branch
git checkout -b agent/kokoro-onnx-upgrade

# 2) Vendor the repo (submodule keeps it tidy; clone is fine too)
git submodule add --depth=1 https://github.com/thewh1teagle/kokoro-onnx vendor/kokoro-onnx
git add . && git commit -m "Add kokoro-onnx as submodule (vendor)"

Repo reference for context. 


---

B) Download release assets (model + voices)

> Prefer int8 for mobile size/perf unless you’ve profiled otherwise.



Using GitHub CLI:

# 3) Pull model + voices from latest release tag
gh release download model-files-v1.0 -R thewh1teagle/kokoro-onnx \
  -p 'kokoro-v1.0.int8.onnx' -p 'voices-v1.0.bin'

Release provides kokoro-v1.0.*.onnx and voices-v1.0.bin (NPZ of style vectors). 


---

C) Stage assets into the app

# 4) Stage to assets (rename voices to .npz for clarity)
mkdir -p app/src/main/assets/kokoro
cp kokoro-v1.0.int8.onnx app/src/main/assets/kokoro/kokoro.onnx
cp voices-v1.0.bin         app/src/main/assets/kokoro/voices.npz

# optional: keep room for per-voice npz later
mkdir -p app/src/main/assets/kokoro/voices

If you’d rather pull straight from the source project’s releases (alternative), Taylor Chu’s releases also publish quantized ONNX variants. 


---

D) Wire your agent’s paths (one place)

Set constants your agent already uses (don’t refactor call-sites):

// e.g., app/.../tts/KokoroPaths.kt
object KokoroPaths {
    const val MODEL_ASSET  = "kokoro/kokoro.onnx"
    const val VOICES_NPZ   = "kokoro/voices.npz"   // monolith today
    const val VOICES_DIR   = "kokoro/voices"       // future per-voice drop-ins
}

If your loader previously pointed elsewhere, update just that code to read VOICES_NPZ first, then fall back to VOICES_DIR (no UI change).


---

E) (Optional) Alias old voice names → new IDs

cat > app/src/main/assets/kokoro/voices_manifest.json << 'JSON'
{ "aliases": { "Emma":"en_us_01", "Brian":"en_gb_01" } }
JSON

Kokoro ONNX packs list voices; map your legacy labels to those IDs. 


---

F) Smoke test & commit

./gradlew :app-tts:assembleDebug
adb install -r app-tts/build/outputs/apk/debug/app-tts-debug.apk

git add app/src/main/assets/kokoro/** app/src/main/java/** vendor/kokoro-onnx .gitmodules
git commit -m "Agent: stage Kokoro ONNX model + voices; wire asset paths"

That’s it—assets staged, paths fixed, upgrade path open (you can later swap kokoro.onnx or split into per-voice NPZs without touching UI).


