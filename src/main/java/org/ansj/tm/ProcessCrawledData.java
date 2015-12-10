package org.ansj.tm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.lang.StringUtils;

public final class ProcessCrawledData {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String outputPath = "E:\\work\\project\\site-analysis\\ContentRetrieve\\auto\\topic\\thined\\autotopicpost";
		FileOutputStream os = new FileOutputStream(new File(outputPath));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"utf8"));
		
		String inputPath = "E:\\work\\project\\site-analysis\\ContentRetrieve\\auto\\topic\\output";
		File dir = new File(inputPath);
		if(dir.isHidden() || !dir.isDirectory()){
			System.out.println("Invalid path ! " + dir.getAbsolutePath());
			return;
		}
		for(File f : dir.listFiles()){
			if(f.isHidden() || f.isDirectory()){
				System.out.println("Invalid file " + f.getAbsolutePath());
				continue;
			}
			try{
				// todo
				Integer.parseInt(f.getName());
			}
			catch (Exception e){
				System.out.println("file name not a number !" + f.getName());
				continue;
			}
			FileInputStream is = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf8"));
			String line = null;
			while((line = reader.readLine()) != null){
				line = line.trim();
				String[] parts0 = StringUtils.splitPreserveAllTokens(line, "\t");
				if(parts0.length != 2){
					System.out.println("Split by \t failed !" + f.getAbsolutePath());
					continue;
				}
				String[] parts1 = StringUtils.splitPreserveAllTokens(parts0[1], "^A");
				System.out.println(parts0[1]);
				if(parts1.length != 2){
					System.out.println("Split by \001 failed !" + f.getAbsolutePath());
					continue;
				}
				writer.write(parts1[1]+"\n");
				break;
			}
			reader.close();
			is.close();
			break;
		}
		writer.close();
		os.close();
	}
}
