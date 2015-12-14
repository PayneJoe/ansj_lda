package org.ansj.tm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.ansj.inference.Model;
import org.ansj.lda.LDA;
import org.ansj.util.impl.AnsjAnalysis;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class InferTest {
	public static void main(String[] args) throws IOException {
		String inputModelPath = "";
		String inputCorpusPath = "";
		String outputModelPath = "";
		int twords = 30;
		int topicNum = 20;
		String charset = "utf8";
		int niter = 100;
		boolean isInfer = true;
		int inferDocNum = 10000;
		LDA lda = new LDA(AnsjAnalysis.DEFAUlT,new Model(inputModelPath,outputModelPath,charset,niter,twords,isInfer),inferDocNum);
		File file = new File(inputCorpusPath);
		File[] inputFiles = file.listFiles();
		int k = 0;
		for(int i = 0; i < inputFiles.length; i += 1){
		//	if(i > 800){
		//		break;
		//	}
			BufferedReader newReader = Files.newReader(inputFiles[i], Charsets.UTF_8);
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
			System.out.println("File : " + inputFiles[i].getName());
		}
		lda.inferAndSave() ;
		return ;
	}
}
