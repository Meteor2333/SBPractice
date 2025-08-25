package cc.meteormc.sbpractice.arena.setup;

import cc.meteormc.sbpractice.arena.session.SetupSession;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum SetupType {
    MAP_AREA(XMaterial.END_PORTAL_FRAME, ChatColor.AQUA + "" + ChatColor.BOLD + "Set Map Area") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    ChatColor.GRAY + "Left-Click: Set the map area pos1, Right-Click: Set the map area pos2",
                    "",
                    SetupType.buildLocationInfo("Pos1", session.getMapAreaPos1()),
                    SetupType.buildLocationInfo("Pos2", session.getMapAreaPos2())
            );
        }
    },
    MAP_BUILD_AREA(XMaterial.GRASS_BLOCK, ChatColor.AQUA + "" + ChatColor.BOLD + "Set Map Build Area") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    ChatColor.GRAY + "Click to set the map build area.",
                    "",
                    SetupType.buildLocationInfo("Pos1", session.getMapBuildAreaPos1()),
                    SetupType.buildLocationInfo("Pos2", session.getMapBuildAreaPos2())
            );
        }
    },
    MAP_SPAWN(XMaterial.ENDER_PEARL, ChatColor.AQUA + "" + ChatColor.BOLD + "Set Map Spawn") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    ChatColor.GRAY + "Click to set the map spawn point.",
                    "",
                    SetupType.buildLocationInfo("Point", session.getMapSpawnPoint())
            );
        }
    },
    MAP_SIGN(XMaterial.OAK_SIGN, ChatColor.AQUA + "" + ChatColor.BOLD + "Set Map Sign") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    ChatColor.GRAY + "Click to set the map sign."
            );
        }
    };

    private final XMaterial icon;
    private final String hint;

    public abstract List<String> getState(SetupSession session);

    private static String buildLocationInfo(String name, Location location) {
        StringBuilder builder = new StringBuilder(ChatColor.GRAY + name + ": ");
        if (location != null) {
            builder.append(ChatColor.YELLOW).append(String.format("%.2f", location.getX()))
                    .append(ChatColor.DARK_GRAY).append(", ")
                    .append(ChatColor.YELLOW).append(String.format("%.2f", location.getY()))
                    .append(ChatColor.DARK_GRAY).append(", ")
                    .append(ChatColor.YELLOW).append(String.format("%.2f", location.getZ()))
                    .append(" ").append(ChatColor.GREEN).append("✔");
        } else {
            builder.append(ChatColor.RED).append("Not Set ✘");
        }
        return builder.toString();
    }

    @Getter
    @RequiredArgsConstructor
    public enum SetupSignType {
        GROUND(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Ground Sign", "groundSign"),
        RECORD(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Record Sign", "recordSign"),
        CLEAR(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Clear Sign", "clearSign"),
        SELECT_ARENA(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Select Arena Sign", "selectArenaSign"),
        MODE(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Mode Sign", "modeSign"),
        PRESET(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Preset Sign", "presetSign"),
        START(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Start Sign", "startSign"),
        PREVIEW(ChatColor.AQUA + "" + ChatColor.BOLD + "Set Preview Sign", "previewSign");

        private final String hint, field;
    }
}
