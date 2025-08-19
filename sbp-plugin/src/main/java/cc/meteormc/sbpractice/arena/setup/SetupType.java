package cc.meteormc.sbpractice.arena.setup;

import cc.meteormc.sbpractice.arena.session.SetupSession;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum SetupType {
    MAP_AREA(XMaterial.END_PORTAL_FRAME, "&b&lSet Map Area") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    "&7Left-Click: Set the map area pos1, Right-Click: Set the map area pos2",
                    "",
                    SetupType.buildLocationInfo("Pos1", session.getMapAreaPos1()),
                    SetupType.buildLocationInfo("Pos2", session.getMapAreaPos2())
            );
        }
    },
    MAP_BUILD_AREA(XMaterial.GRASS_BLOCK, "&b&lSet Map Build Area") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    "&7Click to set the map build area.",
                    "",
                    SetupType.buildLocationInfo("Pos1", session.getMapBuildAreaPos1()),
                    SetupType.buildLocationInfo("Pos2", session.getMapBuildAreaPos2())
            );
        }
    },
    MAP_SPAWN(XMaterial.ENDER_PEARL, "&b&lSet Map Spawn") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList(
                    "&7Click to set the map spawn point.",
                    "",
                    SetupType.buildLocationInfo("Point", session.getMapSpawnPoint())
            );
        }
    },
    MAP_SIGN(XMaterial.OAK_SIGN, "&b&lSet Map Sign") {
        @Override
        public List<String> getState(SetupSession session) {
            return Arrays.asList("&7Click to set the map sign.");
        }
    };

    private final XMaterial icon;
    private final String hint;

    public abstract List<String> getState(SetupSession session);

    private static String buildLocationInfo(String name, Location location) {
        StringBuilder builder = new StringBuilder("&7" + name + ": ");
        if (location != null) {
            builder.append("&e")
                    .append(String.format("%.2f", location.getX())).append("&8, &e")
                    .append(String.format("%.2f", location.getY())).append("&8, &e")
                    .append(String.format("%.2f", location.getZ())).append(" &a✔");
        } else {
            builder.append("&cNot Set ✘");
        }
        return builder.toString();
    }

    @Getter
    @RequiredArgsConstructor
    public enum SetupSignType {
        GROUND("&b&lSet Ground Sign", "groundSign"),
        RECORD("&b&lSet Record Sign", "recordSign"),
        CLEAR("&b&lSet Clear Sign", "clearSign"),
        SELECT_ARENA("&b&lSet Select Arena Sign", "selectArenaSign"),
        MODE("&b&lSet Mode Sign", "modeSign"),
        PRESET("&b&lSet Preset Sign", "presetSign"),
        START("&b&lSet Start Sign", "startSign"),
        PREVIEW("&b&lSet Preview Sign", "previewSign");

        private final String hint, field;
    }
}
