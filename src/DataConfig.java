
/**************************************************************************
The DataConfig class is used to hold the configurations of the dataSets for
easy iteration through the algorithms. See implementation in Main.class
**************************************************************************/

public class DataConfig {
	
	String testSet = "";
	String trainSet = "";
	String dataSetName = "";
	int numClasses = 0;
	int numFeatures = 0;
	
	public DataConfig(String test, String train, String name, int classes, int features){
		this.testSet = test;
		this.trainSet = train;
		this.dataSetName = name;
		this.numClasses = classes;
		this.numFeatures = features;
	}

}
