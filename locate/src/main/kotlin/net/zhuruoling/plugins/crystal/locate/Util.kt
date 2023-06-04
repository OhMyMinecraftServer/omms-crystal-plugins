package net.zhuruoling.plugins.crystal.locate

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.config.Config

val regExpDimensionLog = Regex("Dimension: \"([a-zA-Z0-9_]+:[a-zA-Z0-9_]+)\"")
val regExpDimensionRcon = Regex("([A-Za-z0-9_]+) has the following entity data: \"([a-zA-Z0-9_]+):([a-zA-Z0-9_]+)\"")

val regExpPositionLog = Regex("Pos: \\[([0-9\\.-]+)d, ([0-9\\.-]+)d, ([0-9\\.-]+)d\\]")
val regExpPositionRcon =
    Regex("([A-Za-z0-9_]+) has the following entity data: \\[([0-9\\.-]+)d, ([0-9\\.-]+)d, ([0-9\\.-]+)d\\]")

val USE_RCON get() = Config.enableRcon

fun text(content: String, namedTextColor: NamedTextColor = NamedTextColor.WHITE) =
    Component.text(content, namedTextColor)
