package com.flag.engine.apis;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.ItemViewPair;
import com.flag.engine.models.Log;
import com.flag.engine.models.PMF;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Logs {
	@ApiMethod(name = "logs.insert", path = "log", httpMethod = "post")
	public Log insertLog(Log log) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(log);
		pm.close();

		return log;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "logs.get", path = "log", httpMethod = "get")
	public List<Log> getLogs(@Nullable @Named("start") long start, @Nullable @Named("end") long end) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Log.class);
		query.setFilter("createdAt > start && createdAt < end");
		query.declareParameters("long start, long end");
		List<Log> logs = (List<Log>) pm.newQuery(query).execute(start, end);

		return logs;
	}

	@ApiMethod(name = "logs.insert.ivpair", path = "log_ivpair", httpMethod = "post")
	public ItemViewPair insertItemViewPair(ItemViewPair pair) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(pair);
		pm.close();

		return pair;
	}

}
