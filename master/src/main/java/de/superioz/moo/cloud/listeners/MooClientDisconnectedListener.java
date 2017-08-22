package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.events.MooClientDisconnectEvent;
import de.superioz.moo.protocol.server.MooClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MooClientDisconnectedListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMooClientDisconnect(MooClientDisconnectEvent event) {
        MooClient client = event.getClient();

        // if the moo client disconnects ..
        // if the type is PROXY
        if(client.getType() == ClientType.PROXY) {
            List<UUID> toRemove = new ArrayList<>();
            for(UUID uuid : Cloud.getInstance().getMooProxy().getPlayerServerMap().keySet()) {
                InetSocketAddress address = Cloud.getInstance().getMooProxy().getPlayerServerMap().get(uuid);
                if(address.equals(client.getAddress())) toRemove.add(uuid);
            }

            toRemove.forEach(uuid -> Cloud.getInstance().getMooProxy().getPlayerServerMap().remove(uuid));

            // update player count
            MooCache.getInstance().getConfigMap().fastPutAsync(MooConfigType.PLAYER_COUNT.getKey(),
                    Cloud.getInstance().getMooProxy().getPlayers().size());
        }
    }

}