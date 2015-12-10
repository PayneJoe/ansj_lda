package org.ansj.tm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import org.ansj.lda.LDA;
import org.ansj.lda.impl.LDAGibbsModel;
import org.ansj.util.impl.AnsjAnalysis;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Test3 {

	public static void main(String[] args) throws IOException {
		LDA lda = new LDA(AnsjAnalysis.DEFAUlT,new LDAGibbsModel(40, 50/(double)40, 0.02, 150, Integer.MAX_VALUE, Integer.MAX_VALUE));
//		String inputPath = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\baike";
		String inputPath1 = "/home/yuanpingzhou/data/ansj_lda/input/autotopicpost";
		String inputPath2 = "/home/yuanpingzhou/data/ansj_lda/input/autobrandpost";
		String outputPath = "/home/yuanpingzhou/data/ansj_lda/output/baike";
		File file1 = new File(inputPath1);
		File[] inputFiles1 = file1.listFiles();
		File file2 = new File(inputPath2);
		File[] inputFiles2 = file2.listFiles();
		File[] totalFiles =  new File[inputFiles1.length + inputFiles2.length];
		Arrays.copyOf(original, newLength)
		
		int j = 0;
		for(File f : inputFiles){
			if(j == 40){
				break;
			}
			BufferedReader newReader = Files.newReader(f, Charsets.UTF_8);
			String temp =null ;
			int i = 0 ;
			while((temp=newReader.readLine())!=null){
//				if(i == 2){
//					break;
//				}
				lda.addDoc(String.valueOf(++i),temp) ;
				i++;
			}
			j++;
			System.out.println("File : "+f.getName());
		}
		lda.trainAndSave(outputPath, "utf-8") ;
	}
}
