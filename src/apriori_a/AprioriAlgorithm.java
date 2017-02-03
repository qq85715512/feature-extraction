package apriori_a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AprioriAlgorithm {
	private Map<Integer, Set<String>> txDatabase; // 事务数据库
	private Map<Integer, ArrayList<String>> txDatabaseWithoutTag; // 未标注的分词数据库
	private Map<Integer, String> commonwords;
	private Map<Integer, String> manualfeature;
	private Float minSup; // 最小支持度
	private Float minConf; // 最小置信度
	private Integer txDatabaseCount; // 事务数据库中的事务数

	private Map<Integer, Set<Set<String>>> freqItemSet; // 频繁项集集合
	private Map<Set<String>, Set<Set<String>>> assiciationRules; // 频繁关联规则集合

	Map<String, Float> candidate1Item = new TreeMap<String, Float>();
	Map<String, Float> candidate2Item = new TreeMap<String, Float>();
	Map<String, Float> candidate3Item = new TreeMap<String, Float>();
	private Map<String, Float> feature;

	public AprioriAlgorithm(Map<Integer, Set<String>> txDatabase,
			Map<Integer, ArrayList<String>> txDatabaseWithoutTag,
			Map<Integer, String> commonwords,
			Map<Integer, String> manualfeature, Float minSup, Float minConf) {
		this.txDatabase = txDatabase;
		this.txDatabaseWithoutTag = txDatabaseWithoutTag;
		this.commonwords = commonwords;
		this.manualfeature = manualfeature;
		this.minSup = minSup;
		this.minConf = minConf;
		this.txDatabaseCount = this.txDatabase.size();
		freqItemSet = new TreeMap<Integer, Set<Set<String>>>();
		assiciationRules = new HashMap<Set<String>, Set<Set<String>>>();
		feature = new TreeMap<String, Float>();
	}

	/**
	 * 扫描事务数据库，计算频繁1-项集
	 * 
	 * @return
	 */
	public Map<Set<String>, Float> getFreq1ItemSet() {
		Map<Set<String>, Float> freq1ItemSetMap = new HashMap<Set<String>, Float>();
		Map<Set<String>, Integer> candFreq1ItemSet = this.getCandFreq1ItemSet();
		Iterator<Map.Entry<Set<String>, Integer>> it = candFreq1ItemSet
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Set<String>, Integer> entry = it.next();
			// 计算支持度
			Float supported = new Float(entry.getValue().toString())
					/ new Float(txDatabaseCount);
			if (supported >= minSup) {
				freq1ItemSetMap.put(entry.getKey(), supported);
			}
		}
		return freq1ItemSetMap;
	}

	/**
	 * 计算候选频繁1-项集
	 * 
	 * @return
	 */
	public Map<Set<String>, Integer> getCandFreq1ItemSet() {
		Map<Set<String>, Integer> candFreq1ItemSetMap = new HashMap<Set<String>, Integer>();
		Iterator<Map.Entry<Integer, Set<String>>> it = txDatabase.entrySet()
				.iterator();
		// 统计支持数，生成候选频繁1-项集
		while (it.hasNext()) {
			Map.Entry<Integer, Set<String>> entry = it.next();
			Set<String> itemSet = entry.getValue();
			for (String item : itemSet) {
				Set<String> key = new HashSet<String>();
				key.add(item.trim());
				if (!candFreq1ItemSetMap.containsKey(key)) {
					Integer value = 1;
					candFreq1ItemSetMap.put(key, value);
				} else {
					Integer value = 1 + candFreq1ItemSetMap.get(key);
					candFreq1ItemSetMap.put(key, value);
				}
			}
		}
		return candFreq1ItemSetMap;
	}

	/**
	 * 根据频繁(k-1)-项集计算候选频繁k-项集
	 * 
	 * @param m
	 *            其中m=k-1
	 * @param freqMItemSet
	 *            频繁(k-1)-项集
	 * @return
	 */
	public Set<Set<String>> aprioriGen(int m, Set<Set<String>> freqMItemSet) {
		Set<Set<String>> candFreqKItemSet = new HashSet<Set<String>>();
		Iterator<Set<String>> it = freqMItemSet.iterator();
		Set<String> originalItemSet = null;
		while (it.hasNext()) {
			originalItemSet = it.next();
			Iterator<Set<String>> itr = this.getIterator(originalItemSet,
					freqMItemSet);
			while (itr.hasNext()) {
				Set<String> identicalSet = new HashSet<String>(); // 两个项集相同元素的集合(集合的交运算)
				identicalSet.addAll(originalItemSet);
				Set<String> set = itr.next();
				identicalSet.retainAll(set); // identicalSet中剩下的元素是identicalSet与set集合中公有的元素
				if (identicalSet.size() == m - 1) { // (k-1)-项集中k-2个相同
					Set<String> differentSet = new HashSet<String>(); // 两个项集不同元素的集合(集合的差运算)
					differentSet.addAll(originalItemSet);
					differentSet.removeAll(set); // 因为有k-2个相同，则differentSet中一定剩下一个元素，即differentSet大小为1
					differentSet.addAll(set); // 构造候选k-项集的一个元素(set大小为k-1,differentSet大小为k)
					candFreqKItemSet.add(differentSet); // 加入候选k-项集集合
				}
			}
		}
		return candFreqKItemSet;
	}

	/**
	 * 根据一个频繁k-项集的元素(集合)，获取到频繁k-项集的从该元素开始的迭代器实例
	 * 
	 * @param itemSet
	 * @param freqKItemSet
	 *            频繁k-项集
	 * @return
	 */
	private Iterator<Set<String>> getIterator(Set<String> itemSet,
			Set<Set<String>> freqKItemSet) {
		Iterator<Set<String>> it = freqKItemSet.iterator();
		while (it.hasNext()) {
			if (itemSet.equals(it.next())) {
				break;
			}
		}
		return it;
	}

	/**
	 * 根据频繁(k-1)-项集，调用aprioriGen方法，计算频繁k-项集
	 * 
	 * @param k
	 * @param freqMItemSet
	 *            频繁(k-1)-项集
	 * @return
	 */
	public Map<Set<String>, Float> getFreqKItemSet(int k,
			Set<Set<String>> freqMItemSet) {
		Map<Set<String>, Integer> candFreqKItemSetMap = new HashMap<Set<String>, Integer>();
		// 调用aprioriGen方法，得到候选频繁k-项集
		Set<Set<String>> candFreqKItemSet = this
				.aprioriGen(k - 1, freqMItemSet);

		// 扫描事务数据库
		Iterator<Map.Entry<Integer, Set<String>>> it = txDatabase.entrySet()
				.iterator();
		// 统计支持数
		while (it.hasNext()) {
			Map.Entry<Integer, Set<String>> entry = it.next();
			Iterator<Set<String>> kit = candFreqKItemSet.iterator();
			while (kit.hasNext()) {
				Set<String> kSet = kit.next();
				Set<String> set = new HashSet<String>();
				set.addAll(kSet);
				set.removeAll(entry.getValue()); // 候选频繁k-项集与事务数据库中元素做差元算
				if (set.isEmpty()) { // 如果拷贝set为空，支持数加1
					if (candFreqKItemSetMap.get(kSet) == null) {
						Integer value = 1;
						candFreqKItemSetMap.put(kSet, value);
					} else {
						Integer value = 1 + candFreqKItemSetMap.get(kSet);
						candFreqKItemSetMap.put(kSet, value);
					}
				}
			}
		}
		// 计算支持度，生成频繁k-项集，并返回
		return support(candFreqKItemSetMap);
	}

	/**
	 * 根据候选频繁k-项集，得到频繁k-项集
	 * 
	 * @param candFreqKItemSetMap
	 *            候选k项集(包含支持计数)
	 */
	public Map<Set<String>, Float> support(
			Map<Set<String>, Integer> candFreqKItemSetMap) {
		Map<Set<String>, Float> freqKItemSetMap = new HashMap<Set<String>, Float>();
		Iterator<Map.Entry<Set<String>, Integer>> it = candFreqKItemSetMap
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Set<String>, Integer> entry = it.next();
			// 计算支持度
			Float supportRate = new Float(entry.getValue().toString())
					/ new Float(txDatabaseCount);
			if (supportRate < minSup) { // 如果不满足最小支持度，删除
				it.remove();
			} else {
				freqKItemSetMap.put(entry.getKey(), supportRate);
			}
		}
		return freqKItemSetMap;
	}

	/**
	 * 挖掘全部频繁项集
	 */
	public void mineFreqItemSet() {
		// 计算频繁1-项集
		Set<Set<String>> freqKItemSet = this.getFreq1ItemSet().keySet();
		freqItemSet.put(1, freqKItemSet);
		// 计算频繁k-项集(k>1)
		int k = 2;
		while (true) {
			Map<Set<String>, Float> freqKItemSetMap = this.getFreqKItemSet(k,
					freqKItemSet);
			if (!freqKItemSetMap.isEmpty()) {
				this.freqItemSet.put(k, freqKItemSetMap.keySet());
				freqKItemSet = freqKItemSetMap.keySet();
			} else {
				break;
			}
			k++;
		}
	}

	/**
	 * <P>
	 * 挖掘频繁关联规则
	 * <P>
	 * 首先挖掘出全部的频繁项集，在此基础上挖掘频繁关联规则
	 */
	public void mineAssociationRules() {
		freqItemSet.remove(1); // 删除频繁1-项集
		Iterator<Map.Entry<Integer, Set<Set<String>>>> it = freqItemSet
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Set<Set<String>>> entry = it.next();
			for (Set<String> itemSet : entry.getValue()) {
				// 对每个频繁项集进行关联规则的挖掘
				mine(itemSet);
			}
		}
	}

	/**
	 * 对从频繁项集集合freqItemSet中每迭代出一个频繁项集元素，执行一次关联规则的挖掘
	 * 
	 * @param itemSet
	 *            频繁项集集合freqItemSet中的一个频繁项集元素
	 */
	public void mine(Set<String> itemSet) {
		int n = itemSet.size() / 2; // 根据集合的对称性，只需要得到一半的真子集
		for (int i = 1; i <= n; i++) {
			// 得到频繁项集元素itemSet的作为条件的真子集集合
			Set<Set<String>> properSubset = ProperSubsetCombination
					.getProperSubset(i, itemSet);
			// 对条件的真子集集合中的每个条件项集，获取到对应的结论项集，从而进一步挖掘频繁关联规则
			for (Set<String> conditionSet : properSubset) {
				Set<String> conclusionSet = new HashSet<String>();
				conclusionSet.addAll(itemSet);
				conclusionSet.removeAll(conditionSet); // 删除条件中存在的频繁项
				confide(conditionSet, conclusionSet); // 调用计算置信度的方法，并且挖掘出频繁关联规则
			}
		}
	}

	/**
	 * 对得到的一个条件项集和对应的结论项集，计算该关联规则的支持计数，从而根据置信度判断是否是频繁关联规则
	 * 
	 * @param conditionSet
	 *            条件频繁项集
	 * @param conclusionSet
	 *            结论频繁项集
	 */
	public void confide(Set<String> conditionSet, Set<String> conclusionSet) {
		// 扫描事务数据库
		Iterator<Map.Entry<Integer, Set<String>>> it = txDatabase.entrySet()
				.iterator();
		// 统计关联规则支持计数
		int conditionToConclusionCnt = 0; // 关联规则(条件项集推出结论项集)计数
		int conclusionToConditionCnt = 0; // 关联规则(结论项集推出条件项集)计数
		int supCnt = 0; // 关联规则支持计数
		while (it.hasNext()) {
			Map.Entry<Integer, Set<String>> entry = it.next();
			Set<String> txSet = entry.getValue();
			Set<String> set1 = new HashSet<String>();
			Set<String> set2 = new HashSet<String>();
			set1.addAll(conditionSet);

			set1.removeAll(txSet); // 集合差运算：set-txSet
			if (set1.isEmpty()) { // 如果set为空，说明事务数据库中包含条件频繁项conditionSet
				// 计数
				conditionToConclusionCnt++;
			}
			set2.addAll(conclusionSet);
			set2.removeAll(txSet); // 集合差运算：set-txSet
			if (set2.isEmpty()) { // 如果set为空，说明事务数据库中包含结论频繁项conclusionSet
				// 计数
				conclusionToConditionCnt++;

			}
			if (set1.isEmpty() && set2.isEmpty()) {
				supCnt++;
			}
		}
		// 计算置信度
		Float conditionToConclusionConf = new Float(supCnt)
				/ new Float(conditionToConclusionCnt);
		if (conditionToConclusionConf >= minConf) {
			if (assiciationRules.get(conditionSet) == null) { // 如果不存在以该条件频繁项集为条件的关联规则
				Set<Set<String>> conclusionSetSet = new HashSet<Set<String>>();
				conclusionSetSet.add(conclusionSet);
				assiciationRules.put(conditionSet, conclusionSetSet);
			} else {
				assiciationRules.get(conditionSet).add(conclusionSet);
			}
		}
		Float conclusionToConditionConf = new Float(supCnt)
				/ new Float(conclusionToConditionCnt);
		if (conclusionToConditionConf >= minConf) {
			if (assiciationRules.get(conclusionSet) == null) { // 如果不存在以该结论频繁项集为条件的关联规则
				Set<Set<String>> conclusionSetSet = new HashSet<Set<String>>();
				conclusionSetSet.add(conditionSet);
				assiciationRules.put(conclusionSet, conclusionSetSet);
			} else {
				assiciationRules.get(conclusionSet).add(conditionSet);
			}
		}
	}

	/**
	 * 根据邻近规则剪枝，处理2-项频繁项集
	 */
	public Map<String, Float> trim2ItemSet(
			Map<Set<String>, Float> freqKItemSetMap) {
		Map<String, Float> mapAfterTrim = new TreeMap<String, Float>();
		for (Set<String> key : freqKItemSetMap.keySet()) {// 取2-项频繁集中词项f
			int flag = 0;
			int positive = 0;
			int count = 0;
			Iterator<String> it = key.iterator();
			String str[] = new String[2];
			int i = 0;
			while (it.hasNext()) {
				str[i++] = it.next();
			}
			for (Integer sentenceNo : txDatabase.keySet()) {// 取名词数据库中的事务项s
				Set<String> sentence = txDatabase.get(sentenceNo);
				if (sentence.containsAll(key)) {// 如果事务项s包含词项f，则排查原文数据库中该句子中f中词语的索引
					count++;
					ArrayList<String> list = txDatabaseWithoutTag
							.get(sentenceNo);
					int index[] = new int[2];
					index[0] = list.indexOf(str[0]);
					index[1] = list.indexOf(str[1]);
					if (index[0] >= 0 && index[1] >= 0
							&& Math.abs(index[1] - index[0]) <= 1) {
						flag++;
						positive += index[1] - index[0] > 0 ? 1 : -1;
					}
				}
			}
			if (flag >= count * 0.8) {
				String newstr = positive >= 0 ? (str[0] + str[1])
						: (str[1] + str[0]);
				Float f = freqKItemSetMap.get(key);
				mapAfterTrim.put(newstr, f);
				// 计算独立支出度，被删掉的不再计算
				if (candidate1Item.get(str[0]) != null) {
					Float sup1 = candidate1Item.get(str[0]);
					Float newsup1 = sup1 - f;
					if (newsup1 >= minSup) {
						candidate1Item.put(str[0], newsup1);
					} else {
						candidate1Item.remove(str[0]);
//						System.out.println(str[0]+"deleted");
					}
				}

				if (candidate1Item.get(str[1]) != null) {
					Float sup2 = candidate1Item.get(str[1]);
					Float newsup2 = sup2 - f;
					if (newsup2 >= minSup) {
						candidate1Item.put(str[1], newsup2);
					} else {
						candidate1Item.remove(str[1]);
//						System.out.println(str[1]+"deleted");
					}
				}
//				System.out.println("候选2-项频繁项:" + flag + newstr + count);
			}
		}
		return mapAfterTrim;
	}

	/**
	 * 根据邻近规则剪枝，处理3-项频繁项集
	 */
	public Map<String, Float> trim3ItemSet(
			Map<Set<String>, Float> freqKItemSetMap) {
		Map<String, Float> mapAfterTrim = new TreeMap<String, Float>();
		for (Set<String> key : freqKItemSetMap.keySet()) {// 取2-项频繁集中词项f
			int flag = 0;
			int count = 0;
			int a = 0, b = 0, c = 0;
			Iterator<String> it = key.iterator();
			String str[] = new String[3];
			int i = 0;
			while (it.hasNext()) {
				str[i++] = it.next();
			}
			for (Integer sentenceNo : txDatabase.keySet()) {// 取名词数据库中的事务项s
				Set<String> sentence = txDatabase.get(sentenceNo);
				if (sentence.containsAll(key)) {// 如果事务项s包含词项f，则排查原文数据库中该句子中f中词语的索引
					count++;
					ArrayList<String> list = txDatabaseWithoutTag
							.get(sentenceNo);
					int index[] = new int[3];
					index[0] = list.indexOf(str[0]);
					index[1] = list.indexOf(str[1]);
					index[2] = list.indexOf(str[2]);
					int distance1 = Math.abs(index[1] - index[0]);
					int distance2 = Math.abs(index[2] - index[1]);
					int distance3 = Math.abs(index[2] - index[0]);
					if (distance1 <= 3 && distance2 <= 3 && distance3 <= 3) {
						flag++;
						if (index[0] < index[1]) {
							a = 0;
							b = 1;
						} else {
							b = 0;
							a = 1;
						}
						if (index[2] < index[a]) {
							c = b;
							b = a;
							a = 2;
						} else if (index[2] > index[b]) {
							c = 2;
						} else {
							c = b;
							b = 2;
						}
					}
				}
			}
			if (flag >= count * 0.8) {
				String newstr = str[a] + str[b] + str[c];
				Float f = freqKItemSetMap.get(key);
				mapAfterTrim.put(newstr, f);
//				System.out.println("候选3-项频繁项:" + flag + newstr + count);
			}
		}
		Iterator<Entry<String, Float>> it = candidate2Item.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, Float> entry = it.next();
			String strIn2 = entry.getKey();
			Float oldsup = entry.getValue();
			for (String strIn3 : mapAfterTrim.keySet()) {
				if (strIn3.contains(strIn2)) {
					Float sup = oldsup - mapAfterTrim.get(strIn3);
					if (sup >= minSup)
						candidate2Item.put(strIn2, sup);
					else {
						it.remove();
//						System.out.println(strIn2 + "deleted");
					}
				}
			}
		}
		return mapAfterTrim;
	}

	/**
	 * 计算候选特征集
	 */
	public void genFeature() {
		Map<Set<String>, Float> freq1ItemSet = this.getFreq1ItemSet();
		Iterator<Entry<Set<String>, Float>> it = freq1ItemSet.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<Set<String>, Float> entry = it.next();
			Set<String> key = entry.getKey();
			String realkey = key.iterator().next();
			if (realkey.length() == 1) {
				it.remove();
			} else {
				Float value = entry.getValue();
				candidate1Item.put(realkey, value);
			}
		}

		Map<Set<String>, Float> freq2ItemSet = this.getFreqKItemSet(2,
				freq1ItemSet.keySet());
		Map<Set<String>, Float> freq3ItemSet = this.getFreqKItemSet(3,
				freq2ItemSet.keySet());

		// for (Entry<Set<String>, Float> entry : freq1ItemSet.entrySet()) {
		// Set<String> key = entry.getKey();
		// String realkey = key.iterator().next();
		// if (realkey.length() == 1)
		// continue;
		// Float value = entry.getValue();
		// candidate1Item.put(realkey, value);
		// }

		candidate2Item = trim2ItemSet(freq2ItemSet);
		candidate3Item = trim3ItemSet(freq3ItemSet);
		// System.out.println(candidate1Item);
		// System.out.println(candidate2Item);
		// System.out.println(candidate3Item);
		feature.putAll(candidate1Item);
		feature.putAll(candidate2Item);
		feature.putAll(candidate3Item);
		dropCommonWords();
		// dropSingle();
		System.out.println(feature.keySet());
	}

	/**
	 * 去掉常见词语
	 */
	public void dropCommonWords() {
		for (int i : commonwords.keySet()) {
			String word = commonwords.get(i);
			feature.remove(word);
		}
	}

	/**
	 * 去除单字名词
	 */
	public void dropSingle() {
		Iterator<String> it = feature.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key.length() == 1)
				it.remove();
		}
	}

	/**
	 * 性能评估
	 */
	public void functionTest() {
		float sizeOfCan = feature.size();
		float sizeOfReal = manualfeature.size();
		float sizeOfRight = 0;
		for (String f : manualfeature.values()) {
			if (feature.containsKey(f)) {
				sizeOfRight++;
			}
		}
		float precision = sizeOfRight / sizeOfCan;
		float recall = sizeOfRight / sizeOfReal;
		System.out.println("挖掘到的特征数为：" + sizeOfCan);
		System.out.println("正确查出的特征数为：" + sizeOfRight);
		System.out.println("查准率为：" + precision);
		System.out.println("查全率为：" + recall);
		System.out.println("F值为：" + ((precision * recall * 2)/(precision + recall)));
	}

	/**
	 * 经过挖掘得到的频繁项集Map
	 * 
	 * @return 挖掘得到的频繁项集集合
	 */
	public Map<Integer, Set<Set<String>>> getFreqItemSet() {
		return freqItemSet;
	}

	/**
	 * 获取挖掘到的全部的频繁关联规则的集合
	 * 
	 * @return 频繁关联规则集合
	 */
	public Map<Set<String>, Set<Set<String>>> getAssiciationRules() {
		return assiciationRules;
	}
}
