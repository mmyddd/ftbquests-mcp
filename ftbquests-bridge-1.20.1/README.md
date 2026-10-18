# ftbquests-bridge — Minecraft 1.20.1 / Forge

Forge port of the `ftbquests-bridge` mod: the loopback HTTP+JSON API that the
`ftbquests` Claude Code plugin talks to. Same protocol (v1) and same routes as the
1.21.1 Architectury tree in `../ftbquests-bridge/`; only the FTB Quests backend is
rewritten against the 1.20.1 API.

- **Minecraft 1.20.1 · Forge 47.4.1 · Java 17**
- **FTB Quests 2001.4.11** (2001.x line), FTB Library 2001.2.4, FTB Teams 2001.3.0,
  Architectury 9.2.14

Port by **mmyddd**; original mod by **west3436**.

## Build

```bash
./gradlew build          # -> build/libs/ftbquests-bridge-forge-1.2.0.jar
./gradlew test           # JUnit 5; no Minecraft bootstrap required
```

Gradle 8.14 + a Java 17 toolchain. `gradle.properties` pins
`org.gradle.java.home`; override with
`-Dorg.gradle.java.home=/path/to/jdk17` if yours lives elsewhere.

## Run a dev server

```bash
./gradlew runServer      # 1.20.1 + Forge + FTB Quests, world in run/server/
```

The first run stops at the EULA prompt: create `run/server/eula.txt` containing
`eula=true` and start it again (`run/` is git-ignored, so it is never committed).
On `ServerStartedEvent` the
bridge writes `run/server/config/ftbquests-bridge.json` (settings) and
`run/server/config/ftbquests-bridge/runtime.json` (bound port + bearer token), and
listens on `127.0.0.1:25599`. Ping it with the token from `runtime.json`:

```bash
TOKEN=$(grep -o '"token": *"[^"]*"' run/server/config/ftbquests-bridge/runtime.json | cut -d'"' -f4)
curl -s -H "Authorization: Bearer $TOKEN" http://127.0.0.1:25599/health
# {"ok":true,"questsLoaded":true,"protocolVersion":1,"loader":"forge",
#  "minecraftVersion":"1.20.1","ftbQuestsVersion":"2001.4.11"}
```

The FTB jars are `modRuntimeOnly` for that run only — they are **not** bundled into
the produced jar, which keeps a normal `modCompileOnly` dependency on FTB Quests.

## What changed from the 1.21.1 tree

Everything outside `backend/ftbq/` is identical to
`../ftbquests-bridge/common/src/main/java/com/ftbqbridge/` — the HTTP server, router,
auth, config, JSON helpers and `QuestBackend` interface are Minecraft-free.

| File | 1.21.1 | 1.20.1 |
|---|---|---|
| `FtbQuestsBackend` | `holderLookup()`, 2-arg `writeData`, `NetworkHelper` | `writeData(CompoundTag)`, `Message.sendToAll(server)`, `deleteObject(long)` |
| `TranslationFields` | splits title/subtitle/description into `TranslationManager` writes | **deleted** — 2001.x stores them as plain NBT (`Quest.java:232` subtitle, `:242` description) |
| `TypeSchemas` | `ResourceLocation.parse` | `new ResourceLocation(id)`, plus bare ids normalized to `ftbquests:` |
| `RegistryReader` | `AdvancementHolder` | `Advancement` + nullable `getDisplay()`; ids rebuilt from the resource listing |
| `QuestTree` | `Path`-based `chapterFilename` | `String`-based (2001.x `getPath()` returns `Optional<String>`) |
| Entrypoint | Architectury `@ExpectPlatform` + Fabric/NeoForge modules | single `FtbQuestsBridgeForge` `@Mod` on the Forge event bus |

Full-file line references for the 2001.4.11 API quirks are in the code comments.

## License

MIT — see [../LICENSE](../LICENSE).
