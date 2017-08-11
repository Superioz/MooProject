package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketPlayerState;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class ProxyPlayerConnectionListener implements Listener {

    /**
     * When the player is about connecting to a server
     *
     * @param event The event
     */
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if(!event.getTarget().getName().contains(Thunder.LOBBY_NAME) &&
                !event.getTarget().getName().contains(Thunder.LIMBO_NAME)) {
            return;
        }
        List<ServerInfo> l = Thunder.getInstance().getServers(Thunder.LOBBY_REGEX);

        // if no lobby server is registered
        if(l.isEmpty()) {
            return;
        }

        // get lobby with fewest player online
        ServerInfo lobbyFewest = null;
        int fewestPlayer = -1;
        for(ServerInfo serverInfo : l) {
            if(fewestPlayer == -1 || serverInfo.getPlayers().size() < fewestPlayer) {
                fewestPlayer = serverInfo.getPlayers().size();
                lobbyFewest = serverInfo;
            }
        }
        if(lobbyFewest == null) {
            // rip
            return;
        }

        event.setTarget(lobbyFewest);
    }

    /**
     * When the player connected to a server
     *
     * @param event The event
     */
    private void onServerConnectedAsync(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // get playerdata for updating state
        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes the state of the player
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.CONNECT_SERVER,
                event.getServer().getInfo().getName(), response -> {
                });
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onServerConnectedAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * When the player disconnected from the server
     *
     * @param event The event
     */
    private void onPlayerDisconnectAsync(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // get playerdata for updating state
        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes the player's state; removes player data
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.LEAVE_PROXY, response -> {
            if(response.isOk()) {
                try {
                    Thread.sleep(1000L);
                }
                catch(InterruptedException e) {
                    //
                }

                // removes data of player after 1s
                MooCache.getInstance().getPlayerPermissionMap().removeAsync(data.uuid);
                MooCache.getInstance().getUniqueIdPlayerMap().removeAsync(data.uuid);
                MooCache.getInstance().getNameUniqueIdMap().removeAsync(data.lastName);
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onPlayerDisconnectAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

}