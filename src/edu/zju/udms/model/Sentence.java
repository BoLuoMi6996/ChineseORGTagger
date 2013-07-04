package edu.zju.udms.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0;i<tokens.length;i++){
			if(tokens[i].getCurTag()==Tag.Begin) buffer.append(" ");
			buffer.append(tokens[i].getContent());
			if(tokens[i].getCurTag()==Tag.End) buffer.append(" ");
		}
		return buffer.toString();
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
			if(tokens[i].getPredictTag()==tokens[i].getCurTag()){
				sum++;
			}
		}
		return sum;
	}
	
	public Map<Integer,String> getOrg(){
		Map<Integer,String> orgs = new HashMap<Integer, String>();
		StringBuffer buffer = new StringBuffer();
		int pos = -1;
		for(int i = 0;i<tokens.length;i++){
			switch(tokens[i].getCurTag()){
			case Other:
				break;
			case Begin:
				pos = i;
			case Inter:
				buffer.append(tokens[i].getContent());
				break;
			case End:
				buffer.append(tokens[i].getContent());
				orgs.put(pos,buffer.toString());
				buffer = new StringBuffer();
			}
		}
		return orgs;
	}
	
	/**
	 * 
	 * @return the start position and the organization name.
	 */
	public Map<Integer,String> getPredictOrg(){
		Map<Integer,String> orgs = new HashMap<Integer, String>();
		StringBuffer buffer = new StringBuffer();
		int pos = -1;
		for(int i = 0;i<tokens.length;i++){
			switch(tokens[i].getPredictTag()){
			case Other:
				break;
			case Begin:
				pos = i;
			case Inter:
				buffer.append(tokens[i].getContent());
				break;
			case End:
				orgs.put(pos,buffer.toString());
				buffer = new StringBuffer();
			}
		}
		return orgs;
	}
}
