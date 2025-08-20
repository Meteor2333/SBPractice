package cc.meteormc.sbpractice.api.arena;

public enum BuildMode {
    DEFAULT,
    ONCE,
    CONTINUOUS;

    public BuildMode next() {
        int index = this.ordinal() + 1;
        if (index >= BuildMode.values().length) index = 0;
        return BuildMode.values()[index];
    }
}
