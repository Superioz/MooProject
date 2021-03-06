package de.superioz.moo.network.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;

import java.io.IOException;

/**
 * Packet to request one (or multiple) daemon sub-server to be started
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerRequest extends AbstractPacket {

    /**
     * The type of the server
     */
    public String type;

    /**
     * Does the server autoSave? Otherwise everything will be resetted
     */
    public boolean autoSave;

    /**
     * The amount of servers to be started
     */
    public int amount;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readString();
        this.autoSave = buf.readBoolean();
        this.amount = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(type);
        buf.writeBoolean(autoSave);
        buf.writeVarInt(amount);
    }

}
