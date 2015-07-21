package com.flag.engine.apis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.PMF;
import com.flag.engine.models.UserInfo;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class UserInfos {
	private static final Logger log = Logger.getLogger(UserInfos.class.getName());

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "userinfos.phone.test", path = "user_info_phone_test", httpMethod = "post")
	public UserInfo phoneTest(UserInfo userInfo) {
		// check if there is another user using same phone number
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Query query = pm.newQuery(UserInfo.class);
		query.setFilter("phone == thePhone");
		query.declareParameters("String thePhone");
		List<UserInfo> userInfos = (List<UserInfo>) pm.newQuery(query).execute(userInfo.getPhone());

		// phone number already taken
		if (!userInfos.isEmpty())
			return userInfo;

		try {
			String verNumber = getVerNumberString();
			sendVerNumber(userInfo.getPhone(), verNumber);

			// save verification code and phone number in verCode field together
			userInfo.setVerificationCode(verNumber + userInfo.getPhone());
			userInfo.setPhone("");
			update(userInfo);

			// erase verCode field before send back to the user
			userInfo.setVerificationCode("");
		} catch (TwilioRestException e) { // failed to send code message
		}

		return userInfo;
	}

	private String getVerNumberString() {
		String randomNumber = String.valueOf(Math.abs(new Random().nextInt()));
		if (randomNumber.length() > 5)
			return randomNumber.substring(0, 5);
		else {
			String verNumber = randomNumber;
			for (int i = 0; i < 5 - randomNumber.length(); i++)
				verNumber = verNumber + "0";
			return verNumber;
		}
	}

	private void sendVerNumber(String phone, String verNumber) throws TwilioRestException {
		TwilioRestClient client = new TwilioRestClient(Constants.TWILLIO_ACCOUNT_SID, Constants.TWILLIO_AUTH_TOKEN);

		// Build the parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("To", phone));
		params.add(new BasicNameValuePair("From", "+16147636291"));
		params.add(new BasicNameValuePair("Body", "달샵 가입자 확인번호 : " + verNumber));

		MessageFactory messageFactory = client.getAccount().getMessageFactory();
		Message message = messageFactory.create(params);
		log.warning("sent phone verification number, sid: " + message.getSid());
	}

	@ApiMethod(name = "userinfos.phone.insert", path = "user_info_phone_insert", httpMethod = "post")
	public UserInfo phoneInsert(UserInfo userInfo) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		UserInfo target = null;

		try {
			target = pm.getObjectById(UserInfo.class, userInfo.getUserId());

			if (target.verifyCode(userInfo.getPhone())) {
				target.setPhone(target.getVerificationCode().substring(target.getVerificationCode().indexOf("+")));
				target.setVerificationCode("");
			} else
				return null;
		} catch (JDOObjectNotFoundException e) {
			return null;
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		} finally {
			pm.close();
		}

		return target;
	}

	@ApiMethod(name = "userinfos.get", path = "user_info", httpMethod = "get")
	public UserInfo get(@Nullable @Named("userId") long userId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		UserInfo userInfo = null;

		try {
			userInfo = pm.getObjectById(UserInfo.class, userId);
		} catch (JDOObjectNotFoundException e) {
		}

		return userInfo;
	}

	@ApiMethod(name = "userInfos.update", path = "user_info", httpMethod = "put")
	public UserInfo update(UserInfo userInfo) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		UserInfo target = null;

		// test
		if (userInfo.getPushToken() != null) {
			log.warning("user id : " + userInfo.getUserId());
			log.warning("push token : " + userInfo.getPushToken());
		}

		try {
			target = pm.getObjectById(UserInfo.class, userInfo.getUserId());
			target.update(userInfo);
		} catch (JDOObjectNotFoundException e) {
			return target;
		} finally {
			pm.close();
		}

		return target;
	}
}
