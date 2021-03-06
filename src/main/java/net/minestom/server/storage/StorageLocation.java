package net.minestom.server.storage;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataContainer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.reader.DataReader;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an area which contain data
 */
public class StorageLocation {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    private final StorageSystem storageSystem;
    private final String folderPath;

    private final Map<String, SerializableData> cachedData;

    protected StorageLocation(StorageSystem storageSystem, String folderPath, StorageOptions storageOptions) {
        this.storageSystem = storageSystem;
        this.folderPath = folderPath;

        this.cachedData = new HashMap<>();

        this.storageSystem.open(folderPath, storageOptions);
    }

    /**
     * Get the data associated with a key
     *
     * @param key the key
     * @return the data associated to {@code key}
     * @see StorageSystem#get(String)
     */
    public byte[] get(String key) {
        return storageSystem.get(key);
    }

    /**
     * Set a data associated to a key
     *
     * @param key  the key of the data
     * @param data the data
     * @see StorageSystem#set(String, byte[])
     */
    public void set(String key, byte[] data) {
        this.storageSystem.set(key, data);
    }

    /**
     * Delete a key
     *
     * @param key the key
     * @see StorageSystem#delete(String)
     */
    public void delete(String key) {
        this.storageSystem.delete(key);
    }

    /**
     * Close the {@link StorageLocation}
     *
     * @see StorageSystem#close()
     */
    public void close() {
        this.storageSystem.close();
    }

    /**
     * Set an object associated to a key
     * <p>
     * It does use registered {@link DataType} located on {@link DataManager}
     * So you need to register all the types that you use
     *
     * @param key    the key
     * @param object the data object
     * @param type   the class of the data
     * @param <T>    the type of the data
     */
    public <T> void set(String key, T object, Class<T> type) {
        final DataType<T> dataType = DATA_MANAGER.getDataType(type);
        Check.notNull(dataType, "You can only save registered DataType type!");

        // Encode the data
        BinaryWriter binaryWriter = new BinaryWriter();
        dataType.encode(binaryWriter, object); // Encode
        final byte[] encodedValue = binaryWriter.toByteArray(); // Retrieve bytes

        // Write it
        set(key, encodedValue);
    }

    /**
     * Retrieve a serialized object associated to a key
     * <p>
     * It does use registered {@link DataType} located on {@link DataManager}
     * So you need to register all the types that you use
     *
     * @param key  the key
     * @param type the class of the data
     * @param <T>  the type of the data
     * @return the object associated to the key
     */
    public <T> T get(String key, Class<T> type) {
        final DataType<T> dataType = DATA_MANAGER.getDataType(type);
        Check.notNull(dataType, "You can only get registered DataType type!");

        final byte[] data = get(key);
        // No key
        if (data == null)
            return null;

        // Decode the data
        BinaryReader binaryReader = new BinaryReader(data);
        return dataType.decode(binaryReader);
    }

    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        T value;
        return (value = get(key, type)) != null ? value : defaultValue;
    }

    /**
     * Get an unique {@link SerializableData} which is cloned if cached or retrieved with the default {@link StorageSystem}
     *
     * @param key           the key of the data
     * @param dataContainer the {@link DataContainer} which will contain the new data
     */
    public void getAndCloneData(String key, DataContainer dataContainer) {
        synchronized (cachedData) {
            // Copy data from the cachedMap
            if (cachedData.containsKey(key)) {
                SerializableData data = cachedData.get(key);
                dataContainer.setData(data.clone());
                return;
            }
        }

        // Load it from the storage system
        final byte[] bytes = get(key);
        SerializableData data;

        if (bytes != null) {
            data = DataReader.readIndexedData(new BinaryReader(bytes));
        } else {
            data = new SerializableDataImpl();
        }

        dataContainer.setData(data);

    }

    /**
     * Get a shared {@link SerializableData} if already in memory or retrieve it from the default {@link StorageSystem} and save it in cache
     * for further request.
     * Those cached data can be saved using {@link #saveCachedData()} or individually with {@link #saveCachedData(String)}
     * It is also possible to save an individual data and remove it directly with {@link #saveAndRemoveCachedData(String)}
     *
     * @param key           the key of the data
     * @param dataContainer the {@link DataContainer} which will contain the new data
     */
    public void getAndCacheData(String key, DataContainer dataContainer) {
        synchronized (cachedData) {
            // Give the cached SerializableData if already loaded
            if (cachedData.containsKey(key)) {
                dataContainer.setData(cachedData.get(key));
                return;
            }

            // Load it from the storage system and cache it
            final byte[] bytes = get(key);
            SerializableData data;

            if (bytes != null) {
                data = DataReader.readIndexedData(new BinaryReader(bytes));
            } else {
                data = new SerializableDataImpl();
            }

            dataContainer.setData(data);

            this.cachedData.put(key, data);

        }
    }

    /**
     * Save a specified cached data and remove it from memory
     *
     * @param key the specified cached data key
     */
    public void saveAndRemoveCachedData(String key) {
        synchronized (cachedData) {
            final SerializableData serializableData = cachedData.get(key);
            if (serializableData == null)
                return;

            // Save the data
            set(key, serializableData.getIndexedSerializedData());

            // Remove from map
            this.cachedData.remove(key);
        }
    }

    /**
     * Save the whole cached data
     */
    public void saveCachedData() {
        synchronized (cachedData) {
            cachedData.forEach((key, data) -> {
                set(key, data.getIndexedSerializedData());
            });
        }
    }

    /**
     * Save an unique cached data
     *
     * @param key the data key
     */
    public void saveCachedData(String key) {
        synchronized (cachedData) {
            final SerializableData data = cachedData.get(key);
            set(key, data.getIndexedSerializedData());
        }
    }

    public String getFolderPath() {
        return folderPath;
    }
}
