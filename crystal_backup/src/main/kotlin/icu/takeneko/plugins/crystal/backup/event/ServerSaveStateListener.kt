package icu.takeneko.plugins.crystal.backup.event

import icu.takeneko.omms.crystal.event.*
import icu.takeneko.omms.crystal.plugin.api.annotations.EventHandler
import icu.takeneko.plugins.crystal.backup.file.SlotManager

class ServerSaveStateListener {
    @EventHandler(event = ServerInfoEvent::class)
    /*@EventHandler(event = ServerInfoEvent::class.java)*/
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

    @EventHandler(event = ServerStartedEvent::class)
    fun onServerStarted(e: ServerStartedEventArgs) {
        ServerStatus.serverState = ServerStatus.State.RUNNING
    }

    @EventHandler(event = ServerStoppedEvent::class)
    fun onServerStopped(e: ServerStoppedEventArgs) {
        ServerStatus.serverState = ServerStatus.State.STOPPED
    }

}