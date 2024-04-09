package icu.takeneko.plugins.crystal.backup.file

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.config.Config
import icu.takeneko.omms.crystal.event.ServerStartEvent
import icu.takeneko.omms.crystal.event.ServerStartEventArgs
import icu.takeneko.omms.crystal.event.ServerStopEvent
import icu.takeneko.omms.crystal.event.ServerStopEventArgs
import icu.takeneko.omms.crystal.main.SharedConstants
import icu.takeneko.omms.crystal.text.Color
import icu.takeneko.omms.crystal.text.Text
import icu.takeneko.omms.crystal.util.joinFilePaths
import icu.takeneko.plugins.crystal.backup.LOGGER
import icu.takeneko.plugins.crystal.backup.command.text
import icu.takeneko.plugins.crystal.backup.copyWorld
import icu.takeneko.plugins.crystal.backup.event.ServerStatus
import icu.takeneko.plugins.crystal.backup.logResponse
import java.io.File

class ScheduledRestoreProcedure(
    scheduledTimeMillis: Long,
    private val sourceSlot: Slot,
    private val commandSourceStack: CommandSourceStack
) : Procedure(scheduledTimeMillis, "RestoreProcedure") {
    override fun run() {
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("There is a running backup/restore job.").withColor(Color.red))
            return
        }
        SlotManager.hasActiveJob = true
        for (i in 5 downTo 1) {
            commandSourceStack.logResponse(
                Text("Server will stop after ${i}s, type ${Config.config.commandPrefix}backup abort to abort operation.").withColor(
                    Color.red
                )
            )
            for (j in 1 until 20) {
                sleep(50)
                if (this.aborted) {
                    commandSourceStack.logResponse(Text("Aborted.").withColor(Color.green))
                    SlotManager.hasActiveJob = false
                    SlotManager.scheduledProcedure = null
                    return
                }
            }
        }
        if (this.aborted) {
            return
        }
        val startTime = System.currentTimeMillis()
        if (ServerStatus.serverState != ServerStatus.State.STOPPED) {
            ServerStatus.serverState = ServerStatus.State.RUNNING
            SharedConstants.eventDispatcher.dispatchEvent(ServerStopEvent, ServerStopEventArgs("backup", false))
            while (ServerStatus.serverState != ServerStatus.State.STOPPED) {
                sleep(50)
            }
        }
        try {
            LOGGER.info("Backing up current world to avoid idiot.")
            val overwriteProtectDir = File(joinFilePaths("backup", "overwriteProtection"))
            if (overwriteProtectDir.exists()) {
                overwriteProtectDir.deleteRecursively()
            }
            overwriteProtectDir.mkdir()
            SlotManager.config.worldDir.forEach {
                copyWorld(File(joinFilePaths(Config.config.workingDir, it)), overwriteProtectDir.resolve("it"))
            }
            LOGGER.info("Deleting world.")
            SlotManager.config.worldDir.forEach {
                LOGGER.info("Deleting ${joinFilePaths(Config.config.workingDir, it)}.")
                File(joinFilePaths(Config.config.workingDir, it)).deleteRecursively()
                File(joinFilePaths(Config.config.workingDir, it)).mkdir()
            }
            LOGGER.info("Copying world.")
            sourceSlot.worldDir.forEach {
                LOGGER.info(
                    "Copying ${
                        joinFilePaths(
                            "backup",
                            sourceSlot.storageDir,
                            it
                        )
                    } -> ${joinFilePaths(Config.config.workingDir,it)}."
                )
                copyWorld(
                    File(joinFilePaths("backup", sourceSlot.storageDir, it)),
                    File(joinFilePaths(Config.config.workingDir, it))
                )
            }
        } catch (e: Exception) {
            commandSourceStack.sendFeedback(
                text(
                    "Cannot delete slot because an error occurred, ${e.javaClass.name}: ${e.message ?: e.localizedMessage}",
                    NamedTextColor.RED
                ).hoverEvent(HoverEvent.showText(Component.text(e.stackTraceToString())))
            )
            e.printStackTrace()
        }
        val endTime = System.currentTimeMillis()
        LOGGER.info("Done! (${(endTime - startTime) / 1000}s)")
        SharedConstants.eventDispatcher.dispatchEvent(
            ServerStartEvent,
            ServerStartEventArgs(Config.config.launchCommand, Config.config.workingDir)
        )
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }
}
