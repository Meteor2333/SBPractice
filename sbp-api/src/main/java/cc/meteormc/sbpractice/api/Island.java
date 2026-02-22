package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.Operation;
import cc.meteormc.sbpractice.api.storage.BlockData;
import cc.meteormc.sbpractice.api.storage.PresetData;
import cc.meteormc.sbpractice.api.storage.SignData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public interface Island {
    Player getOwner();

    List<Player> getGuests();

    List<Player> getAllPlayers();

    Zone getZone();

    Area getArea();

    Area getBuildArea();

    Location getSpawnPoint();

    boolean isActive();

    void setActive(boolean active);

    BuildMode getMode();

    void setMode(BuildMode mode);

    BukkitTask getModeTask();

    void setModeTask(BukkitTask task);

    SignData getSigns();

    Map<Location, BlockData> getRecordedBlocks();

    boolean isCanStart();

    void setCanStart(boolean canStart);

    double getTime();

    String getFormattedTime();

    boolean isStarted();

    void startTimer();

    void stopTimer();

    List<Player> getNearbyPlayers();

    void resetPlayer(Player player);

    void addGuest(Player player);

    void removeGuest(Player player);

    @ApiStatus.Internal
    void removeAny(Player player, boolean createNew);

    void refreshSigns();

    void refreshCountdown();

    boolean executeOperation(Operation... operations);

    void applyPreset(PresetData preset);

    void remove();

    enum BuildMode {
        DEFAULT, ONCE, CONTINUOUS
    }
}
