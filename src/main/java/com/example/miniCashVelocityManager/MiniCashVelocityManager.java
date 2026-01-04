package com.example.miniCashVelocityManager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "minicashvelocitymanager", name = "MiniCashVelocityManager", version = BuildConstants.VERSION)
public class MiniCashVelocityManager {


    private ProxyServer server;
    private Logger logger;


    @Inject
    public void MyVelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("プラグインがロードされました！");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // プロキシ起動時の処理
        logger.info("Velocityプラグインが有効になりました！");
    }
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        String username = event.getPlayer().getUsername();
        logger.info(username + " が参加しました！");
    }
}
