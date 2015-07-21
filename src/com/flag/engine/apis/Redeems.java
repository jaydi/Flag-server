package com.flag.engine.apis;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Redeem;
import com.flag.engine.models.RedeemCollection;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Redeems {

	@ApiMethod(name = "redeems.insert", path = "redeem", httpMethod = "post")
	public Redeem insert(Redeem redeem) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(redeem);
		pm.close();
		
		return redeem;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "redeems.list", path = "redeem", httpMethod = "get")
	public RedeemCollection list(@Nullable @Named("mark") int mark) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Redeem.class);
		List<Redeem> redeems = (List<Redeem>) pm.newQuery(query).execute();

		return new RedeemCollection(redeems);
	}
}
