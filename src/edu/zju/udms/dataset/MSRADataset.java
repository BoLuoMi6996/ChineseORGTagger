package edu.zju.udms.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;
import edu.zju.udms.model.Token;

public class MSRADataset extends NERDataset {
	@Override
	public Sentence getOrgFromString(String text) {
		String pairs[] = text.split("\\s+");
		List<Token> tokens = new ArrayList<Token>();
		int i = 0;
		for (String pair : pairs) {
			int id = pair.lastIndexOf("/");
			if (id >= 0) {
				String word = pair.substring(0, id);
				String tag = pair.substring(id + 1);
				if (tag.equals("nt")) {
					tokens.add(new Token(word.substring(0, 1), Tag.Begin));
					i++;
					for (int j = 1; j < word.length() - 1; j++) {
						tokens.add(new Token(word.substring(j, j + 1),
								Tag.Inter));
						i++;
					}
					if (word.length() > 1) {
						tokens.add(new Token(
								word.substring(word.length() - 1), Tag.End));
						i++;
					}
				} else {
					for (int j = 0; j < word.length(); j++) {
						tokens.add(new Token(word.substring(j, j + 1),
								Tag.Other));
						i++;
					}
				}
			}
		}

		return new Sentence(tokens);
	}

	@Override
	public List<Sentence> getOrgFromFile(String file) throws IOException {
		List<Sentence>  rs = new ArrayList<Sentence>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int lineno = 0;
		while((line = reader.readLine())!=null){
			Sentence sentence = getOrgFromString(line);
			if(sentence!=null){
				rs.add(sentence);
			}
			
			lineno++;
		}
		return rs;
	}

}
