package dkramer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class BetterConfiguration {
	private File file;
	
	private LinkedHashMap<String, String> segments = new LinkedHashMap<String, String>();
	
	public BetterConfiguration(String path) {
		file = new File(path);
	}
	
	public void load() {
		segments.clear();
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(isKeyLine(line)) {
					segments.put(keyOfLine(line), valueOfLine(line));
				}
			}
			scanner.close();
		} catch (FileNotFoundException e1) {}
	}
	
	public void save() {
		try {
			PrintWriter out = new PrintWriter(file);
			for(String key : segments.keySet()) {
				if(!key.equals("pasteschematicair")) {
					out.println(key + ": " + segments.get(key));
				}
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void addDefault(String key, String value) {
		if(!segments.containsKey(key)) {
			segments.put(key, value);
		}
	}
	
	public void set(String key, Object value) {
		segments.put(key, value.toString());
	}
	
	public int getInt(String key, int def) {
		addDefault(key, def + "");
		return Integer.parseInt(segments.get(key));
	}
	
	public double getDouble(String key, double def) {
		addDefault(key, def + "");
		return Double.parseDouble(segments.get(key));
	}
	
	public String getString(String key, String def) {
		addDefault(key, def);
		return segments.get(key);
	}
	
	public boolean getBoolean(String key, boolean def) {
		addDefault(key, def + "");
		return Boolean.parseBoolean(segments.get(key));
	}
	
	private boolean isKeyLine(String line) {
		line = line.trim();
		return line.length() > 0 && line.contains(":");
	}
	
	private String keyOfLine(String line) {
		return line.substring(0, line.indexOf(":")).trim();
	}
	
	private String valueOfLine(String line) {
		return line.substring(line.indexOf(":") + 1).trim();
	}
}