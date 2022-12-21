package parser.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperty
{
	private Properties configProperties = null;
	static File configFile = null;

	private static ConfigProperty configProperty = null;
	private static String version;
	private ConfigProperty(){

		configProperties = new Properties();
	}

	private void loadProperty() throws IOException	{

		FileInputStream inputStream = new FileInputStream(configFile);

		try{
			configProperties.load(inputStream);
		} catch (IOException e) {
			System.out.println("Exception: " + e);
		}finally{
			inputStream.close();
		}
	}

	public static void setVersion(String ver) {
		version = ver;
	}
	
	public static String getVersion() {
		return version;
	}
	
	private String fetchProperty(String key)
	{
		String propVal = configProperties.getProperty(key);
		if(propVal != null){
			return propVal.trim();
		}else{
			return null;
		}
	}
	
	public static void loadconfig(String config) {
		configFile = new File(config);
	}
	public static String getProperty(String key){

		if(configProperty == null){
			configProperty =  new ConfigProperty();
			try {
				configProperty.loadProperty();
			} catch (IOException e) {
				System.out.println("Exception: " + e);
			}
		}

		return configProperty.fetchProperty(key);
	}
}
