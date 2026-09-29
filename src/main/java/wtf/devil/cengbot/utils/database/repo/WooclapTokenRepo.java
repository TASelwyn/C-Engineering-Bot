package wtf.devil.cengbot.utils.database.repo;

import wtf.devil.cengbot.utils.database.model.WooclapToken;

// No logging here so tokens never end up in the logs
public class WooclapTokenRepo extends CrudRepo<WooclapToken, Long> {

    public WooclapTokenRepo() {
        super(WooclapToken.class);
    }
}
