package de.superioz.moo.cloud.database.cache;

import de.superioz.moo.api.database.DatabaseCache;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.cloud.Cloud;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GroupCache extends DatabaseCache<String, Group> {

    public GroupCache(Builder builder) {
        super(builder);
    }

    @Override
    public void load() {
        Cloud.getInstance().getLogger().debug("Loading groups from database ..");
        getDatabaseCollection().fetch(null, -1).forEach((Consumer<Document>) document -> {
            Group group = getDatabaseCollection().convert(document);
            insert(group.getName(), group);
        });

        List<String> l = new ArrayList<>();
        queryLooped(null).forEach(group -> l.add(group.getName()));
        Cloud.getInstance().getLogger().debug("Found groups(" + size() + "): " + l);
    }

}
