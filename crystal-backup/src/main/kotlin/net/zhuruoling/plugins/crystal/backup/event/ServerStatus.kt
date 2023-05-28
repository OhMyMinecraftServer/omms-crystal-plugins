package net.zhuruoling.plugins.crystal.backup.event

object ServerStatus {
    var saveState = State.WORLD_SAVING
    var autoSaveState = State.SAVE_ON
    var serverState = State.RUNNING
    enum class State{
        SAVE_ON,SAVE_OFF,WORLD_SAVED,WORLD_SAVING,RUNNING,STOPPED
    }
}