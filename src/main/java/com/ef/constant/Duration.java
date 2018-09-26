package com.ef.constant;

/**
 * @author Pratik
 *
 */
public enum Duration {
    DAILY, HOURLY;

    public static Duration getValueOf(String value) {
	if (value == null) {
	    return null;
	}
	return Duration.valueOf(value.toUpperCase());

    }

}
