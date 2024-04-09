package icu.takeneko.plugins.crystal.backup

import icu.takeneko.omms.crystal.command.CommandManager
import icu.takeneko.omms.crystal.plugin.PluginInitializer
import icu.takeneko.plugins.crystal.backup.command.BackupCommand
import icu.takeneko.plugins.crystal.backup.file.SlotManager

class CrystalBackup : PluginInitializer{
    override fun onInitialize() {
        CommandManager.register(BackupCommand.register())
        SlotManager.loadFiles()
        LOGGER.info("Crystal Backup Loaded!")
    }

}