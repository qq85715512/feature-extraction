package apriori_a;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class TestAprioriAlgorithm {
	private AprioriAlgorithm apriori;
	private Map<Integer, Set<String>> txDatabase;
	private Map<Integer, ArrayList<String>> txDatabaseWithoutTag;
	private Float minSup = new Float("0.01");
	private Float minConf = new Float("0.10");
	private String filestring = "";
	private String filestringwithouttag = "";
	private Map<Integer, String> commonwords;
	private Map<Integer, String> manualfeature;
	/**
	 * iphone6 5880条
	 * mi4 5317条
	 * rongyao6 2283条
	 * samsungS6 2039条
	 */
	private String name="samsungS6";

	protected void setUp() throws Exception {
		readFile();
		create(); // 构造事务数据库
		readFileWithoutTag();
		createWithoutTag();
		readWordFile();
		readFeatureFile();
		apriori = new AprioriAlgorithm(txDatabase, txDatabaseWithoutTag,
				commonwords, manualfeature, minSup, minConf);
	}

	/**
	 * 读入人工标注的产品属性
	 */
	public void readFeatureFile() {
		manualfeature = new HashMap<Integer, String>();
		try {
			FileReader fos = new FileReader("files/manual/" + name + "_manualfeature.txt");
			BufferedReader targetRes = new BufferedReader(fos);// 读取文件内容并输入到缓冲区
			String s = new String();
			int i = 0;

			while ((s = targetRes.readLine()) != null)
				// 如果从输入流中按行读取字符不为空
				manualfeature.put(i++, s);
			targetRes.close();
			 System.out.println(i+"读入的标注属性为："+manualfeature);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 读入常见词汇
	 */
	public void readWordFile() {
		commonwords = new HashMap<Integer, String>();
		try {
			FileReader fos = new FileReader("files/commonwords.txt");
			BufferedReader targetRes = new BufferedReader(fos);// 读取文件内容并输入到缓冲区
			String s = new String();
			int i = 0;

			while ((s = targetRes.readLine()) != null)
				// 如果从输入流中按行读取字符不为空
				commonwords.put(i++, s);
			targetRes.close();
			// System.out.println("读入的常见单词为："+commonwords);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * 读入分词后的文件
	 */
	public void readFile() {
		try {
			FileReader fos = new FileReader("files/output/" + name + "_result1.txt");
			BufferedReader targetRes = new BufferedReader(fos);// 读取文件内容并输入到缓冲区
			String s = new String();
			String s2 = new String();

			while ((s = targetRes.readLine()) != null && s.trim() != "")
				// 如果从输入流中按行读取字符不为空
				s2 += s + "\r\n";
			targetRes.close();
			filestring = s2;
			// System.out.println("导入的文本内容为："+filestring);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * 读入分词后未标注的文件
	 */
	public void readFileWithoutTag() {
		try {
			FileReader fos = new FileReader("files/output/" + name+"_result2.txt");
			BufferedReader targetRes = new BufferedReader(fos);// 读取文件内容并输入到缓冲区
			String s = new String();
			String s2 = new String();

			while ((s = targetRes.readLine()) != null)
				// 如果从输入流中按行读取字符不为空
				s2 += s + "\r\n";
			targetRes.close();
			filestringwithouttag = s2;
			// System.out.println("导入的文本内容为："+filestring);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * 构造模拟事务数据库txDatabase
	 */
	public void create() {
		txDatabase = new HashMap<Integer, Set<String>>();
		//正则表达式判断是否为标点符号
		//Pattern patPunc = Pattern.compile("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
		
		// 构建事务
		String line = "";
		String word = "";
		int i = 0;
		StringTokenizer st1 = new StringTokenizer(filestring, "\n");// 利用StringTokenizer将文章分解成单词
		while (st1.hasMoreTokens()) {// 先按行取
			line = st1.nextToken().trim();
			StringTokenizer st2 = new StringTokenizer(line, " ");
			Set<String> temp_line = new TreeSet<String>();
			int flag=0;String noun="";
			while (st2.hasMoreTokens()) {// 然后按单词取
				word = st2.nextToken().trim();// 得到下一个字符串同时去掉字符串中的空格
				int len = word.length();
				
				if (len < 2){
				    continue;
				}
				//只提取名词
				if (word.charAt(len - 1) == 'n'&&word.charAt(len-2)!='w'
						|| word.substring(len - 2, len).compareTo("ng") == 0 || word.substring(len - 2, len).compareTo("nl") == 0) {// 若单词为名词，加到临时的StringBuffer中					
										
					temp_line.add(word.substring(0, word.indexOf("/")));				
				}
				//提取名词短语
//				if (word.charAt(len - 1) == 'n'&&word.charAt(len-2)!='w'
//						|| word.substring(len - 2, len).compareTo("ng") == 0) {// 若单词为名词，加到临时的StringBuffer中					
//					if(flag==1){
//						noun=noun+word.substring(0, word.indexOf("/"));							
//					}else{
//						flag=1;
//						noun=word.substring(0, word.indexOf("/"));					
//					}					
//				}else if(flag==1){
//					flag=0;
//					temp_line.add(noun);
//				}
			}
			// 将名词组成的临时行加到列表中
			txDatabase.put(i, temp_line);
			i++;
			//System.out.println("第" + i + "个事务项为：" + txDatabase.get(i - 1));
		}
	}

	/**
	 * 构造模拟事务数据库txDatabaseWithoutTag
	 */
	public void createWithoutTag() {
		txDatabaseWithoutTag = new HashMap<Integer, ArrayList<String>>();
		// 构建事务
		String line = "";
		String word = "";
		int i = 0;
		StringTokenizer st1 = new StringTokenizer(filestringwithouttag, "\n");// 利用StringTokenizer将文章分解成单词
		while (st1.hasMoreTokens()) {// 先按行取
			line = st1.nextToken().trim();
			StringTokenizer st2 = new StringTokenizer(line, " ，。！？、；：,.!?;:…");
			ArrayList<String> temp_line = new ArrayList<String>();
			while (st2.hasMoreTokens()) {// 然后按单词取
				word = st2.nextToken().trim();// 得到下一个字符串同时去掉字符串中的空格
				temp_line.add(word);
			}
			// 将名词组成的临时行加到列表中
			txDatabaseWithoutTag.put(i, temp_line);
			i++;
			// System.out.println("第" + i + "个句子中的词语为：" +
			// txDatabaseWithoutTag.get(i - 1));
		}
	}

	/**
	 * 测试挖掘频繁1-项集
	 */
	public void testFreq1ItemSet() {
		System.out.println("挖掘频繁1-项集 : " + apriori.getFreq1ItemSet());
	}

	/**
	 * 测试aprioriGen方法，生成候选频繁项集
	 */
	public void testAprioriGen() {
		System.out.println("候选频繁2-项集 ： "
				+ this.apriori.aprioriGen(1, this.apriori.getFreq1ItemSet()
						.keySet()));
	}

	/**
	 * 测试挖掘频繁2-项集
	 */
	public void testGetFreq2ItemSet() {
		System.out.println("挖掘频繁2-项集 ："
				+ this.apriori.getFreqKItemSet(2, this.apriori
						.getFreq1ItemSet().keySet()));
	}

	/**
	 * 测试挖掘频繁3-项集
	 */
	public void testGetFreq3ItemSet() {
		System.out.println("挖掘频繁3-项集 ："
				+ this.apriori.getFreqKItemSet(
						3,
						this.apriori.getFreqKItemSet(2,
								this.apriori.getFreq1ItemSet().keySet())
								.keySet()));
	}

	/**
	 * 测试挖掘全部频繁项集
	 */
	public void testGetFreqItemSet() {
		this.apriori.mineFreqItemSet(); // 挖掘频繁项集
		System.out.println("挖掘频繁项集 ：" + this.apriori.getFreqItemSet());
	}

	/**
	 * 测试挖掘全部频繁关联规则
	 */
	public void testMineAssociationRules() {
		this.apriori.mineFreqItemSet(); // 挖掘频繁项集
		this.apriori.mineAssociationRules();
		System.out.println("挖掘频繁关联规则 ：" + this.apriori.getAssiciationRules());
	}

	/**
	 * 测试邻近规则剪枝
	 */
	public void testGenFeature() {
		this.apriori.genFeature();
		this.apriori.functionTest();
	}
	

}
