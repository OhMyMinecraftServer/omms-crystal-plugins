package icu.takeneko.plugins.crystal.backup.file

import net.kyori.adventure.text.format.NamedTextColor
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.text.Text
import icu.takeneko.plugins.crystal.backup.command.text
import icu.takeneko.plugins.crystal.backup.logResponse

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