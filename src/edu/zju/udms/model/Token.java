package edu.zju.udms.model;

import java.util.List;


public class Token {
	private String content;
	private Tag curTag;
	private Tag preTag = Tag.Other;
	private Tag predictTag;
	
	private String[] features;
	private int[] featureIds;
	
	public Token(String content){
		this.content = content;
	}
	
	public Token(String content,Tag tag){
		this.content = content;
		this.curTag = tag;
	}
	public Token(String content,Tag tag,Tag preTag){
		this.content = content;
		this.curTag = tag;
		this.preTag = preTag;
	}
	
	public Token(String content, Tag tag, List<String> features){
		this.content = content;
		this.curTag = tag;
		this.features = features.toArray(new String[]{});
	}
	
	public void setFeatures(String[] features){
		this.features = features;
	}
	
	public void setFeatures(List<String> features){
		this.features = features.toArray(new String[]{});
	}
	
	public String[] getFeatures(){
		return this.features;
	}
	public int[] indexFeatures(Index index){
		this.featureIds = new int[features.length];
		int i = 0;
		for(String f:features){
			this.featureIds[i] = index.add(f);
			i++;
		}
		return this.featureIds;
	}
	
	public int[] getFeatureIds(){
		return this.featureIds;
	}
	
	public Tag getCurTag(){
		return this.curTag;
	}
	public Tag getPreTag(){
		return this.preTag;
	}
	
	public void setPreTag(Tag preTag){
		this.preTag = preTag;
	}
	
	public String getContent(){
		return this.content;
	}
	
	public void setPredictTag(Tag tag){
		this.predictTag = tag;
	}
	
	public Tag getPredictTag(){
		return this.predictTag;
	}
	
	public int getTagclassId(){
		return this.preTag.ordinal()*Tag.values().length+this.curTag.ordinal();
	}
	
	@Override
	public String toString(){
		return content+"\t"+curTag+"\t"+preTag+"\t"+predictTag+"\n";
	}
	
}
