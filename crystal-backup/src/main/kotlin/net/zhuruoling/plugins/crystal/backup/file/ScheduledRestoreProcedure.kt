package net.zhuruoling.plugins.crystal.backup.file

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.config.Config
import net.zhuruoling.omms.crystal.event.ServerStartEvent
import net.zhuruoling.omms.crystal.event.ServerStartEventArgs
import net.zhuruoling.omms.crystal.event.ServerStopEvent
import net.zhuruoling.omms.crystal.event.ServerStopEventArgs
import net.zhuruoling.omms.crystal.main.SharedConstants
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.omms.crystal.util.joinFilePaths
import net.zhuruoling.plugins.crystal.backup.LOGGER
import net.zhuruoling.plugins.crystal.backup.command.text
import net.zhuruoling.plugins.crystal.backup.copyWorld
import net.zhuruoling.plugins.crystal.backup.event.ServerStatus
import net.zhuruoling.plugins.crystal.backup.logResponse
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
                Text("Server will stop after ${i}s, type ${Config.commandPrefix}backup abort to abort operation.").withColor(
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
                copyWorld(File(joinFilePaths(Config.serverWorkingDirectory, it)), overwriteProtectDir.resolve("it"))
            }
            LOGGER.info("Deleting world.")
            SlotManager.config.worldDir.forEach {
                LOGGER.info("Deleting ${joinFilePaths(Config.serverWorkingDirectory, it)}.")
                File(joinFilePaths(Config.serverWorkingDirectory, it)).deleteRecursively()
                File(joinFilePaths(Config.serverWorkingDirectory, it)).mkdir()
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
                    } -> ${joinFilePaths(Config.serverWorkingDirectory,it)}."
                )
                copyWorld(
                    File(joinFilePaths("backup", sourceSlot.storageDir, it)),
                    File(joinFilePaths(Config.serverWorkingDirectory, it))
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
            ServerStartEventArgs(Config.launchCommand, Config.serverWorkingDirectory)
        )
        SlotManager.hasActiveJob = false
        SlotManager.scheduledProcedure = null
    }
}
