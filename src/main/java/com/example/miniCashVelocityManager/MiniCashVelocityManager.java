package miniCashVelocityManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import miniCashVelocityManager.DB.Setup;
import miniCashVelocityManager.discord.Bot;
import miniCashVelocityManager.japanese.Kana;
import miniCashVelocityManager.japanese.JPMain;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "minicashvelocitymanager", name = "MiniCashVelocityManager", version = BuildConstants.VERSION)
public class MiniCashVelocityManager {

    public static final ChannelIdentifier CHANNEL =
            MinecraftChannelIdentifier.from("minicash:velocity");


    private ProxyServer server;
    private Logger logger;
    private Path path;
    private ConfigurationNode config;

    private JPMain main;
    private Setup setupDB;
    private Bot bot;
    private Model model;

    @Inject
    public MiniCashVelocityManager(ProxyServer server, Logger logger, EventManager eventManager,@DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.path = path;
        Kana ckana = new Kana();
        this.main = new JPMain(this, server, logger,ckana);
        this.model = new Model(server,logger);

        newFile();

        this.setupDB = new Setup(logger,config);
        this.bot = new Bot(logger,config,server);
        logger.info("プラグインがロード完了");

        bot.connect();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // プロキシ起動時の処理
        logger.info("MiniCashVelocityManagerプラグイン有効化！");
        server.getChannelRegistrar().register(CHANNEL);

        server.getEventManager().register(this, new MyListener());



        CommandManager commandManager = server.getCommandManager();

        miniCashVelocityManager.commands.Main mainCMD = new miniCashVelocityManager.commands.Main(model);
        BrigadierCommand commandToRegister = mainCMD.createBrigadierCommand(server);

        CommandMeta commandMeta = commandManager.metaBuilder("minicashvelocitymanager")
                .aliases("minicashvm", "mvm") // 短縮コマンド登録
                .plugin(this)
                .build();

        commandManager.register(commandMeta, commandToRegister);
        logger.info("コマンド登録完了");

    }





    @Subscribe
    public void proxyShutdown(ProxyShutdownEvent event) {
        bot.disconnect();
        setupDB.disconnect();
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        String playerName = event.getPlayer().getUsername();

        bot.sendMessage(playerName + "が接続しました");
        logger.info(playerName + " が接続しました！");
    }

    @Subscribe
    public void playerDisconnect(DisconnectEvent event) {
        String playerName = event.getPlayer().getUsername();

        bot.sendMessage(playerName + "が切断しました");
        logger.info(playerName + "が切断しました");
    }




    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {


        Player player = event.getPlayer();
        String message = event.getMessage();

        logger.info("メッセージ内容：" + message);


        // デバッグ用に抽出後の文字を表示
        String finalMessage = message.replace("@","＠");

        Optional<ServerConnection> connection = player.getCurrentServer();

        if (connection.isPresent()) {
            RegisteredServer server = connection.get().getServer();

            // サーバー名を取得して表示
            String serverName = server.getServerInfo().getName();

//            player.sendMessage(Component.text("あなたは現在 " + serverName + " に接続しています。"));
        } else {
            // まだどのサーバーにも接続されていない場合
            player.sendMessage(Component.text("サーバーに接続されていません"));
        }

        // サーバー名の取得
        String serverName = player.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse("Unknown");

        logger.info("プレイヤー接続中のサーバー：" + serverName);

        main.checkJapaneseMessage(player, finalMessage, serverName);


        bot.sendMessage("[" + serverName + ":"+ player.getUsername() + " ] " + finalMessage );

        // Discordへ送信
//            sendToDiscord(player.getUsername(), finalMessage);


    }



    public void newFile(){

        // フォルダなければ生成
        if (!Files.exists(path)) {
            try{
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }


        Path configFilePath =  path.resolve("config.yml");

        if (!Files.exists(configFilePath)) {
            try (InputStream configFile = getClass().getResourceAsStream("/config.yml")) {
                if (configFile != null) {
                    Files.copy(configFile, configFilePath);
                } else {
                    Files.createFile(configFilePath);
                }
            } catch (IOException e) {
                logger.error("configファイルの生成にエラーが発生しました" + e.getMessage());
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configFilePath)
                .build();

        try {
            config = loader.load();
            logger.info("configファイルの読み込みました");
        } catch (IOException e) {
            logger.error("configファイルの読み込み時にエラーが発生しました" +  e.getMessage());
        }


    }


    public class MyListener {


        @Subscribe
        public void onPluginMessage(PluginMessageEvent event) {
            logger.info("PluginMessage 受信: " + event.getIdentifier().getId());

            if (!event.getIdentifier().equals(CHANNEL)) return;

            logger.info("CHANNEL一致");

            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String sub = in.readUTF();
            logger.info("sub=" + sub);

            if (!sub.equals("broadcast")) return;

            String message = in.readUTF();
            logger.info("message=" + message);

            server.getAllPlayers().forEach(player ->
                    player.sendMessage(Component.text("[Proxy] " + message))
            );
        }


    }


}

