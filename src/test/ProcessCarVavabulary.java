package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessCarVavabulary {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputFile = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\car\\CarVacabulary";
		String outputFile = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\dict\\car.dic";
//		BufferedReader newReader = Files.newReader(new File(inputFile), Charsets.UTF_8);
//		BufferedWriter newWriter = Files.newWriter(new File(outputFile), Charsets.UTF_8);
		FileInputStream fi = new FileInputStream(new File(inputFile));
		FileOutputStream fo = new FileOutputStream(new File(outputFile));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fi,"utf-8"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fo,"utf-8"));
		
		String t = "1号厂房工艺布置方案图 ";
		String regex = "([1-9\u4E00-\u9FA5]+)+";
		Pattern pattern = Pattern.compile(regex);

		
		String tmp = null;
//		int min = 10;
//		int max = 200;
//		Random random = new Random();
		int lineCnt = 0;
		while((tmp = reader.readLine()) != null){
			tmp = tmp.trim();
			if(lineCnt%2 == 0){
				Matcher matcher = pattern.matcher(tmp);
				if(matcher.find()){
					String ret = matcher.group(1);
					try{
						Integer.parseInt(ret) ;
					}
					catch (Exception e){
						if(ret.length() <= 8){
							ret = ret + "\n";
							writer.write(ret);
						}
					}
				}
				lineCnt++;
				continue;
			}
			lineCnt++;
//			tmp = tmp +"\n";
//			String line = new String(tmp.getBytes("utf-8"),"utf-8");
//			writer.write(line);
//			writer.flush();
//			break;
		}
		reader.close();
		writer.close();
	}

}
