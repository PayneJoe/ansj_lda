package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ProcessBaike {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String inputFile = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\dict\\baike_20151203.dict";
		String outputFile = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\dict\\baike.dict";
//		BufferedReader newReader = Files.newReader(new File(inputFile), Charsets.UTF_8);
//		BufferedWriter newWriter = Files.newWriter(new File(outputFile), Charsets.UTF_8);
		FileInputStream fi = new FileInputStream(new File(inputFile));
		FileOutputStream fo = new FileOutputStream(new File(outputFile));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fi,"gbk")); 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fo));
		String tmp = null;
		int min = 10;
		int max = 200;
		Random random = new Random();
		while((tmp = reader.readLine()) != null){
			String[] parts = tmp.split(" ", -1);
			if(parts.length > 1){
				continue;
			}
			tmp = tmp + "\t" + "n\t" + random.nextInt(max)%(max-min+1) + min + "\n"; 
			String line = new String(tmp.getBytes("utf-8"),"utf-8");
			writer.write(line);
			writer.flush();
		}
		reader.close();
		writer.close();
	}

}
