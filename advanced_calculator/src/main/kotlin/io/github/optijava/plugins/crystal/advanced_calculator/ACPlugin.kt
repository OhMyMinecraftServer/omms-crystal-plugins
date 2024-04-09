package io.github.optijava.plugins.crystal.advanced_calculator

import com.mojang.brigadier.context.CommandContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.command.getWord
import icu.takeneko.omms.crystal.command.greedyStringArgument
import icu.takeneko.omms.crystal.command.literal
import icu.takeneko.omms.crystal.i18n.withTranslateContext
import icu.takeneko.omms.crystal.plugin.PluginInitializer
import icu.takeneko.omms.crystal.plugin.api.CommandApi
import icu.takeneko.omms.crystal.util.createLogger
import javax.script.ScriptEngineManager

class ACPlugin : PluginInitializer {
    private var logger = createLogger("advanced_calculator")

    override fun onInitialize() {
        logger.info("Advanced Calculator Plugin is loading...")

        CommandApi.registerCommand(
            literal("==")
                .executes {
                    withTranslateContext("advanced_calculator") {
                        it.source.sendFeedback(
                            Component.text(this.tr("help_msg"))
                        )
                    }
                    0
                }
                .then(
                    greedyStringArgument("expr")
                        .executes { calculate(it, getWord(it, "expr")) }
                )
        )
    }

    private fun calculate(context: CommandContext<CommandSourceStack>, expression: String): Int {
        return try {
            context.source.sendFeedback(
                Component.text(
                    eval(expression).toString(),
                    Style.style(TextColor.color(0x00ff00))  // green color
                )
            )
            0
        } catch (t: Throwable) {
            withTranslateContext("advanced_calculator") {
                context.source.sendFeedback(
                    Component.text(
                        this.tr("err"),
                        Style.style(TextColor.color(0xff0000))  // red color
                    )
                )
            }
            logger.error("Unexpected exception occurred when calculating the expression", t)
            1
        }
    }

    private fun eval(expression: String): Any {
        return ScriptEngineManager().getEngineByExtension("kts").eval(expression)!!
    }
}