package icu.takeneko.plugins.crystal.locate

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.math.roundToInt


data class Vec3d(val x: Double, val y: Double, val z: Double){
    fun toComponent(): Component{
        return text("[${x.roundToInt()}, ${y.roundToInt()}, ${z.roundToInt()}]")
    }
}
data class Identifier(val namespace: String, val value: String) {
    override fun toString(): String {
        return "$namespace:$value"
    }

    companion object {
        fun parse(string: String): Identifier {
            return string.split(":").run { Identifier(this[0], this[1]) }
        }
    }

}

enum class Dimension(val identifier: Identifier, val description:Component) {
    OVERWORLD(Identifier.parse("minecraft:overworld"), text("Overworld", NamedTextColor.GREEN)),
    THE_NETHER(Identifier.parse("minecraft:the_end"), text("Nether", NamedTextColor.RED)),
    THE_END(Identifier.parse("minecraft:the_nether"), text("End", NamedTextColor.LIGHT_PURPLE))
}

data class PlayerData(val player:String, val pos: Vec3d, val dimension: Dimension?, val dimensionIdentifier: Identifier)