package net.zhuruoling.plugins.crystal.backup.file

import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.plugins.crystal.backup.command.text
import net.zhuruoling.plugins.crystal.backup.logResponse

class CreateSlotProcedure(val id: String, val commandSourceStack: CommandSourceStack) :
    Procedure(0, "CreateSlotProcedure") {
    override fun run() {
        if (SlotManager.hasActiveJob) {
            commandSourceStack.sendFeedback(text("There is a running backup/restore job.", NamedTextColor.RED))
            return
        }
        SlotManager.hasActiveJob = true
        val startTime = System.currentTimeMillis()
        SlotManager.createSlot(id, commandSourceStack)
        val endTime = System.currentTimeMillis()
        commandSourceStack.logResponse(Text("Done! (${(endTime - startTime) / 1000.0}s)"))
        SlotManager.hasActiveJob = false
    }
}