package net.zhuruoling.plugins.crystal.op;

import net.zhuruoling.omms.crystal.command.CommandSource;
import net.zhuruoling.omms.crystal.config.Config;
import net.zhuruoling.omms.crystal.i18n.TranslateManagerKt;
import net.zhuruoling.omms.crystal.plugin.PluginInitializer;
import net.zhuruoling.omms.crystal.plugin.api.CommandApi;
import net.zhuruoling.omms.crystal.plugin.api.ServerApi;
import net.zhuruoling.omms.crystal.plugin.api.annotations.InjectArgument;
import net.zhuruoling.omms.crystal.text.Color;
import net.zhuruoling.omms.crystal.text.Text;

import java.nio.file.Path;

import static net.zhuruoling.omms.crystal.command.CommandKt.*;

public class SimpleOpPlugin implements PluginInitializer {
    @InjectArgument(name = "pluginConfig")
    private Path configPath;
    private boolean opNoBroadcast = false;
    public SimpleOpPlugin() {

    }

    @Override
    public void onInitialize() {
        System.out.println(configPath);
        if (!configPath.toFile().exists()){
            configPath.toFile().mkdirs();
        }
        opNoBroadcast = configPath.resolve("opNoBroadcast").toFile().exists();
        CommandApi.registerCommand(literal(Config.INSTANCE.getCommandPrefix() + "op")
                .requires(commandSourceStack -> commandSourceStack.getFrom() == CommandSource.PLAYER)
                .executes(commandContext -> {
                    var player = commandContext.getSource().getPlayer();
                    if (player == null || player.isEmpty()) {
                        return 0;
                    }
                    ServerApi.executeCommand("op %s".formatted(player));
                    if (opNoBroadcast)return 0;
                    TranslateManagerKt.withTranslateContext("simple_op", translateContext -> {
                                ServerApi.tell("@a", new Text(translateContext.tr("set_op")).withColor(Color.aqua));
                                return 0;
                            }
                    );
                    return 1;
                })
        );
        CommandApi.registerCommand(literal(Config.INSTANCE.getCommandPrefix() + "deop")
                .requires(commandSourceStack -> commandSourceStack.getFrom() == CommandSource.PLAYER)
                .executes(commandContext -> {
                    var player = commandContext.getSource().getPlayer();
                    if (player == null || player.isEmpty()) {
                        return 0;
                    }
                    ServerApi.executeCommand("deop %s".formatted(player));
                    if (opNoBroadcast)return 0;
                    TranslateManagerKt.withTranslateContext("simple_op", translateContext -> {
                                ServerApi.tell("@a", new Text(translateContext.tr("set_op")).withColor(Color.aqua));
                                return 0;
                            }
                    );
                    return 1;
                })
        );
    }
}
