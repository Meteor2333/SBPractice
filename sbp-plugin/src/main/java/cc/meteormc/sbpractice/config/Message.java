package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredTitle;
import cc.meteormc.sbpractice.config.adapter.XMaterialAdapter;

@ConfigPath(root = true)
public interface Message extends Configuration {
    static void initialize(ConfigurationHolder<?> holder) {
        holder.adapters().register(new XMaterialAdapter());
    }

    static ConfiguredMessage.Builder<String> asText(String... defaults) {
        return ConfiguredMessage.asString()
                .dispatcher((sender, message) -> sender.sendMessage(
                        message.stream()
                                .map(msg -> PREFIX.parseLine(sender) + msg)
                                .toArray(String[]::new)
                ))
                .defaults(defaults);
    }

    static ConfiguredMessage<String> ofText(String... defaults) {
        return asText(defaults).build();
    }

    static ConfiguredTitle.Builder asTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return ConfiguredTitle.create()
                .defaults(title, subtitle)
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut);
    }

    static ConfiguredTitle ofTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return asTitle(title, subtitle, fadeIn, stay, fadeOut).build();
    }

    ConfiguredMessage<String> PREFIX = ConfiguredMessage.ofString("&8[&bSBPractice&8] ");

    interface BASIC extends Configuration {
        ConfiguredMessage<String> TIME_FORMAT = asText("&b&l%(time)").params("time").build();

        ConfiguredMessage<String> PLAYER_NOT_FOUND = ofText("&cPlayer not found!");

        ConfiguredMessage<String> CANNOT_DO_THAT = ofText("&cYou cannot do that!");

        ConfiguredMessage<String> PRESET_FULL = ofText("&cPreset slots are full!");

        ConfiguredMessage<String> INVENTORY_FULL = ofText("&cInventory is full. Some items may not have been added!");
    }

    interface ITEM extends Configuration {
        ConfiguredMessage<String> START = ofText("&d&lUse &e&lEgg &d&lto start");

        ConfiguredMessage<String> CLEAR = ofText("&9&lUse &f&lSnowball &9&lto clear");
    }

    interface TITLE extends Configuration {
        ConfiguredTitle START = ofTitle("", "&e&lSTART", 0, 10, 10);

        ConfiguredTitle COUNTDOWN = asTitle("", "&6&l%(countdown)", 0, 20, 0).params("countdown").build();

        ConfiguredTitle PERFECT_MATCH = asTitle("&a&lGreat Job", "&6&l%(time)", 5, 30, 5).params("time").build();
    }

    interface COMMAND extends Configuration {
        ConfiguredMessage<String> USAGE = asText("&cUsage: %(usage)").params("usage").build();

        ConfiguredMessage<String> NO_PERMISSION = ofText("&cYou do not have permission to use this command!");

        interface ADMIN extends Configuration {
            ConfiguredMessage<String> ENABLE = ofText("&aAdmin mode enabled!");

            ConfiguredMessage<String> DISABLE = ofText("&cAdmin mode disabled!");
        }

        interface HELP extends Configuration {
            ConfiguredMessage<String> MAIN = ofText(
                    "&2▪ &7/&esbp clear &8- &aClear current",
                    "&2▪ &7/&esbp ground &8- &aSync ground",
                    "&2▪ &7/&esbp highjump &8- &aToggle highjump",
                    "&2▪ &7/&esbp preview &8- &aPreview recorded",
                    "&2▪ &7/&esbp record &8- &aRecord current",
                    "&2▪ &7/&esbp start &8- &aStart countdown"
            );

            ConfiguredMessage<String> MULTIPLAYER = ofText(
                    "&2▪ &7/&emp accept <player> &8- &aAccept an invitation from a player",
                    "&2▪ &7/&emp deny <player> &8- &aDeny an invitation from a player",
                    "&2▪ &7/&emp invite <player> &8- &aInvite another player to your island",
                    "&2▪ &7/&emp join <player> &8- &aJoin another player's island",
                    "&2▪ &7/&emp kick <player> &8- &aRemove a player from your island",
                    "&2▪ &7/&emp leave &8- &aLeave your current island"
            );
        }
    }

    interface OPERATION extends Configuration {
        ConfiguredMessage<String> PRESET = asText("&aPreset applied: %(preset)").params("preset").build();

        interface CONTINUOUS extends Configuration {
            ConfiguredMessage<String> ACTIVE = ofText("&aTimer has been active!");

            ConfiguredMessage<String> INACTIVE = ofText("&cTimer has been inactive!");
        }
    }

    interface GUI extends Configuration {
        ConfiguredMessage<String> PREVIOUS_PAGE = ofText("&ePrevious Page");

        ConfiguredMessage<String> NEXT_PAGE = ofText("&eNext Page");

        ConfiguredMessage<String> CLOSE = ofText("&cClose");

        interface PRESET extends Configuration {
            ConfiguredMessage<String> TITLE = ofText("Presets");

            ConfiguredMessage<String> DESCRIPTION = asText(
                    "&7Blocks: %(blocks)",
                    "",
                    "&eClick to apply!",
                    "&bRight-Click to remove this preset!"
            ).params("blocks").build();

            ConfiguredMessage<String> DESCRIPTION_NO_PERMISSION = asText(
                    "&7Blocks: %(blocks)",
                    "",
                    "&eClick to apply!"
            ).params("blocks").build();

            interface FILTER extends Configuration {
                interface ITEM extends Configuration {
                    ConfiguredMessage<String> NAME = ofText("&aSearch");

                    ConfiguredMessage<String> LORE = asText(
                            "&7Find preset by name.",
                            "",
                            "&7Filtered: &e%(filtered)",
                            "",
                            "&eClick to edit filter!",
                            "&bRight-Click to clear!"
                    ).params("filtered").build();
                }

                ConfiguredMessage<String> QUERY = ofText("Enter query");
            }

            interface SAVE extends Configuration {
                interface ITEM extends Configuration {
                    ConfiguredMessage<String> NAME = ofText("&aSave preset");

                    ConfiguredMessage<String> LORE = ofText(
                            "&eClick to save!"
                    );
                }

                interface ITEM_GLOBAL extends Configuration {
                    ConfiguredMessage<String> NAME = ofText("&aSave preset globally");

                    ConfiguredMessage<String> LORE = ofText(
                            "&eClick to save!"
                    );
                }

                ConfiguredMessage<String> SET_NAME = ofText("Enter name");

                ConfiguredMessage<String> SET_ICON = ofText("&aPlease drop the icon for this preset!");

                ConfiguredMessage<String> SUCCESS = ofText("&aPreset saved successfully!");

                ConfiguredMessage<String> FAILED = ofText("&cFailed to save preset! Please check the console for more!");
            }
        }

        interface SETTINGS extends Configuration {
            ConfiguredMessage<String> TITLE = ofText("Settings");

            interface HIGHJUMP extends Configuration {
                interface ITEM extends Configuration {
                    ConfiguredMessage<String> NAME = ofText("&aHighjump state");

                    ConfiguredMessage<String> LORE = asText(
                            "&7Toggle your highjump state.",
                            "",
                            "&7Current state: %(state)",
                            "",
                            "&eLeft-Click to increase!",
                            "&eRight-Click to decrease!"
                    ).params("state").build();
                }

                interface STATE extends Configuration {
                    ConfiguredMessage<String> ENABLED = asText("&aEnabled &7(&e%(height) blocks&7)").params("height").build();

                    ConfiguredMessage<String> DISABLED = ofText("&cDisabled");
                }
            }
        }
    }

    interface SIGN extends Configuration {
        ConfiguredMessage<String> CLEAR = ofText(
                "",
                "Clear",
                "",
                ""
        );

        ConfiguredMessage<String> GROUND = ofText(
                "",
                "Ground",
                "",
                ""
        );

        ConfiguredMessage<String> MODE = asText(
                "",
                "Mode",
                "%(mode)",
                ""
        ).params("mode").build();

        ConfiguredMessage<String> PRESET = ofText(
                "",
                "Preset",
                "",
                ""
        );

        ConfiguredMessage<String> PREVIEW = ofText(
                "",
                "Preview",
                "",
                ""
        );

        ConfiguredMessage<String> RECORD = ofText(
                "",
                "Record",
                "",
                ""
        );

        ConfiguredMessage<String> START = ofText(
                "",
                "Start",
                "",
                ""
        );

        ConfiguredMessage<String> TOGGLE_ZONE = ofText(
                "",
                "Toggle Zone",
                "",
                ""
        );

        interface OPTIONS extends Configuration {
            interface MODE extends Configuration {
                ConfiguredMessage<String> DEFAULT = ofText("Default");

                ConfiguredMessage<String> ONCE = ofText("Once");

                ConfiguredMessage<String> CONTINUOUS = ofText("Continuous");
            }
        }
    }

    interface MULTIPLAYER extends Configuration {
        ConfiguredMessage<String> ACCEPT = ofText("&a&l[Accept]");

        ConfiguredMessage<String> DENY = ofText("&c&l[Deny]");

        ConfiguredMessage<String> ALREADY_DENIED = asText("&c%(player) already denied you! Please try again later.").params("player").build();

        interface INVITE extends Configuration {
            ConfiguredMessage<String> ACTIVE = asText("&aYour invite has been sent to %(player)!").params("player").build();

            ConfiguredMessage<String> PASSIVE = asText("&d%(player) invited you to their island!").params("player").build();

            ConfiguredMessage<String> ALREADY_INVITED = asText("&cYou already invited %(player)!").params("player").build();

            ConfiguredMessage<String> ALREADY_ON_ISLAND = asText("&c%(player) is already on your island!").params("player").build();

            ConfiguredMessage<String> ACCEPTED_ACTIVE = asText("&aYou have accepted %(player)'s invite.").params("player").build();

            ConfiguredMessage<String> ACCEPTED_PASSIVE = asText("&a%(player) has accepted your invite!").params("player").build();

            ConfiguredMessage<String> DENIED_ACTIVE = asText("&cYou have denied %(player)'s invite.").params("player").build();

            ConfiguredMessage<String> DENIED_PASSIVE = asText("&c%(player) has denied your invite!").params("player").build();
        }

        interface JOIN extends Configuration {
            ConfiguredMessage<String> ACTIVE = asText("&aYour request has been sent to %(player)!").params("player").build();

            ConfiguredMessage<String> PASSIVE = asText("&d%(player) requested to join your island!").params("player").build();

            ConfiguredMessage<String> ALREADY_JOINED = asText("&cYou already requested %(player)!").params("player").build();

            ConfiguredMessage<String> ALREADY_ON_ISLAND = asText("&cYou are already on %(player)'s island!").params("player").build();

            ConfiguredMessage<String> ACCEPTED_ACTIVE = asText("&aYou have accepted %(player)'s request.").params("player").build();

            ConfiguredMessage<String> ACCEPTED_PASSIVE = asText("&a%(player) has accepted your request!").params("player").build();

            ConfiguredMessage<String> DENIED_ACTIVE = asText("&cYou have denied %(player)'s request.").params("player").build();

            ConfiguredMessage<String> DENIED_PASSIVE = asText("&c%(player) has denied your request!").params("player").build();
        }

        interface KICK extends Configuration {
            ConfiguredMessage<String> ACTIVE = asText("&a%(player) has been kicked from your island").params("player").build();

            ConfiguredMessage<String> PASSIVE = asText("&c%(player) kicked you from their island!").params("player").build();
        }

        interface LEAVE extends Configuration {
            ConfiguredMessage<String> ACTIVE = asText("&aYou left %(player)'s island").params("player").build();

            ConfiguredMessage<String> PASSIVE = asText("&c%(player) left your island!").params("player").build();
        }
    }
}
