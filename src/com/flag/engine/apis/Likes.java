package com.flag.engine.apis;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.Like;
import com.flag.engine.models.PMF;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Likes {
	private static final Logger log = Logger.getLogger(Likes.class.getName());

	@ApiMethod(name = "likes.insert", path = "like", httpMethod = "post")
	public Like insert(Like like) {
		log.info("insert like: " + like.getUserId() + "/" + like.getTargetId() + "/" + like.getType());

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(like);
		pm.close();

		return like;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "likes.delete", path = "like", httpMethod = "delete")
	public void delete(@Nullable @Named("userId") Long userId, @Nullable @Named("itemId") Long itemId, @Nullable @Named("type") int type) {
		log.info("delete like: " + userId + "/" + itemId);

		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		try {
			Query query = pm.newQuery(Like.class);
			query.setFilter("userId == theUserId && targetId == itemId && type == itemType");
			query.declareParameters("long theUserId, long itemId, int itemType");
			List<Like> targets = (List<Like>) pm.newQuery(query).execute(userId, itemId, type);

			pm.deletePersistentAll(targets);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
	}
}
