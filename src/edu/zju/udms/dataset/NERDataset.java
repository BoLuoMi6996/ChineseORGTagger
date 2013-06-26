package edu.zju.udms.dataset;

import java.io.IOException;
import java.util.List;

import edu.zju.udms.model.Sentence;

public abstract class NERDataset {
	
	public abstract Sentence getOrgFromString(String text);
	public abstract List<Sentence> getOrgFromFile(String file) throws IOException;
		
}
