package com.flag.engine.comparators;

import java.util.Comparator;

import com.flag.engine.models.Shop;

public class ShopRewardComparator implements Comparator<Shop> {

	@Override
	public int compare(Shop s1, Shop s2) {
		int r1 = 0;
		if (!s1.isRewarded())
			r1 = s1.getReward();
		int r2 = 0;
		if (!s2.isRewarded())
			r2 = s2.getReward();

		if (r1 > r2)
			return -1;
		else if (r1 < r2)
			return 1;
		else
			return 0;
	}

}
