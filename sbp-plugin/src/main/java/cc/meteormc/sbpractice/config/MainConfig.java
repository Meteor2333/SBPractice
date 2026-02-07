package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import com.cryptomorin.xseries.XMaterial;

@ConfigPath(root = true)
public interface MainConfig extends Configuration {
    @HeaderComments("Enable automatic update checking to get notified of new versions.")
    ConfiguredValue<Boolean> CHECK_UPDATE = ConfiguredValue.of(true);

    @HeaderComments("Normalize timer step from 0.001s to 0.05s (1 Minecraft tick = 1/20 second).")
    ConfiguredValue<Boolean> NORMALIZE_TIME = ConfiguredValue.of(true);

    @HeaderComments("Island generation settings.")
    interface ISLAND_GENERATE extends Configuration {
        @HeaderComments("Set the distance interval between generated islands.")
        ConfiguredValue<Integer> DISTANCE = ConfiguredValue.of(50);

        @HeaderComments({
                "Set the rectangular width for island generation.",
                "Note: The default width is 1, which creates a linear layout. Increase this value for a rectangular grid."
        })
        ConfiguredValue<Integer> WIDTH = ConfiguredValue.of(1);

        @HeaderComments("Set the maximum number of islands per zone. (Set to -1 for unlimited)")
        ConfiguredValue<Integer> AMOUNT = ConfiguredValue.of(-1);

        @HeaderComments({
                "Enable pre-generation of islands at server startup.",
                "Recommended for community servers to improve initial player experience."
        })
        ConfiguredValue<Boolean> PRE_GENERATE = ConfiguredValue.of(false);
    }

    @HeaderComments("Automatically replace partial blocks with similar full blocks during placement. (BETA)")
    ConfiguredValue<Boolean> AUTO_TO_FULL_BLOCK = ConfiguredValue.of(true);

    @HeaderComments("Hide players when they leave their island.")
    ConfiguredValue<Boolean> HIDE_PLAYER = ConfiguredValue.of(true);

    @HeaderComments("Set the maximum number of presets a player can save.")
    ConfiguredValue<Integer> MAX_PRESETS_LIMIT = ConfiguredValue.of(18);

    @HeaderComments("MySQL database configuration.")
    interface MYSQL extends Configuration {
        ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(false);

        ConfiguredValue<String> HOST = ConfiguredValue.of("localhost");

        ConfiguredValue<Integer> PORT = ConfiguredValue.of(3306);

        ConfiguredValue<String> DATABASE = ConfiguredValue.of("sbpractice");

        ConfiguredValue<String> USER = ConfiguredValue.of(String.class);

        ConfiguredValue<String> PASSWORD = ConfiguredValue.of(String.class);

        ConfiguredValue<Boolean> USESSL = ConfiguredValue.of(false);
    }

    @HeaderComments("In-game material configuration.")
    interface MATERIAL extends Configuration {
        ConfiguredValue<XMaterial> START_ITEM = ConfiguredValue.of(XMaterial.EGG);

        ConfiguredValue<XMaterial> CLEAR_ITEM = ConfiguredValue.of(XMaterial.SNOWBALL);

        ConfiguredList<String> BLOCKED_ITEMS = ConfiguredList.of(
                "ARMOR_STAND",
                "MINECART",
                "ITEM_FRAME",
                "BOAT",
                "BONE_MEAL",
                "POTION",
                "MILK_BUCKET",
                "PAINTING",
                "MAP"
        );

        ConfiguredValue<XMaterial> GROUND_BLOCK = ConfiguredValue.of(XMaterial.GRASS_BLOCK);
    }
}
