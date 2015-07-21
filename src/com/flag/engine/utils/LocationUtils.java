package com.flag.engine.utils;

public class LocationUtils {
	public static final double NEAR_DISTANCE_DEGREE = 0.05;
	public static final double CLOSE_DISTANCE_DEGREE = 0.001;

	public static boolean isNear(double latx, double lonx, double laty, double lony) {
		if (distance(latx, lonx, laty, lony) < NEAR_DISTANCE_DEGREE)
			return true;
		else
			return false;
	}

	public static boolean isClose(double latx, double lonx, double laty, double lony) {
		if (distance(latx, lonx, laty, lony) < CLOSE_DISTANCE_DEGREE)
			return true;
		else
			return false;
	}

	public static double distance(double latx, double lonx, double laty, double lony) {
		return Math.sqrt((latx - laty) * (latx - laty) + (lonx - lony) * (lonx - lony));
	}

	public static double metricDistance(double latx, double lonx, double laty, double lony) {
		double scale = 6400 * 1000 * (Math.PI / 180);
		double dX = scale * Math.abs((latx - laty));
		double dY = scale * Math.abs((lonx - lony));

		return Math.sqrt(dX * dX + dY * dY);
	}
}
