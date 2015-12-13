package org.ansj.inference;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.ansj.lda.pojo.Doc;
import org.ansj.lda.pojo.Vector;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

public class Model {
	
	//
	protected String inputModelPath;
	protected String outputModelPath;
	protected String modelName;
	protected String charSet;
	
	// data
	protected DataSet data;
	
	// --- model parameters and variables ---    
	protected int M;					// dataset size (i.e., number of docs)
	protected int V;					// vocabulary size
	protected int K;					// number of topics
	protected double alpha, beta;		// LDA hyperparameters 
	protected int niters;				// number of Gibbs sampling iterations
	protected int liter;				// the iteration at which the model was saved
	protected int savestep;			   // saving period
	protected int twords;				// print out top words per each topic
    
	protected double[] p ; 				// temp variable for sampling
	protected TIntArrayList[] z;				// topic assignments for words, size M x doc.size()
							
	protected int[][] nw;				// cwt[i][j]: number of instances of word/term i assigned to topic j, size V x K，
								
	protected int[][] nd ;				// na[i][j]: number of words in document i assigned to topic j, size M x K
								
	protected int[] nwsum ;			// nwsum[j]: total number of words assigned to topic j, size K
								
	protected int[] ndsum ;			// nasum[i]: total number of words in document i, size M
								
	public double[][] theta ;		// theta: document-topic distributions, size M x K

	public double[][] phi ;		// phi: topic-word distributions, size K x V
		
    // for inference only
	protected boolean perplexityFlag;
    protected int inf_liter;
    protected int			newM;
    protected int			newV;
    protected TIntArrayList[]		newz;
    protected int[][]		newnw;
    protected int[][]		newnd;
    protected int[]	newnwsum;
    protected int[]		newndsum;
    protected double[][]	newtheta;
    protected double[][]	newphi  ;
    protected double[] perplexity;
	
	// constructor
	public Model(String inputPath,String outputPath,String charSetName,int iter,int ntop,boolean pFlag){
	    
		inputModelPath = inputPath;
		outputModelPath = outputPath;
		modelName = "lda_result";
		charSet = charSetName;
		
		data = null;
		
	    M = 0;
	    V = 0;
	    K = 20;
	    alpha = 50.0 / K;
	    beta = 0.1;
	    if(iter > 0){
	    	niters = iter;
	    }
	    else{
	    	niters = 100;
	    }
	    liter = 0;
	    savestep = 0;
	    if(ntop > 0){
	    	twords = ntop;
	    }
	    else{
	    	twords = 50;
	    }
	    perplexityFlag = pFlag;
	    
	    p = null;
	    z = null;
	    nw = null;
	    nd = null;
	    nwsum = null;
	    ndsum = null;
	    theta = null;
	    phi = null;
	    
	    newM = 0;
	    newV = 0;
	    newz = null;
	    newnw = null;
	    newnd = null;
	    newnwsum = null;
	    newndsum = null;
	    newtheta = null;
	    newphi = null;
	}
	
	/*
	 * initiation with previous model
	 */
	public boolean init(){
		// load parameters from previous model
		if(LoadParams() == false){
			System.out.println("Failed to load params file of the model !");
			return false;
		}
		// load model doc-word-topic assignment
		if(LoadModel() == false){
			System.out.println("Fail to load word-topic assignmetn file of the model!");
			return false;
		}
		// load word map of previous training
		if(LoadWordMap() == false){
			System.out.println("No word map available! ");
			return false;
		}
		// word-topic matrix
		nw = new int[V][K];
		for (int w = 0; w < V; w++){
			nw[w] = new int[K];
			for (int k = 0; k < K; k++)	
				nw[w][k] = 0;
		}
		// doc-topic matrix
		nd = new int[M][K];
		for (int m = 0; m < M; m++){
			nd[m] = new int[K];
			for (int k = 0; k < K; k++)	
				nd[m][k] = 0;
		}
		// topic margin
		nwsum = new int[K];
		for (int k = 0; k < K; k++)	
			nwsum[k] = 0;
		// doc margin,equally number of words in one doc    
		ndsum = new int[M];
		for (int m = 0; m < M; m++)	
			ndsum[m] = 0;
		// assign values for nw, nd, nwsum, and ndsum
		for (int m = 0; m < data.M; m++){
				Doc mydoc = data.docs.get(m);
				// total number of words in one doc
				int N = mydoc.vectors.size();

		        for (int n = 0; n < N; n++){
		        	Vector wt = mydoc.vectors.get(0);
		        	int w = wt.id;
		    	    int topic = wt.topicId;
		    	    
		    	    // number of instances of word i assigned to topic j
		    	    nw[w][topic] += 1;
		    	    // number of words in document i assigned to topic j
		    	    nd[m][topic] += 1;
		    	    // total number of words assigned to topic j
		    	    nwsum[topic] += 1;
		        } 
		        // total number of words in document i
		        ndsum[m] = N;
		}
		// clear doc-word-topic assignment
		data.docs.clear();
		
		return true;
	}
	
	// called before AddDoc
	/*
	 * set number of inference doc
	 */
	public boolean SetDocNum(int num){
		data.SetDocNum(num);
		return true;
	}
	/*
	 *  add doc one-by-one
	 */
	public boolean AddDoc(String name,List<String> words){
		
		data.AddDoc(name,words,K);
		
		return true;
	}
	/*
	 * initiation once added all docs
	 */
	public boolean InitInfer(){
		 newM = data.M;
		 newV = data.V;					//the size of terms in unseen data dictionary.
			
		 newnw = new int[newV][K];
		 for (int w = 0; w < newV; w++){
			 newnw[w] = new int[K];
			 for(int k = 0; k < K; k++){
				 newnw[w][k] = 0;
			 }
		  }
			
		  newnd = new int[newM][K];
		  for (int m = 0; m < newM; m++){
			  newnd[m] = new int[K];
		      for(int k = 0; k < K;k++){
		    	  newnd[m][k] = 0;
		      }
		  }
			
		  newnwsum = new int[K];
		  for (int k = 0; k < K; k++)
			  newnwsum[k] = 0;
		  
		  newndsum = new int[newM];
		  for (int m = 0; m < newM; m++)
			  newndsum[m] = 0;

		  newz = new TIntArrayList[M];
		  for (int m = 0; m < data.M; m++){
				Doc _mydoc= data.docs.get(m);
				int N = _mydoc.vectors.size();
				newz[m] = new TIntArrayList();
												// assign values for nw, nd, nwsum, and ndsum	
		        for (int n = 0; n < N; n++) 
				{
		    	    Vector wt = _mydoc.vectors.get(n);
		    	    int topicId = wt.topicId;
		    	    int newWordId = data.newId2trnId.inverse().get(wt.id);
		    	    newz[m].add(topicId);
		    	    // number of instances of word i assigned to topic j
		    	    newnw[newWordId][topicId] += 1;		  //unseen data  wordIndex...********

		    	    // number of words in document i assigned to topic j
		    	    newnd[m][topicId] += 1;
		    	    // total number of words assigned to topic j
		    	    newnwsum[topicId] += 1;
		        } 
		        // total number of words in document i
		        newndsum[m] = N;      
		    }    
		    
			// initiate newtheta and newphi
		    newtheta = new double[newM][K];	  
		    newphi = new double[K][newV];
		// todo
		
		return true;
	}
	
	public boolean Inference() throws IOException{
	    System.out.println("Sampling " + niters + " iterations for inference!\n");
	    double result=0.0;
	    if(perplexityFlag == true){
	    	perplexity = new double[niters];
	    }
	    for(inf_liter = 1; inf_liter <= niters; inf_liter++){
	    	double perplexity_result = 0.0;
			System.out.println("Iteration " + inf_liter + " ...\n");
			// for all newz_i
			for(int m = 0; m < newM; m++){
				for(int n = 0; n < data.docs.size(); n++){
					// (newz_i = newz[m][n])
					// sample from p(z_i|z_-i, w)
					int topic = InfSampling(m, n);
					newz[m].set(n, topic);
				}
			}
			if(perplexityFlag){
				ComputeNewtheta();
				ComputeNewphi()  ;
				perplexity_result = TestPerplexity();
				perplexity[inf_liter] = perplexity_result;
				System.out.println("Perplexity is : " + perplexity_result);
			}
	    }
	    
	    System.out.println("Gibbs sampling for inference completed!");
	    System.out.println("Saving the inference outputs!");
	    ComputeNewtheta();
	    ComputeNewphi();
		inf_liter--;
		savestep = inf_liter;

	    if(SaveInfModel() == false){
	    	System.out.println("Save inference model failed !");
	    	return false;
	    }
		return true;
	}
	
	private boolean SaveInfModel() throws IOException
	{
		File modelDir = new File(outputModelPath);
		// 创建路径
		if (!modelDir.isDirectory()) {
			modelDir.mkdirs();
		}

		Charset charset = Charset.forName(charSet);

		/**
		 * 配置信息
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("alpha = " + alpha + "\n");
		sb.append("beta = " + beta + "\n");
		sb.append("topicNum = " + K + "\n");
		sb.append("iterations = " + niters + "\n");
		sb.append("termNum = " + newV + "\n");
		sb.append("docNum = " + newM + "\n");
		
//		sb.append("saveStep = " + saveStep + "\n");
//		sb.append("beginSaveIters = " + beginSaveIters);

		Files.write(sb, new File(modelDir, modelName + ".params"), charset);

		/**
		 * lda.phi K*V
		 */
		BufferedWriter writer = Files.newWriter(new File(modelDir, modelName + ".phi"), charset);
		for (int i = 0; i < K; i++) {
			writer.write(Joiner.on("\t").join(Doubles.asList(newphi[i])));
			writer.write("\n".toCharArray());
		}
		writer.flush();
		writer.close();

		// lda.theta M*K
		writer = Files.newWriter(new File(modelDir, modelName + ".theta"), charset);
		for (int i = 0; i < newM; i++) {
			writer.write(data.docs.get(i).getName() +"\t") ;
			writer.write(Joiner.on("\t").join(Doubles.asList(newtheta[i])));
			writer.write("\n".toCharArray());
		}
		writer.flush();
		writer.close();

		// lda.tassign
		writer = Files.newWriter(new File(modelDir, modelName + ".tassign"), charset);
		Doc doc = null;
		Vector vector = null;
		for (int m = 0; m < newM; m++) {
			doc = data.docs.get(m);
			writer.write(doc.getName()+"\t") ;
			for (int n = 0; n < doc.vectors.size(); n++) {
				vector = doc.vectors.get(n);
				writer.write(vector.id + ":" + vector.topicId + "\t");
			}
			writer.write("\n");
		}
		writer.flush();
		writer.close();

		// lda.twords phi[][] K*V
		writer = Files.newWriter(new File(modelDir, modelName + ".twords"), charset);
		// default number of keywords under a topic
		double[] scores = null;
		VecotrEntry pollFirst = null;
		for (int i = 0; i < twords; i++) {
			writer.write("topic " + i + "\t:\n");
			MinMaxPriorityQueue<VecotrEntry> mmp = MinMaxPriorityQueue.create();
			scores = phi[i];
			for (int j = 0; j < newV; j++) {
				mmp.add(new VecotrEntry(j, scores[j]));
			}

			for (int j = 0; j < K; j++) {
				if (mmp.isEmpty()) {
					break;
				}
				pollFirst = mmp.pollFirst();
				writer.write("\t" + data.trnWordMap.inverse().get(pollFirst.id) + " " + pollFirst.score + "\n");
			}
			writer.write("\n");
		}
		writer.flush();
		writer.close();
		
		// perplexity 
		writer = Files.newWriter(new File(modelDir, modelName + ".pp"), charset);
		for(double p : perplexity){
			writer.write(p+"\n");
		}
	    writer.flush();
	    writer.close();
	    
	    return true;
	}
	
	private double TestPerplexity(){
		double result       = 0.0 ;
	    double expindex  = 0.0    ;
	    double wordcount    =  0  ;		//thewords count in the whole documents.
	    int k ;
	 	for(k=0;k<K;k++){
	 		p[k] = 0 ;
	 	}

	    for (int m = 0;m < newM; m++){
	 		 Doc  _mydoc = data.docs.get(m);
	         for (int n = 0; n < _mydoc.vectors.size(); n++){
	        	 Vector wt = _mydoc.vectors.get(n);
	             int v =  data.newId2trnId.inverse().get(wt.id);
	             for(k=0 ; k<K ; k++ )
	            	 p[k] = newtheta[m][k]*newphi[k][v];

	             for(k = 1; k < K; k++)
	            	 p[k] += p[k-1];

	             expindex += Math.log(p[k-1]);
	         }
	         wordcount+=ndsum[m];
	     }
	     result = Math.exp(-expindex/wordcount);
	     return result; 
	 }
	
	// compute newtheta
	private void ComputeNewtheta(){
	     for (int m = 0; m < newM; m++){
	 		for (int k = 0; k < K; k++){
	 			newtheta[m][k] = ( newnd[m][k]+alpha )/( newndsum[m] + K*alpha );
	 		}
	     }
	 }
	 // compute new phi
	 private void ComputeNewphi(){
		 for (int k = 0; k < K; k++){
	 		for (int w = 0; w < newV; w++){
	 			int trnWordId = data.newId2trnId.get(w);
	 			newphi[k][w] = (nw[trnWordId][k] + newnw[w][k]+beta) / (nwsum[k] + newnwsum[k] + V * beta);
	 		}
	     }
	 }
	 
	 // inference sampling
	 private int InfSampling(int m, int n){
	     // remove z_i from the count variables
	     int topic = newz[m].get(n);
	     Vector  wt = data.docs.get(m).vectors.get(n);
	     int w = wt.id;
	     int _w = data.newId2trnId.inverse().get(w);
	     newnw[_w][topic] -= 1;
	     newnd[m][topic]  -= 1;
	     newnwsum[topic]  -= 1;
	     newndsum[m]      -= 1;
	     
	     double Vbeta =  V  * beta;
	     double Kalpha = K * alpha;
	   
	     for (int k = 0; k < K; k++){
	    	 p[k] = (nw[w][k] + newnw[_w][k] + beta) / (nwsum[k] + newnwsum[k] + Vbeta) *(newnd[m][k] + alpha) / (newndsum[m] + Kalpha);
	     }
	     // cumulate multinomial parameters
	     for(int k = 1; k < K; k++){
	 		p[k] += p[k - 1];
	     }
	     
	     // scaled sample because of unnormalized p[]
	     double u=((double)Math.random())*p[K-1];
	     
	     for(topic = 0; topic < K; topic++){
	 		if (p[topic] > u)
	 			break; 
	     }
	     
	     // add newly estimated z_i to count variables
	     newnw[_w][topic] += 1;
	 	 newnwsum[topic]  += 1;

	     newnd[m][topic]  += 1;
	     newndsum[m] += 1;
	     
	     return topic;
	 }
	/*
	 * read word map of training period
	 */
	private boolean LoadWordMap(){
    	HashMap<String,Integer> hs = new HashMap<String,Integer>();
        try{
        	String wordMapFile = inputModelPath + modelName + ".wordmap";
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(
                            new FileInputStream(wordMapFile)), "UTF-8"));
            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                StringTokenizer wid = new StringTokenizer(line, "\t");
                data.trnWordMap.put(wid.nextToken(), Integer.parseInt(wid.nextToken()));
            }
            reader.close();
        }
        catch (Exception e) {
            System.out.println("Error while reading dictionary:" + e.getMessage());
            e.printStackTrace();
            return false;
        }	
		return true;
	}
	
	/*
	 * read parameters of training period
	 */
	private boolean LoadParams(){
		try {
			String otherFile = inputModelPath + modelName +".params"; 
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(
                            new FileInputStream(otherFile)), "UTF-8"));
            String line;
            while((line = reader.readLine()) != null){
                StringTokenizer tknr = new StringTokenizer(line,"= \t\r\n");

                int count = tknr.countTokens();
                if (count != 2)
                    continue;

                String optstr = tknr.nextToken();
                String optval = tknr.nextToken();

                if (optstr.equalsIgnoreCase("alpha")){
                    alpha = Double.parseDouble(optval);					
                }
                else if (optstr.equalsIgnoreCase("beta")){
                    beta = Double.parseDouble(optval);
                }
                else if (optstr.equalsIgnoreCase("topicNum")){
                    K = Integer.parseInt(optval);
                }
                else if (optstr.equalsIgnoreCase("docNum")){
                    M = Integer.parseInt(optval);
                }
                else if (optstr.equalsIgnoreCase("termNum")){
                    V = Integer.parseInt(optval);
                }
                else if (optstr.equalsIgnoreCase("iterations")){
                    liter = Integer.parseInt(optval);
                }
                else {
                    // any more?
                }
            }
            reader.close();
        }
        catch (Exception e){
            System.out.println("Error while reading other file:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	// load doc-word-topic assign file of previous period
	private boolean LoadModel(){
		try {
            int i,j;
            String tassginFile = inputModelPath + modelName + ".tassign";
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(
                            new FileInputStream(tassginFile)), "UTF-8"));
            String line;
            if(this.M <= 0){
            	System.out.println("Only find dcount illegal When loading tassign file !");
            	return false;
            }
            data = new DataSet(M);
            data.V = V;
            for (i = 0; i < M; i++){
                line = reader.readLine();
                StringTokenizer tknr = new StringTokenizer(line, " \t\r\n");

                int length = tknr.countTokens();
                Doc doc = new Doc(i+"",K);
                for (j = 0; j < length; j++){
                    String token = tknr.nextToken();

                    StringTokenizer tknr2 = new StringTokenizer(token, ":");
                    if (tknr2.countTokens() != 2){
                        System.out.println("Invalid word-topic assignment line\n");
                        return false;
                    }
                    doc.addVector(new Vector(Integer.parseInt(tknr2.nextToken()),Integer.parseInt(tknr2.nextToken())));
                }//end for each topic assignment

                data.docs.add(doc);

            }//end for each doc
            reader.close();
        }
        catch (Exception e){
            System.out.println("Error while loading model: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
		return true;
	}
	static class VecotrEntry implements Comparable<VecotrEntry> {
		int id;
		double score;

		public VecotrEntry(int id, double score) {
			this.id = id;
			this.score = score;
		}

		@Override
		public int compareTo(VecotrEntry o) {
			// TODO Auto-generated method stub
			if (this.score > o.score) {
				return -1;
			} else {
				return 1;
			}
		}

	}
}
