package edu.lu.uni.dataset.cnn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

public class DataInitializer {
	
	private static Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	private static final String FEATURE_INTEGER_VECTOR_FILE_PATH = "dataset/integer-vectors/features/";
	private static final String LABEL_INTEGER_VECTOR_FILE_PATH = "dataset/integer-vectors/labels/";
	private static final String OUT_PUT_FILE_PATH = "src/main/resources/data-for-CNN/";
	
	public static void main(String[] args) throws IOException {
		DataInitializer di = new DataInitializer();
		
		List<File> featureFiles = FileHelper.getAllFiles(FEATURE_INTEGER_VECTOR_FILE_PATH, ".list");
		List<File> labelFiles = FileHelper.getAllFiles(LABEL_INTEGER_VECTOR_FILE_PATH, ".list");
		
		di.initializeDataForCNN(featureFiles, labelFiles);
	}
	
	public void initializeDataForCNN(List<File> featureFiles, List<File> labelFiles) throws IOException {
		Map<String, Map<String, Integer>> labelsClassData = new HashMap<>();
		for (File file : labelFiles) {
			Map<String, Integer> labelClass = getLabelClass(file);
			labelsClassData.put(file.getName(), labelClass);
		}
		
		Map<String, List<Map<String, List<String>>>> featuresData = new HashMap<>();
		Map<String, Integer> featuresMaxSizes = new HashMap<>();
		for (File file : featureFiles) {
			List<Map<String, List<String>>> featureData = getFeatureData(file);
			int maxSize = getMaxSizeOfVector(featureData);
			featuresData.put(file.getName(), featureData);
			featuresMaxSizes.put(file.getName(), maxSize);
		}
		
		outputDataForCNNIntoFile(labelsClassData, featuresData, featuresMaxSizes);
	}
	
	private Map<String, Integer> getLabelClass(File file) throws IOException {
		String fileContent = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(fileContent));

		Map<String, Integer> labelClass = new HashMap<String, Integer>();
		String label = null;
		while ((label = br.readLine()) != null) {
			int indexOfHarshKey = label.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below integer vector is invalid!\n" + label);
				continue;
			}
			
			String dataKey = label.substring(0, indexOfHarshKey + 1);
			String dataClass = label.substring(indexOfHarshKey + 1, label.length());
			
			labelClass.put(dataKey, Integer.parseInt(dataClass));
		}
		
		return labelClass;
	}

	private void outputDataForCNNIntoFile(Map<String, Map<String, Integer>> labelsClassData,
			Map<String, List<Map<String, List<String>>>> featuresData, Map<String, Integer> featuresMaxSizes) {
		
		for (Map.Entry<String, List<Map<String, List<String>>>> featureData : featuresData.entrySet()) {
			// each kind of features combines with each kind of label classes.
			int maxSizeOfFeature = featuresMaxSizes.get(featureData.getKey());
			String fileName = featureData.getKey();
			List<Map<String, List<String>>> features = featureData.getValue();
			
			for (Map.Entry<String, Map<String, Integer>> labelData : labelsClassData.entrySet()) {
				
				if (labelData.getKey().endsWith("(1).list")) {
					fileName.replace(".list", "(1).list");
				} else if (labelData.getKey().endsWith("(2).list")) {
					fileName.replace(".list", "(2).list");
				} else if (labelData.getKey().endsWith("(3).list")) {
					fileName.replace(".list", "(3).list");
				}
				fileName += "MaxSize=" + maxSizeOfFeature + ".csv";
				
				Map<String, Integer> labelsClass = labelData.getValue();
				StringBuilder sb = new StringBuilder();
				int numberOfData = 0;
				for (Map<String, List<String>> feature : features) {
					//combination of each feature
					Object[] key = feature.keySet().toArray();
					List<String> featureList = new ArrayList<String>();
					featureList.addAll(feature.get(key[0]));
					
					appendZero(featureList, maxSizeOfFeature);
					
					sb.append(featureList.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
					sb.append(",");
					sb.append("" + labelsClass.get(key[0]));
					sb.append("\n");
					
					numberOfData ++;
					if (numberOfData % 1000 == 0) {
						FileHelper.outputToFile(OUT_PUT_FILE_PATH + fileName, sb);
						sb = new StringBuilder();
					}
				}
				if (sb.length() > 0) {
					FileHelper.outputToFile(OUT_PUT_FILE_PATH + fileName, sb);
				}
			}
		}
	}

//	private void outputDataForCNNIntoFile(Map<String, Map<String, List<String>>> labelsData,
//			Map<String, Integer> labelsMaxSizes, Map<String, List<Map<String, List<String>>>> featuresData,
//			Map<String, Integer> featuresMaxSizes) {
//		
//		for (Map.Entry<String, List<Map<String, List<String>>>> featureData : featuresData.entrySet()) {
//			int maxSizeOfFeature = featuresMaxSizes.get(featureData.getKey());
//			List<Map<String, List<String>>> features = featureData.getValue();
//			for (Map.Entry<String, Map<String, List<String>>> labelData : labelsData.entrySet()) {
//				int maxSizeOfLabel = labelsMaxSizes.get(labelData.getKey());
//				Map<String, List<String>> labels = labelData.getValue();
//				String fileName = featureData.getKey();
//				fileName = fileName.substring(0, fileName.lastIndexOf(".list"));
//				if (labelData.getKey().contains("RAW_CAMEL_TOKENIATION")) {
//					fileName += "(RAW_CAMEL_TOKENIATION)";
//				} else if (labelData.getKey().contains("SIMPLIFIED_NLP")) {
//					fileName += "(SIMPLIFIED_NLP)";
//				} else if (labelData.getKey().contains("TOKENAZATION_WITH_NLP")) {
//					fileName += "(TOKENAZATION_WITH_NLP)";
//				}
//				fileName += "MaxSize=" + maxSizeOfFeature + "-" + maxSizeOfLabel + ".csv";
//				
//				StringBuilder sb = new StringBuilder();
//				int numberOfData = 0;
//				for (Map<String, List<String>> feature : features) {
//					Object[] key = feature.keySet().toArray();
//					List<String> featureList = new ArrayList<String>();
//					featureList.addAll(feature.get(key[0]));
//					List<String> label = new ArrayList<String>();
//					label.addAll(labels.get(key[0]));
//					
//					appendZero(featureList, maxSizeOfFeature);
//					appendZero(label, maxSizeOfLabel);
//					
//					sb.append(featureList.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
//					sb.append(",");
//					sb.append(label.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
//					sb.append("\n");
//					
//					numberOfData ++;
//					if (numberOfData % 1000 == 0) {
//						FileHelper.outputToFile(OUT_PUT_FILE_PATH + fileName, sb);
//						sb = new StringBuilder();
//					}
//				}
//				if (sb.length() > 0) {
//					FileHelper.outputToFile(OUT_PUT_FILE_PATH + fileName, sb);
//				}
//			}
//		}
//	}

	private void appendZero(List<String> list, int maxSize) {
		for (int i = list.size(); i < maxSize; i ++) {
			list.add("0");
		}
	}

	private List<Map<String, List<String>>> getFeatureData(File file) throws IOException {
		
		String fileContent = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(fileContent));
		
		String feature = null;
		List<Map<String, List<String>>> featureData = new ArrayList<>();
		
		while ((feature = br.readLine()) != null) {
			int indexOfHarshKey = feature.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below integer vector is invalid!\n" + feature);
				continue;
			}
			
			String dataKey = feature.substring(0, indexOfHarshKey + 1);
			String dataVector = feature.substring(indexOfHarshKey + 2, feature.length());
			List<String> integerVector = Arrays.asList(dataVector.split(","));
			
			Map<String, List<String>> data = new HashMap<>();
			data.put(dataKey, integerVector);
			featureData.add(data);
		}

		return featureData;
	}
	
//	public Map<String, List<String>> getLabelData(File file) throws IOException {
//		Map<String, List<String>> data = new HashMap<>();
//		
//		String fileContent = FileHelper.readFile(file);
//		BufferedReader br = new BufferedReader(new StringReader(fileContent));
//		
//		String feature = null;
//		while ((feature = br.readLine()) != null) {
//			int indexOfHarshKey = feature.indexOf("#");
//			
//			if (indexOfHarshKey < 0) {
//				logger.error("The below integer vector is invalid!\n" + feature);
//				continue;
//			}
//			
//			String dataKey = feature.substring(0, indexOfHarshKey + 1);
//			String dataVector = feature.substring(indexOfHarshKey + 2, feature.length());
//			List<String> integerVector = Arrays.asList(dataVector.split(","));
//			
//			data.put(dataKey, integerVector);
//		}
//		return data;
//	}
	
//	private int getMaxSizeOfVector(Map<String, List<String>> map) {
//		int maxSize = 0;
//		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//			if (entry.getValue().size() > maxSize) {
//				maxSize = entry.getValue().size();
//			}
//		}
//		
//		return maxSize;
//	}
	
	private int getMaxSizeOfVector(List<Map<String, List<String>>> list) {
		int maxSize = 0;
		for (Map<String, List<String>> map : list) {
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				if (entry.getValue().size() > maxSize) {
					maxSize = entry.getValue().size();
				}
			}
		}
		
		return maxSize;
	}
}
