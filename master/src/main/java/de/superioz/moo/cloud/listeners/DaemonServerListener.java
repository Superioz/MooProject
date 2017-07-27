package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.*;

import java.net.InetSocketAddress;

public class DaemonServerListener implements PacketAdapter {

    @PacketHandler
    public void onServerAttempt(PacketServerAttempt packet) {
        Cloud.getLogger().debug("Daemon attempted to " + packet.type.name() + " a server. [" + packet.id + "]");
    }

    @PacketHandler
    public void onServerDone(PacketServerDone packet) {
        String ip = packet.getAddress() + "[:" + packet.port + "]";
        PacketMessenger.message(packet, ClientType.PROXY);

        // daemon started a server
        boolean startedServer = packet.doneType == PacketServerDone.Type.START;
        Reaction.react(startedServer, () -> {
            Cloud.getLogger().debug("Daemon successfully started server " + ip + " with type '" + packet.type + "'");

            PacketMessenger.message(new PacketServerRegister(packet.type, packet.getAddress().getHostName(), packet.port), ClientType.PROXY);
            Cloud.getInstance().getMooProxy().getSpigotServers().put(packet.uuid, new MooServer(packet.uuid, packet.getAddress(), packet.type));
        });

        // daemon stopped a server
        Reaction.react(!startedServer, () -> {
            Cloud.getLogger().debug("Daemon has stopped server " + ip + " with type '" + packet.type + "'");

            PacketMessenger.message(new PacketServerUnregister(packet.port), ClientType.PROXY);
            Cloud.getInstance().getMooProxy().getSpigotServers().remove(packet.uuid);
        });
    }

    @PacketHandler
    public void onRamUsage(PacketRamUsage packet) {
        Cloud.getLogger().debug("Updates ram usage for " + packet.getChannel().remoteAddress() + " (to " + packet.ramUsage + "%)");

        Cloud.getInstance().getHub().updateRamUsage((InetSocketAddress) packet.getChannel().remoteAddress(), packet.ramUsage);
    }

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet) {
        Cloud.getInstance().getMooProxy().requestServer(packet.type, packet.autoSave, packet.amount, abstractPacket -> packet.respond(abstractPacket));
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        Cloud.getInstance().getMooProxy().requestServerShutdown(packet.host, packet.port, abstractPacket -> packet.respond(abstractPacket));
    }

}
