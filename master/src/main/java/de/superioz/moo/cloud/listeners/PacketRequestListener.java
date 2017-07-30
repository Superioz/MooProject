package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketRequest;

import java.util.UUID;

public class PacketRequestListener implements PacketAdapter {

    @PacketHandler
    public void onRequest(PacketRequest packet) {
        PacketRequest.Type type = packet.type;
        String information = packet.meta;

        // I want to know the ping of the player
        Reaction.react(type, PacketRequest.Type.PING, () -> {
            PlayerData player = Validation.UNIQUEID.matches(information) ?
                    Cloud.getInstance().getMooProxy().getPlayer(UUID.fromString(information))
                    : Cloud.getInstance().getMooProxy().getPlayer(information);

            // didn't found the player
            if(player == null) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            // sends a request to fetch the players ping
            PacketMessenger.message(packet, response -> packet.respond(response.getHandle()),
                    Cloud.getInstance().getMooProxy().getClient(player));
        });
    }

}
