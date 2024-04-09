package icu.takeneko.plugins.crystal.backup.file

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.text.Color
import icu.takeneko.omms.crystal.text.Text
import icu.takeneko.omms.crystal.util.joinFilePaths
import icu.takeneko.plugins.crystal.backup.command.text
import icu.takeneko.plugins.crystal.backup.logResponse
import java.io.File

class ScheduledDeleteSlotProcedure(
    scheduledTimeMillis: Long,
    private val srcSlot: Slot,
    private val commandSourceStack: CommandSourceStack
) : Procedure(scheduledTimeMillis, "DeleteSlotProcedure") {
    override fun run() {
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("There is a running backup/restore job.").withColor(Color.red))
            return
        }
        SlotManager.hasActiveJob = true
        commandSourceStack.logResponse(Text("Deleting slot ${srcSlot.id}."))
        try {
            val startTime = System.currentTimeMillis()
            val dir = File(joinFilePaths("backup", srcSlot.storageDir))
            if (dir.exists())
                dir.deleteRecursively()
            SlotManager.slots.remove(srcSlot.id)
            val endTime = System.currentTimeMillis()
            commandSourceStack.logResponse(Text("Done! (${(endTime - startTime) / 1000}s)"))
        } catch (e: Exception) {
            commandSourceStack.sendFeedback(
                text(
                    "Cannot delete slot because an error occurred, ${e.javaClass.name}: ${e.message ?: e.localizedMessage}",
                    NamedTextColor.RED
                ).hoverEvent(HoverEvent.showText(Component.text(e.stackTraceToString())))
            )
            e.printStackTrace()
        }
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }
}