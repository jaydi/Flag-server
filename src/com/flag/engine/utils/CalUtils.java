package com.flag.engine.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CalUtils {
	public static float toNumber(String str) {
		StringBuilder sb = new StringBuilder();
		int dotCount = 0;

		if (str.length() == 0)
			return 0;

		for (int i = 0; i < str.length(); i++)
			try {
				int n = Integer.valueOf(str.substring(i, i + 1));
				sb.append(n);
			} catch (NumberFormatException e) {
				if (str.charAt(i) == '.' && dotCount == 0) {
					sb.append('.');
					dotCount++;
				} else if (str.charAt(i) == '.' && dotCount > 0)
					return 0;
			}

		if (sb.length() == 0)
			return 0;

		if (sb.charAt(0) == '.')
			sb.insert(0, '0');

		if (sb.charAt(sb.length() - 1) == '.')
			sb.append(0);

		return Float.valueOf(sb.toString());
	}

	public static int discountRate(String oldPrice, String price) {
		float a = toNumber(oldPrice);
		float b = toNumber(price);
		return (int) (((a - b) / a) * 100);
	}

	public static String currencyFormat(String price) {
		int p = (int) toNumber(price);
		DecimalFormat format = new DecimalFormat("###,###,###,###");
		String priceString = format.format(p) + "원";
		
		return priceString;
	}

	public static List<Integer> randomIndexes(int size, int get) {
		List<Integer> picks = new ArrayList<Integer>();

		if (size <= get)
			for (int i = 0; i < size; i++)
				picks.add(i);
		else {
			Random random = new Random();
			if (size > 0)
				for (int i = 0; i < get; i++) {
					int pick = random.nextInt(size);
					if (!picks.contains(pick))
						picks.add(pick);
					else
						i--;
				}
		}

		return picks;
	}
}
