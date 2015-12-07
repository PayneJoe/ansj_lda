package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.ansj.lda.LDA;
import org.ansj.lda.impl.LDAGibbsModel;
import org.ansj.util.impl.AnsjAnalysis;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Test3 {

	public static void main(String[] args) throws IOException {
		LDA lda = new LDA(AnsjAnalysis.DEFAUlT,new LDAGibbsModel(40, 50/(double)40, 0.02, 150, Integer.MAX_VALUE, Integer.MAX_VALUE));
		String inputPath = "E:\\work\\project\\site-analysis\\ContentRetrieve\\video\\baike";
		File file = new File(inputPath);
		File[] inputFiles = file.listFiles();
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
		lda.trainAndSave("E:\\work\\project\\site-analysis\\test", "utf-8") ;
	}
}
