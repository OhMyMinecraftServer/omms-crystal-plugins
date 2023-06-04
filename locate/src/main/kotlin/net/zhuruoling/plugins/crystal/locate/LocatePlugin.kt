package net.zhuruoling.plugins.crystal.locate

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.zhuruoling.omms.crystal.command.*
import net.zhuruoling.omms.crystal.config.Config
import net.zhuruoling.omms.crystal.plugin.PluginInitializer
import net.zhuruoling.omms.crystal.plugin.api.CommandApi
import net.zhuruoling.omms.crystal.plugin.api.ServerApi
import net.zhuruoling.omms.crystal.rcon.RconClient
import net.zhuruoling.omms.crystal.util.joinFilePaths
import java.io.File
import kotlin.math.roundToInt

class LocatePlugin : PluginInitializer {
    override fun onInitialize() {
        CommandApi.registerCommand(literal("${Config.commandPrefix}locate").requires { it.from == CommandSource.PLAYER }
            .then(wordArgument("player").executes {
                locatePlayer(getWord(it, "player"), it.source)
                1
            })
            .executes {
                locatePlayer(it.source.player!!, it.source)
                1
            }
        )
    }

    private fun locatePlayer(player: String, commandSourceStack: CommandSourceStack) {
        if (USE_RCON) {
            val infoPos = RconClient.executeCommand("data get entity $player Pos")
            val infoDimension = RconClient.executeCommand("data get entity $player Dimension")
            val posMatcher = regExpPositionRcon.toPattern().matcher(infoPos)
            val dimMatcher = regExpDimensionRcon.toPattern().matcher(infoDimension)
            if (!posMatcher.matches() or !dimMatcher.matches()) {
                commandSourceStack.sendFeedback(text("Player $player not exist", NamedTextColor.RED))
                return
            }
            val dimension: Dimension? = when (dimMatcher.group(3)) {
                "overworld" -> Dimension.OVERWORLD
                "the_nether" -> Dimension.THE_NETHER
                "the_end" -> Dimension.THE_END
                else -> null
            }
            announce(
                PlayerData(
                    player,
                    Vec3d(
                        posMatcher.group(2).toDouble(),
                        posMatcher.group(3).toDouble(),
                        posMatcher.group(4).toDouble()
                    ),
                    dimension,
                    Identifier(
                        dimMatcher.group(2),
                        dimMatcher.group(3)
                    )
                )
            )
        } else {
            Result
            TODO()
        }
    }

    private fun tell(player: String, text: Component) {
        ServerApi.executeCommand("tellraw $player ${GsonComponentSerializer.colorDownsamplingGson().serialize(text)}")
    }

    private fun announce(playerData: PlayerData) {
        val text = text(playerData.player, NamedTextColor.YELLOW)
            .append(text(" @ "))
            .append(playerData.dimension.run {
                this?.description ?: text(
                    playerData.dimensionIdentifier.toString(),
                    NamedTextColor.AQUA
                )
            })
            .append(text(" "))
            .append(
                playerData.pos.toComponent().color(
                    when (playerData.dimensionIdentifier) {
                        Identifier("minecraft", "the_nether") -> NamedTextColor.RED
                        Identifier("minecraft", "overworld") -> NamedTextColor.GREEN
                        else -> NamedTextColor.AQUA
                    }
                )
            )
            .append(text(" "))
            .append {
                text("[+V]", NamedTextColor.AQUA).hoverEvent(
                    HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, text("Show Voxel map waypoint"))
                ).clickEvent(
                    ClickEvent.clickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/newWaypoint x:${playerData.pos.x.roundToInt()} y:${playerData.pos.y.roundToInt()} z:${playerData.pos.z.roundToInt()}, dim:${playerData.dimensionIdentifier}"
                    )
                )
            }
            .append(text(" "))
            .append {
                text("[+X]", NamedTextColor.GOLD).hoverEvent(
                    HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, text("Show Xaero map waypoint"))
                ).clickEvent(
                    ClickEvent.clickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "xaero_waypoint_add:${playerData.player}'s Location:${playerData.player[0]}:${playerData.pos.x.roundToInt()}:${playerData.pos.y.roundToInt()}:${playerData.pos.z.roundToInt()}:6:false:0"
                            .run {
                                this.plus(
                                    when (playerData.dimensionIdentifier) {
                                        Identifier(
                                            "minecraft",
                                            "the_nether"
                                        ) -> ":Internal_${playerData.dimensionIdentifier.value}_waypoints"

                                        Identifier(
                                            "minecraft",
                                            "overworld"
                                        ) -> ":Internal_${playerData.dimensionIdentifier.value}_waypoints"

                                        Identifier(
                                            "minecraft",
                                            "the_end"
                                        ) -> ":Internal_${playerData.dimensionIdentifier.value}_waypoints"

                                        else -> ""
                                    }
                                )
                                this
                            }
                    )
                )
            }
            .append {
                when (playerData.dimensionIdentifier) {
                    Identifier("minecraft", "the_nether") -> text(" -> ").append {
                        playerData.pos.run { Vec3d(x * 8, y * 8, z * 8) }.toComponent().color(NamedTextColor.GREEN)
                    }

                    Identifier("minecraft", "overworld") -> (text(" -> ")).append {
                        playerData.pos.run { Vec3d(x / 8, y / 8, z / 8) }.toComponent().color(NamedTextColor.RED)
                    }

                    else -> text("")
                }
            }
        ServerApi.executeCommand("tellraw @a ${GsonComponentSerializer.colorDownsamplingGson().serialize(text)}")
    }

}