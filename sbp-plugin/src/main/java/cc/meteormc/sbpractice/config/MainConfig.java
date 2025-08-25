package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import com.cryptomorin.xseries.XMaterial;

@ConfigPath(root = true)
public interface MainConfig extends Configuration {
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

        ConfiguredList<XMaterial> BLOCKED_ITEMS = ConfiguredList.of(
                XMaterial.ARMOR_STAND,
                XMaterial.MINECART,
                XMaterial.ITEM_FRAME,
                XMaterial.OAK_BOAT,
                XMaterial.BONE_MEAL,
                XMaterial.POTION,
                XMaterial.MILK_BUCKET,
                XMaterial.PAINTING,
                XMaterial.MAP
        );
    }

    ConfiguredValue<Integer> ISLAND_DISTANCE = ConfiguredValue.of(80);

    ConfiguredValue<Integer> MAX_PRESETS_LIMIT = ConfiguredValue.of(18);

    ConfiguredValue<Boolean> AUTO_TO_FULL_BLOCK = ConfiguredValue.of(true);
}
