package com.flag.engine.apis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.CheckinForm;
import com.flag.engine.models.Flag;
import com.flag.engine.models.InvitationForm;
import com.flag.engine.models.InviteRewardValue;
import com.flag.engine.models.Item;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Provider;
import com.flag.engine.models.Redeem;
import com.flag.engine.models.Redemption;
import com.flag.engine.models.Reward;
import com.flag.engine.models.RewardCollection;
import com.flag.engine.models.Shop;
import com.flag.engine.models.User;
import com.flag.engine.models.UserInfo;
import com.flag.engine.models.UserShopSet;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Rewards {
	private static final Logger log = Logger.getLogger(Rewards.class.getName());

	@ApiMethod(name = "rewards.insert", path = "reward", httpMethod = "post")
	public User insert(Reward reward) {
		log.info("insert reward: " + reward.toString());

		PersistenceManager pm = PMF.getPersistenceManager();
		PersistenceManager pmSQL = PMF.getPersistenceManagerSQL();

		// mark
		reward.setCreatedAt(new Date().getTime());

		try {
			// refresh provider balance
			// dirty branch... delete later
			Provider provider = null;
			if (reward.getType() == Reward.TYPE_SHOP) {
				Shop shop = pmSQL.getObjectById(Shop.class, reward.getTargetId());
				if (shop.getType() == Shop.TYPE_HQ)
					provider = pm.getObjectById(Provider.class, shop.getProviderId());
				else {
					Shop hqShop = pmSQL.getObjectById(Shop.class, shop.getParentId());
					provider = pm.getObjectById(Provider.class, hqShop.getProviderId());
				}
			} else if (reward.getType() == Reward.TYPE_ITEM) {
				Item item = pmSQL.getObjectById(Item.class, reward.getTargetId());
				Shop shop = pmSQL.getObjectById(Shop.class, item.getShopId());
				provider = pm.getObjectById(Provider.class, shop.getProviderId());
			} else if (reward.getType() == Reward.TYPE_INVITATION)
				provider = pm.getObjectById(Provider.class, 5101363511951360l);

			if (provider != null)
				provider.setBalance(provider.getBalance() - reward.getReward());

			// refresh user balance
			User user = pm.getObjectById(User.class, reward.getUserId());
			user.rewarded(reward.getReward());

			// save reward
			pmSQL.makePersistent(reward);

			return new User(user);
		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
			pmSQL.close();
		}
	}

	@ApiMethod(name = "rewards.checkin", path = "reward_checkin", httpMethod = "post")
	public UserShopSet checkin(CheckinForm checkinForm) {
		log.warning("user: " + checkinForm.getUserId() + " flag: " + checkinForm.getFlagId());
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		User user = null;
		Shop shop = null;

		try {
			Flag flag = pm.getObjectById(Flag.class, checkinForm.getFlagId());
			shop = pm.getObjectById(Shop.class, flag.getShopId());
			List<Shop> shops = new ArrayList<Shop>();
			shops.add(shop);
			Shop.setRewardVariables(shops, checkinForm.getUserId());

			if (!shops.get(0).isRewarded())
				user = insert(new Reward(checkinForm.getUserId(), (shop.getParentId() != 0 && shop.getType() == Shop.TYPE_BR) ? shop.getParentId()
						: shop.getId(), flag.getShopName(), Reward.TYPE_SHOP, shop.getReward()));

		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
		}

		return new UserShopSet(user, shop);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "rewards.invited", path = "reward_invite", httpMethod = "post")
	public User invited(InvitationForm invitationForm) {
		PersistenceManager pm = PMF.getPersistenceManager();
		PersistenceManager pmSQL = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(User.class);
		query.setFilter("email == recoEmail");
		query.declareParameters("String recoEmail");
		List<User> recoUsers = (List<User>) pm.newQuery(query).execute(invitationForm.getRecoEmail());

		if (recoUsers.size() == 0)
			return null;

		try {
			User user = pm.getObjectById(User.class, invitationForm.getUserId());
			UserInfo userInfo = pmSQL.getObjectById(UserInfo.class, invitationForm.getUserId());

			if (userInfo.getRecoEmail() != null && !userInfo.getRecoEmail().isEmpty())
				return null;

			if (user.getId().equals(recoUsers.get(0).getId()))
				return null;

			insert(new Reward(recoUsers.get(0).getId(), invitationForm.getUserId(), user.getEmail(), Reward.TYPE_INVITATION, Reward.VALUE_INVITATION));
			userInfo.setRecoEmail(invitationForm.getRecoEmail());

			return user;

		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
			pmSQL.close();
		}
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "rewards.list", path = "reward", httpMethod = "get")
	public RewardCollection list(@Nullable @Named("userId") long userId, @Nullable @Named("mark") int mark) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Reward.class);
		query.setFilter("userId == theUserId");
		query.declareParameters("long theUserId");
		query.setOrdering("createdAt desc");
		query.setRange(mark * 30, (mark + 1) * 30);
		List<Reward> rewards = (List<Reward>) pm.newQuery(query).execute(userId);

		return new RewardCollection(rewards);
	}

	@ApiMethod(name = "rewards.redeem", path = "reward_redeem", httpMethod = "post")
	public User redeem(Redemption redemption) {
		PersistenceManager pm = PMF.getPersistenceManager();
		PersistenceManager pmSQL = PMF.getPersistenceManagerSQL();
		User user = null;

		try {
			user = pm.getObjectById(User.class, redemption.getUserId());
			UserInfo userInfo = pmSQL.getObjectById(UserInfo.class, redemption.getUserId());
			Redeem redeem = pmSQL.getObjectById(Redeem.class, redemption.getRedeemId());

			if (user.getReward() < redeem.getPrice())
				return null;

			redemption.setUserPhone(userInfo.getPhone());
			redemption.setRedeemName(redeem.getName());
			redemption.setRedeemPrice(redeem.getPrice());
			redemption.setCreatedAt(new Date().getTime());
			sendRedemption(redemption);

			Reward reward = new Reward();
			reward.setUserId(user.getId());
			reward.setTargetId(redeem.getId());
			reward.setTargetName(redeem.getName());
			reward.setType(Reward.TYPE_REDEMPTION);
			reward.setReward(redeem.getPrice());
			reward.setCreatedAt(new Date().getTime());
			pmSQL.makePersistent(reward);

			user.setReward(user.getReward() - redeem.getPrice());

			sendAdminNotice(redemption);
		} catch (JDOObjectNotFoundException e) {
			return null;
		} catch (TwilioRestException e) {
		} finally {
			pm.close();
			pmSQL.close();
		}

		return user;
	}

	private void sendAdminNotice(Redemption redemption) throws TwilioRestException {
		TwilioRestClient client = new TwilioRestClient(Constants.TWILLIO_ACCOUNT_SID, Constants.TWILLIO_AUTH_TOKEN);

		// Build the parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("To", "+821032227876"));
		params.add(new BasicNameValuePair("From", "+16147636291"));
		params.add(new BasicNameValuePair("Body", "Redemption call : " + redemption.toString()));

		MessageFactory messageFactory = client.getAccount().getMessageFactory();
		messageFactory.create(params);
	}

	private void sendRedemption(Redemption redemption) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(redemption);
		pm.close();
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "rewards.get.invite.value", path = "reward_invite_value", httpMethod = "get")
	public InviteRewardValue getInviteRewardValue() {
		PersistenceManager pm = PMF.getPersistenceManager();
		Query query = pm.newQuery(InviteRewardValue.class);
		List<InviteRewardValue> values = (List<InviteRewardValue>) pm.newQuery(query).execute();

		return values.get(0);
	}
}
