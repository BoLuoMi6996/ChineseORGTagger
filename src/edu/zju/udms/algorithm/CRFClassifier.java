package edu.zju.udms.algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private double alpha = 0.01;
	private double sigma = 100;
	private double[][] weights;

	public CRFClassifier() {
		this.featureIndex = new Index();
		this.fGenerator = new FeatureGenerator();
	}

	public double logLiklihood(Sentence sentence) {
		double liklihood = 0;
		for (int i = 0; i < sentence.size(); i++) {
			liklihood += Math.log(getFactorValue(weights, sentence, i));
		}
		liklihood -= Math.log(getz(sentence));
		for (int i = 0; i < this.weights.length; i++) {
			for (int j = 0; j < 16; j++) {
				liklihood -= this.weights[i][j] * this.weights[i][j] / 2
						/ this.sigma / this.sigma;
			}
		}
		return liklihood;
	}

	public double getz(Sentence sentence) {
		// forward algorithm
		int size = sentence.size();
		int tagSize = Tag.values().length;
		double alpha[][] = new double[size][tagSize];

		for (int j = 0; j < tagSize; j++) {
			alpha[0][j] = getFactorValue(weights, Tag.Other.ordinal(), j,
					sentence, 0);
		}
		// The first element cannot be Tag.inter or Tag.End
		alpha[0][Tag.Inter.ordinal()] = 0;
		alpha[0][Tag.End.ordinal()] = 0;

		for (int i = 1; i < size; i++) {
			for (int j = 0; j < tagSize; j++) {
				alpha[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					if (Tag.adjacent(Tag.index(k), Tag.index(j))) {
						alpha[i][j] += alpha[i - 1][k]
								* getFactorValue(weights, k, j, sentence, i);
					}
				}
			}
		}

		double z = 0;
		for (int i = 0; i < tagSize; i++) {
			z += alpha[sentence.size() - 1][i];
		}
		return z;
	}

	public double[][] getu(double alpha[][], double beta[][]) {
		return null;
	}

	public void train(List<Sentence> list, int n) {

		Collections.shuffle(list);
		List<List<Sentence>> nfoldList = new ArrayList<List<Sentence>>();

	}

	public void train(List<Sentence> list, boolean random) {

		if (random) {
			Collections.shuffle(list);
		}

		assert (list != null);
		long start = System.currentTimeMillis();
		int N = list.size();
		for (Sentence sen : list) {
			for (int i = 0; i < sen.size(); i++) {
				this.fGenerator.window(sen, i);
				sen.getToken(i).indexFeatures(this.featureIndex);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Index Feature Use Time:" + (end - start) / 1000.0);
		// System.err.print("Total Number of Features:"
		// + this.featureIndex.size());

		System.err.print(this.featureIndex.size());

		//
		int tagClassSum = Tag.values().length * (Tag.values().length);
		this.weights = new double[this.featureIndex.size()][tagClassSum];
		for (int i = 0; i < this.featureIndex.size(); i++) {
			Arrays.fill(weights[i], 0);
		}

		start = System.currentTimeMillis();
		// use sgd to train the data
		// TODO: random sort all the data.

		int ci = 0;
		for (Sentence sentence : list) {
			// System.out.println(ci);
			Map<Integer, int[]> empiricalFeatureCounterMap = new HashMap<Integer, int[]>();

			// Expectation of Empirical feature
			for (int i = 0; i < sentence.size(); i++) {
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

			// System.out.println(ci+"\tliklihood\t"+logLiklihood(sentence));
			Map<Integer, double[]> modelFeatureStatisticsMap = new HashMap<Integer, double[]>();

			for (int i = 0; i < sentence.size(); i++) {
				Token token = sentence.getToken(i);
				int[] fids = token.getFeatureIds();
				for (int j = 0; j < fids.length; j++) {
					double[] statistics = modelFeatureStatisticsMap
							.get(fids[j]);
					for (int k = 0; k < tagClassSum; k++) {
						if (statistics == null) {
							statistics = new double[tagClassSum];
							Arrays.fill(statistics, 0);
							modelFeatureStatisticsMap.put(fids[j], statistics);
						}
						statistics[k] += prob[i][k];
						if (Double.isNaN(statistics[k])
								|| Double.isInfinite(statistics[k])) {
							System.out.println("statistics[" + k + "][" + i
									+ "]\t" + statistics[k] + "\t" + sentence);
						}
					}
				}
			}

			for (Integer featureId : empiricalFeatureCounterMap.keySet()) {
				int[] count = empiricalFeatureCounterMap.get(featureId);
				double[] statistics = modelFeatureStatisticsMap.get(featureId);
				for (int i = 0; i < count.length; i++) {
					if (Math.abs(weights[featureId][i]) > 100) {
						System.out.print(ci + "\t" + featureId + "\t" + i
								+ "\t" + weights[featureId][i]);
						weights[featureId][i] = weights[featureId][i]
								+ this.alpha * (count[i] - statistics[i])
								- weights[featureId][i] / this.sigma;
						System.out.println("\t" + weights[featureId][i] + "\t"
								+ count[i] + "\t" + statistics[i] + "\t"
								+ (count[i] - statistics[i]));
					} else {
						// System.out.print(ci + "\t" + featureId + "\t" + i
						// + "\t" + weights[featureId][i]);
						weights[featureId][i] = weights[featureId][i]
								+ this.alpha * (count[i] - statistics[i])
								- weights[featureId][i] / this.sigma;
						// System.out.println("\t" + weights[featureId][i] +
						// "\t"
						// + count[i]+"\t"+statistics[i]+"\t"+(count[i] -
						// statistics[i]));
					}
					if (Double.isNaN(weights[featureId][i])
							|| Double.isInfinite(weights[featureId][i])) {
						System.out.println("weights[" + featureId + "][" + i
								+ "]\t" + weights[featureId][i] + "\t"
								+ sentence);
					}
				}
			}
			// System.out.println(ci+"\tliklihood\t"+logLiklihood(sentence));
			ci++;
		}
		end = System.currentTimeMillis();
		// System.out.print("\tSGD Train Use Time:" + (end - start) / 1000.0);

		int mostImportantFetaure[][] = new int[10][2];
		for (int i = 0; i < 10; i++) {
			double max = Double.MIN_NORMAL;
			for (int j = 0; j < weights.length; j++) {
				for (int k = 0; k < tagClassSum; k++) {
					boolean find = false;
					for (int l = 0; l < i; l++) {
						if (j == mostImportantFetaure[l][0]
								&& k == mostImportantFetaure[l][1]) {
							find = true;
							break;
						}
					}
					if (find)
						continue;
					if (max < weights[j][k]) {
						max = weights[j][k];
						mostImportantFetaure[i][0] = j;
						mostImportantFetaure[i][1] = k;
					}
				}
			}
			System.out
					.println(mostImportantFetaure[i][0]
							+ "\t"
							+ mostImportantFetaure[i][1]
							+ "\t"
							+ this.weights[mostImportantFetaure[i][0]][mostImportantFetaure[i][1]]
							+ "\t"
							+ this.featureIndex.get(mostImportantFetaure[i][0]));
		}

		System.out.print("\t" + (end - start) / 1000.0);

	}

	void normalize(double[] a) {
		assert (a != null);
		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i];
		for (int i = 0; i < a.length; i++)
			a[i] /= sum;
	}

	double getFactorValue(double[][] weights, Sentence sentence, int i) {
		assert (i < sentence.size());
		Token token = sentence.getToken(i);
		return getFactorValue(weights, token.getPreTag().ordinal(), token
				.getCurTag().ordinal(), sentence, i);
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
			sum += weights[fid][tagClass];
		}
		if (Double.isNaN(sum) || Double.isInfinite(sum)) {
			System.out.println(preState + "\t" + curState);
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
		}
		// The first element cannot be Tag.inter or Tag.End
		alpha[0][Tag.Inter.ordinal()] = 0;
		alpha[0][Tag.End.ordinal()] = 0;
		normalize(alpha[0]);

		for (int i = 1; i < size; i++) {
			for (int j = 0; j < tagSize; j++) {
				alpha[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					if (Tag.adjacent(Tag.index(k), Tag.index(j))) {
						alpha[i][j] += alpha[i - 1][k]
								* getFactorValue(weights, k, j, sentence, i);
					}
				}
			}
			normalize(alpha[i]);
		}
		alpha[size - 1][Tag.Inter.ordinal()] = 0;
		alpha[size - 1][Tag.Begin.ordinal()] = 0;
		normalize(alpha[size - 1]);

		// backward algorithm
		double beta[][] = new double[size][tagSize];
		for (int j = 0; j < tagSize; j++) {
			beta[size - 1][j] = getFactorValue(weights, j, Tag.Other.ordinal(),
					sentence, size - 1);
		}

		beta[size - 1][Tag.Begin.ordinal()] = 0;
		beta[size - 1][Tag.Inter.ordinal()] = 0;
		normalize(beta[size - 1]);

		for (int i = size - 2; i >= 0; i--) {
			for (int j = 0; j < tagSize; j++) {
				beta[i][j] = 0;
				for (int k = 0; k < tagSize; k++) {
					if (Tag.adjacent(Tag.index(k), Tag.index(j))) {
						beta[i][j] += beta[i + 1][k]
								* getFactorValue(weights, j, k, sentence, i);
					}
				}
			}
			normalize(beta[i]);
		}
		beta[0][Tag.Inter.ordinal()] = 0;
		beta[0][Tag.End.ordinal()] = 0;
		normalize(beta[0]);

		double u[][] = new double[size][tagSize * tagSize];
		for (int i = 1; i < size; i++) {
			for (int j = 0; j < tagSize * tagSize; j++) {
				int preState = j % tagSize;
				int curState = j / tagSize;
				u[i][j] = alpha[i - 1][preState] * beta[i][curState];
				u[i][j] *= getFactorValue(weights, preState, curState,
						sentence, i);
			}
			normalize(u[i]);
			// TODO: remove
			for (int j = 0; j < tagSize * tagSize; j++) {
				if (Double.isNaN(u[i][j]) || Double.isInfinite(u[i][j])
						|| u[i][j] > 1) {
					System.out.println("In forward backward u[" + i + "][" + j
							+ "]=" + u[i][j]);
				}
			}
		}

		return u;
	}

	public void eval(List<Sentence> list) {
		int tokenSum = 0;
		int correctSum = 0;

		for (Sentence sen : list) {
			tokenSum += sen.size();
			correctSum += sen.predictCorrectCount();
		}
		double precision = (double) correctSum / tokenSum;
		// System.out.print("\tToken tag precision:"+precision+"="+correctSum+"/"+tokenSum);
		System.out.print("\t" + precision + "=" + correctSum + "/" + tokenSum);

		List<String> notFindList = new ArrayList<String>();
		List<String> correctList = new ArrayList<String>();
		int orgsum = 0;
		int correctOrgSum = 0;
		for (Sentence sen : list) {
			Map<Integer, String> orgs = sen.getOrg();
			orgsum += orgs.size();
			Map<Integer, String> predictOrgs = sen.getPredictOrg();
			for (Integer id : orgs.keySet()) {
				if (predictOrgs.containsKey(id)
						&& orgs.get(id).equals(predictOrgs.get(id))) {
					correctOrgSum++;
					correctList.add(predictOrgs.get(id));
				} else {
					notFindList.add(orgs.get(id));
				}
			}
		}
		double recall = (double) correctOrgSum / orgsum;
		// System.out.print("\tOrganization recall:"+recall+"="+correctOrgSum+"/"+orgsum);
		System.out.print("\t" + recall + "=" + correctOrgSum + "/" + orgsum);

		List<String> wrongList = new ArrayList<String>();
		orgsum = 0;
		correctOrgSum = 0;
		for (Sentence sen : list) {
			Map<Integer, String> orgs = sen.getOrg();

			Map<Integer, String> predictOrgs = sen.getPredictOrg();
			orgsum += predictOrgs.size();

			for (Integer id : predictOrgs.keySet()) {
				if (orgs.containsKey(id)
						&& orgs.get(id).equals(predictOrgs.get(id))) {
					correctOrgSum++;
				} else {
					wrongList.add(predictOrgs.get(id));
				}
			}
		}
		precision = (double) correctOrgSum / orgsum;
		// System.out.print("\tOrganization precision:"+precision+"="+correctOrgSum+"/"+orgsum);
		System.out.print("\t" + precision + "=" + correctOrgSum + "/" + orgsum);
		System.out.println("");
		System.out.println("Not Found List:"
				+ notFindList.subList(0, 100).toString());
		System.out
				.println("Wrong List:" + wrongList.subList(0, 100).toString());
		System.out.println("Correct List:"
				+ correctList.subList(0, 100).toString());
	}

	public void write(String file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		write(bw);
	}

	public void write(Writer bw) throws IOException {
		this.featureIndex.write(bw);
		for (int i = 0; i < weights.length; i++) {
			bw.write(this.weights[i] + "\n");
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

			// viterbi algorithm
			double a[][] = new double[senSize][tagSize];
			int path[][] = new int[senSize][tagSize];
			for (int j = 0; j < tagSize; j++) {
				a[0][j] = getFactorValue(this.weights, Tag.Other.ordinal(), j,
						sen, 0);
			}
			a[0][Tag.Inter.ordinal()] = 0;
			a[0][Tag.End.ordinal()] = 0;
			normalize(a[0]);

			for (int i = 1; i < senSize; i++) {
				for (int j = 0; j < tagSize; j++) {
					a[i][j] = Double.MIN_VALUE;
					path[i][j] = -1;
					for (int k = 0; k < tagSize; k++) {
						if (Tag.adjacent(Tag.index(k), Tag.index(j))) {
							double value = a[i - 1][k]
									* getFactorValue(this.weights, k, j, sen, i);
							if (a[i][j] < value) {
								a[i][j] = value;
								path[i][j] = k;
							}
						}
					}
				}
				normalize(a[i]);
			}
			a[senSize-1][Tag.Inter.ordinal()] = 0;
			a[senSize-1][Tag.Begin.ordinal()] = 0;
			normalize(a[senSize-1]);

			double maxp = Double.MIN_VALUE;
			int maxpTag = -1;
			for (int j = 0; j < tagSize; j++) {
				if (maxp < a[senSize - 1][j]) {
					maxp = a[senSize - 1][j];
					maxpTag = j;
				}
			}
			
			Tag[] values = Tag.values();
			sen.getToken(senSize - 1).setPredictTag(values[maxpTag]);

			for (int i = senSize - 1; i >= 1; i--) {
				maxpTag = path[i][maxpTag];
				sen.getToken(i - 1).setPredictTag(values[maxpTag]);
			}
		}
	}

	public void indexFeatures(List<Sentence> list) {

	}

}
