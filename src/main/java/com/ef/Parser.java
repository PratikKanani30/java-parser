package com.ef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.ef.constant.Duration;
import com.ef.dao.DatabaseFactory;
import com.ef.dao.LogComment;
import com.ef.dao.LogDao;
import com.ef.parser.LogParser;

/**
 * @author Pratik
 *
 */
public class Parser {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
    private Map<String, Integer> map;
    private Date startDate;
    private Integer thresold;
    private Duration duration;
    private LogParser parser;
    private String fileLocation;
    private String dbConfig;

    public static void main(String[] args) {
	Parser parser = new Parser();
	parser.init(args);
	parser.parse();
    }

    /**
     * Initializing parser
     * 
     * @param arguments
     */
    private void init(String[] arguments) {
	// if input parameter is valid then only it will process further and start parsing
	configureParser(arguments);
	parser = new LogParser();
	map = new HashMap<>();
    }

    private void configureParser(String[] arguments) {
	for (String arg : arguments) {
	    String[] params = arg.split("=");
	    if (params.length > 1) {
		initParaserConfig(params[0], params[1]);
	    }
	}
	validateParam();
	if (dbConfig != null) {
	    initDBFactory();
	} else {
	    dbConfig = "/db.properties";
	    initDBFactory(getClass().getResourceAsStream("/db.properties"));
	}
    }

    /**
     * Initializing db factory
     */
    private void initDBFactory() {
	Properties properties = new Properties();
	try (FileInputStream stream = new FileInputStream(new File(dbConfig))) {
	    initDBFactory(stream);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Initializing db factory
     */
    private void initDBFactory(InputStream propertiesStream) {
	try {
	    Properties properties = new Properties();
	    properties.load(propertiesStream);
	    DatabaseFactory.initFactory(properties);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private void initParaserConfig(String name, String value) {
	String paramName = name.toLowerCase();
	if (paramName.contains("startdate")) {
	    try {
		startDate = DATE_FORMATTER.parse(value);
	    } catch (ParseException e) {
		throw new IllegalArgumentException("Invalid date format");
	    }
	} else if (paramName.contains("duration")) {
	    duration = Duration.getValueOf(value);
	} else if (paramName.contains("threshold")) {
	    thresold = Integer.parseInt(value);
	} else if (paramName.contains("accesslog")) {
	    fileLocation = value;
	} else if (paramName.toLowerCase().contains("dbconfig")) {
	    dbConfig = value;
	}
    }

    /**
     * Validating user provided input after initializing the parameters
     */
    private void validateParam() {
	if (startDate == null) {
	    throw new RuntimeException("Start date is missing");
	}
	if (duration == null) {
	    throw new RuntimeException("Duration is missing");
	}
	if (thresold == null) {
	    throw new RuntimeException("Threshold is missing");
	}
	if (fileLocation == null) {
	    throw new RuntimeException("Access log missing");
	}
	if (dbConfig == null) {
	    System.err
		    .println("You haven't configured database. It uses default properties file. Please pass dbCofig.");
	}
    }

    /**
     * This starts parsing logs
     */
    private void parse() {
	Long startTime = System.currentTimeMillis();
	Date maxDateToFilter = calculateMaxDate();
	try (Scanner scanner = new Scanner(new File(fileLocation)); LogDao dao = new LogDao()) {
	    while (scanner.hasNextLine()) {
		String line = scanner.nextLine();
		LogData data = parser.parse(line);
		if (isDateInRange(data.getDate(), maxDateToFilter)) {
		    map.put(data.getIp(), computeOccurance(data.getIp()));
		}
		dao.addToSave(data);
	    }
	    dao.saveBatch();
	    Long endTime = System.currentTimeMillis();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	printAllBlockedIps();
    }

    /**
     * Printing blocked IPs with the comment
     */
    private void printAllBlockedIps() {
	LogComment commentDao = new LogComment();
	map.forEach((ip, count) -> {
	    if (count >= thresold) {
		String comment = "IP " + ip + " is blocked because it exceeded " + duration.toString()
			+ " request quota " + thresold;
		commentDao.addToSave(ip, comment);
		System.out.println(comment);
	    }
	});
	commentDao.saveBatch();
    }

    /**
     * Checks whether the log date is in range or not
     * 
     * @param logDate
     * @param maxDate
     * @return
     */
    private boolean isDateInRange(Date logDate, Date maxDate) {
	return (logDate.after(startDate) || logDate.equals(startDate))
		&& (logDate.before(maxDate) || logDate.equals(maxDate));
    }

    /**
     * Computes the occurrence of IP in given time frame
     * 
     * @param ip
     * @return
     */
    private Integer computeOccurance(String ip) {
	Integer count = map.get(ip);
	return count == null ? 1 : count + 1;
    }

    /**
     * This method computes the date range for the calculation
     * 
     * @return
     */
    private Date calculateMaxDate() {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(startDate);
	if (Duration.HOURLY.equals(duration)) {
	    calendar.add(Calendar.MINUTE, 59);
	    calendar.add(Calendar.SECOND, 59);
	} else if (Duration.DAILY.equals(duration)) {
	    calendar.add(Calendar.HOUR_OF_DAY, 23);
	    calendar.add(Calendar.MINUTE, 59);
	    calendar.add(Calendar.SECOND, 59);
	}
	return calendar.getTime();
    }

}
