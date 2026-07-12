package org.team5459.config;

import java.io.File;

public final class ConfigManager {

    private ConfigManager() {
    }

    public static ConfigRoot load(File jsonFile) {
        return JsonConfigLoader.load(jsonFile);
    }

    public static ConfigRoot loadDefault() {
        return JsonConfigLoader.loadDefault();
    }
}
