package net.zhuruoling.plugins.crystal.locate

import net.zhuruoling.omms.crystal.event.ServerInfoEventArgs
import net.zhuruoling.omms.crystal.plugin.api.annotations.EventHandler

class ServerInfoListener {


    @EventHandler(event = "")
    fun onInfo(e: ServerInfoEventArgs){
        val info = e.info.info

    }
}