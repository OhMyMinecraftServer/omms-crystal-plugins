package net.zhuruoling.plugins.crystal.backup.file

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.omms.crystal.util.joinFilePaths
import net.zhuruoling.plugins.crystal.backup.command.text
import net.zhuruoling.plugins.crystal.backup.logResponse
import java.io.File

class ClearSlotProcedure(
    scheduledTimeMillis: Long,
    private val srcSlot: Slot,
    private val commandSourceStack: CommandSourceStack,
    private val force: Boolean
) : Procedure(scheduledTimeMillis, "ClearSlotProcedure") {
    override fun run() {
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("There is a running backup/restore job.").withColor(Color.red))
            return
        }
        SlotManager.hasActiveJob = true
        commandSourceStack.logResponse(Text("Clearing slot ${srcSlot.id}."))
        try {
            val startTime = System.currentTimeMillis()
            if (srcSlot.isEmpty and !force) {
                commandSourceStack.sendFeedback(text("Slot ${srcSlot.id} is empty.", NamedTextColor.YELLOW))
                SlotManager.hasActiveJob = false
                SlotManager.scheduledProcedure = null
                return
            }
            srcSlot.worldDir.forEach {
                val dir = File(joinFilePaths("backup", srcSlot.storageDir, it))
                if (dir.exists())
                    dir.deleteRecursively()
            }
            srcSlot.isEmpty = true
            SlotManager.writeSlotConfig(srcSlot)
            val endTime = System.currentTimeMillis()
            commandSourceStack.logResponse(Text("Done! (${(endTime - startTime) / 1000}s)"))
        } catch (e: Exception) {
            commandSourceStack.sendFeedback(
                text(
                    "Cannot clear slot because an error occurred, ${e.javaClass.name}: ${e.message ?: e.localizedMessage}",
                    NamedTextColor.RED
                ).hoverEvent(HoverEvent.showText(Component.text(e.stackTraceToString())))
            )
            e.printStackTrace()
        }
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }
}