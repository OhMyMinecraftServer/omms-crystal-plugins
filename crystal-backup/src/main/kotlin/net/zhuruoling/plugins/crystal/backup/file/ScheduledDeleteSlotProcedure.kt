package net.zhuruoling.plugins.crystal.backup.file

import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.omms.crystal.util.joinFilePaths
import net.zhuruoling.plugins.crystal.backup.logResponse
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
        val startTime = System.currentTimeMillis()
        val dir = File(joinFilePaths("backup", srcSlot.storageDir))
        dir.deleteRecursively()
        val endTime = System.currentTimeMillis()
        commandSourceStack.logResponse(Text("Done! (${(endTime - startTime) / 1000}s)"))
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }
}