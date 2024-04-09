package icu.takeneko.plugins.crystal.locate

import icu.takeneko.omms.crystal.event.ServerInfoEvent
import icu.takeneko.omms.crystal.event.ServerInfoEventArgs
import icu.takeneko.omms.crystal.plugin.api.annotations.EventHandler

class ServerInfoListener {


    @EventHandler(event = ServerInfoEvent::class)
    fun onInfo(e: ServerInfoEventArgs){
        val info = e.info.info

    }
}