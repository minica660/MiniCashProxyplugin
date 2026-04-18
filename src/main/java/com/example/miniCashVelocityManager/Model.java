package miniCashVelocityManager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.Optional;

public class Model {

    private final ProxyServer proxy;
    private final Logger logger;

    public Model(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    // バックエンドサーバーへのメッセージ送信
    public void sendAllServerMessage(String message) {

        Component sendMessage = Component.text(message);


        proxy.getAllPlayers().forEach(player -> {

            Sound sound = Sound.sound(
                    Key.key("entity.elder_guardian.curse"),
                    Sound.Source.MASTER,
                    1.0f,
                    2.0f
            );


            player.playSound(sound, Sound.Emitter.self());





            player.sendMessage(sendMessage);

        });


        logger.info(message + "\n全プレイヤーへ送信完了");

    }


    // バックエンドサーバーへのタイトルメッセージ
    public void sendAllServerTitleMessage(String message, Sound sound) {

        Component sendMessage = Component.text(message);

        Sound sound1 = sound;

        proxy.getAllPlayers().forEach(player -> {

            if (sound1 != null) {
                player.playSound(sound1);
            }

            player.sendMessage(sendMessage);

        });



    }




    // サーバー転送用メソッド
    public void sendPlayer(Player player,String serverName){

        Optional<RegisteredServer> targetServerName = proxy.getServer(serverName);

        if (targetServerName.isPresent()) {

            RegisteredServer targetServer = targetServerName.get();


            // player.createConnectionRequest(targetServer).fireAndForget();

            player.createConnectionRequest(targetServer).connect().thenAccept(result -> {
                if (result.isSuccessful()) {
                    logger.info(player.getUsername() + "を" + targetServerName + "に転送完了");
                } else {
                    player.sendMessage(Component.text("サーバーの接続に失敗しました", NamedTextColor.RED));
                }
            });

        } else {
            player.sendMessage(Component.text("そのサーバーは存在しません", NamedTextColor.RED));
        }


    }









}
