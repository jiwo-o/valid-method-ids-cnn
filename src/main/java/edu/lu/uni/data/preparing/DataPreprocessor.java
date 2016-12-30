package edu.lu.uni.data.preparing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

/**
 * Preprocess data:
 * Append 0 to all vectors, make all vectors hold the same size
 * 
 * @author kui.liu
 *
 */
public class DataPreprocessor {
	
	private static Logger logger = LoggerFactory.getLogger(DataPreprocessor.class);

	private static final String INPUT_FILE_PATH = "inputData/unsupervised-learning/";
	private static final String OUTPUT_FILE_PATH = "outputData/WithoutNormalization/";
	
	public static void main(String[] args) throws IOException {
		DataPreprocessor dp = new DataPreprocessor();
		
		dp.appendZeroForVectorsInFiles(INPUT_FILE_PATH);
	}
	
	public void appendZeroForVectorsInFiles(String inputFolderPath) throws IOException {
		List<File> integerVectorsFiles = FileHelper.getAllFiles(inputFolderPath, ".list");
		
		for (File vectorFile : integerVectorsFiles) {
			appendZeroForVectors(vectorFile);
		}
	}

	public void appendZeroForVectors(File vectorFile) throws IOException {
		String outputFileName = vectorFile.toString().replace(INPUT_FILE_PATH, OUTPUT_FILE_PATH);
		int maxSizeOfVector = 0;
		if (outputFileName.contains("/features/")) {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("SIZE=") + "SIZE=".length(),
					outputFileName.lastIndexOf(".list")));
		} else {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("MAXSize=") + "MAXSize=".length(),
					outputFileName.lastIndexOf(".list")));
		}
		outputFileName = outputFileName.replace(".list", ".csv");
		String vectors = FileHelper.readFile(vectorFile);
		BufferedReader br = new BufferedReader(new StringReader(vectors));
		String vectorLine = null;
		int lines = 0; 
		StringBuilder content = new StringBuilder();
		
		while ((vectorLine = br.readLine()) != null) {
			int indexOfHarshKey = vectorLine.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + vectorLine);
				continue;
			}
			
//			String dataKey = vectorLine.substring(0, indexOfHarshKey + 1);
			String dataVector = vectorLine.substring(indexOfHarshKey + 2, vectorLine.length() - 1);
			List<String> vector = new ArrayList<>();
			vector.addAll(Arrays.asList(dataVector.split(", ")));
			int sizeOfVector = vector.size();
			
			for (int i = sizeOfVector; i < maxSizeOfVector; i ++) {
				vector.add("0");
			}
			
			lines ++;
			content.append(vector.toString().replace("[", "").replace("]", "") + "\n");
			if (lines % 1000 == 0) {
				FileHelper.outputToFile(outputFileName, content);
				content = new StringBuilder();
			}
		}
		if (content.length() > 0) {
			FileHelper.outputToFile(outputFileName, content);
		}
	}

}
