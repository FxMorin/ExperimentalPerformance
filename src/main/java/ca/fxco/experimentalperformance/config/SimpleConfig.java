package ca.fxco.experimentalperformance.config;

import ca.fxco.experimentalperformance.ExperimentalPerformance;
import ca.fxco.experimentalperformance.memoryDensity.InfoHolderData;
import ca.fxco.experimentalperformance.memoryDensity.VersionedInfoHolderData;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.infoHolderDataMap;
import static ca.fxco.experimentalperformance.memoryDensity.HolderDataRegistry.versionedInfoHolderDataMap;

/**
    This config clears all its information once it's done loading
 */

public class SimpleConfig {

    // TODO: This is going to need a major rewrite, currently its not even being used. It should instead be a package
    //      limited. Something like "net.minecraft.client.*"=false to disable entire packages. Since its automatic
    //      now, so you have no idea what classes will be modified!

    private final Path configPath;
    private boolean parsed = false;

    // Could clear this set if memoryleakfix is present
    private final Set<String> validHolders = new HashSet<>();

    public SimpleConfig() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("experimentalperformance.properties");
    }

    public boolean hasParsed() {
        return this.parsed;
    }

    public boolean shouldLoad(String id) {
        return this.validHolders.contains(id);
    }

    public void clearHolders() {
        this.validHolders.clear();
    }

    public void parseConfig() {
        if (parsed) return;
        parsed = true;
        if (Files.exists(this.configPath)) {
            Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(this.configPath)) {
                properties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }
            loadProperties(properties);
        } else {
            try {
                writeDefaultConfig();
            } catch (IOException e) {
                ExperimentalPerformance.LOGGER.warn("Unable to create the config file", e);
            }
        }
    }

    private void loadProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value.equalsIgnoreCase("true"))
                this.validHolders.add(key);
        }
    }

    private void writeDefaultConfig() throws IOException {
        Path path = this.configPath.getParent();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        } else if (!Files.isDirectory(path)) {
            throw new IOException("Unable to create config file without a directory");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(this.configPath)) {
            writer.write("# This is the ExperimentalPerformance mod config\n");
            writer.write("# Use this config to change what options should be enabled or disabled\n");
            writer.write("#\n");
            writer.write("# The default value will be placed here when writing the config for the first time\n\n");
            for (Map.Entry<String, InfoHolderData> entry : infoHolderDataMap.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue().getDefaultValue() + "\n");
                if (entry.getValue().getDefaultValue()) this.validHolders.add(entry.getKey());
            }
            for (Map.Entry<String, VersionedInfoHolderData> entry : versionedInfoHolderDataMap.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue().getDefaultValue() + "\n");
                if (entry.getValue().getDefaultValue()) this.validHolders.add(entry.getKey());
            }
        }
    }
}
