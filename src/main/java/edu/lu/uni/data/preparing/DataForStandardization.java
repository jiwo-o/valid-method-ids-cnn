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

public class DataForStandardization {
	
	private static Logger logger = LoggerFactory.getLogger(DataForStandardization.class);

	private static final String INPUT_FILE_PATH = "inputData/unsupervised-learning/";
	private static final String OUTPUT_FILE_PATH = "outputData/Standardization/";
	
	public static void main(String[] args) throws IOException {
		DataForStandardization dp = new DataForStandardization();
		
		/*
		 * The first step: use R language to standardize vectors.
		 */
//		dp.uprightVectorsInFiles(INPUT_FILE_PATH);
		/*
		 * The second step: output the standardized vectors.
		 */
		dp.standardizeVectorsInFiles(INPUT_FILE_PATH);
	}
	
	public void uprightVectorsInFiles(String inputFolderPath) throws IOException {
		List<File> files = FileHelper.getAllFiles(inputFolderPath, ".list");
		
		for (File file : files) {
			uprightVectors(file);
		}
	}
	
	public void uprightVectors(File file) throws IOException {
		StringBuilder content = new StringBuilder("TOKEN\n");
		
		String vectors = FileHelper.readFile(file);
		
		BufferedReader br = new BufferedReader(new StringReader(vectors));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			int indexOfHarshKey = line.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + line);
				continue;
			}
			
			String dataVector = line.substring(indexOfHarshKey + 2, line.length() - 1);

			String[] vector = dataVector.split(", ");
			content.append(Arrays.asList(vector).toString().replace("[", "").replace("]", "").replaceAll(", ", "\n") + "\n");
		}
		
		String fileName = file.getName().replace(".list", ".csv");
		FileHelper.outputToFile(file.getParent() + "/" + fileName, content);
	}
	
	public void standardizeVectorsInFiles(String inputFolderPath) throws IOException {
		List<File> standardizedResultsFiles = FileHelper.getAllFiles(inputFolderPath, ".txt");
		List<File> integerVectorsFiles = FileHelper.getAllFiles(inputFolderPath, ".list");
		
		for (File normalizedFile : standardizedResultsFiles) {
			for (File vectorFile : integerVectorsFiles) {
				if (isMatched(normalizedFile, vectorFile)) {
					standardizeVectors(normalizedFile, vectorFile);
					break;
				}
			}
		}
	}

	public void standardizeVectors(File normalizedFile, File vectorFile) throws IOException {
		String outputFileName = normalizedFile.toString().replace(INPUT_FILE_PATH, OUTPUT_FILE_PATH);
		int maxSizeOfVector = 0;
		if (outputFileName.contains("/features/")) {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("SIZE=") + "SIZE=".length(),
					outputFileName.lastIndexOf(".txt")));
		} else {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("MAXSize=") + "MAXSize=".length(),
					outputFileName.lastIndexOf(".txt")));
		}
		outputFileName = outputFileName.replace(".txt", ".csv");
		List<String> normalizedResults = readNormalizedResults(normalizedFile);
		String vectors = FileHelper.readFile(vectorFile);
		BufferedReader br = new BufferedReader(new StringReader(vectors));
		String vectorLine = null;
		int index = 0;
		int lines = 0; 
		StringBuilder content = new StringBuilder();
		
		while ((vectorLine = br.readLine()) != null) {
			int indexOfHarshKey = vectorLine.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + vectorLine);
				continue;
			}
			
			String dataVector = vectorLine.substring(indexOfHarshKey + 2, vectorLine.length() - 1);
			List<String> vector = new ArrayList<>();
			vector.addAll(Arrays.asList(dataVector.split(", ")));
			int sizeOfVector = vector.size();
			
			vector.clear();
			for (int i = 0; i < maxSizeOfVector; i ++) {
				if (i < sizeOfVector) {
					vector.add(normalizedResults.get(index));
					index ++;
				} else {
					vector.add("0");
				}
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

	private List<String> readNormalizedResults(File fileName) throws IOException {
		String normalization = FileHelper.readFile(fileName);
		BufferedReader br = new BufferedReader(new StringReader(normalization));
		String line = null;
		
		List<String> normalizedResults = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			normalizedResults.add(line);
		}
		
		return normalizedResults;
	}

	private boolean isMatched(File normalizedFile, File vectorFile) {
		String parentPath1 = normalizedFile.getParentFile().toString();
		String parentPath2 = vectorFile.getParentFile().toString();
		
		if (parentPath1.equals(parentPath2)) {
			String fileName1 = normalizedFile.getName();
			String fileName2 = vectorFile.getName();
			fileName1 = fileName1.substring(0, fileName1.indexOf("."));
			fileName2 = fileName2.substring(0, fileName2.indexOf("."));
			if (fileName1.equals(fileName2)) {
				return true;
			}
		}
		
		return false;
	}

}
