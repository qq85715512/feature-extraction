package test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class FeatureExtraction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filestring = "";
		HashMap<String, Integer> freq_map = new HashMap<String, Integer>();// 新建HashMap类
		// 读入分词后的文件内容
		try {
			FileReader fos = new FileReader("iphone5s_result.txt");
			BufferedReader targetRes = new BufferedReader(fos);// 读取文件内容并输入到缓冲区
			String s = new String();
			String s2 = new String();

			while ((s = targetRes.readLine()) != null)
				// 如果从输入流中按行读取字符不为空
				s2 += s + "\r\n";
			targetRes.close();
			filestring = s2;
			// System.out.println("导入的文本内容为："+filestring);

		} catch (Exception e) {
			System.out.println(e);
		}

		
		// 构建事务
		String line = "";
		String word = "";
		ArrayList<ArrayList<String>> noun_in_lines=new ArrayList<ArrayList<String>>();
//		ArrayList<StringBuffer> noun_in_lines = new ArrayList<StringBuffer>();
		int i = 0;
		StringTokenizer st1 = new StringTokenizer(filestring, "\n");// 利用StringTokenizer将文章分解成单词
		// 先按行取
		while (st1.hasMoreTokens()) {// 判断是否有字符串
			line = st1.nextToken().trim();
			StringTokenizer st2 = new StringTokenizer(line, " ");
			ArrayList<String> temp_line=new ArrayList<String>();
//			StringBuffer temp_line = new StringBuffer();
			// 然后按单词取
			while (st2.hasMoreTokens()) {
				word = st2.nextToken().trim();// 得到下一个字符串同时去掉字符串中的空格
				int len = word.length();
				// 若单词为名词，加到临时的StringBuffer中
				if (word.charAt(len - 1) == 'n') {
					temp_line.add(word.substring(0, len - 2) + " ");
				}
			}
			// 将名词组成的临时行加到列表中
			Collections.sort(temp_line);
			noun_in_lines.add(i, temp_line);
			i++;
			System.out.println("第"+i+"个事务项为："+noun_in_lines.get(i - 1));
		}

		// 找1项频繁项
//		String temp_str = "";
		int temp_num = 0;
		for (ArrayList<String> temp_line : noun_in_lines) {
			for(String temp_str : temp_line){
//			StringTokenizer st = new StringTokenizer(temp_line.toString());// 利用StringTokenizer将文章分解成单词
			Object freq_obj = new Object();
//			while (st.hasMoreTokens()) {// 判断是否有字符串
//				temp_str = st.nextToken().trim();// 得到下一个字符串同时去掉字符串中的空格
				if (freq_map.containsKey(temp_str)) {// 对象已经有
					freq_obj = freq_map.get(temp_str);
					temp_num = Integer.parseInt(freq_obj.toString());//
					temp_num++;
					freq_map.put(temp_str, temp_num);// 将统计出来的单词以及它的次数放在用HashMap类定义的temp_str中
				} else {// 对象没有，就新生成
					freq_map.put(temp_str, new Integer(1));
				}
			}
//			}
		}
		System.out.println("计算词频的结果是：" + freq_map);

		//去掉不满足最小支持度的项
		int num = 1;
		Iterator it = freq_map.values().iterator();
		while (it.hasNext()) {
			Integer fre = (Integer) it.next();
			if (fre <= num) {
				it.remove();
			}
		}
		System.out.println("一项频繁项：" + freq_map);

		//写文本
		try {
			OutputStream f1 = new FileOutputStream("result.txt");
//			String source = freq_map.toString();
			String source=noun_in_lines.toString();
			byte buf[] = source.getBytes();
			f1.write(buf);
			f1.close();
		} catch (IOException e) {
			System.out.println(e);
		}

	}
}
