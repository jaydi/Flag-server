package com.flag.engine.apis;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Provider;
import com.flag.engine.models.ProviderForm;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Providers {
	private static final Logger log = Logger.getLogger(Providers.class.getName());

	@ApiMethod(name = "providers.insert", path = "new_provider", httpMethod = "post")
	public Provider insert(ProviderForm providerForm) {
		log.info("new provider: " + providerForm.getEmail());
		
		Provider provider = new Provider(providerForm);
		PersistenceManager pm = PMF.getPersistenceManager();
		pm.makePersistent(provider);
		pm.close();

		return new Provider(provider);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "providers.get", path = "old_provider", httpMethod = "post")
	public Provider retain(ProviderForm providerForm) {
		PersistenceManager pm = PMF.getPersistenceManager();

		Query query = pm.newQuery(Provider.class);
		query.setFilter("email == theEmail && password == thePassword");
		query.declareParameters("String theEmail, String thePassword");
		List<Provider> providers = (List<Provider>) pm.newQuery(query).execute(providerForm.getEmail(), providerForm.getPassword());

		if (providers.isEmpty())
			return null;
		else
			return providers.get(0);
	}
}
