package edu.lu.uni.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
	
	public static String readFile(File file) {
		byte[] input = null;
		BufferedInputStream bis = null;
		
		try {
			
			bis = new BufferedInputStream(new FileInputStream(file));
			input = new byte[bis.available()];
			bis.read(input);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bis);
		}
		
		String sourceCode = null;
		if (input != null) {
			sourceCode = new String(input);
		}
		
		return sourceCode;
	}
	
	/**
	 * Check whether a file path is valid or not.
	 * 
	 * @param path, file path.
	 * @return true, the file path is valid.
	 * 		   false, the file path is invalid.
	 */
	public static boolean isValidPath(String path) {
		File file = new File(path);
		
		if (file.exists()) {
			return true;
		}
		
		return false;
	}

	public static List<File> getAllFiles(String filePath, String type) {
		return listAllFiles(new File(filePath), type);
	}

	/**
	 * Recursively list all files in file.
	 * 
	 * @param file
	 * @return
	 */
	private static List<File> listAllFiles(File file, String type) {
		List<File> fileList = new ArrayList<>();
		
		File[] files = file.listFiles();
		
		for (File f : files) {
			if (f.isFile()) {
				if (f.toString().endsWith(type)) {
					fileList.add(f);
				}
			} else {
				fileList.addAll(listAllFiles(f, type));
			}
		}
		
		return fileList;
	}

	public static String getFileName(String filePath) {
		File file = new File(filePath);
		
		if (file.exists()) {
			return file.getName();
		} else {
			return null;
		}
	}

	public static String getParentFilePath(String filePath) {
		File file = new File(filePath);
		
		if (file.exists()) {
			return file.getParent();
		}
		
		return null;
	}

	public static void outputToFile(String fileName, StringBuilder content) {
		File file = new File(fileName);
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file, true);
			bw = new BufferedWriter(writer);
			bw.write(content.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bw);
			close(writer);
		}
	}

	private static void close(FileWriter writer) {
		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void close(BufferedWriter bw) {
		try {
			if (bw != null) {
				bw.close();
				bw = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void close(BufferedInputStream bis) {
		try {
			if (bis != null) {
				bis.close();
				bis = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
