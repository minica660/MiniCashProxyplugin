package miniCashVelocityManager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import miniCashVelocityManager.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Main {

    private final Model model;


    public Main(Model model) {
        this.model = model;
    }


    public BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> helloNode = BrigadierCommand.literalArgumentBuilder("test")

                .requires(source -> source.hasPermission("minicashvelocitymanager.*"))

                .executes(context -> {
                    CommandSource source = context.getSource();

                    Component message = Component.text("Hello World", NamedTextColor.AQUA);
                    source.sendMessage(message);

                    return Command.SINGLE_SUCCESS;
                })

                .then(BrigadierCommand.literalArgumentBuilder("broadcast")
                        .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                            .executes(context -> {

                                String message = context.getArgument("message", String.class);

                                model.sendAllServerMessage(message);


                                return Command.SINGLE_SUCCESS;
                            })


                        )
                )
                .build();

        return new BrigadierCommand(helloNode);
    }
}

