package com.flag.engine.comparators;

import java.util.Comparator;

import com.flag.engine.models.Flag;
import com.flag.engine.utils.LocationUtils;

public class FlagDistanceComparator implements Comparator<Flag> {
	private double lat;
	private double lon;
	
	public FlagDistanceComparator(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public int compare(Flag lhs, Flag rhs) {
		if (lat == 0 || lon == 0)
			return 0;

		double distanceL = LocationUtils.metricDistance(lat, lon, lhs.getLat(), lhs.getLon());
		double distanceR = LocationUtils.metricDistance(lat, lon, rhs.getLat(), rhs.getLon());

		lhs.setDistance(distanceL);
		rhs.setDistance(distanceR);

		return (distanceL < distanceR) ? -1 : 0;
	}

}
