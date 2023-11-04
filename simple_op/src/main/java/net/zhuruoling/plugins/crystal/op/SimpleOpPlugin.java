package net.zhuruoling.plugins.crystal.op;

import net.zhuruoling.omms.crystal.command.CommandSource;
import net.zhuruoling.omms.crystal.config.Config;
import net.zhuruoling.omms.crystal.i18n.TranslateContext;
import net.zhuruoling.omms.crystal.i18n.TranslateManagerKt;
import net.zhuruoling.omms.crystal.plugin.PluginInitializer;
import net.zhuruoling.omms.crystal.plugin.api.CommandApi;
import net.zhuruoling.omms.crystal.plugin.api.ServerApi;
import net.zhuruoling.omms.crystal.plugin.api.annotations.InjectArgument;
import net.zhuruoling.omms.crystal.text.Color;
import net.zhuruoling.omms.crystal.text.Text;

import java.nio.file.Path;
import java.util.function.Function;

import static net.zhuruoling.omms.crystal.command.CommandKt.literal;

public class SimpleOpPlugin implements PluginInitializer {
    @InjectArgument(name = "pluginConfig")
    private Path configPath;
    private boolean opNoBroadcast = false;
    public SimpleOpPlugin() {

    }

    private static class TellFunction implements Function<TranslateContext, Integer>{
        String who;
        String template;
        public TellFunction(String who, String template) {
            this.who = who;
            this.template = template;
        }
        @Override
        public Integer apply(TranslateContext translateContext) {
            ServerApi.tell(who, new Text(translateContext.tr(template)).withColor(Color.aqua));
            return 0;
        }
    }
    //use java to write this plugin is a big mistake
    private static final TellFunction TELL_FN_OP = new TellFunction("@a", "set_op");
    private static final TellFunction TELL_FN_DEOP = new TellFunction("@a", "unset_op");

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
                    TranslateManagerKt.withTranslateContext("simple_op", TELL_FN_OP);
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
                    TranslateManagerKt.withTranslateContext("simple_op", TELL_FN_DEOP);
                    return 1;
                })
        );
    }
}
