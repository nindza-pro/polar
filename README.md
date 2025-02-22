# Polar

[![license](https://img.shields.io/github/license/Minestom/MinestomDataGenerator.svg)](LICENSE)

### This fork is available on [JitPack](https://jitpack.io/#nindza-pro/polar)


A world format for Minestom designed for simpler and smaller handling of small worlds, particularly for user generated
content where size matters.

Polar generally should not be used for large worlds, since it stores worlds in a single file and does not
allow random access of chunks (the entire world must be loaded to read chunks). As a general rule of thumb,
Polar should only be used for worlds small enough that they are OK being completely kept in memory.

The Polar format is described in [FORMAT.md](FORMAT.md).

## Features

* [Fast to load](#benchmark)
* [Small file size](#benchmark)
* Simple to use
* [Anvil conversion](#anvil-interop)

## Install

Polar is available on [maven central](https://search.maven.org/search?q=g:dev.hollowcube%20AND%20a:polar).

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'dev.hollowcube:polar:<see releases>'
}
```

## Usage

Polar provides a `ChunkLoader` implementation for use with Minestom `Instance`s.

```
// Loading
Instance instance=...;
instance.setChunkLoader(new PolarLoader(Path.of("/path/to/file.polar")));

// Saving
instance.saveChunksToStorage();
```

### Anvil interop

Anvil conversion utilities are also included, and can be used something like the following.

> Note: Anvil conversion is only guaranteed to work on recent (1.19+) worlds.

```
var polarWorld = AnvilPolar.anvilToPolar(Path.of("/path/to/anvil/world/dir"));
var polarWorldBytes=PolarWriter.write(polarWorld);
```

### ChunkSelector

Most Polar functions take a `ChunkSelector` as an optional parameter to select which chunks to include in that
operation.
For example, to convert an anvil world while only selecting a 5 chunk radius around 0,0, you could do the following:

```
AnvilPolar.anvilToPolar(Path.of("/path/to/anvil/world/dir"), ChunkSelector.radius(5));
```

### User data & callbacks

By default, Polar only stores blocks, biomes, block entities, and light data. However, in many cases it is desirable
to have some additional user specific data stored in the world. To accommodate this use case, Polar chunks each have
a "user data" field, which can contain any arbitrary data. To work with it, you must implement `PolarWorldAccess`,
and provide an instance to the `PolarLoader` for example, the following will write the time of save in each chunks
user data:

```java
public class UpdateTimeWorldAccess implements PolarWorldAccess {
    private static final Logger logger = LoggerFactory.getLogger(UpdateTimeWorldAccess.class);

    @Override
    public void loadChunkData(@NotNull Chunk chunk, @Nullable NetworkBuffer userData) {
        if (userData == null) return; // No saved data, probably first load

        long lastSaveTime = userData.read(NetworkBuffer.LONG);
        logger.info("loading chunk {}, {} which was saved at {}.", chunk.getChunkX(), chunk.getChunkZ(), lastSaveTime);
    }

    @Override
    public void saveChunkData(@NotNull Chunk chunk, @NotNull NetworkBuffer userData) {
        userData.write(NetworkBuffer.LONG, System.currentTimeMillis());
    }
}
```

Using a `PolarWorldAccess` implementation is as simple as attaching it to the `PolarLoader`: 
`new PolarLoader(world).setWorldAccess(new UpdateTimeWorldAccess())`

## Comparison to others

### "Benchmark"

Using a very basic benchmark, we can make some rough guesses about performance between Polar, Anvil, and TNT.
The benchmark loads a single region 10 times, averaging the runtime of each iteration.
More information about the test can be found in [BENCHMARK.md](BENCHMARK.md)

| Scenario        | Iterations | Polar (v1, zstd) | Polar (v1, uncompressed) | TNT (v1)       | Anvil          |
|-----------------|------------|------------------|--------------------------|----------------|----------------|
| 1.19.4 Region   | 10         | 0.61400 s/iter   | 0.56449 s/iter           | 3.56732 s/iter | 9.65274 s/iter |
| EmortalMC Lobby | 10         | 0.06565 s/iter   | 0.04759 s/iter           | 0.05501 s/iter | 0.56378 s/iter |
| EmortalMC Lobby | 500        | 0.06777 s/iter   | 0.06650 s/iter           | 0.07553 s/iter | -              |

| Scenario        | Polar (v1, zstd) | Polar (v1, uncompressed) | TNT (v1) | Anvil |
|-----------------|------------------|--------------------------|----------|-------|
| 1.19.4 Region   | 5.9mb            | 26.1mb                   | 115.9mb  | 9.7mb |
| EmortalMC Lobby | 105kb            | 800kb                    | 1.3mb    | 13mb* |

* This is not a fair comparison. Polar and TNT are only covering the 10x10 relevant chunks, anvil has 4 regions.

1.19.4 Region is a single 32x32 chunk region created in 1.19.4, see `src/test/resources/bench` for the world.

EmortalMC Lobby is 10x10 chunk world, see `mc.emortal.dev` for more info.

## Contributing

Contributions via PRs and issues are always welcome.

## License

This project is licensed under the [MIT License](LICENSE).
