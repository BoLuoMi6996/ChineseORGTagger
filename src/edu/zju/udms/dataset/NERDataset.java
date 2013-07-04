package edu.zju.udms.dataset;

import java.io.IOException;
import java.util.List;

import edu.zju.udms.model.NamedEntityType;
import edu.zju.udms.model.Sentence;

public abstract class NERDataset {
	public abstract List<Sentence> fromString(String text,NamedEntityType netype,boolean fragment);
	public abstract List<Sentence> fromFile(String file,NamedEntityType netype,boolean fragment) throws IOException;
}
