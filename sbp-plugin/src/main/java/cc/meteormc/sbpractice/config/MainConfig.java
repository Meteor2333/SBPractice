package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import com.cryptomorin.xseries.XMaterial;

@ConfigPath(root = true)
public interface MainConfig extends Configuration {
    @HeaderComments("Change the timer step from 0.001s to 0.05s, because in Minecraft 1 tick = 1/20 second.")
    ConfiguredValue<Boolean> NORMALIZE_TIME = ConfiguredValue.of(true);

    interface ISLAND_GENERATE extends Configuration {
        @HeaderComments("Set the interval for island generation.")
        ConfiguredValue<Integer> DISTANCE = ConfiguredValue.of(80);

        interface PRE_GENERATE extends Configuration {
            @HeaderComments({
                    "Pre-generate the specified number of islands at startup, and keep them even after players leave.",
                    "Enable this option if you want to create a more community-friendly server."
            })
            ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(false);

            @HeaderComments("Set the number of islands to pre-generate")
            ConfiguredValue<Integer> AMOUNT = ConfiguredValue.of(10);
        }
    }

    @HeaderComments("Replace partial blocks with similar full blocks when performing certain placement actions. (BETA)")
    ConfiguredValue<Boolean> AUTO_TO_FULL_BLOCK = ConfiguredValue.of(true);

    @HeaderComments("Set the maximum number of presets a player can save.")
    ConfiguredValue<Integer> MAX_PRESETS_LIMIT = ConfiguredValue.of(18);

    @HeaderComments("Set MySQL database service.")
    interface MYSQL extends Configuration {
        ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(false);

        ConfiguredValue<String> HOST = ConfiguredValue.of("localhost");

        ConfiguredValue<Integer> PORT = ConfiguredValue.of(3306);

        ConfiguredValue<String> DATABASE = ConfiguredValue.of("sbpractice");

        ConfiguredValue<String> USER = ConfiguredValue.of(String.class);

        ConfiguredValue<String> PASSWORD = ConfiguredValue.of(String.class);

        ConfiguredValue<Boolean> USESSL = ConfiguredValue.of(false);
    }

    @HeaderComments("Set in-game material.")
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
