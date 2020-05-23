package com.haizhi.iap.tag.trie;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;


/**
 * 把字按UTF8编码，取其byte[]，trie字符集256
 * 
 * @author livingroom
 *
 */
public class ByteCharacterMapping implements CharacterMapping, Serializable {

    private static final Charset utf8Charset = Charset.forName("UTF8");
    ThreadLocal<CharsetEncoder> encoder = new ThreadLocal<CharsetEncoder>() {
          @Override
          protected CharsetEncoder initialValue() {
    return utf8Charset.newEncoder();
    }};

	private static final long serialVersionUID = 5536124861751803466L;
	private static final int N = 256;
	private static final int[] EMPTYLIST = new int[]{};

    public ByteCharacterMapping() {
    }
	
	public int getInitSize() {
		return N;
	}

	public int getCharsetrSize() {
		return N;
	}

	public int toId(char character) {
		return (int)character;
	}

	public char toCharacter(int id) {
		return (char)id;
	}

	public int[] toIdList(String str) {
		byte[] bytes = null;

                try {
                bytes = str.getBytes("UTF8");
                } catch (Exception e) {
                    
                }
		if(bytes != null) {
			int[] res = new int[bytes.length];
			for(int i = 0;i < res.length; i++) {
				res[i] = bytes[i] & 0xff;
			}
			return res;
		}
		return EMPTYLIST;
	}

	public static void main(String[] argv) {
		ByteCharacterMapping b = new ByteCharacterMapping();
		//b.toIdList("a" + ((char)0));
		String str = "阿胶";
		System.out.println("阿--" + b.toId(str.charAt(0)));
		System.out.println("胶--" + b.toId(str.charAt(1)));

		int[] ids = b.toIdList(str);
		for(int i = 0; i < ids.length; i++){
			System.out.println(ids[i]);
		}
	}
}
