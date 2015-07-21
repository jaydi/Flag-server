package com.flag.engine.comparators;

import java.util.Comparator;
import java.util.Map;

public class ItemIdRelationComparator implements Comparator<Long> {
	private Map<Long, Integer> countMap;

	public ItemIdRelationComparator(Map<Long, Integer> countMap) {
		super();
		this.countMap = countMap;
	}

	@Override
	public int compare(Long id1, Long id2) {
		if (countMap.get(id1) > countMap.get(id2))
			return -1;
		else if (countMap.get(id1) < countMap.get(id2))
			return 1;
		else
			return 0;
	}

}
