package net.zhuruoling.plugins.crystal.backup.file

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.config.Config
import net.zhuruoling.omms.crystal.plugin.api.ServerApi
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.omms.crystal.util.joinFilePaths
import net.zhuruoling.plugins.crystal.backup.command.text
import net.zhuruoling.plugins.crystal.backup.copyWorld
import net.zhuruoling.plugins.crystal.backup.event.ServerStatus
import net.zhuruoling.plugins.crystal.backup.logResponse
import java.io.File

class ScheduledBackupProcedure(
    scheduledTimeMillis: Long,
    private val destinationSlot: Slot,
    private val commandSourceStack: CommandSourceStack,
    private val comment: String?
) :
    Procedure(scheduledTimeMillis, "BackupProcedure") {
    override fun run() {
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("There is a running backup/restore job.").withColor(Color.red))
            return
        }
        SlotManager.hasActiveJob = true

        val startTime = System.currentTimeMillis()
        if (ServerStatus.serverState == ServerStatus.State.RUNNING) {
            ServerStatus.autoSaveState = ServerStatus.State.SAVE_ON
            ServerApi.executeCommand(SlotManager.config.serverCommand.saveOff)
            while (true) {
                sleep(50)
                if (ServerStatus.autoSaveState == ServerStatus.State.SAVE_OFF) break
            }
            ServerStatus.saveState = ServerStatus.State.WORLD_SAVING
            ServerApi.executeCommand(SlotManager.config.serverCommand.saveAll)
            while (true) {
                sleep(50)
                if (ServerStatus.saveState == ServerStatus.State.WORLD_SAVED) break
            }
        }
        try {
            commandSourceStack.logResponse(Text("Clearing backup world.").withColor(Color.green))
            destinationSlot.worldDir.forEach {
                val f = File(joinFilePaths("backup", destinationSlot.storageDir, it))
                if (f.exists()){
                    f.deleteRecursively()
                }
            }
            commandSourceStack.logResponse(Text("Copying world.").withColor(Color.green))
            SlotManager.config.worldDir.forEach {
                File(joinFilePaths("backup", destinationSlot.storageDir, it)).mkdir()
                copyWorld(
                    File(joinFilePaths(Config.serverWorkingDirectory, it)),
                    File(joinFilePaths("backup", destinationSlot.storageDir, it))
                )
            }
            ServerStatus.autoSaveState = ServerStatus.State.SAVE_OFF
            ServerApi.executeCommand(SlotManager.config.serverCommand.saveOn)
            while (true) {
                sleep(50)
                if (ServerStatus.autoSaveState == ServerStatus.State.SAVE_ON) break
            }
            val slotConfig = destinationSlot
            slotConfig.creationTimeMillis = startTime
            slotConfig.worldDir = SlotManager.config.worldDir
            slotConfig.isEmpty = false
            slotConfig.comment = comment ?: ""
            SlotManager.writeSlotConfig(slotConfig)
            val endTime = System.currentTimeMillis()
            commandSourceStack.logResponse(Text("Done! (${(endTime - startTime) / 1000.0}s)"))
        } catch (e: Exception) {
            commandSourceStack.sendFeedback(
                text(
                    "Cannot make backup because an error occurred, ${e.javaClass.name}: ${e.message ?: e.localizedMessage}",
                    NamedTextColor.RED
                ).hoverEvent(HoverEvent.showText(Component.text(e.stackTraceToString())))
            )
            e.printStackTrace()
        }
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }

}
