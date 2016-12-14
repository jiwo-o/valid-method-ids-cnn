package edu.lu.uni.feature;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.util.FileHelper;

/**
 * Label: method names.
 * Parsing the name of each method.
 * 
 * @author kui.liu
 *
 */
public class Label {

	private static final String INPUT_FILE_PATH = "dataset/original-features/";
	
	public static void main(String[] args) throws IOException {
		List<File> files = FileHelper.getAllFiles(INPUT_FILE_PATH, ".list");
		
		for (File file : files) {
//			LabelGenerator validatingMethod = new LabelGenerator();
//			validatingMethod.generateLables(file.getName(), INPUT_FILE_PATH + file.getName());
//			break;
		}
		
	}
}
