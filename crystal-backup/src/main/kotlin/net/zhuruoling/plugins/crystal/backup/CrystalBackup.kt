package net.zhuruoling.plugins.crystal.backup

import net.zhuruoling.omms.crystal.command.CommandManager
import net.zhuruoling.omms.crystal.plugin.PluginInitializer
import net.zhuruoling.plugins.crystal.backup.command.BackupCommand
import net.zhuruoling.plugins.crystal.backup.file.SlotManager

class CrystalBackup : PluginInitializer{
    override fun onInitialize() {
        CommandManager.register(BackupCommand.register())
        SlotManager.loadFiles()
        LOGGER.info("Crystal Backup Loaded!")
    }

}