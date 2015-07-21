package com.flag.engine.comparators;

import java.util.Comparator;
import java.util.Map;

import com.flag.engine.models.Shop;

public class ShopPointComparator implements Comparator<Shop> {
	private Map<Long, Integer> pointMap;

	public ShopPointComparator(Map<Long, Integer> pointMap) {
		super();
		this.pointMap = pointMap;
	}

	@Override
	public int compare(Shop s1, Shop s2) {
		int p1 = 0;
		long sid1 = (s1.getType() == Shop.TYPE_BR) ? s1.getParentId() : s1.getId();
		if (pointMap.get(sid1) != null)
			p1 = pointMap.get(sid1);
		int p2 = 0;
		long sid2 = (s2.getType() == Shop.TYPE_BR) ? s2.getParentId() : s2.getId();
		if (pointMap.get(sid2) != null)
			p2 = pointMap.get(sid2);

		if (p1 > p2)
			return -1;
		else if (p1 < p2)
			return 1;
		else
			return 0;
	}

}
