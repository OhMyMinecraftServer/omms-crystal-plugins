package net.zhuruoling.plugins.crystal.backup.file

import cn.hutool.core.io.FileUtil
import net.zhuruoling.omms.crystal.config.Config
import net.zhuruoling.omms.crystal.plugin.PluginException
import net.zhuruoling.omms.crystal.util.joinFilePaths
import net.zhuruoling.plugins.crystal.backup.configFile
import net.zhuruoling.plugins.crystal.backup.createDefaultConfig
import net.zhuruoling.plugins.crystal.backup.gson
import net.zhuruoling.plugins.crystal.backup.storageDir
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


    fun getSlotSize(id: String): Long {
        val slot = this[id] ?: throw PluginException("Slot $id not found!")
        val slotDir = File(joinFilePaths("backup", slot.storageDir))
        return FileUtil.size(slotDir)
    }

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


}