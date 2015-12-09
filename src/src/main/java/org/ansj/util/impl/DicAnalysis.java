package org.ansj.util.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

//import love.cq.domain.Forest;
//import love.cq.library.Library;
//import love.cq.splitWord.GetWord;
//import love.cq.util.IOUtil;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.tire.domain.*;
import org.nlpcn.commons.lang.tire.library.*;
import org.nlpcn.commons.lang.tire.*;

import org.ansj.util.Analysis;

public class DicAnalysis implements Analysis {

	public static Analysis getInstance(File dic, String charset) {
		try {
			return new DicAnalysis(dic, charset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Forest forest = null;

	public DicAnalysis(File dic, String charset) throws Exception {
		forest = initForest(dic, charset);
	}

	private Forest initForest(File dic, String charset) throws Exception {
		BufferedReader reader = IOUtil.getReader(dic.getAbsolutePath(), charset);
		return Library.makeForest(reader);
	}

	@Override
	public List<String> getWords(Reader reader) throws IOException {
		// TODO Auto-generated method stub
		List<String> all = new ArrayList<String>();
		String temp = null;
		BufferedReader br = new BufferedReader(reader);
		while ((temp = br.readLine()) != null) {
			GetWord gw = new GetWord(forest, temp);
			while ((temp = gw.getFrontWords()) != null) {
				all.add(temp);
			}
		}
		return all;
	}

	@Override
	public boolean filter(String word) {
		// TODO Auto-generated method stub
		return false;
	}

}
