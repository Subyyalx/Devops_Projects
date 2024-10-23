package com.xavor.agile;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author TE901708
 *
 */
public class Utils {

	private static Logger logger = null;
	private static String APP_NAME = "AgileAxmlToFileFolder";
	private static String PROPS_DIR = "PX_PROPS";
	private static String ENV_VARIABLE_NAME = "XAVOR_HOME";
	
	
	/**
	 * Initializes log4j logger
	 */
	public static void initLog4j(Properties props) {
		Properties properties = new Properties();
		try {
			if (props == null) {
				properties = getProjectProperties(true);
			} else {
				properties = props;
			}
			String filePath = properties.getProperty("log4j.appender."+APP_NAME+".File");
			logInfo("log4j.appender."+APP_NAME+".File=" + filePath, null);
			if (filePath != null && !"".equals(filePath)) {
				String fullPath = filePath;
				fullPath = fullPath.replace(APP_NAME+".log","");
				if((new File(fullPath)).exists()){
					logInfo("File Path:" + filePath, null);
				}else{
					String envPath = getEnvPath() + "log";
					logInfo("checking if log folder [" + envPath + "] exists", null);
					if ((new File(envPath)).exists()) {
						filePath = filePath.replace("#logfolder", envPath);
						logInfo("File Path:" + filePath, null);
					} else {
						 envPath = getAgileDomainPath() + "log";
						 if ((new File(envPath)).exists()) {
							 filePath = filePath.replace("#logfolder", envPath);
							logInfo("File Path:" + filePath, null);
						 }else{
							logInfo("File Path:" + envPath + " does exist", null);
							filePath = "./log/"+APP_NAME+".log";
						 }
						
					}
				}
				
			} else {
				filePath = "./log/"+APP_NAME+".log";
			}
			properties.setProperty("log4j.appender."+APP_NAME+".File", filePath);
			logInfo("log file path [" + filePath + "]", null);
			String loggerName = "";
			for (Object key : properties.keySet()) {
				if (key != null && key.toString().startsWith("log4j.category.")) {
					String keyName = key.toString();
					loggerName = keyName.substring( keyName.lastIndexOf(".") + 1, keyName.length());
					break;
				}
			}
			PropertyConfigurator.configure(properties);
			Utils.logger = Logger.getLogger(loggerName);
			Utils.logger.info(":::::Logger Initialized::::");
		} catch (Exception exp) {
			logInfo("Error in initializing log4j ERROR:" + exp.getMessage(), exp);
		}
	}

	/**
	 * Returns Logger, if log4j logger is not initialized it initializes it, if
	 * fails than return console logger
	 * 
	 * @return
	 */
	public static Logger getLogger(Properties props) {
		Logger logger = Utils.logger;
		if (logger == null) {
			if(props != null && !props.isEmpty()){
				initLog4j(props);
				logger = Utils.logger;
			}
			
			if (logger == null) {
				StackTraceElement[] stackTraceElements = Thread.currentThread() .getStackTrace();
				String callerClass = stackTraceElements[stackTraceElements.length - 1] .getClassName();
				System.out.println(callerClass);
				System.out .println("ALERT: Logger is not initialized from Log4j PROPERTIES, please check the setup, returning console logger");
				logger = Logger.getLogger(callerClass);
				ConsoleAppender ca = new ConsoleAppender();
				ca.setWriter(new OutputStreamWriter(System.out));
				ca.setLayout(new PatternLayout("%d %-5p %C (%13F:%L) %3x - %m%n"));
				logger.addAppender(ca);
				logInfo(callerClass + ": Returning Logger [" + logger + "]",null);
				return logger;
			}
		}
		return logger;
	}

	
	/**
	 * returns the full path of agiledomain of weblogic
	 * @return
	 */
	public static String getAgileDomainPath(){
		String domainDir = System.getProperty("weblogic.domainDir");
		logInfo("System.getenv(weblogic.domainDir):"+domainDir,null);
		if(isNullOrEmpty(domainDir)){
			try {
				domainDir=new File(".").getCanonicalPath();
			} catch (Exception e) {
				logInfo("ERROR in File(.).getCanonicalPath() ERROR:"+e.getMessage(),null);
			}
			logInfo("File(.).getCanonicalPath() :"+domainDir,null);
			if(isNullOrEmpty(domainDir)){
				File propFile = new File("AgileAuthenticatorAgileRealm.properties");
				if(propFile != null && propFile.exists()){
					logInfo("AgileAuthenticatorAgileRealm.properties exists", null);
					domainDir = propFile.getAbsolutePath();
					if(domainDir.contains("AgileAuthenticatorAgileRealm.properties")){
						domainDir = domainDir.replace("AgileAuthenticatorAgileRealm.properties", "");
					}
				}
			}
		}
		
		domainDir = fixNullString(domainDir);
		if(!domainDir.endsWith(File.separator)){
			domainDir = domainDir+File.separator;
		}
		logInfo("returning AgileDomainPath"+domainDir,null);
		return domainDir; 
	} 
	
	private static Properties properties = null;
	private static long lastModified = 0;	
	
	/**
	 * Returns Properties from agileDomain, if fails to
	 * tries load from PXS_PROPS_HOME Environment Variable if fails 
	 * and retryfromclassPath is true loads file from jar
	 * 
	 * @param retryfromclassPath
	 * @return
	 * @throws Exception
	 */
	public static Properties getProjectProperties(boolean retryfromclassPath) {
		Properties props = null;
		String agileDomain = getAgileDomainPath()+PROPS_DIR+File.separator;
		logInfo("agileDomain path :"+agileDomain, null);
		props = getProjectPropertiesFrmPath(agileDomain+APP_NAME+".properties"); // try to load from agile domain
		logInfo("Properties from agile domain "+(props == null?"null":" size"+props.size()), null);
		if(props == null || props.isEmpty()){
			logInfo("Propperties are :"+props+" hence loading from PXS_PROPS_HOME Environment Variable path", null);
			try {
				props = getProjectPropertiesFrmEnvPath(APP_NAME);
			} catch (Exception e) {
				logInfo(e.getMessage(), null);
				if(retryfromclassPath && (props == null || props.isEmpty())){
					logInfo("loading properties from class path [/resources/"+ APP_NAME + ".properties]",null);
					try {
						InputStream is = Utils.class.getResourceAsStream("/resources/"+ APP_NAME + ".properties");
						props = new Properties();
						props.load(is);
					} catch (IOException e1) {
						logInfo("Error in loading properties file from classpath",null);
					}
				}
			}
		}
		
		return props;
	}
	public static String getTmpFolderPath()throws Exception{
		String path = getPropertyValueWithDefault(properties, "tempfolderpath", "same");
		if("same".equalsIgnoreCase(path)){
			path = getAgileDomainPath()+PROPS_DIR;
		}
		File tempFolder = new File(path);
		if(tempFolder != null && !tempFolder.exists()){
			String pathWoPXP = path.replace(PROPS_DIR, "");
			tempFolder = new File(pathWoPXP);
			if(tempFolder != null && !tempFolder.exists()){
				throw new Exception("tempfolder path ["+path+"] does not exist");
			}else{
				path = pathWoPXP;
			}
		}
		return  path;
	}
	private static Properties getProjectPropertiesFrmPath(String path) {
		logInfo("Loading properties file from path ["+path+"]", null);
		Properties propers = new Properties();
		try {
			File propFile = new File(path);
			if (!propFile.exists()) {
				throw new Exception("File [" + path + "] does not exist");
			}
			synchLoadProperties(propFile);
			propers =  properties;
		logInfo("properties file loaded from path ["+path+"]", null);
		} catch (Exception e) {
			logInfo("Error in loading properties from path ["+path+"] ERROR:"+e.getMessage(), null);
		}
		return propers;
	}

	private static Properties getProjectPropertiesFrmEnvPath(String projectName) throws Exception {
		String filePath = getEnvPath();
		filePath = filePath + projectName + ".properties";
		logInfo("Properties File path [" + filePath + "]", null);
		File propFile = new File(filePath);
		if (!propFile.exists()) {
			throw new Exception("Cannot load properties file please check [" + ENV_VARIABLE_NAME + "], Environment variable");
		}
		synchLoadProperties(propFile);
		return properties;
	}

	private static void synchLoadProperties(File propFile) throws Exception{
		synchronized (Utils.class) {
			if (properties == null || lastModified == 0 || propFile.lastModified() != lastModified) {
				logInfo("Properties loaded before [" + (properties == null) + "]", null);
				logInfo("Properties last modified [" + lastModified + "]", null);
				properties = new Properties();
				properties.load(new FileInputStream(propFile));
				logInfo("Properties loaded [" + properties + "]", null);
				lastModified = propFile.lastModified();
				logInfo("Initialzing LOG4J", null);
				initLog4j(properties);
			}else{
				logInfo("Nothing to reload no changes have been detected in properties file", null);
			}
		}
	}
	
	/**
	 * Returns the value of PXS_PROPS_HOME Environment Variable
	 * @return
	 */
	public static String getEnvPath() {
		String envPath = System.getenv(ENV_VARIABLE_NAME);
		logInfo(ENV_VARIABLE_NAME + ":" + envPath, null);
		if (envPath != null && !"".equals(envPath)) {
			envPath = envPath.replace("\\", "/");
			envPath = envPath + "/";
		}else{
			envPath = getAgileDomainPath();
		}
		return envPath;
	}

	private static void logInfo(String msg, Throwable t) {
		if (logger != null) {
			logger.info(msg);
			if (t != null) {
				logger.error(msg, t);
			}
		} else {
			System.out.println("XAVOR:" + msg);
			if (t != null) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Returns the base id value from the properties only call this method when
	 * getting an integer value
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static Integer getBaseIdVal(Properties props, String key, Integer defaultBaseId) {
		Integer baseId = -10000;
		try {
			String strVal = getPropertyValue(props, key);
			if(!(strVal == null || "".equals(strVal.trim()))){
				baseId = Integer.parseInt(strVal);
			}else if(defaultBaseId != null){
				logInfo("BaseID from properties is null returning default:", null);
				baseId = defaultBaseId;
			}
		} catch (Exception e) {
			logInfo("Error in getting base id value from properties, Key [" + key + "], ERROR:" + e.getMessage(), null);
		}
		return baseId;

	}

	/**
	 * Returns the value of property
	 * 
	 * @param props
	 * @param key
	 * @return
	 */
	public static String getPropertyValue(Properties props, String key) {
		String propValue = "";
		try {
			if (props == null) {
				logInfo("NULL Properties were passed", null);
				props = getProjectProperties(true);
			}
			propValue = props.getProperty(key);
			//logInfo("Value of [" + key + "] in properties [" + propValue + "]", null);
		} catch (Exception e) {
			logInfo("Error in getting base id value from properties, Key [" + key+ "], ERROR:" + e.getMessage(), e);
		}
		return propValue;
	}
	/**
	 * if properties file does not contain property returns default value instead of null
	 * @param props
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getPropertyValueWithDefault(Properties props, String key, String defaultValue) {
		String propValue = getPropertyValue(props, key);
		if(isNullOrEmpty(propValue)){
			propValue = defaultValue;
		}
		return propValue;
	}
	public static boolean isNullOrEmpty(String s) {
		if ((s == null) || (s.equals("")) || (s.trim().length() == 0) || "null".equalsIgnoreCase(s.trim())) {
			return true;
		}
		return false;
	}
	/**
	 * returns empty string is str is null else returns same str
	 * @param str
	 * @return
	 */
	public static String fixNullString(String str){
		if(isNullOrEmpty(str)){
			return "";
		}
		return str;
	}
}
