package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.api.storage.sign.SignGroup;
import cc.meteormc.sbpractice.api.util.Region;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public interface Island {
    Player getOwner();

    List<Player> getGuests();

    Arena getArena();

    Region getArea();

    Region getBuildArea();

    Location getSpawnPoint();

    boolean isStartCountdown();

    void setStartCountdown(boolean start);

    BuildMode getMode();

    void setMode(BuildMode mode);

    SignGroup getSigns();

    Map<Location, BlockState> getRecordedBlocks();

    void setCanStart(boolean canStart);

    boolean isStarted();

    double getTime();

    String getFormattedTime();

    void startTimer();

    void stopTimer();

    void addGuest(Player player);

    void removeGuest(Player player);

    void refreshSigns();

    void ground();

    void record();

    void clear();

    void preview();

    void start();

    void applyPreset(PresetData preset);

    void remove();
}
