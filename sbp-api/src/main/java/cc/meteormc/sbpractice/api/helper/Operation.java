package cc.meteormc.sbpractice.api.helper;

import cc.meteormc.sbpractice.api.Island;

//todo: 实现异步执行器
@FunctionalInterface
public interface Operation {
    boolean execute(Island island) throws Throwable;
}
