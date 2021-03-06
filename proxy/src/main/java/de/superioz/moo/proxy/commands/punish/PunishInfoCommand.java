package de.superioz.moo.proxy.commands.punish;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RunAsynchronous
public class PunishInfoCommand {

    private static final String LABEL = "punishinfo";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {

    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = LABEL, usage = "<player>")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        PlayerProfile playerInfo = args.get(0, PlayerProfile.class);
        context.invalidArgument(playerInfo == null, LanguageManager.get("error-player-doesnt-exist", args.get(0)));

        // list current ban/mute
        Ban currentBan = playerInfo.getCurrentBan();

        // if he is not banned and muted
        if(currentBan == null) {
            context.sendMessage(LanguageManager.get("punishment-player-is-nothing", playerInfo.getName()));
            return;
        }
        context.sendMessage(LanguageManager.get("punishment-header", playerInfo.getName()));

        // if the current ban is not null list the executor and send info
        // otherwise send (not-banned)
        /*if(currentBan != null) {
            String banExecutorName = CommandContext.CONSOLE_NAME;
            PlayerData banExecutor = MooQueries.getInstance().getPlayerData(currentBan.getBy());
            if(banExecutor != null) banExecutorName = banExecutor.getLastName();

            // values for formatting
            long current = System.currentTimeMillis();
            String start = TimeUtil.getFormat(currentBan.getStart());
            BanCategory subType = currentBan.getSubType();
            String typeColor = subType.getBanType() == BanType.GLOBAL ? "&c" : "&9";
            String end = TimeUtil.getFormat(current + currentBan.getDuration());

            context.sendMessage(LanguageManager.get("punishment-ban-info",
                    start, typeColor + subType.getName(),
                    "Details",
                    start,
                    end,
                    typeColor + currentBan.getReason(),
                    Moo.getInstance().getColoredName(banExecutor == null ? null : banExecutor.getUuid()) + banExecutorName));
        }
        else {
            context.sendMessage(LanguageManager.get("punishment-player-isnt-banned", playerInfo.getName()));
        }*/
    }

}
