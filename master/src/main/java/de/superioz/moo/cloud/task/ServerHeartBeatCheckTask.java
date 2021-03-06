package de.superioz.moo.cloud.task;

import de.superioz.moo.network.common.MooCache;
import de.superioz.moo.network.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.client.ClientType;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.packets.PacketServerUnregister;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ServerHeartBeatCheckTask implements Runnable {

    private int delay;
    private int threshold;

    @Override
    public void run() {
        while(true){
            long now = System.currentTimeMillis();

            // check for last update with server
            List<UUID> toDelete = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getNetworkProxy().getSpigotServers().values()) {
                if(server.getLastHeartBeat() != -1
                        && (now - server.getLastHeartBeat()) > threshold) {
                    toDelete.add(server.getUuid());
                }
            }

            // delete server if they timed out and send to bungee!
            if(!toDelete.isEmpty()) {
                toDelete.forEach(uuid -> {
                    MooServer serverDeleted = Cloud.getInstance().getNetworkProxy().getSpigotServers().remove(uuid);
                    Cloud.getInstance().getLogger().debug("Server " + serverDeleted.getType()
                            + " [" + serverDeleted.getAddress().getHostName() + ":" + serverDeleted.getAddress().getPort() + "] timed out.");
                    PacketMessenger.message(new PacketServerUnregister(serverDeleted.getAddress()), ClientType.PROXY);

                    // sync with redis
                    MooCache.getInstance().getServerMap().removeAsync(uuid);
                });
            }

            // delay
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
