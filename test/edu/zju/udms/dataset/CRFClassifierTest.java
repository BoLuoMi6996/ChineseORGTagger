package edu.zju.udms.dataset;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import edu.zju.udms.algorithm.CRFClassifier;
import edu.zju.udms.model.Sentence;

public class CRFClassifierTest {
	private MSRADataset dataset = new MSRADataset();
	private CRFClassifier classifier;
	private final String trainFile = "msra/train.txt";
	private final String testFile = "msra/train.txt";
	@Test
	public void train() throws IOException{
		long start = System.currentTimeMillis();
		List<Sentence> sentencesTrain = dataset.getOrgFromFile(trainFile);
		long end = System.currentTimeMillis();
		
		System.out.println("Read Dataset From File Use Time:"+(end-start)/1000.0);
		
		classifier = new CRFClassifier();
		classifier.train(sentencesTrain);
		
		//List<Sentence> sentencesTest = dataset.getOrgFromFile(testFile);
		classifier.classify(sentencesTrain);
		classifier.eval(sentencesTrain);
	}
}
