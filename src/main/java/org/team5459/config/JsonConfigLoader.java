package org.team5459.config;

import java.io.File;

public final class JsonConfigLoader {

    private JsonConfigLoader() {
    }

    public static ConfigRoot load(File jsonFile) {
        // TODO: parse JSON from file into ConfigRoot
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static ConfigRoot loadDefault() {
        // TODO: locate deploy/config.json or another default path and load it
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
