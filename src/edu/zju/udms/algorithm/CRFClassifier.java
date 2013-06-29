package edu.zju.udms.algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
	private Index featureIndex;
	private FeatureGenerator fGenerator;
	private double alpha = 0.3;
	private double[][] weights;

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
		System.out.println("Index Feature Use Time:" + (end - start) / 1000.0);
		System.err
				.println("Total Number of Features:" + this.featureIndex.size());

		//
		int tagClassSum = Tag.values().length * (Tag.values().length);
		this.weights = new double[this.featureIndex.size()][tagClassSum];
		for (int i = 0; i < this.featureIndex.size(); i++) {
			Arrays.fill(weights[i], 0);
		}

		start = System.currentTimeMillis();
		// use sgd to train the data
		// TODO: random sort all the data.

		for (Sentence sentence : list) {
			Map<Integer, int[]> empiricalFeatureCounterMap = new HashMap<Integer, int[]>();

			// Expectation of Empirical feature
			for (int i = 1; i < sentence.size(); i++) {
				Token token = sentence.getToken(i);
				int tcid = token.getTagclassId();
				int[] fids = token.getFeatureIds();
				for (int j = 0; j < fids.length; j++) {
					int[] count = empiricalFeatureCounterMap.get(fids[j]);
					if (count == null) {
						count = new int[tagClassSum];
						Arrays.fill(count, 0);
						empiricalFeatureCounterMap.put(fids[j], count);
					}
					count[tcid]++;
				}
			}

			double[][] prob = forward_backward(weights, sentence);

			Map<Integer, double[]> modelFeatureStatisticsMap = new HashMap<Integer, double[]>();

			for (int i = 0; i < sentence.size(); i++) {
				Token token = sentence.getToken(i);
				int[] fids = token.getFeatureIds();
				for (int k = 0; k < tagClassSum; k++) {
					for (int j = 0; j < fids.length; j++) {
						double[] statistics = modelFeatureStatisticsMap
								.get(fids[j]);
						if (statistics == null) {
							statistics = new double[tagClassSum];
							Arrays.fill(statistics, 0);
							modelFeatureStatisticsMap.put(fids[j], statistics);
						}
						statistics[k] += prob[i][k];
					}
				}
			}

			assert (empiricalFeatureCounterMap.keySet()
					.equals(modelFeatureStatisticsMap.keySet()));
			
			for (Integer featureId : empiricalFeatureCounterMap.keySet()) {
				int[] count = empiricalFeatureCounterMap.get(featureId);
				double[] statistics = modelFeatureStatisticsMap.get(featureId);
				for (int i = 0; i < count.length; i++) {
					weights[featureId][i] = weights[featureId][i] + this.alpha
							* (count[i] - statistics[i]);
				}
			}
		}
		end = System.currentTimeMillis();
		System.out.println("SGD Train Use Time:" + (end - start) / 1000.0);
	}

	void normalize(double[] a) {
		assert (a != null);
		int sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i];
		for (int i = 0; i < a.length; i++)
			a[i] /= sum;
	}

	double getFactorValue(double[][] weights, int preState, int curState,
			Sentence sentence, int i) {
		assert (i < sentence.size());
		Token token = sentence.getToken(i);
		int[] fids = token.getFeatureIds();
		return getFactorValue(weights, preState, curState, fids);
	}

	double getFactorValue(double[][] weights, int preState, int curState,
			int[] fids) {
		double sum = 0;
		int tagSize = Tag.values().length;
		int tagClass = preState * tagSize + curState;
		for (int fi = 0; fi < fids.length; fi++) {
			int fid = fids[fi];
			if(tagClass==16) System.out.println(preState+"\t"+curState);
			sum += weights[fid][tagClass];
		}
		return Math.exp(sum);
	}

	double[][] forward_backward(double[][] weights, Sentence sentence) {
		// forward algorithm
		int size = sentence.size();
		int tagSize = Tag.values().length;
		double alpha[][] = new double[size][tagSize];

		for (int j = 0; j < tagSize; j++) {
			alpha[0][j] = getFactorValue(weights, Tag.Other.ordinal(), j,
					sentence, 0);
			normalize(alpha[0]);
		}

		for (int i = 1; i < size; i++) {
			for (int j = 0; j < tagSize; j++) {
				alpha[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					alpha[i][j] += alpha[i - 1][k]
							* getFactorValue(weights, k, j, sentence, i);
				}
			}
			normalize(alpha[i]);
		}
		
		

		// backward algorithm
		double beta[][] = new double[size][tagSize];
		for (int j = 0; j < tagSize; j++) {
			beta[size - 1][j] = 1;
			normalize(beta[0]);
		}

		for (int i = size - 2; i >= 0; i--) {
			for (int j = 0; j < tagSize; j++) {
				beta[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					beta[i][j] += beta[i + 1][k]
							* getFactorValue(weights, j, k, sentence, i);

				}
			}
			normalize(beta[i]);
		}

		double u[][] = new double[size][tagSize * tagSize];
		for (int i = 1; i < size ; i++) {
			for (int j = 0; j < tagSize * tagSize; j++) {
				int preState = j % tagSize;
				int curState = j / tagSize;
				u[i][j] = alpha[i-1][preState] * beta[i][curState];
				u[i][j] *= getFactorValue(weights, preState, curState,
						sentence, i);
			}
		}

		double sum = 0;
		for (int i = 0; i < tagSize; i++) {
			sum += alpha[size - 1][i];
		}

		for (int i = 0; i < size - 1; i++) {
			for (int j = 0; j < tagSize * tagSize; j++) {
				u[i][j] /= sum;
			}
		}

		return u;
	}
	
	public void eval(List<Sentence>  list){
		int tokenSum = 0;
		int correctSum = 0;
		
		for(Sentence sen:list){
			tokenSum +=sen.size();
			correctSum += sen.predictCorrectCount();
		}
		double precision = (double) correctSum/tokenSum;
		System.out.printf("Token tag precision:",precision);
		
		int orgsum = 0;
		int correctOrgSum = 0;
		for(Sentence sen:list){
			Map<Integer, String> orgs = sen.getOrg();
			orgsum += orgs.size();
			Map<Integer, String> predictOrgs = sen.getPredictOrg();
			for(Integer id: orgs.keySet()){
				if(predictOrgs.containsKey(id) &&
				orgs.get(id).equals(predictOrgs.get(id))){
					correctOrgSum ++;
				}
			}
		}
		double recall = (double) correctOrgSum/orgsum;
		System.out.printf("Organization recall :",recall);	
	}
	
	public void write(String file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		write(bw);
	}
	
	 public void write(Writer bw) throws IOException {
		this.featureIndex.write(bw);
		for(int i = 0;i<weights.length;i++){
			bw.write(this.weights[i]+"\n");
		}
	 }

	public void classify(List<Sentence> list) {
		for (Sentence sen : list) {
			for (int i = 0; i < sen.size(); i++) {
				this.fGenerator.window(sen, i);
				sen.getToken(i).indexFeatures(this.featureIndex);
			}
			
			int senSize = sen.size();
			int tagSize = Tag.values().length;
			
			//viterbi algorithm
			double a[][] = new double[senSize][tagSize];
			int path[][] = new int[senSize][tagSize];
			for(int j = 0;  j < tagSize; j++){
				a[0][j] = getFactorValue(this.weights, Tag.Other.ordinal(), j, sen,0); 
				normalize(a[0]);
			}
			
			for(int i = 1;i<sen.size();i++){
				for(int j = 0;j<tagSize;j++){
					a[i][j] = 0;
					double value = a[i-1][0] * getFactorValue(this.weights, 0, j, sen, i);
					if(a[i][j]<value){
						a[i][j] = value;
						path[i][j] = 0; 
					}
					for(int k = 1;k<tagSize;k++){
						value = a[i-1][k] * getFactorValue(this.weights, k, j, sen, i);
						if(a[i][j]<value){
							a[i][j] = value;
							path[i][j] = k; 
						}
					}
				}
				normalize(a[i]);
			}
			
			double maxp = a[senSize-1][0];
			int maxpTag = 0;
			for(int j = 1;j<tagSize;j++){
				if(maxp<a[senSize-1][j]){
					maxp = a[senSize-1][j];
					maxpTag = j;
				}
			}
			Tag[] values = Tag.values();
			sen.getToken(senSize-1).setPredictTag(values[maxpTag]);
			
			for(int i = senSize-1;i>1;i--){
				maxpTag = path[i][maxpTag];
				sen.getToken(i-1).setPredictTag(values[maxpTag]);
			}	
		}
	}

	public void indexFeatures(List<Sentence> list) {

	}

}
