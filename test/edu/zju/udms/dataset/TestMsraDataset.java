package edu.zju.udms.dataset;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;

public class TestMsraDataset {
	
	private  MSRADataset msraDataset = new MSRADataset();
	@Test
	public void getOrgFromString(){
		Sentence actual = msraDataset.getOrgFromString("����ίԱ��/nt ����/o �ڼ�/o ��/o ����/o �ǳ�/o ˳��/o ��/o ���/o ��/O");
		String[] content = {"��","��","ί","Ա","��","��","��","��","��","��","��","��","��","��","˳","��","��","��","��","��"};
		Tag[] tags = {Tag.Begin,Tag.Inter,Tag.Inter,Tag.Inter,Tag.End,Tag.Other,Tag.Other,
				Tag.Other,Tag.Other,Tag.Other,Tag.Other,Tag.Other,Tag.Other,Tag.Other,Tag.Other,
				Tag.Other,Tag.Other,Tag.Other,Tag.Other,Tag.Other};
		assertEquals(actual.size(), content.length);
		for(int i = 0;i<actual.size();i++){
			assertEquals(content[i],actual.getToken(i).getContent());
			assertEquals(tags[i],actual.getToken(i).getTag());
		}
	}
}
