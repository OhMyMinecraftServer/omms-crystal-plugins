package net.zhuruoling.plugins.crystal.backup.event

import net.zhuruoling.omms.crystal.event.ServerInfoEventArgs
import net.zhuruoling.omms.crystal.event.ServerStartedEventArgs
import net.zhuruoling.omms.crystal.event.ServerStoppedEventArgs
import net.zhuruoling.omms.crystal.plugin.api.annotations.EventHandler
import net.zhuruoling.plugins.crystal.backup.file.SlotManager

class ServerSaveStateListener {
    @EventHandler(event = "crystal.server.info")
    fun onInfo(e: ServerInfoEventArgs) {
        if (SlotManager.config.worldSavingKeywords.any{it in e.info.info}){
            ServerStatus.saveState = ServerStatus.State.WORLD_SAVING
        }
        if (SlotManager.config.worldSavedKeywords.any { it in e.info.info }) {
            ServerStatus.saveState = ServerStatus.State.WORLD_SAVED;
        }
        if (SlotManager.config.autoSaveOnKeywords.any{it in e.info.info}){
            ServerStatus.autoSaveState = ServerStatus.State.SAVE_ON
        }
        if (SlotManager.config.autoSaveOffKeywords.any { it in e.info.info }) {
            ServerStatus.autoSaveState = ServerStatus.State.SAVE_OFF;
        }
    }

    @EventHandler(event = "crystal.server.started")
    fun onServerStarted(e: ServerStartedEventArgs) {
        ServerStatus.serverState = ServerStatus.State.RUNNING
    }

    @EventHandler(event = "crystal.server.stopped")
    fun onServerStopped(e: ServerStoppedEventArgs) {
        ServerStatus.serverState = ServerStatus.State.STOPPED
    }

}