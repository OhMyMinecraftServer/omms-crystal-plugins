package net.zhuruoling.plugins.crystal.backup.command

import cn.hutool.core.date.DateTime
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.zhuruoling.omms.crystal.command.*
import net.zhuruoling.omms.crystal.config.Config
import net.zhuruoling.omms.crystal.text.Color
import net.zhuruoling.omms.crystal.text.HoverAction
import net.zhuruoling.omms.crystal.text.Text
import net.zhuruoling.omms.crystal.text.TextGroup
import net.zhuruoling.plugins.crystal.backup.file.ScheduledBackupProcedure
import net.zhuruoling.plugins.crystal.backup.file.Slot
import net.zhuruoling.plugins.crystal.backup.file.SlotManager
import net.zhuruoling.plugins.crystal.backup.logResponse
import net.zhuruoling.plugins.crystal.backup.requirePermission

fun text(s: String, color: NamedTextColor = NamedTextColor.WHITE): TextComponent {
    return Component.text(s, color)
}

object BackupCommand {

    private val helpTexts = TextGroup(
        Text(Config.commandPrefix + "backup -> Show help info"),
        Text(Config.commandPrefix + "backup slot create <slot> -> Create a new backup slot"),
        Text(Config.commandPrefix + "backup slot delete <slot> -> Delete a backup slot "),
        Text(Config.commandPrefix + "backup slot info <slot> -> Show slot info"),
        Text(Config.commandPrefix + "backup slot list -> List all slot details"),
        Text(Config.commandPrefix + "backup make [<slot>] [<comment>]-> Make a backup to <slot> with a <comment>"),
        Text(Config.commandPrefix + "backup restore [<slot>] -> Restore world from <slot>"),
        Text(Config.commandPrefix + "backup confirm -> Confirm a operation"),
        Text(Config.commandPrefix + "backup abort -> Abort a operation."),
        Text("*Any arguments wrapped with [] is optional*")
    )

    fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal(Config.commandPrefix + "backup").then(
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
                    listSlots(it.source)
                    1
                }
            ).then(
                literal("clear").then(
                    wordArgument("slot").executes {
                        1
                    }
                )
            )
        ).then(
            literal("make").then(
                wordArgument("slot").then(
                    greedyStringArgument("comment").executes {//slot + comment
                        makeBackup(getWord(it, "slot"), getWord(it, "comment"), it.source)
                        1
                    }
                ).executes {// slot
                    makeBackup(getWord(it, "slot"), null, it.source)
                    1
                }
            ).executes {//nothing
                makeBackup(null, null, it.source)
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
            literal("abort").executes {
                processAbort(it.source)
                1
            }
        ).executes {
            helpInfo(it.source)
            1
        }
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

    private fun helpInfo(commandSourceStack: CommandSourceStack) {
        displayHeader(commandSourceStack)
        commandSourceStack.sendFeedback(Text("[COMMAND HELP]").withbold(true).withColor(Color.green))
        helpTexts.getTexts().forEach {
            commandSourceStack.sendFeedback(it)
        }
        commandSourceStack.sendFeedback(Text("[QUICK ACTION]").withbold(true).withColor(Color.green))
//        commandSourceStack.sendFeedback(TextGroup(Text(">>> Create a backup to first slot <<<").withColor(Color.green).withClickEvent(
//            ClickEvent(ClickAction.suggest_command, "${Config.commandPrefix}backup make")
//        )))
    }

    private fun listSlots(commandSourceStack: CommandSourceStack) {
        if (!commandSourceStack.requirePermission(SlotManager.config.permissionLevelRequirement.showSlot)) return
        displayHeader(commandSourceStack)
        commandSourceStack.logResponse(Text("[SLOT INFO]").withbold(true).withColor(Color.green))
        SlotManager.forEach {
            val formattedTimeStr = DateTime.of(it.creationTimeMillis).toString("yyyy-MM-dd hh:mm:ss")
            commandSourceStack.sendFeedback(Component.text("[")
                .append(Component.text(it.id, NamedTextColor.YELLOW))
                .append(Component.text("]"))
                .append {
                    if (!it.isEmpty)
                        text(" [▶]", NamedTextColor.GREEN).clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "${Config.commandPrefix}backup restore ${it.id}"
                            )
                        ).hoverEvent(
                            HoverEvent.showText(
                                Component.text(
                                    "Restore from this slot",
                                    NamedTextColor.LIGHT_PURPLE
                                )
                            )
                        )
                    else
                        text(" [▶]", NamedTextColor.GREEN).clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "${Config.commandPrefix}backup make ${it.id}"
                            )
                        ).hoverEvent(
                            HoverEvent.showText(
                                Component.text(
                                    "Make a backup into this slot",
                                    NamedTextColor.LIGHT_PURPLE
                                )
                            )
                        )
                }.append {
                    if (!it.isEmpty)
                        Component.text(" [×]", NamedTextColor.RED).clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "${Config.commandPrefix}backup slot clear ${it.id}"
                            )
                        ).hoverEvent(
                            HoverEvent.showText(
                                Component.text(
                                    "Clear this slot",
                                    NamedTextColor.LIGHT_PURPLE
                                )
                            )
                        )
                    else
                        Component.text(" Empty Slot", NamedTextColor.YELLOW)
                }.append {
                    if (!it.isEmpty) {
                        text(" Time: $formattedTimeStr; Comment: ${it.comment.ifEmpty { "Empty" }}")
                    } else
                        text("")
                }
            )
        }
    }

    private fun displayHeader(commandSourceStack: CommandSourceStack) {
        commandSourceStack.sendFeedback(Text("======== Crystal Backup ========").withColor(Color.aqua))
        commandSourceStack.sendFeedback(Text("A multi slot backup & restore plugin."))
    }

    private fun processAbort(commandSourceStack: CommandSourceStack) {
        if (!commandSourceStack.requirePermission(SlotManager.config.permissionLevelRequirement.revokeOperation)) return
        if (SlotManager.hasActiveJob) {
            commandSourceStack.sendFeedback(Text("Aborting.").withColor(Color.yellow))
            SlotManager.scheduledProcedure!!.aborted = true
            SlotManager.scheduledProcedure = null
            return
        }
        if (SlotManager.scheduledProcedure == null) {
            commandSourceStack.sendFeedback(Text("Nothing to abort.").withColor(Color.yellow))
            return
        }
        SlotManager.scheduledProcedure = null
        commandSourceStack.sendFeedback(Text("One operation was aborted.").withColor(Color.green))
    }

    private fun makeBackup(id: String?, comment: String?, commandSourceStack: CommandSourceStack) {
        if (SlotManager.slots.isEmpty()) {
            commandSourceStack.sendFeedback(text("No slot configured."))
        }
        val slot = if (id == null) SlotManager[SlotManager.slots.keys.first()]!! else SlotManager[id].run {
            if (this == null) {
                commandSourceStack.sendFeedback(text("Slot $id not exist!", NamedTextColor.RED))
                return
            } else this
        }

        if (SlotManager.scheduledProcedure == null) {
            synchronized(SlotManager) {
                SlotManager.scheduledProcedure = ScheduledBackupProcedure(0, slot, commandSourceStack, comment)
                SlotManager.scheduledProcedure!!.start()
            }
        } else {
            commandSourceStack.sendFeedback(Text("There is a running backup/restore job.").withColor(Color.red))
        }
    }
}
