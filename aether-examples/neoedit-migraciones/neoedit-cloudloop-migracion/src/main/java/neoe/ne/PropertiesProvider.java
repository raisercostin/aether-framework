package neoe.ne;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesProvider {
	private static Properties properties = new Properties();

	static {
		try {
			properties.load(new FileInputStream(new File("src/main/resources/properties.properties")));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		try {
			properties.load(new FileInputStream(new File("properties.properties")));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
