package cc.meteormc.sbpractice.api.arena.exception;

public class ArenaCreationException extends Exception {
    protected ArenaCreationException() {
    }

    public ArenaCreationException(String message) {
        super(message);
    }

    public ArenaCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArenaCreationException(Throwable cause) {
        super(cause);
    }
}
