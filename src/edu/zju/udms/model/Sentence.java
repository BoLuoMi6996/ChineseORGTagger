package edu.zju.udms.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sentence {
	private Token[] tokens; 
	public Sentence(List<Token> tokens){
		this.tokens = tokens.toArray(new Token[]{});
	}
	public Sentence(Token[] tokens){
		this.tokens = tokens;
	}
	public Sentence(String content){
		String[] tokens = content.split("\\s+");
		this.tokens = new Token[tokens.length];
		for(int i = 0;i<tokens.length;i++){
			this.tokens[i] = new Token(tokens[i]);
		}
	}
	
	public int size(){
		if(this.tokens!=null){
			return this.tokens.length;
		}
		return 0;
	}
	public Token getToken(int i){
		assert(i>=0 && i<this.tokens.length);
		return this.tokens[i];
	}
	
	public int predictCorrectCount(){
		int sum = 0;
		for(int i = 0;i<tokens.length;i++){
			if(tokens[i].getPredictTag()==tokens[i].getTag()){
				sum++;
			}
		}
		return sum;
	}
}
