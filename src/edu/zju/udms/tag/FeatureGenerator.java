package edu.zju.udms.tag;

import java.util.ArrayList;
import java.util.List;

import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Token;
import edu.zju.udms.tools.MathUtils;

public class FeatureGenerator {
	
	public void window(Sentence sentence, int i) {
		List<String> features = new ArrayList<String>();
		assert (sentence != null);
		int slen = sentence.size();

		// add unigram features c0
		assert (i < slen);
		features.add("c0=" + sentence.getToken(i).getContent());

		
		// add bigram features c-1c0
		if (MathUtils.rangeCheck(i - 1, slen, 0)) {
			features.add("c_1c0=" + sentence.getToken(i - 1).getContent()
					+ sentence.getToken(i).getContent());
		}else{
			features.add("c_1c0=SEN_BEG"  
					+ sentence.getToken(i).getContent());
		}

		// add bigram features c0c1
		if (MathUtils.rangeCheck(i + 1, slen, 0)) {
			features.add("c0c1=" + sentence.getToken(i).getContent()
					+ sentence.getToken(i + 1).getContent());
		}else{
			features.add("c0c1=" + sentence.getToken(i).getContent()
					+"SEN_END");
		}
		
		// add bigram features c-1c1
		if (MathUtils.rangeCheck(i - 1, slen, 0) && MathUtils.rangeCheck(i+1, slen, 0)) {
			features.add("c_1c1=" + sentence.getToken(i-1).getContent()
					+ sentence.getToken(i+1).getContent());
		}else if(MathUtils.rangeCheck(i - 1, slen, 0)){
			features.add("c_1c1=" + sentence.getToken(i-1).getContent()
					+"SEN_END");
		}else if(MathUtils.rangeCheck(i+1, slen, 0)){
			features.add("c_1c1=SEN_BEG" + sentence.getToken(i+1).getContent());
		}else{
			features.add("c_1c1=SEN_BEG,SEN_END");
		}
		
		/*
		//add trigram features c-2c-1c0
		if (MathUtils.rangeCheck(i - 2, slen, 0)) {
			features.add("c2c1c0=" + sentence.getToken(i - 1).getContent()
					+ sentence.getToken(i - 1).getContent() 
					+ sentence.getToken(i).getContent());
		}

		// add trigram features c0c1c2
		if (MathUtils.rangeCheck(i + 2, slen, 0)) {
			features.add("c0c1c2=" + sentence.getToken(i).getContent()
					+ sentence.getToken(i + 1).getContent() 
					+ sentence.getToken(i + 2).getContent());
		}

		// add trigram features c-1c0c1
		if (MathUtils.rangeCheck(i - 1, slen, 0)
				&& MathUtils.rangeCheck(i + 1, slen, 0)) {
			features.add("c1c0c1=" + sentence.getToken(i - 1).getContent()
					+ sentence.getToken(i).getContent());
		}
		*/
		
		Token ithToken = sentence.getToken(i);
		ithToken.setFeatures(features);
	}
}
