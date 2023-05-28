package net.zhuruoling.plugins.crystal.backup.file

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
                copyWorld(File(joinFilePaths(Config.serverWorkingDirectory, it)), overwriteProtectDir)
            }
            LOGGER.info("Deleting world.")
            SlotManager.config.worldDir.forEach {
                LOGGER.info("Deleting ${joinFilePaths(Config.serverWorkingDirectory, it)}.")
                File(joinFilePaths(Config.serverWorkingDirectory, it)).deleteRecursively()
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
                    } -> ${joinFilePaths(Config.serverWorkingDirectory)}."
                )
                copyWorld(
                    File(joinFilePaths("backup", sourceSlot.storageDir, it)),
                    File(joinFilePaths(Config.serverWorkingDirectory))
                )
            }
        } catch (e: Exception) {
            LOGGER.error("Cannot restore world!", e)
        }
        val endTime = System.currentTimeMillis()
        LOGGER.info("Done! (${(endTime - startTime) / 1000}s)")
        SharedConstants.eventDispatcher.dispatchEvent(
            ServerStartEvent,
            ServerStartEventArgs(Config.launchCommand, Config.serverWorkingDirectory)
        )
        SlotManager.hasActiveJob = false
    }
}
