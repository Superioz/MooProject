package de.superioz.moo.proxy.commands.punish;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunAsynchronous
public class PunishArchiveCommand {

    private static final String LABEL = "punisharchive";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {

    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = LABEL, usage = "<player> [page]", flags = "l")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        String playerName = args.get(0);
        context.invalidArgument(!Validation.PLAYERNAME.matches(playerName), LanguageManager.get("error-invalid-player-name", playerName));

        // if live fetching is enabled don't cache the list
        boolean liveFetching = args.hasFlag("l");

        // list the ban archive of the given player
        // if not banned display error message
        List<Ban> banArchiveList = null;
        if(!liveFetching) {
            banArchiveList = context.get(playerName);
        }
        if(banArchiveList == null) {
            banArchiveList = MooQueries.getInstance().getBanArchive(playerName);

            if(!liveFetching) {
                context.setExpireAfterCreation(playerName, banArchiveList, 30, TimeUnit.SECONDS);
            }
        }
        context.invalidArgument(banArchiveList.isEmpty(), LanguageManager.get("punishment-archive-list-empty"));

        // display archive at given page
        PageableList<Ban> pageableList = new PageableList<>(banArchiveList);

        // list page
        int page = args.getInt(0, 0);

        // sends the list
        /*context.sendDisplayFormat(new PageableListFormat<Ban>(pageableList)
                .page(page).header("punishment-archive-list-header").emptyList("punishment-archive-list-empty")
                .doesntExist("error-page-doesnt-exist")
                .emptyEntry("punishment-archive-list-entry-empty").entryFormat("punishment-archive-list-entry")
                .entry(replacor -> {
                    Ban ban = replacor.get();

                    String banExecutorName = CommandContext.CONSOLE_NAME;
                    PlayerData banExecutor = MooQueries.getInstance().getPlayerData(ban.getBy());
                    if(banExecutor != null) banExecutorName = banExecutor.getLastName();

                    String typeColor = ban.getSubType().getBanType() == BanType.GLOBAL ? "&c" : "&9";
                    String start = TimeUtil.getFormat(ban.getStart());
                    String end = TimeUtil.getFormat(ban.until());

                    replacor.accept(start, end, typeColor + ban.getReason(),
                            Arrays.asList("Details", start, end, typeColor + ban.getReason(),
                                    BungeeTeamChat.getInstance().getColor(banExecutor == null ? null : banExecutor.getUuid()) + banExecutorName));
                })
                .footer("punishment-archive-next-page", "/punisharchive " + playerName + " " + (page + 1))
        );*/
    }

}
