package icu.takeneko.plugins.crystal.backup

import ch.qos.logback.core.util.FileUtil
import cn.hutool.core.io.file.FileNameUtil
import com.google.gson.GsonBuilder
import icu.takeneko.omms.crystal.command.CommandSource
import icu.takeneko.omms.crystal.command.CommandSourceStack
import icu.takeneko.omms.crystal.permission.Permission
import icu.takeneko.omms.crystal.permission.comparePermission
import icu.takeneko.omms.crystal.text.Color
import icu.takeneko.omms.crystal.text.Text
import icu.takeneko.omms.crystal.util.createLogger
import icu.takeneko.omms.crystal.util.joinFilePaths
import icu.takeneko.plugins.crystal.backup.file.ConfigStorage
import icu.takeneko.plugins.crystal.backup.file.PermissionLevelRequirement
import icu.takeneko.plugins.crystal.backup.file.ServerCommand
import icu.takeneko.plugins.crystal.backup.file.SlotManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

val LOGGER = createLogger("CrystalBackup")
val storageDir = File(joinFilePaths("backup"))
val configFile = File(joinFilePaths("backup", "crystal_backup.json"))
val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
fun CommandSourceStack.requirePermission(level: Permission): Boolean {
    return if (this.from == CommandSource.PLAYER) {
        return comparePermission(this.permissionLevel!!, level).run {
            if (!this) this@requirePermission.logResponse(
                Text(
                    "Permission Denied!"
                ).withColor(Color.red)
            )
            this
        }
    } else {
        true
    }
}

fun createDefaultConfig(): ConfigStorage {
    return ConfigStorage(
        requireConfirm = true,
        showSlotSize = true,
        worldDir = mutableListOf("world"),
        permissionLevelRequirement = PermissionLevelRequirement(
            Permission.USER,
            Permission.ADMIN,
            Permission.GUEST,
            Permission.ADMIN,
            Permission.ADMIN,
            Permission.ADMIN,
            Permission.ADMIN,
            Permission.ADMIN,
            Permission.ADMIN
        ),
        serverCommand = ServerCommand(
            "save-on",
            "save-off",
            "save-all"
        ),
        ignoredFiles = mutableListOf("session.lock"),
        worldSavingKeywords = mutableListOf("Saving the game", "Saving the game"),
        worldSavedKeywords = mutableListOf("Saved the game", "Saved the world"),
        autoSaveOnKeywords = mutableListOf("Saving is already turned on", "Automatic saving is now enabled"),
        autoSaveOffKeywords = mutableListOf("Saving is already turned off", "Automatic saving is now disabled"),
        slots = mutableListOf<String>().run { repeat(5) { this += randomStringGen(8) };this }
    )
}

fun CommandSourceStack.logResponse(content: Text) {
    synchronized(this) {
//        if (this.from == CommandSource.PLAYER || this.from == CommandSource.CENTRAL) {
//            LOGGER.info(if(this.from == CommandSource.PLAYER) "[${this.player}] " else if(this.from == CommandSource.CENTRAL) "[CENTRAL] " else "" + content.toRawString())
//        }
        this.sendFeedback(content)
    }
}

fun randomStringGen(len: Int): String {
    return randomStringGen(len, hasInteger = true, hasUpperLetter = true)
}

fun randomStringGen(len: Int, hasInteger: Boolean, hasUpperLetter: Boolean): String {
    val ch =
        "abcdefghijklmnopqrstuvwxyz" + (if (hasUpperLetter) "ABCDEFGHIGKLMNOPQRSTUVWXYZ" else "") + if (hasInteger) "0123456789" else ""
    val stringBuffer = StringBuilder()
    for (i in 0 until len) {
        val random = Random(System.nanoTime())
        val num = random.nextInt(62)
        stringBuffer.append(ch[num])
    }
    return stringBuffer.toString()
}

fun copyWorld(src: File, dst: File) {
    FileUtils.copyDirectory(src, dst) {
        FileNameUtil.getName(it.absolutePath) !in SlotManager.config.ignoredFiles
    }
}
