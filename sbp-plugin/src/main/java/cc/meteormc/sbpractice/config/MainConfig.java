package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import com.cryptomorin.xseries.XMaterial;

//todo: 添加批注
@ConfigPath(root = true)
public interface MainConfig extends Configuration {
    ConfiguredValue<Boolean> NORMALIZE_TIME = ConfiguredValue.of(true);

    interface ISLAND_GENERATE extends Configuration {
        ConfiguredValue<Integer> DISTANCE = ConfiguredValue.of(80);

        interface PRELOAD extends Configuration {
            ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(false);

            ConfiguredValue<Integer> AMOUNT = ConfiguredValue.of(10);
        }
    }

    ConfiguredValue<Boolean> AUTO_TO_FULL_BLOCK = ConfiguredValue.of(true);

    ConfiguredValue<Integer> MAX_PRESETS_LIMIT = ConfiguredValue.of(18);

    interface MYSQL extends Configuration {
        ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(false);

        ConfiguredValue<String> HOST = ConfiguredValue.of("localhost");

        ConfiguredValue<Integer> PORT = ConfiguredValue.of(3306);

        ConfiguredValue<String> DATABASE = ConfiguredValue.of("sbpractice");

        ConfiguredValue<String> USER = ConfiguredValue.of(String.class);

        ConfiguredValue<String> PASSWORD = ConfiguredValue.of(String.class);

        ConfiguredValue<Boolean> USESSL = ConfiguredValue.of(false);
    }

    interface MATERIAL extends Configuration {
        ConfiguredValue<XMaterial> CLEAR = ConfiguredValue.of(XMaterial.SNOWBALL);

        ConfiguredValue<XMaterial> START = ConfiguredValue.of(XMaterial.EGG);

        ConfiguredValue<XMaterial> GROUND_BLOCK = ConfiguredValue.of(XMaterial.GRASS_BLOCK);

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
    }
}
