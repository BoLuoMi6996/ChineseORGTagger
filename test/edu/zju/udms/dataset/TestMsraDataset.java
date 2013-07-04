package edu.zju.udms.dataset;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import edu.zju.udms.model.Fragment;
import edu.zju.udms.model.NamedEntityType;
import edu.zju.udms.model.Sentence;
import edu.zju.udms.model.Tag;

public class TestMsraDataset {

	private MSRADataset msraDataset = new MSRADataset();

	@Test
	public void getSentenceFromString() {
		// test sentence
		List<Sentence> actual = msraDataset.fromString(
				"����ίԱ��/nt ����/o �ڼ�/o ��/o ����/o �ǳ�/o ˳��/o ��/o ���/o ��/O",
				NamedEntityType.Organization, false);
		String[] content = { "��", "��", "ί", "Ա", "��", "��", "��", "��", "��", "��",
				"��", "��", "��", "��", "˳", "��", "��", "��", "��", "��" };
		Tag[] tags = { Tag.Begin, Tag.Inter, Tag.Inter, Tag.Inter, Tag.End,
				Tag.Other, Tag.Other, Tag.Other, Tag.Other, Tag.Other,
				Tag.Other, Tag.Other, Tag.Other, Tag.Other, Tag.Other,
				Tag.Other, Tag.Other, Tag.Other, Tag.Other, Tag.Other };
		
		assertEquals(actual.size(), 1);
		assertEquals(actual.get(0).size(), content.length);
		for(int i = 0;i<content.length;i++){
			assertEquals(content[content.length-1-i], actual.get(0).getToken(i).getContent());
			assertEquals(tags[content.length-1-i], actual.get(0).getToken(i).getCurTag());
		}

		// test fragment
		actual = msraDataset.fromString(
				"����ίԱ��/nt ����/o �ڼ�/o ��/o ����/o �ǳ�/o ˳��/o ��/o ���/o ��/O",
				NamedEntityType.Organization, true);
		String[] content0 = { "��", "��", "ί", "Ա", "��", "��", "��", "��", "��" };
		Tag[] tags0 = { Tag.Begin, Tag.Inter, Tag.Inter, Tag.Inter, Tag.End,
				Tag.Other, Tag.Other, Tag.Other, Tag.Other };
		String[] content1 = { "��", "��", "��", "��", "˳", "��", "��", "��", "��" };
		Tag[] tags1 = { Tag.Other, Tag.Other, Tag.Other, Tag.Other, Tag.Other,
				Tag.Other, Tag.Other, Tag.Other, Tag.Other };
		
		assertEquals(actual.size(), 2);
		assertEquals(actual.get(0).size(), content0.length);
		for(int i = 0;i<content0.length;i++){
			assertEquals(content0[content0.length-1-i], actual.get(0).getToken(i).getContent());
			assertEquals(tags0[content0.length-1-i], actual.get(0).getToken(i).getCurTag());
		}
		
		assertEquals(actual.get(1).size(), content1.length);
		for(int i = 0;i<content1.length;i++){
			assertEquals(content1[content1.length-1-i], actual.get(1).getToken(i).getContent());
			assertEquals(tags1[content1.length-1-i], actual.get(1).getToken(i).getCurTag());
		}
	}

}
