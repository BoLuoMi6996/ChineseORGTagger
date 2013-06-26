package edu.zju.udms.algorithm;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.zju.udms.model.Index;
import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;
import edu.zju.udms.model.Token;
import edu.zju.udms.tag.FeatureGenerator;

public class CRFClassifier {
	private String modelFile;
	private Index featureIndex;
	private FeatureGenerator fGenerator;
	private double alpha = 0.03;

	public CRFClassifier() {
		this.featureIndex = new Index();
		this.fGenerator = new FeatureGenerator();
	}

	public void train(List<Sentence> list) {
		assert (list != null);
		long start = System.currentTimeMillis();
		for (Sentence sen : list) {
			for (int i = 0; i < sen.size(); i++) {
				this.fGenerator.window(sen, i);
				sen.getToken(i).indexFeatures(this.featureIndex);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Index Feature Use Time:"+(end-start)/1000.0);
        System.err.print("Total Number of Features:" + this.featureIndex.size());
		
		//
		int tagClassSum = Tag.values().length * (Tag.values().length);
		double[][] weights = new double[this.featureIndex.size()][tagClassSum];
		for (int i = 0; i < this.featureIndex.size(); i++){
			Arrays.fill(weights[i], 0);
		}
		
		start = System.currentTimeMillis();
		// use sgd to train the data
		// TODO: random sort all the data.
		
		for (Sentence sentence : list) {
			double[][] weightsHat = new double[this.featureIndex.size()][tagClassSum];
			Map<Integer, int[]> featureCounterMap = new HashMap<Integer, int[]>();
			for (int i = 0; i < sentence.size(); i++) {
				Token token = sentence.getToken(i);
				int tcid = token.getTagclassId();
				int[] fids = token.getFeatureIds();
				for (int j = 0; j < fids.length; j++) {
					int[] count = featureCounterMap.get(fids[j]);
					if (count == null) {
						count = new int[tagClassSum];
						Arrays.fill(count, 0);
						featureCounterMap.put(fids[j], count);
					}
					count[tcid]++;
				}
			}

			for (int i = 0; i < weights.length; i++) {
				int[] eef = featureCounterMap.get(i);
				if(eef==null){
					eef = new int[tagClassSum];
					Arrays.fill(eef,0);
				}
				double emf[] = new double[tagClassSum];
				Arrays.fill(emf, 0);
				
				double[][] prob = forward_backward(weights, sentence);
				
				for (int k = 0; k < tagClassSum; k++) {
					for (int j = 0; j < sentence.size(); j++) {
						double p = eef[k] * prob[j][k];
						emf[k] += p;
					}
				}
				for (int j = 0; j < tagClassSum; j++) {
					weightsHat[i][j] = weights[i][j] + this.alpha
							* (eef[j] - emf[j]);
				}
			}
			weights = weightsHat;
		}
		end = System.currentTimeMillis();
		System.out.println("SGD Train Use Time:"+(end-start)/1000.0);
	}

	double[][] forward_backward(double[][] weights, Sentence sentence) {
		// forward algorithm
		int size = sentence.size();
		int tagSize = Tag.values().length;
		double alpha[][] = new double[size][tagSize];
		for (int i = 0; i < size; i++) {
			Token token = sentence.getToken(i);
			int fids[] = token.getFeatureIds();
			for (int j = 0; j < tagSize; j++) {
				alpha[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					int tagClass = k * tagSize + j;
 					for (int fi = 0; fi < fids.length; fi++) {
 						if(i>1){
 							alpha[i][j] += alpha[i - 1][k]
								* Math.exp(weights[fids[fi]][tagClass]);
 						}else{
 							alpha[i][j] += Math.exp(weights[fids[fi]][tagClass]);
 						}
					}
				}
			}
		}

		// backward algorithm
		double beta[][] = new double[size][tagSize];
		for (int i = size - 1; i >= 0; i--) {
			Token token = sentence.getToken(i);
			int fids[] = token.getFeatureIds();
			for (int j = 0; j < tagSize; j++) {
				beta[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					int tagClass = j * tagSize + k;
					for (int fi = 0; fi < fids.length; fi++) {
						if(i+1<size){
							beta[i][j]+= beta[i + 1][k]
								 * Math.exp(weights[fids[fi]][tagClass]);
						}else{
							beta[i][j] += Math.exp(weights[fids[fi]][tagClass]);
						}
					}
				}
			}
		}

		Token token = sentence.getToken(0);
		int fids[] = token.getFeatureIds();
		double u[][] = new double[size][tagSize * tagSize];
		for (int j = 0; j < tagSize * tagSize; j++) {
			int preTagId = j % tagSize;
			int curTagId = j / tagSize;
			if(preTagId== Tag.Other.ordinal()){
				u[0][j] =  beta[0][curTagId];
			}else{
				u[0][j] = 0; 
			}
			double sumOfWeights  = 0;
			for (int fi = 0; fi < fids.length; fi++) {
				sumOfWeights += weights[fids[fi]][j];
			}
			u[0][j] *= Math.exp(sumOfWeights);
		}
		
		for (int i = 1; i < size; i++) {
			token = sentence.getToken(i);
			fids = token.getFeatureIds();
			for (int j = 0; j < tagSize * tagSize; j++) {
				int preTagId = j % tagSize;
				int curTagId = j / tagSize;
				u[i][j] = alpha[i-1][preTagId] * beta[i][curTagId];
				double sumOfWeights  = 0;
				for (int fi = 0; fi < fids.length; fi++) {
					sumOfWeights += weights[fids[fi]][j];
				}
				u[i][j] *= Math.exp(sumOfWeights);
			}
		}

		double sum = 0;
		for(int i = 0; i<tagSize;i++){
			sum += alpha[size-1][i];
		}
		
		for(int i = 0;i<size;i++){
			for(int j = 0;j<tagSize * tagSize;j++){
				u[i][j] /= sum; 
			}
		}
		
		return u;
	}

	public void classify() {

	}

	public void indexFeatures(List<Sentence> list) {

	}

}
