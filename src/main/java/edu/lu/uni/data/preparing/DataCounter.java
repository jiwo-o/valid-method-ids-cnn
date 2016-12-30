package edu.lu.uni.data.preparing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.util.FileHelper;

@Deprecated
public class DataCounter {
	
	private static final String FILE_PATH = "inputData/supervised-learning/labels/";
	
	public static void main(String[] args) throws IOException {
		List<File> files = FileHelper.getAllFiles(FILE_PATH, ".list");
		
		for(File file : files) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			String content = FileHelper.readFile(file);
			BufferedReader br = new BufferedReader(new StringReader(content));
			String line;
			
			while((line = br.readLine()) != null) {
				String label = line.substring(line.lastIndexOf("#") + 1);
				if (map.containsKey(label)) {
					map.put(label, map.get(label) + 1);
				} else {
					map.put(label, 1);
				}
			}
			
			Map<Integer, String> map2 = new HashMap<Integer, String>();
			for(Map.Entry<String, Integer> entry : map.entrySet()) {
				if (map2.containsKey(entry.getValue())) {
					map2.put(entry.getValue(), map2.get(entry.getValue()) + " # " + entry.getKey());
				} else {
					map2.put(entry.getValue(), entry.getKey() + " # ");
				}
			}
			
			for(Map.Entry<Integer, String> entry : map2.entrySet()) {
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}
			
			System.out.println("=================");
		}
	}
}
