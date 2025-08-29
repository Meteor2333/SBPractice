package cc.meteormc.sbpractice.api.helper;

import cc.meteormc.sbpractice.api.Island;

import java.util.concurrent.CompletableFuture;

public interface Operation {
    boolean execute(Island island) throws Throwable;

    default CompletableFuture<Boolean> executeAsync(Island island) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.execute(island);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
