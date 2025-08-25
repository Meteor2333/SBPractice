package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.api.util.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;

public interface Island {
    Player getOwner();

    List<Player> getGuests();

    Arena getArena();

    Region getArea();

    Region getBuildArea();

    Location getSpawnPoint();

    boolean isActive();

    void setActive(boolean active);

    BuildMode getMode();

    void setMode(BuildMode mode);

    BukkitTask getModeTask();

    void setModeTask(BukkitTask task);

    SignData getSigns();

    Map<Location, BlockData> getRecordedBlocks();

    void setCanStart(boolean canStart);

    boolean isStarted();

    double getTime();

    String getFormattedTime();

    void startTimer();

    void stopTimer();

    List<Player> getNearbyPlayers();

    void resetPlayer(Player player);

    void addGuest(Player player);

    void removeGuest(Player player);

    void refreshSigns();

    boolean executeOperation(Operation... operations);

    void applyPreset(PresetData preset);

    void remove();
}
