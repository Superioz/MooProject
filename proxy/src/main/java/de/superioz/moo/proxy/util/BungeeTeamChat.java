package de.superioz.moo.proxy.util;

import de.superioz.moo.network.common.MooCache;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.client.Moo;
import de.superioz.moo.minecraft.chat.TeamChat;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.ResponseStatus;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeTeamChat extends TeamChat<CommandSender, ResponseStatus> {

    private static BungeeTeamChat instance;

    public static synchronized BungeeTeamChat getInstance() {
        if(instance == null) {
            instance = new BungeeTeamChat();
        }
        return instance;
    }

    @Override
    public ResponseStatus send(String formattedMessage, boolean colored, boolean formatted) {
        Integer rank = MooCache.getInstance().getConfigEntry(NetworkConfigType.TEAM_RANK);
        if(rank == null) return ResponseStatus.BAD_REQUEST;
        return Moo.getInstance().broadcast(formattedMessage, rank, colored, formatted);
    }

    @Override
    public boolean canTeamchat(CommandSender commandSender) {
        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            Group group = MooQueries.getInstance().getGroup(player.getUniqueId());

            return group != null
                    && group.getRank() >= (Integer) MooCache.getInstance().getConfigEntry(NetworkConfigType.TEAM_RANK);
        }
        return false;
    }

    @Override
    public String getColoredName(CommandSender sender) {
        if(!(sender instanceof ProxiedPlayer)) {
            return getColor(null) + CommandContext.CONSOLE_NAME;
        }
        else {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            return getColor(player.getUniqueId()) + player.getDisplayName();
        }
    }

    @Override
    public String getColor(UUID uuid) {
        if(uuid == null) {
            return "&4";
        }
        else {
            Group group = MooQueries.getInstance().getGroup(uuid);
            if(group == null) return "&f";
            return group.getColor();
        }
    }

}
