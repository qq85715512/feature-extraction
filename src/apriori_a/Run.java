package apriori_a;

public class Run {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		TestAprioriAlgorithm test=new TestAprioriAlgorithm();
		try {
			test.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test.testGenFeature();


//		test.testFreq1ItemSet();
//		test.testAprioriGen();
//		test.testGetFreq2ItemSet();		
//		test.testGetFreq3ItemSet();
//		test.testGetFreqItemSet();
//		test.testMineAssociationRules();

	}

}
