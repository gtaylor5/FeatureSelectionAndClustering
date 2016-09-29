import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**************************************************************************
This is the Main class that holds the main method. This runs HAC and KMeans
for SFS and GAS. 
**************************************************************************/

public class Main {
	
	/**************************************************************************
	 Runs all 3 dataSets through each selection and clustering algorithm.
	**************************************************************************/
	static File results = new File("Results.txt");
	public static void main(String[] args) throws IOException {
		System.out.println("running...");
		DataConfig[] configs = new DataConfig[3];
		configs[0] = new DataConfig("irisTest.txt", "irisTrain.txt","Iris", 3, 4);
		configs[1] = new DataConfig("glassTest.txt", "glassTrain.txt","Glass", 6, 9);
		configs[2] = new DataConfig("spambaseTest.txt", "spambaseTrain.txt","SpamBase", 2, 57);
		for(int i = 0; i < configs.length; i++){
			System.out.println(i);
			Tester testKMEANS = new Tester(configs[i]);
			Tester testHAC = new Tester(configs[i]);
			testKMEANS.SFS_KMEANS(testKMEANS.defaultFeatures, configs[i].trainSet, configs[i].testSet, configs[i].numClasses);
			System.out.println(i);
			testHAC.SFS_HAC(testHAC.defaultFeatures, configs[i].trainSet, configs[i].testSet, configs[i].numClasses);
			System.out.println(i);
			testKMEANS.GeneticAlgorithmSelection(configs[i].trainSet, configs[i].testSet, configs[i].numClasses);
			System.out.println(i);
			testHAC.GeneticAlgorithmSelectionHAC(configs[i].trainSet, configs[i].testSet, configs[i].numClasses);
			System.out.println("Last");
		}
		System.out.println("Done");
	}
}
