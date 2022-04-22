package parser.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperty
{
	private Properties configProperties = null;
	File configFile = null;

	private static ConfigProperty configProperty = null;

	private ConfigProperty(){

		configProperties = new Properties();
		configFile = new File("config.properties");
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

	private String fetchProperty(String key)
	{
		String propVal = configProperties.getProperty(key);
		if(propVal != null){
			return propVal.trim();
		}else{
			return null;
		}
	}

	public static String getProperty(String key) throws IOException{

		if(configProperty == null){
			configProperty =  new ConfigProperty();
			configProperty.loadProperty();
		}

		return configProperty.fetchProperty(key);
	}
}
