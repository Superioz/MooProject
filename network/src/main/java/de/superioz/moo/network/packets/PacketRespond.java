package de.superioz.moo.network.packets;

import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.AbstractPacket;
import lombok.NoArgsConstructor;
import de.superioz.moo.network.packet.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This packet is for respond to anything. Can be used as response to every packet (Not only {@link PacketRequest})
 */
@NoArgsConstructor
public class PacketRespond extends AbstractPacket {

    public static final String MODIFICATION_PREFIX = "mod-";

    /**
     * The header of the response (like "playerInfo")
     */
    public String header;

    /**
     * The message of the respond (can be a list of playerData or just plain messages)
     */
    public List<String> message;

    /**
     * The status of the response (similar to http)
     */
    public ResponseStatus status;

    public PacketRespond(String header, List<String> message, ResponseStatus status) {
        this.header = header;
        this.message = message;
        this.status = status;
    }

    public PacketRespond(String header, String message, ResponseStatus status) {
        this(header, Arrays.asList(message), status);
    }

    public PacketRespond(boolean state) {
        this("", new ArrayList<>(), state ? ResponseStatus.OK : ResponseStatus.NOK);
    }

    public PacketRespond(ResponseStatus status) {
        this("", new ArrayList<>(), status);
    }

    /**
     * Gets the message of the response (either an empty string or the first entry of the messages list)
     *
     * @return The message as string
     */
    public String getMessage() {
        if(message.isEmpty()) return "";
        return message.get(0);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.header = buf.readString();
        this.message = buf.readStringList();
        this.status = buf.readEnumValue(ResponseStatus.class);
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(header);
        buf.writeStringList(message);
        buf.writeEnumValue(status);
    }

}
