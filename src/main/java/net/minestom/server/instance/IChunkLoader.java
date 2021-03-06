package net.minestom.server.instance;

import java.util.function.Consumer;

/**
 * Interface implemented to change the way chunks are loaded/saved
 * see {@link MinestomBasicChunkLoader} for the default implementation used in {@link InstanceContainer}
 */
public interface IChunkLoader {

    /**
     * Load a specific chunk, all blocks should be set since the {@link ChunkGenerator} is not applied
     *
     * @param instance the instance where the chunk belong
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback the callback executed when the chunk is done loading
     * @return true if the chunk loaded successfully, false otherwise
     */
    boolean loadChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback);

    /**
     * Save a specific chunk with a callback for when it is done
     *
     * @param chunk    the chunk to save
     * @param callback the callback executed when the chunk is done saving
     */
    void saveChunk(Chunk chunk, Runnable callback);

    /**
     * Does this ChunkLoader allow for multi-threaded saving of chunks?
     *
     * @return true if the chunk loader supports parallel saving
     */
    default boolean supportsParallelSaving() {
        return false;
    }

    /**
     * Does this ChunkLoader allow for multi-threaded loading of chunks?
     *
     * @return true if the chunk loader supports parallel loading
     */
    default boolean supportsParallelLoading() {
        return false;
    }
}
