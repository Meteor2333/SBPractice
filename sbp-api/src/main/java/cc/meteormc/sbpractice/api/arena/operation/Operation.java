package cc.meteormc.sbpractice.api.arena.operation;

import cc.meteormc.sbpractice.api.Island;

//todo: 未来加异步执行器 这个类是为了统一它们
@FunctionalInterface
public interface Operation {
    boolean execute(Island island) throws Throwable;
}
