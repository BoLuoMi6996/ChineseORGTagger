package edu.zju.udms.model;

import java.util.List;

//Fragment of sentence, should not contain punctuation mark, except ¡¶¡·
public class Fragment extends Sentence {
	public Fragment(List<Token> tokens) {
		super(tokens);
		if (check(tokens) == false) {
			throw new IllegalStateException(
					"Fragment should not contain punctuation mark!");
		}
	}

	public Fragment(Token[] tokens) {
		super(tokens);
		if (check(tokens) == false) {
			throw new IllegalStateException(
					"Fragment should not contain punctuation mark!");
		}
	}

	private boolean check(Token[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			String content = tokens[i].getContent();
			if (Constants.FragmentMark.contains(content)) {
				return false;
			}
		}
		return true;
	}

	private boolean check(List<Token> tokens) {
		for (Token token : tokens) {
			String content = token.getContent();
			if (Constants.FragmentMark.contains(content)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
