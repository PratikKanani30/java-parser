package com.ef.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import com.ef.LogData;

/**
 * @author Pratik
 *
 */
public class LogParser {

    private static final String LOG_DELEMETER = "|";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public LogData parse(String line) {
	StringTokenizer tokenizer = new StringTokenizer(line, LOG_DELEMETER);
	int count = 0;
	LogData data = new LogData();
	while (tokenizer.hasMoreTokens()) {
	    if (setData(count++, tokenizer.nextToken(), data)) {
		break;
	    }
	}
	return data;
    }

    private boolean setData(int index, String value, LogData data) {
	boolean isDone = false;
	if (index == 0) {
	    try {
		data.setDate(DATE_FORMATTER.parse(value));
	    } catch (ParseException e) {
		throw new IllegalArgumentException();
	    }
	} else if (index == 1) {
	    data.setIp(value);
	} else if (index == 2) {
	    data.setRequest(value);
	} else if (index == 3) {
	    data.setStatus(value);
	} else if (index == 4) {
	    data.setUserAgent(value);
	    isDone = true;
	} else {
	    isDone = true;
	}
	return isDone;
    }

}
