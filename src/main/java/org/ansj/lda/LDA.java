package org.ansj.lda;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

import org.ansj.inference.Model;
import org.ansj.lda.impl.LDAGibbsModel;
import org.ansj.util.Analysis;
import org.ansj.util.impl.AnsjAnalysis;

import com.google.common.io.Files;

/**
 * LDA 的主类
 * 
 * @author ansj
 * 
 */
public class LDA {

	/**
	 * 训练模型类
	 */
	private LDAModel ldaAModel = null;

	/*
	 * inference model
	 */
	private Model inferModel = null;
	
	/**
	 * 集成分词
	 */
	private Analysis analysis = null;

	/**
	 * @param modelPath模型的存储路径
	 */
	public LDA(int topicNum) {
		this.analysis = AnsjAnalysis.DEFAUlT;
		this.ldaAModel = new LDAGibbsModel(topicNum, 50/(double)topicNum, 0.1, 100, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param modelPath存储路径
	 * @param analysis分词器
	 * @param ldaModel模型
	 */
	public LDA(Analysis analysis, LDAModel ldaModel) {
		this.analysis = analysis;
		this.ldaAModel = ldaModel;
	}

	/*
	 * inference initiation
	 */
	public LDA(Analysis analysis, Model inferModel,int docNum){
		this.analysis = analysis;
		this.inferModel = inferModel;
		this.inferModel.init();
		this.inferModel.SetDocNum(docNum);
	}
	
	/**
	 * 用户自定义分词器的设置
	 * 
	 * @param analysis
	 */
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}

	/**
	 * LDA 根据文件训练.一个文件相当于一个文档,ps:文件不好太大
	 * 
	 * @throws IOException
	 */
	public void addDoc(File file, String charset) throws IOException {
		addDoc(file.getName(),Files.newReader(file, Charset.forName(charset)));
	}

	/**
	 * LDA 根据文本训练,一个文本相当于一个文档
	 */
	public void addDoc(String name ,String content) {
		addDoc(name,new StringReader(content));
	}

	/**
	 * LDA 根据文本训练,一个流相当于一个文档
	 */
	public void addDoc(String name ,Reader reader) {
		List<String> words = null;
		try {
			words = analysis.getWords(reader);
			ldaAModel.addDoc(name,words);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * add doc for inference
	 */
	public void AddDoc(String name,String content){
		Reader reader = new StringReader(content);
		List<String> words = null;
		try {
			words = analysis.getWords(reader);
			inferModel.AddDoc(name,words);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * inference and save inference model
	 */
	public void inferAndSave() throws IOException{
		if(inferModel.InitInfer() == false){
			System.out.println("init infer failed !");
			return ;
		}
		
		if(inferModel.Inference() == false){
			System.out.println("inference failed ! ");
			return;
		}
		
		if(inferModel.SaveInfModel() == false){
			System.out.println("save infer failed !");
			return;
		}
	}
	/*
	 * train and save estimate model
	 */
	public void trainAndSave(String modelPath, String charset) throws IOException {
		ldaAModel.trainAndSave(modelPath, charset);
	}

}
