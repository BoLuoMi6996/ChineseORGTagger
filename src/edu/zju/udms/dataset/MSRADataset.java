package edu.zju.udms.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.zju.udms.model.Constants;
import edu.zju.udms.model.Fragment;
import edu.zju.udms.model.NamedEntityType;
import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;
import edu.zju.udms.model.Token;

public class MSRADataset extends NERDataset {
	@Override
	public List<Sentence> fromString(String text, NamedEntityType netype,
			boolean fragment) {
		List<Sentence> result = new ArrayList<Sentence>();

		String pairs[] = text.split("\\s+");
		List<Token> tokens = new ArrayList<Token>();
		int i = 0;
		for (String pair : pairs) {
			int id = pair.lastIndexOf("/");
			if (id >= 0) {
				String word = pair.substring(0, id);
				String tag = pair.substring(id + 1);
				if (tag.equals("nt") && netype == NamedEntityType.Organization) {
					// tokens.add(new Token(word.substring(0, 1), Tag.Begin));
					tokens.add(new Token(word.substring(0, 1), Tag.End));
					i++;
					for (int j = 1; j < word.length() - 1; j++) {
						tokens.add(new Token(word.substring(j, j + 1),
								Tag.Inter));
						i++;
					}
					if (word.length() > 1) {
						// tokens.add(new Token(word.substring(word.length() -
						// 1),Tag.End));
						tokens.add(new Token(word.substring(word.length() - 1),
								Tag.Begin));
						i++;
					}
				} else if (Constants.FragmentMark.contains(word) && fragment) {
					if (tokens.size() > 0) {
						Collections.reverse(tokens);
						result.add(new Fragment(tokens));
					}
					tokens = new ArrayList<Token>();
				} else {
					for (int j = 0; j < word.length(); j++) {
						tokens.add(new Token(word.substring(j, j + 1),
								Tag.Other));
						i++;
					}
				}
			}
		}
		if (fragment == false && tokens.size() > 0) {
			Collections.reverse(tokens);
			Iterator<Token> iterator = tokens.iterator();
			Token pre = iterator.next();
			while(iterator.hasNext()){
				Token cur = iterator.next();
				cur.setPreTag(pre.getCurTag());
				pre = cur;
			}
			result.add(new Sentence(tokens));
		}
		return result;
	}

	@Override
	public List<Sentence> fromFile(String file, NamedEntityType netype,
			boolean fragment) throws IOException {
		List<Sentence> rs = new ArrayList<Sentence>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int lineno = 0;
		while ((line = reader.readLine()) != null) {
			List<Sentence> sentences = fromString(line, netype, fragment);
			if (sentences != null) {
				for (Sentence s : sentences) {
					rs.add(s);
				}
			}
			lineno++;
		}
		return rs;
	}

}
