package de.superioz.moo.network.common;

import de.superioz.moo.network.client.ClientType;
import de.superioz.moo.network.events.PacketQueueEvent;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packets.PacketRespond;
import de.superioz.moo.network.queries.Response;
import de.superioz.moo.network.queries.ResponseScope;
import de.superioz.moo.network.client.ClientManager;
import de.superioz.moo.network.client.MooClient;
import de.superioz.moo.network.util.NettyUtil;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.util.LazySupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * The packetMessenger is for sending {@link AbstractPacket}'s to a target
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PacketMessenger {

    /**
     * The class of the response scope (e.g. {@link PacketRespond})
     */
    private Class<? extends AbstractPacket> responseScopeClass = AbstractPacket.class;

    /**
     * The responseScope enum to override {@link #responseScopeClass}
     */
    private ResponseScope responseScope;

    /**
     * The targets to send the packet to
     */
    private List<Channel> target = new ArrayList<>();

    /**
     * Sync packet sending (recommended is async)
     */
    private boolean sync = false;

    /**
     * Simply sends packet without awaiting a response.
     *
     * @param packet   The packet to be sent
     * @param channels The channels to the packet be sent to
     * @see #transfer(AbstractPacket, Class, Consumer[])
     */
    public static <P extends AbstractPacket> void message(P packet, Channel... channels) {
        PacketMessenger.create().target(channels).send(packet);
    }

    public static <P extends AbstractPacket> void message(P packet, MooClient... clients) {
        PacketMessenger.create().target(clients).send(packet);
    }

    public static <P extends AbstractPacket> void message(P packet, List<MooClient> clients) {
        PacketMessenger.create().target(clients.toArray(new MooClient[]{})).send(packet);
    }

    public static <P extends AbstractPacket> void message(P packet, ClientType... types) {
        PacketMessenger.create().target(types).send(packet);
    }

    public static <P extends AbstractPacket> void message(P packet) {
        PacketMessenger.create().send(packet);
    }

    /**
     * Similar to {@link #message(AbstractPacket, Channel...)} but with awaiting a response
     *  <b>ASYNC METHOD</b>
     *
     * @param packet   The packet to be sent
     * @param consumer The consumer if a response comes back
     * @param channels The channels to be receiving the packets
     */
    public static <P extends AbstractPacket> void message(P packet, Consumer<Response> consumer, Channel... channels) {
        PacketMessenger.create().target(channels).send(packet, consumer);
    }

    public static <P extends AbstractPacket> void message(P packet, Consumer<Response> consumer, MooClient... clients) {
        PacketMessenger.create().target(clients).send(packet, consumer);
    }

    public static <P extends AbstractPacket> void message(P packet, Consumer<Response> consumer, List<MooClient> clients) {
        PacketMessenger.create().target(clients.toArray(new MooClient[]{})).send(packet, consumer);
    }

    public static <P extends AbstractPacket> void message(P packet, Consumer<Response> consumer, ClientType... types) {
        PacketMessenger.create().target(types).send(packet, consumer);
    }

    public static <P extends AbstractPacket> void message(P packet, Consumer<Response> consumer) {
        PacketMessenger.create().send(packet, consumer);
    }

    /**
     * Sends a packet to the currently connected client with awaiting a response. Uses this class to achieve that<br>
     * Similar to {@link #transfer(AbstractPacket, ResponseScope)} but with consumer.<br>
     *     <b>ASYNC METHOD</b>
     *
     * @param packet             The packet
     * @param responseScopeClass The scope class
     * @param <R>                The type
     */
    public static <R, P extends AbstractPacket> void transfer(P packet, Class<? extends AbstractPacket> responseScopeClass, Consumer<R>... consumer) {
        PacketMessenger.create().responseScope(responseScopeClass).send(packet, consumer);
    }

    public static <R, P extends AbstractPacket> void transfer(P packet, ResponseScope scope, Consumer<R>... consumer) {
        PacketMessenger.create().responseScope(scope).send(packet, consumer);
    }

    public static <R, P extends AbstractPacket> void transfer(P packet, Consumer<R>... consumer) {
        PacketMessenger.create().send(packet, consumer);
    }

    public static <P extends AbstractPacket> void transferToResponse(P packet, Consumer<Response>... consumer) {
        PacketMessenger.create().responseScope(ResponseScope.RESPONSE).send(packet, consumer);
    }

    /**
     * Sends a packet to the currently connected client with awaiting a response. Uses this class to achieve that<br>
     *     <b>SYNC METHOD</b>
     *
     * @param packet             The packet
     * @param responseScopeClass The scope class
     * @param <R>                The type
     * @return The respond
     */
    public static <R> R transfer(AbstractPacket packet, Class<? extends AbstractPacket> responseScopeClass) {
        return PacketMessenger.create().responseScope(responseScopeClass).sync().send(packet);
    }

    public static <R> R transfer(AbstractPacket packet, ResponseScope scope) {
        return PacketMessenger.create().responseScope(scope).sync().send(packet);
    }

    public static <R> R transfer(AbstractPacket packet) {
        return PacketMessenger.create().sync().send(packet);
    }

    public static Response transferToResponse(AbstractPacket packet) {
        return PacketMessenger.create().responseScope(ResponseScope.RESPONSE).sync().send(packet);
    }

    /**
     * Creates a new PacketMessenger object
     * This is the method to initialize the object
     * IMPORTANT.
     *
     * @return The object
     */
    public static PacketMessenger create() {
        return new PacketMessenger();
    }

    /**
     * Sets the response scope that means which type should be returned as response
     *
     * @param scope The scope as enum
     * @return This
     */
    public PacketMessenger responseScope(ResponseScope scope) {
        this.responseScope = scope;
        if(responseScope.getWrappedClass() != null) {
            this.responseScopeClass = scope.getWrappedClass();
        }
        return this;
    }

    public PacketMessenger responseScope(Class<? extends AbstractPacket> clazz) {
        this.responseScopeClass = clazz;
        return this;
    }

    /**
     * Sets the target of this packet to send to
     *
     * @param channels The channels
     * @return This
     */
    public PacketMessenger target(Channel... channels) {
        this.target.addAll(Arrays.asList(channels));
        return this;
    }

    public PacketMessenger target(MooClient... clients) {
        List<Channel> channels = new ArrayList<>();
        for(MooClient client : clients) {
            channels.add(client.getChannel());
        }
        return target(channels.toArray(new Channel[channels.size()]));
    }

    public PacketMessenger target(ClientType... types) {
        ClientManager hub = ClientManager.getInstance();
        if(hub == null) return this;

        List<MooClient> clients = new ArrayList<>();
        for(ClientType type : types) {
            clients.addAll(hub.getClients(type));
        }
        return target(clients.toArray(new MooClient[]{}));
    }

    /**
     * The packet will be sent sync (means that the {@link #send(AbstractPacket, Consumer[])} method will return something,
     * no need for a consumer here
     *
     * @return This
     */
    public PacketMessenger sync() {
        sync = !sync;
        return this;
    }

    /**
     * Converts {@code abstractPacket} to a response for given consumers
     *
     * @param abstractPacket The abstractPacket (response from smth else)
     * @param consumers      The consumers
     */
    private void convertResponse(AbstractPacket abstractPacket, LazySupplier supplier, Consumer... consumers) {
        if(consumers == null) consumers = new Consumer[0];
        if(responseScope != null) {
            if(responseScope == ResponseScope.DEFAULT) {
                for(Consumer c : consumers) {
                    c.accept(abstractPacket);
                }
                return;
            }

            // response needs to be PacketRespond
            if(responseScope == ResponseScope.RESPONSE || responseScope == ResponseScope.RESPOND) {
                boolean b = ResponseScope.RESPOND.getWrappedClass().isAssignableFrom(abstractPacket.getClass());

                if(responseScope == ResponseScope.RESPONSE) {
                    Response response = new Response((PacketRespond) abstractPacket);

                    for(Consumer c : consumers) {
                        if(b) c.accept(response);
                        else c.accept(null);
                    }

                    if(supplier != null) {
                        supplier.accept(response);
                    }
                }
                else {
                    for(Consumer c : consumers) {
                        if(b) c.accept(abstractPacket);
                        else c.accept(null);
                    }
                }
            }
        }
        else {
            boolean b = responseScopeClass == null || responseScopeClass.isAssignableFrom(abstractPacket.getClass());

            // accept the packet if the scope is correct
            try {
                for(Consumer c : consumers) {
                    if(b) c.accept(abstractPacket);
                    else c.accept(null);
                }
            }
            catch(ClassCastException ex) {
                // consumer wanted to accept something different
                // trying response type if abstractPacket is packetRespond
                if(!PacketRespond.class.isAssignableFrom(abstractPacket.getClass())) {
                    return;
                }
                Response response = new Response((PacketRespond) abstractPacket);

                try {
                    for(Consumer c : consumers) {
                        if(b) c.accept(response);
                        else c.accept(null);
                    }
                }
                catch(ClassCastException ex2) {
                    // givin' up ..
                }
            }
        }
    }

    /**
     * Sends the packet with inherited values. That means that all values passed to this class
     * before will be used to send the packet. Methods to pass down the values:<br>
     * {@link #responseScope(ResponseScope)}, {@link #target(Channel...)} and {@link #sync()}<br>
     * If you want to run the packet processing sync you have to use {@link #sync()} and you have to ignore {@code consumers} as well and just let
     * the method be at it is. If you want to do the other way 'round just don't use {@link #sync()} (either way no consumers needed).
     *
     * @param packet    The packet
     * @param consumers The consumers
     * @param <R>       The type of response
     * @return The response or null
     */
    public synchronized <R> R send(AbstractPacket packet, Consumer<R>... consumers) {
        // automatically copying packet for forwarding
        if(packet.getStamp() != -1) {
            packet = packet.deepCopy();
        }

        boolean sync = isSync() && consumers.length == 0;
        if(sync) {
            NettyUtil.checkAsyncTask();
        }

        // sync handling
        LazySupplier<R> supplier = new LazySupplier<>();
        Consumer<AbstractPacket> packetConsumer = abstractPacket -> {
            try {
                convertResponse(abstractPacket, sync ? supplier : null, consumers);
            }
            catch(Exception e) {
                // error while converting response
                System.err.println("Error while receiving response inside PacketMessenger: ");
                e.printStackTrace();
            }

            if(sync && supplier.isEmpty()) {
                try {
                    supplier.accept((R) abstractPacket);
                }
                catch(Exception e) {
                    // error while casting packet to R
                    supplier.accept(null);
                }
            }
        };

        if(target.size() == 0) {
            EventExecutor.getInstance().execute(new PacketQueueEvent(null, packet, packetConsumer));
        }
        else {
            for(Channel channel : target) {
                EventExecutor.getInstance().execute(new PacketQueueEvent(channel, packet, packetConsumer));
            }
        }

        if(sync) {
            return supplier.get();
        }
        return null;
    }

}
