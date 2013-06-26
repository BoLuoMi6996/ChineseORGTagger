package edu.zju.udms.dataset;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;

public class TestMsraDataset {
	
	private  MSRADataset msraDataset = new MSRADataset();
	@Test
	public void getOrgFromString(){
		Sentence actual = msraDataset.getOrgFromString("民族委员会/nt 开会/o 期间/o ，/o 工作/o 非常/o 顺利/o 和/o 愉快/o 。/O");
		String[] content = {"民","族","委","员","会","开","会","期","间","，","工","作","非","常","顺","利","和","愉","快","。"};
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
