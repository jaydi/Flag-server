package com.flag.engine.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.Shop;

public class GCMUtils {
	private static final String URL = "https://android.googleapis.com/gcm/send";

	private static final String TYPE_SHOP = "shop_type";

	public static int sendJsonMessage(String jsonMsg) {
		try {
			URL url = new URL(URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			con.setRequestProperty("Authorization", "key=" + Constants.SERVER_KEY);
			con.setRequestProperty("Content-Type", "application/json");

			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(jsonMsg);
			writer.close();

			return con.getResponseCode();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return 901;
		} catch (IOException e) {
			e.printStackTrace();
			return 902;
		}
	}

	public static int sendGCMMessage(GCMMessage msg) {
		return sendJsonMessage(JsonCodec.encode(msg));
	}

	public static int sendShop(List<String> recipients, Shop shop) {
		GCMMessage msg = new GCMMessage();
		GCMData data = new GCMData();

		data.setType(TYPE_SHOP);
		data.setEntity(JsonCodec.encode(shop));

		msg.setRegistration_ids(recipients);
		msg.setData(data);

		return sendGCMMessage(msg);
	}

}
