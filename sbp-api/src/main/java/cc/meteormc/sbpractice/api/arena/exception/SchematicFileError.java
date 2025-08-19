package cc.meteormc.sbpractice.api.arena.exception;

import java.io.IOException;

public class SchematicFileError extends Error {
    protected SchematicFileError() {

    }

    protected SchematicFileError(String message) {
        super(message);
    }

    protected SchematicFileError(String message, Throwable cause) {
        super(message, cause);
    }

    protected SchematicFileError(Throwable cause) {
        super(cause);
    }

    public SchematicFileError(String message, IOException exception) {
        super(message, exception);
    }
}
