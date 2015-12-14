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
		LDA lda = new LDA(AnsjAnalysis.DEFAUlT,new LDAGibbsModel(100, 50/(double)100, 0.02, 150, Integer.MAX_VALUE, Integer.MAX_VALUE));
//		String inputPath = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\baike";
		// windows
		/*
  		String inputPath1 = "E:\\work\\project\\site-analysis\\ContentRetrieve\\auto\\brand\\thined";
		String inputPath2 = "E:\\work\\project\\site-analysis\\ContentRetrieve\\auto\\topic\\thined";
		String outputPath = "E:\\work\\project\\site-analysis\\test";
		*/	
		// linux
		int k ;
		int i = 0;
		// part of autohome + baike
//		String inputPath0 = "/home/yuanpingzhou/data/ansj_lda/input/baike";
//		String inputPath1 = "/home/yuanpingzhou/data/ansj_lda/input/autotopicpost";
//		String inputPath2 = "/home/yuanpingzhou/data/ansj_lda/input/autobrandpost";

//		File file0 = new File(inputPath0);
//		File[] inputFiles0 = file0.listFiles();
//		File file1 = new File(inputPath1);
//		File[] inputFiles1 = file1.listFiles();
//		File file2 = new File(inputPath2);
//		File[] inputFiles2 = file2.listFiles();
//		File[] totalFiles =  new File[inputFiles0.length + inputFiles1.length + inputFiles2.length];

//		for(k = 0;k < inputFiles0.length;k++){
//			totalFiles[i++] = inputFiles0[k];
//		}
//		for(k = 0;k < inputFiles1.length;k++){
//			totalFiles[i++] = inputFiles1[k];
//		}
//		for(k =0 ;k < inputFiles2.length;k++){
//			totalFiles[i++] = inputFiles2[k];
//		}
		String outputPath = "/home/yuanpingzhou/data/ansj_lda/output/AutoTopicEst";
		String inputPath = "/home/yuanpingzhou/data/ansj_lda/input/AutoTopicEst";
		File f = new File(inputPath);
		File[] totalFiles = f.listFiles();
		System.out.println("total files : " + totalFiles.length);
		k = 0 ;
		for(i = 0; i < totalFiles.length; i += 1){
		//	if(i > 800){
		//		break;
		//	}
			BufferedReader newReader = Files.newReader(totalFiles[i], Charsets.UTF_8);
			String temp =null ;
			int j = 0;
			while((temp=newReader.readLine())!=null){
				try{		
					lda.addDoc(String.valueOf(++k),temp) ;
				}	
				catch (Exception e){
					System.out.println("line error : " + j);
					break;
				}
				j++;
				k++;
			}
			System.out.println("File : " + totalFiles[i].getName());
		}
		lda.trainAndSave(outputPath, "utf-8") ;
	}
}
