package com.flag.engine.apis;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.PMF;
import com.flag.engine.models.RetainForm;
import com.flag.engine.models.User;
import com.flag.engine.models.UserForm;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Users {
	private static final Logger log = Logger.getLogger(Users.class.getName());

	@ApiMethod(name = "users.guest", path = "guest_user", httpMethod = "post")
	public User guest() {
		log.info("guest user");

		PersistenceManager pm = PMF.getPersistenceManager();

		User user = new User();
		pm.makePersistent(user);

		return new User(user);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "users.insert", path = "new_user", httpMethod = "post")
	public User insert(UserForm userForm) {
		log.warning("new user: " + userForm.toString());
		PersistenceManager pm = PMF.getPersistenceManager();
		User user = null;

		Query query = pm.newQuery(User.class);
		query.setFilter("email == theEmail");
		query.declareParameters("String theEmail");
		List<User> users = (List<User>) pm.newQuery(query).execute(userForm.getEmail());

		if (users.size() > 0)
			return null;

		try {
			user = pm.getObjectById(User.class, userForm.getId());
			user.setEmail(userForm.getEmail());
			user.setPassword(userForm.getPassword());
		} catch (JDOObjectNotFoundException e) {
			user = new User(userForm);
			pm.makePersistent(user);
		} finally {
			pm.close();
		}

		return new User(user);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "users.get", path = "old_user", httpMethod = "post")
	public User get(UserForm userForm) {
		log.info("old user: " + userForm.toString());

		PersistenceManager pm = PMF.getPersistenceManager();

		Query query = pm.newQuery(User.class);
		query.setFilter("email == theEmail && password == thePassword");
		query.declareParameters("String theEmail, String thePassword");
		List<User> users = (List<User>) pm.newQuery(query).execute(userForm.getEmail(), userForm.getPassword());

		User user = null;
		if (!users.isEmpty())
			user = new User(users.get(0));

		try {
			if (userForm.getId() != 0) {
				User target = pm.getObjectById(User.class, userForm.getId());
				pm.deletePersistent(target);
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}

		return user;
	}

	@ApiMethod(name = "users.retain", path = "retain_user", httpMethod = "post")
	public User retain(RetainForm retainForm) {
		log.info("retain user: " + retainForm.toString());

		PersistenceManager pm = PMF.getPersistenceManager();

		try {
			User user = pm.getObjectById(User.class, retainForm.getId());
			return new User(user);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}
	}

	@ApiMethod(name = "users.delete", path = "user", httpMethod = "delete")
	public void delete(@Named("userId") long userId) {
		PersistenceManager pm = PMF.getPersistenceManager();

		try {
			User user = pm.getObjectById(User.class, userId);
			pm.deletePersistent(user);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
	}
}
