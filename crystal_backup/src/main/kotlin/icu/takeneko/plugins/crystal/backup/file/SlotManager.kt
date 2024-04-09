package icu.takeneko.plugins.crystal.backup.file

import cn.hutool.core.io.FileUtil
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.config.Config
import icu.takeneko.omms.crystal.plugin.PluginException
import icu.takeneko.omms.crystal.util.joinFilePaths
import icu.takeneko.plugins.crystal.backup.*
import icu.takeneko.plugins.crystal.backup.command.text
import java.io.File
import java.nio.charset.StandardCharsets

object SlotManager {

    var config = createDefaultConfig()
    var hasActiveJob = false
    var scheduledProcedure: Procedure? = null
    val slots = mutableMapOf<String, Slot>()
    private val ignoredFiles = mutableListOf<File>()
    fun loadFiles() {
        slots.clear()
        try {
            if (storageDir.isFile) {
                throw RuntimeException("Backup storage directory is a file!")
            }
            if (!storageDir.exists()) {
                storageDir.mkdir()
            }
            if (!configFile.exists()) {
                configFile.createNewFile()
                configFile.writer(StandardCharsets.UTF_8).use {
                    gson.toJson(config, it)
                    it.flush()
                }
            }
            configFile.reader(StandardCharsets.UTF_8).use {
                config = gson.fromJson(it, config.javaClass)
            }
            var slotCount = 0
            config.slots.forEach {
                val slotDir = File(joinFilePaths("backup", it))
                if (!slotDir.exists()) {
                    slotDir.mkdir()
                }
                val slotConfig = File(joinFilePaths("backup", it, "slot.json"))
                if (!slotConfig.exists()) {
                    slotConfig.createNewFile()
                    slotConfig.writer(StandardCharsets.UTF_8).use { it1 ->
                        gson.toJson(
                            Slot("slot${slotCount++}", System.currentTimeMillis(), true, it, "", config.worldDir),
                            it1
                        )
                        it1.flush()
                    }
                }
                slotConfig.reader(StandardCharsets.UTF_8).use { it1 ->
                    val slot = gson.fromJson(it1, Slot::class.java);
                    slots[slot.id] = slot
                }
            }
            ignoredFiles.run {
                this.clear()
                config.worldDir.forEach {
                    config.ignoredFiles.forEach { it1 ->
                        this += File(
                            joinFilePaths(
                                Config.serverWorkingDirectory,
                                it,
                                it1
                            )
                        )
                    }
                }
                slots.forEach {
                    config.ignoredFiles.forEach { it1 ->
                        config.worldDir.forEach { it2 ->
                            this += File(joinFilePaths("backup", it.value.storageDir, it2, it1))
                        }
                    }
                }
                this
            }
        } catch (e: Exception) {
            throw PluginException("Cannot load backup files")
        }
    }

    operator fun get(id: String): Slot? = slots[id]
    fun forEach(function: SlotManager.(Slot) -> Unit) {
        this.slots.forEach {
            function(this, it.value)
        }
    }

    fun getIgnoredFiles(): List<File> = ignoredFiles

    fun writeSlotConfig(config: Slot) {
        this.slots[config.id] = config
        val file = File(joinFilePaths("backup", config.storageDir, "slot.json"))
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.writer(StandardCharsets.UTF_8).use {
            gson.toJson(config, it)
        }
    }

    fun saveConfig() {
        config.slots = this.slots.values.map { it.storageDir }.toMutableList()
        File(joinFilePaths("config", "crystal_backup.json")).writer(StandardCharsets.UTF_8).use {
            gson.toJson(config, it)
        }
    }


    fun createSlot(id: String, commandSourceStack: CommandSourceStack) {
        val dirStr = randomStringGen(16)
        val slot = Slot(id, System.currentTimeMillis(), true, dirStr, "", config.worldDir)
        val storeDirectory = File(joinFilePaths("backup", dirStr))
        commandSourceStack.sendFeedback(text("Creating Directory."))
        storeDirectory.mkdir()
        commandSourceStack.sendFeedback(text("Writing Files."))
        writeSlotConfig(slot)
        saveConfig()
    }


}