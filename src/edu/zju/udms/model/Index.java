package edu.zju.udms.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Index {
	private Map<String,Integer> indexes = new HashMap<String,Integer>();
	public int add(String t){
		Integer id = indexes.get(t);
		if(id==null){
			id = indexes.size();
			indexes.put(t, id);
		}
		return id;
	}
	public int[] add(List<String> ts){
		assert(ts!=null);
		int[] idx = new int[ts.size()];
		int i =0;
		for(String t:ts){
			idx[i++] = add(t);
		}
		return idx;
	}
	
	public boolean contains(Object o) {
	    return indexes.containsKey(o);
	}
	
	public int size(){
		return indexes.size();
	}
	public int get(String t){
		Integer id = this.indexes.get(t);
		if(id==null) return -1;
		return id;
	}
	
	public void write(String file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		write(bw);
	}
	
	 public void write(Writer bw) throws IOException {
		 bw.write(this.indexes.size()+'\n');
		 for (String key:indexes.keySet()) {
		    bw.write(key + "=" + get(key) + '\n');
		 }
	 }
	 
	 public void load(String file) throws IOException{
		 this.indexes = new HashMap<String, Integer>();
		 BufferedReader br = new BufferedReader(new FileReader(file));
		 String line;
		 while((line = br.readLine())!=null){
			 int idx = line.indexOf('=');
			 if(idx>=0){
				 this.indexes.put(line.substring(0,idx), Integer.valueOf(line.substring(idx+1)));
			 }
		 }
	 }
}
