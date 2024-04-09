package icu.takeneko.plugins.crystal.op

import com.mojang.brigadier.context.CommandContext
import icu.takeneko.omms.crystal.command.CommandSource
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.command.literal
import icu.takeneko.omms.crystal.config.Config.config
import icu.takeneko.omms.crystal.i18n.withTranslateContext
import icu.takeneko.omms.crystal.plugin.PluginInitializer
import icu.takeneko.omms.crystal.plugin.api.CommandApi
import icu.takeneko.omms.crystal.plugin.api.ServerApi
import icu.takeneko.omms.crystal.plugin.api.annotations.InjectArgument
import icu.takeneko.omms.crystal.text.Color
import icu.takeneko.omms.crystal.text.Text
import java.nio.file.Path

class SimpleOpPlugin : PluginInitializer {

    @InjectArgument(name = "pluginConfig")
    private lateinit var configPath: Path

    private var opNoBroadcast = false

    override fun onInitialize() {
        println(configPath)
        if (!configPath.toFile().exists()) {
            configPath.toFile().mkdirs()
        }
        opNoBroadcast = configPath.resolve("opNoBroadcast").toFile().exists()
        CommandApi.registerCommand(literal(config.commandPrefix + "op")
            .requires { commandSourceStack: CommandSourceStack -> commandSourceStack.from == CommandSource.PLAYER }
            .executes { commandContext: CommandContext<CommandSourceStack> ->
                val player = commandContext.source.player
                if (player.isNullOrEmpty()) {
                    return@executes 0
                }
                ServerApi.executeCommand("op %s".formatted(player))
                if (opNoBroadcast) return@executes 0
                withTranslateContext("simple_op") {
                    ServerApi.tell("@a", Text(tr("set_op")).withColor(Color.aqua))
                }
                1
            }
        )
        CommandApi.registerCommand(literal(config.commandPrefix + "deop")
            .requires { commandSourceStack: CommandSourceStack -> commandSourceStack.from == CommandSource.PLAYER }
            .executes { commandContext: CommandContext<CommandSourceStack> ->
                val player = commandContext.source.player
                if (player.isNullOrEmpty()) {
                    return@executes 0
                }
                ServerApi.executeCommand("deop %s".formatted(player))
                if (opNoBroadcast) return@executes 0
                withTranslateContext("simple_op"){
                    ServerApi.tell("@a", Text(tr("unset_op")).withColor(Color.aqua))
                }
                1
            }
        )
    }
}
