package edu.lu.uni.data.preparing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.lu.uni.util.FileHelper;

/**
 * Normalize data.
 * Normalize the values in the data vectors into values ranging from 0 to 1 by using min-max normalization.
 * 
 * @author kui.liu
 *
 */
public class DataForNormalization {
	
	private static final String INPUT_FILE_PATH = "outputData/WithoutNormalization/";
	
	public static void main(String[] args) throws IOException {
		DataForNormalization dp = new DataForNormalization();
		
		dp.normalizeVectorsInFiles(INPUT_FILE_PATH);
	}
	
	public void normalizeVectorsInFiles(String inputFolderPath) throws IOException {
		List<File> integerVectorsFiles = FileHelper.getAllFiles(inputFolderPath, ".csv");
		
		for (File vectorFile : integerVectorsFiles) {
			normalizeVectors(vectorFile);
		}
	}

	public void normalizeVectors(File vectorFile) throws IOException {
		String outputFileName = vectorFile.toString().replace("/WithoutNormalization/", "/Normalization/");
		int maxSizeOfVector = 0;
		double min = 0; 
		double max = 0;
		if (outputFileName.contains("/features/")) {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("SIZE=") + "SIZE=".length(),
					outputFileName.lastIndexOf(".csv")));
			if (outputFileName.contains("apache$commons-math$feature-ast-node-name-with-node-labelSIZE=82")) {
				max = 4601;
			} else if (outputFileName.contains("apache$commons-math$feature-only-ast-node-nameSIZE=82")) {
				max = 29;
			} else if (outputFileName.contains("apache$commons-math$feature-raw-tokens-with-operatorsSIZE=84")) {
				max = 4634;
			} else if (outputFileName.contains("apache$commons-math$feature-raw-tokens-without-operatorsSIZE=72")) {
				max = 4580;
			} else if (outputFileName.contains("apache$commons-math$feature-statement-node-name-with-all-node-labelSIZE=82")) {
				max = 4600;
			}
		} else {
			maxSizeOfVector = Integer.parseInt(outputFileName.substring(outputFileName.lastIndexOf("MAXSize=") + "MAXSize=".length(),
					outputFileName.lastIndexOf(".csv")));
			if (outputFileName.contains("RAW_CAMEL_TOKENIATION/apache$commons-math$feature-ast-node-name-with-node-labelSIZE=82")) {
				max = 1181;
			} else if (outputFileName.contains("RAW_CAMEL_TOKENIATION/apache$commons-math$feature-raw-tokens-with-operatorsSIZE=84")) {
				max = 1179;
			} else if (outputFileName.contains("RAW_CAMEL_TOKENIATION/apache$commons-math$feature-raw-tokens-without-operatorsSIZE=72")) {
				max = 1181;
			} else if (outputFileName.contains("SIMPLIFIED_NLP/apache$commons-math$feature-ast-node-name-with-node-labelSIZE=82")) {
				max = 491;
			} else if (outputFileName.contains("SIMPLIFIED_NLP/apache$commons-math$feature-raw-tokens-with-operatorsSIZE=84")) {
				max = 488;
			} else if (outputFileName.contains("SIMPLIFIED_NLP/apache$commons-math$feature-raw-tokens-without-operatorsSIZE=72")) {
				max = 491;
			} else if (outputFileName.contains("TOKENAZATION_WITH_NLP/apache$commons-math$feature-ast-node-name-with-node-labelSIZE=82")) {
				max = 1408;
			} else if (outputFileName.contains("TOKENAZATION_WITH_NLP/apache$commons-math$feature-raw-tokens-with-operatorsSIZE=84")) {
				max = 1406;
			} else if (outputFileName.contains("TOKENAZATION_WITH_NLP/apache$commons-math$feature-raw-tokens-without-operatorsSIZE=72")) {
				max = 1408;
			} 
		}
		String vectors = FileHelper.readFile(vectorFile);
		BufferedReader br = new BufferedReader(new StringReader(vectors));
		String vectorLine = null;
		int lines = 0; 
		StringBuilder content = new StringBuilder();
		DecimalFormat df = new DecimalFormat("#.##########");
		
		while ((vectorLine = br.readLine()) != null) {
//			int indexOfHarshKey = vectorLine.indexOf("#");
//			
//			if (indexOfHarshKey < 0) {
//				logger.error("The below raw feature is invalid!\n" + vectorLine);
//				continue;
//			}
			
//			String dataKey = vectorLine.substring(0, indexOfHarshKey + 1);
//			String dataVector = vectorLine.substring(indexOfHarshKey + 2, vectorLine.length() - 1);
			List<String> vector = new ArrayList<>();
			vector.addAll(Arrays.asList(vectorLine.split(", ")));
			
			for (int i = 0; i < maxSizeOfVector; i ++) {
				if ("0".equals(vector.get(i))) {
					break;
				} else {
					double normalizedValue = (Double.parseDouble(vector.get(i)) - min) / (max - min);
					vector.set(i, df.format(normalizedValue));
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

}
