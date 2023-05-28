package net.zhuruoling.plugins.crystal.backup.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.zhuruoling.omms.crystal.command.CommandSourceStack
import net.zhuruoling.omms.crystal.command.literal
import net.zhuruoling.omms.crystal.command.wordArgument
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.plugins.crystal.backup.file.SlotManager
import net.zhuruoling.plugins.crystal.backup.logResponse
import net.zhuruoling.plugins.crystal.backup.requirePermission

object BackupCommand {
    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("backup").then(
            literal("slot").then(
                literal("delete").then(
                    wordArgument("slot").executes {
                        1
                    }
                )
            ).then(
                literal("info").then(
                    wordArgument("slot").executes {
                        1
                    }
                )
            ).then(
                literal("create").then(
                    wordArgument("slot").executes {
                        1
                    }
                )
            ).then(
                literal("list").executes {
                    1
                }
            )
        ).then(
            literal("backup").then(
                wordArgument("slot").executes {
                    1
                }
            ).executes {
                1
            }
        ).then(
            literal("restore").then(
                wordArgument("slot").executes {
                    1
                }
            ).executes {
                1
            }
        ).then(
            literal("confirm").executes {
                processConfirm(it.source)
                1
            }
        ).then(
            literal("revoke").executes {
                processRevoke(it.source)
                1
            }
        )
    }

    private fun processConfirm(commandSourceStack: CommandSourceStack) {
        if (!commandSourceStack.requirePermission(SlotManager.config.permissionLevelRequirement.confirmOperation)) return
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("here is a running backup/restore job.").withColor(Color.yellow))
            return
        }
        if (SlotManager.scheduledProcedure == null) {
            commandSourceStack.logResponse(Text("Nothing to confirm.").withColor(Color.yellow))
            return
        }
        commandSourceStack.logResponse(Text("Confirm.").withColor(Color.green))
        SlotManager.scheduledProcedure!!.start()
    }

    private fun processRevoke(commandSourceStack: CommandSourceStack) {
        if (!commandSourceStack.requirePermission(SlotManager.config.permissionLevelRequirement.revokeOperation)) return
        if (SlotManager.hasActiveJob) {
            commandSourceStack.logResponse(Text("here is a running backup/restore job.").withColor(Color.yellow))
            return
        }
        if (SlotManager.scheduledProcedure == null) {
            commandSourceStack.logResponse(Text("Nothing to revoke.").withColor(Color.yellow))
            return
        }
        SlotManager.scheduledProcedure = null
        commandSourceStack.logResponse(Text("One operation was revoked.").withColor(Color.green))
    }
}
