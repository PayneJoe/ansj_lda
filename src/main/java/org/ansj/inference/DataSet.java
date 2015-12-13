package org.ansj.inference;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ansj.lda.pojo.Doc;
import org.ansj.lda.pojo.Vector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public class DataSet {
	protected ArrayList<Doc> docs ;
//	protected ArrayList<Doc> _docs; // used only for inference
	
	protected BiMap<Integer, Integer> newId2trnId ; // bidirectional map, positive one is new to train 
	
	protected int M;                  // number of documents
	protected int V;				// number of words
	
	protected BiMap<String, Integer> trnWordMap;  // global
//	protected HashMap<String,Integer> newWordMap;  // local
	
	//
	public DataSet(){
		docs = Lists.newArrayList();
//		_docs = Lists.newArrayList();
		
		newId2trnId = HashBiMap.create();
		
		M = 0;
		V = 0;
	}
	//
	public DataSet(int m){
		M = m;
		V = 0;
		
		docs = Lists.newArrayListWithCapacity(M);
//		_docs = null;
		
		newId2trnId = HashBiMap.create();
	}
	
	//
	public void SetDocNum(int num){
		if(docs.isEmpty() == false){
			docs.clear();
		}
		M = num;
		docs = Lists.newArrayListWithCapacity(num);
	}
	
	public void AddDoc(String name,List<String> words,int nTopics){

		Doc trnDoc = new Doc(name,nTopics);
		for(String w : words){
            if (trnWordMap.containsKey(w) == false){
            	// todo
            }
            else{
            	int trnWordId = trnWordMap.get(w);
            	int newWordId ;
            	if(newId2trnId.inverse().containsKey(trnWordId) == false){
            		newWordId = newId2trnId.size();
            		newId2trnId.put(newWordId, trnWordId);
            		V++;
            	}
//            	else{
//            		newWordId = newId2trnId.inverse().get(trnWordId);
//            	}
            	int topicId = (int)Math.floor(Math.random() * nTopics);
            	trnDoc.addVector(new Vector(trnWordId,topicId));
            }
		}
		docs.add(trnDoc);
	}
}
