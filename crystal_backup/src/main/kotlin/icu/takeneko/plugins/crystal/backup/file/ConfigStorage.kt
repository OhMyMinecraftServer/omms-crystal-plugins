package icu.takeneko.plugins.crystal.backup.file

import icu.takeneko.omms.crystal.permission.Permission

data class ConfigStorage(
    val requireConfirm: Boolean,
    val showSlotSize: Boolean,
    val worldDir: MutableList<String>,
    val permissionLevelRequirement: PermissionLevelRequirement,
    val serverCommand: ServerCommand,
    val ignoredFiles: MutableList<String>,
    val worldSavingKeywords: MutableList<String>,
    val worldSavedKeywords: MutableList<String>,
    val autoSaveOnKeywords: MutableList<String>,
    val autoSaveOffKeywords: MutableList<String>,
    var slots: MutableList<String>,
)

data class Slot(
    val id: String,
    var creationTimeMillis: Long,
    var isEmpty: Boolean,
    val storageDir: String,
    var comment: String,
    var worldDir: MutableList<String>
)
data class PermissionLevelRequirement(
    val makeBackup: Permission,
    val deleteBackup: Permission,
    val showSlot: Permission,
    val deleteSlot: Permission,
    val clearSlot:Permission,
    val createSlot: Permission,
    val confirmOperation: Permission,
    val revokeOperation: Permission,
    val reloadConfig: Permission
)

data class ServerCommand(
    val saveOn:String,
    val saveOff: String,
    val saveAll:String
)