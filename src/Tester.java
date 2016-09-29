import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
public class Tester {
	
	
	HashSet<Integer> defaultFeatures = new HashSet<Integer>();
	int features = 0;
	ArrayList<Features> featureSets = new ArrayList<Features>();
	DataConfig config;
	PrintWriter writer;
	
	/**************************************************************************
	 Constructor initializes defaultFeature locations i.e 0-(k-1)
	 * @throws IOException 
	**************************************************************************/
	
	public Tester(DataConfig config) throws IOException{
		if(config.numFeatures == 9){
			for(int i = config.numFeatures-1; i > 0; i--){
				defaultFeatures.add(i);
			}
		}else{
			for(int i = 0; i < config.numFeatures; i++){
				defaultFeatures.add(i);
			}
		}
		this.features = config.numFeatures;
		this.config = config;
	}
	
	/**************************************************************************
	 Stepwise Forward Selection using KMeans Clustering. Takes in testSet,
	 trainingSet, featureSet and numClasses (k).
	 * @throws IOException 
	**************************************************************************/
	
	public HashSet<Integer> SFS_KMEANS(HashSet<Integer> featureSet, String trainSetPath, String testSetPath, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println(this.config.dataSetName + " Data Set");
		writer.println();
		HashSet<Integer> featureIndexes = new HashSet<Integer>(); // empty feature set.
		double basePerformance = Double.MIN_VALUE; // base performance -INF
		KMeans means = new KMeans(trainSetPath, k); //Initialize KMeans instance
		means.initializeMeans(); //See KMeans.initializeMeans() for description.
		while(featureSet.size() != 0){ // while the default featureset is not empty.
			double bestPerformance = Double.MIN_VALUE; // best performance for each featureSet combination initialized to -INF
			int bestF = 0; // best Feature location initialized to zero.
			for(int val : featureSet){//iterate through each value in the featureSet
				featureIndexes.add(val); //add the feature to the test featureSet
				means.generateMeans(featureIndexes); //See KMeans.generateMeans(HashSet<Integer>)
				ArrayList<Double[]> h = means.means; // set mean values for testing.
				means.pruneClusters(); // reset Clusters.
				double currentPerformance = means.testAlgo(testSetPath, h, featureIndexes); // get current performance value against testSet
				if(currentPerformance > bestPerformance){ // update bestPerformcen if current is better.
					bestPerformance = currentPerformance;
					bestF = val; // set best feature location.
				}
				featureIndexes.remove(val); // remove feature and start again.
			}
			if(bestPerformance >= basePerformance){ // if best performance is better than base update baseperformance
				basePerformance = bestPerformance;
				featureSet.remove(bestF); // remove best feature from default set.
				featureIndexes.add(bestF); // add best feature to test featureSet
			}else{//otherwise break
				break;
			}
		}
		
		//below is used for printing results.
		writer.println("Stepwise Forward Selection: ");
		writer.print("Feature Set: ");
		for(int vals : featureIndexes){
			writer.print(vals + " ");
		}
		writer.println();
		writer.printf("%s%.2f\n","Best performance: ",basePerformance);
		writer.println();
		writer.println();
		writer.close();
		return featureIndexes;
	}
	
	/**************************************************************************
	 Stepwise Forward Selection using Heirarchical Agglomerative Clustering. Takes in testSet,
	 trainingSet, featureSet and numClasses (k). Identical to SFS_KMEANS except
	 for testing and generating means.
	 * @throws IOException 
	**************************************************************************/
	
	public HashSet<Integer> SFS_HAC(HashSet<Integer> featureSet, String trainSetPath, String testSetPath, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		HashSet<Integer> featureIndexes = new HashSet<Integer>();
		double basePerformance = Double.MIN_VALUE;
		HAC hac = new HAC(k, features);
		while(featureSet.size() != 0){
			double bestPerformance = Double.MIN_VALUE;
			int bestF = 0;
			hac.generateClusters(trainSetPath);
			for(int val : featureSet){
				//generate initial clusters
				featureIndexes.add(val); // add values to featureSet
				hac.mergeClusters(k, featureIndexes); //merge clusters using featureSet
				hac.perfectClusters(featureIndexes); // perfect clusters using featureSet
				ArrayList<Double[]> h = hac.newMeans; // set means for testign
				//hac.pruneClusters(); // reset clusters
				//hac.pruneClusters1(); // reset clusters
				double currentPerformance = hac.testAlgo(testSetPath, h, featureIndexes); //generate performance
				//rest is same as SFS_KMEANS
				if(currentPerformance > bestPerformance){
					bestPerformance = currentPerformance;
					bestF = val;
				}
				featureIndexes.remove(val);
			}
			if(bestPerformance >= basePerformance){
				basePerformance = bestPerformance;
				featureSet.remove(bestF);
				featureIndexes.add(bestF);
			}else{
				break;
			}
		}
		writer.println("Stepwise Forward Selection (HAC): ");
		writer.print("Feature Set: ");
		for(int vals : featureIndexes){
			writer.print(vals + " ");
		}
		writer.println();
		writer.printf("%s%.2f\n","Best performance: ",basePerformance);
		writer.println();
		writer.println();
		writer.close();
		return featureIndexes;
	}
	
	/**************************************************************************
	 Genetic Algorithm Selection using KMeans Clustering. Takes in testSet,
	 trainingSet and numClasses (k).
	 * @throws IOException 
	**************************************************************************/
	
	public void GeneticAlgorithmSelection(String trainSetPath, String testSetPath, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		initialize(trainSetPath); // See Initialize below. intialize a random set of parents.
		evaluateFitnessKMeans(trainSetPath, testSetPath, k); // evaluates the fitness of each parent
			Features[] weakestFeatures = fitnessProportionateSelection(); // see Below
			Features[] children = mateParents(weakestFeatures); //See below
			children = mutateChildren(children);//See below
			children = evaluateFitnessKMeans(trainSetPath, testSetPath, k, children);//See below
			replaceWeakest(children);//See below
		//prints results
		writer.println("Genetic Algorithm Selection (K-Means): ");
		writer.print("Feature Set: ");
		for(int i = 0; i < featureSets.get(featureSets.size()-1).featureSet.size(); i++){
			writer.print(featureSets.get(featureSets.size()-1).featureSet.get(i) + " ");
		}
		writer.println();
		writer.printf("%s%.2f\n","Performance: ",featureSets.get(featureSets.size()-1).fitness);
		writer.println();
		writer.println();
		writer.close();
	}
	
	/**************************************************************************
	 Genetic Algorithm Selection using HAC. Takes in testSet,
	 trainingSet and numClasses (k).
	 * @throws IOException 
	**************************************************************************/
	
	public void GeneticAlgorithmSelectionHAC(String trainSetPath, String testSetPath, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		initialize(trainSetPath);
		System.out.println("initialize complete");
		evaluateFitnessHAC(trainSetPath, testSetPath, k);
		System.out.println("this sucks");
		Features[] weakestFeatures = fitnessProportionateSelection();
		System.out.println("here?");
		Features[] children = mateParents(weakestFeatures);
		System.out.println(" or here?");
		children = mutateChildren(children);
		System.out.println("there");
		children = evaluateFitnessHAC(trainSetPath, testSetPath, k, children);
		System.out.println("anywhere");
		replaceWeakest(children);
		System.out.println("this is stupid");
		writer.println("Genetic Algorithm Selection (HAC): ");
		writer.print("Feature Set: ");
		for(int i = 0; i < featureSets.get(featureSets.size()-1).featureSet.size(); i++){
			writer.print(featureSets.get(featureSets.size()-1).featureSet.get(i) + " ");
		}
		writer.println();
		writer.printf("%s%.2f\n","Performance: ",featureSets.get(featureSets.size()-1).fitness);
		writer.println();
		writer.println();
		writer.close();
		System.out.println("Done");
	}
	
	/**************************************************************************
	 Helper methods for Genetic algorithm Selection 
	 **************************************************************************/
	
	/**************************************************************************
	 steady state replacement of weakest instances.
	**************************************************************************/
	
	public void replaceWeakest(Features[] children){
		featureSets.set(0, children[0]);
		featureSets.set(1, children[1]);
		sort(); // sorts featureSets based on fitness.
	}
	
	/**************************************************************************
	 Mutation scheme used to mutate children.
	**************************************************************************/
	
	public Features[] mutateChildren(Features[] children){
		double probability = 0.95; // probability has to be high to change values.
		for(int i = 0; i < children.length; i++){
			for(int j = 0; j < children[i].featureSet.size(); j++){
				double random = Math.random();
				if(random > probability){ // swap values.
					if(children[i].featureSet.get(j) == 1){
						children[i].featureSet.set(j, 0);
					}else{
						children[i].featureSet.set(j, 1);
					}
				}
			}
		}
		return children; // return children.
	}
	
	/**************************************************************************
	 Select two parents based on fitness.
	**************************************************************************/
	
	public Features[] fitnessProportionateSelection(){
		Features[] weakest = new Features[2];
		double fitnessSum = getFitnessSum(); // sum all fitness values for each featureSet.
		for(int i = 0; i < featureSets.size(); i++){
			featureSets.get(i).probability = featureSets.get(i).fitness/fitnessSum; // set probability for each featureSet.
		}
		sort();//sort fitness. See Below.
		// get two weakest.
		weakest[0] = featureSets.get(0); 
		weakest[1] = featureSets.get(1);
		return weakest; // return array of weakest parents.
	}
	
	/**************************************************************************
	 Mate parents based on crossover scheme.
	**************************************************************************/
	
	public Features[] mateParents(Features[] parents){
		int crossoverPoint = generateCrossOverPoint(parents[0].featureSet.size()-1); // set crossover point.
		// initialize children
		Features childOne = new Features();
		Features childTwo = new Features();
		int i = 0;
		//set attribute values based on crossover point.
		while(i < parents[0].featureSet.size()){
			if(i < crossoverPoint){
				childOne.featureSet.add(parents[0].featureSet.get(i));
				childTwo.featureSet.add(parents[1].featureSet.get(i));
				i++;
			}else{
				childOne.featureSet.add(parents[1].featureSet.get(i));
				childTwo.featureSet.add(parents[0].featureSet.get(i));
				i++;
			}
		}
		//return array of children.
		Features[] children = new Features[]{childOne, childTwo};
		return children;
	}
	
	/**************************************************************************
	 Evaluate fitness using KMeans. Overloaded method from initialize section below.
	 Uses KMeans to evaluate fitness of children based on KMeans.
	**************************************************************************/
	
	public Features[] evaluateFitnessKMeans(String trainSetPath, String testSetPath, int k, Features[] children) throws FileNotFoundException{
		for(int i = 0; i < children.length; i++){
			KMeans means = new KMeans(trainSetPath, k);
			means.initializeMeans();
			HashSet<Integer> vals = new HashSet<Integer>();
			for(int j = 0; j < children[i].featureSet.size(); j++){
				if(children[i].featureSet.get(j) == 1){
					vals.add(j);
				}
			}
			if(vals.size() == 0){
				children[i].fitness = 0;
				continue;
			}
			means.generateMeans(vals);
			ArrayList<Double[]> h = means.means; // KMeans.
			double currentPerformance = means.testAlgo(testSetPath, h, vals);
			children[i].fitness = currentPerformance;
		}
		return children;
	}

	/**************************************************************************
	 Evaluate fitness using HAC. Overloaded method from initialize section below.
	 Uses KMeans to evaluate fitness of children based on HAC.
	**************************************************************************/
	public Features[] evaluateFitnessHAC(String trainSetPath, String testSetPath, int k, Features[] children) throws FileNotFoundException{
		for(int i = 0; i < children.length; i++){
			HAC hac = new HAC(k, features);
			hac.generateClusters(trainSetPath);
			HashSet<Integer> vals = new HashSet<Integer>();
			for(int j = 0; j < children[i].featureSet.size(); j++){
				if(children[i].featureSet.get(j) == 1){
					vals.add(j);
				}
			}
			if(vals.size() == 0){
				children[i].fitness = 0;
				continue;
			}
			hac.mergeClusters(k, vals);
			hac.perfectClusters(vals);
			ArrayList<Double[]> h = hac.newMeans; // HAC
			double currentPerformance = hac.testAlgo(testSetPath, h, vals);
			children[i].fitness = currentPerformance;
		}
		return children;
	}
	
	/**************************************************************************
	 Helper method to generate crossover point given array length.
	**************************************************************************/
	
	public int generateCrossOverPoint(int length){
		return (int)(Math.random()*length);
	}
	
	/**************************************************************************
	 Sums fitness across all featureSets.
	**************************************************************************/
	
	public double getFitnessSum(){
		double sum = 0;
		for(int i = 0; i < featureSets.size(); i++){
			sum+=featureSets.get(i).fitness;
		}
		return sum;
	}
	
	/**************************************************************************
	 These methods are used to initialize the featureSets Arraylist
	 **************************************************************************/
	
	/**************************************************************************
	 Initialization fitness evaluations. Uses KMeans and initial featureSet
	 generated from initialize method below.
	**************************************************************************/
	
	public void evaluateFitnessKMeans(String trainSetPath, String testSetPath, int k) throws FileNotFoundException{
		for(int i = 0; i < featureSets.size(); i++){
			KMeans means = new KMeans(trainSetPath, k);
			means.initializeMeans();
			HashSet<Integer> vals = new HashSet<Integer>();
			for(int j = 0; j < featureSets.get(i).featureSet.size(); j++){
				if(featureSets.get(i).featureSet.get(j) == 1){
					vals.add(j);
				}
			}
			if(vals.size() == 0){
				continue;
			}
			means.generateMeans(vals);
			ArrayList<Double[]> h = means.means;
			double currentPerformance = means.testAlgo(testSetPath, h, vals);
			featureSets.get(i).fitness = currentPerformance;
		}
		Collections.sort(featureSets);
	}
	
	/**************************************************************************
	 Initialization fitness evaluations. Uses HAC and initial featureSet
	 generated from initialize method below.
	**************************************************************************/
	
	public void evaluateFitnessHAC(String trainSetPath, String testSetPath, int k) throws FileNotFoundException{
		for(int i = 0; i < featureSets.size(); i++){
			HAC hac = new HAC(k, features);
			hac.generateClusters(trainSetPath);
			System.out.println(i);
			HashSet<Integer> vals = new HashSet<Integer>();
			for(int j = 0; j < featureSets.get(i).featureSet.size(); j++){
				if(featureSets.get(i).featureSet.get(j) == 1){
					vals.add(j);
				}
			}
			if(vals.size() == 0){
				continue;
			}
			hac.mergeClusters(k, vals);
			hac.perfectClusters(vals);
			ArrayList<Double[]> h = hac.newMeans;
			double currentPerformance = hac.testAlgo(testSetPath, h, vals);
			featureSets.get(i).fitness = currentPerformance;
		}
		Collections.sort(featureSets);
	}
	
	/**************************************************************************
	Initialize random featureSet that has the size of a file OR 150 points.
	**************************************************************************/
	
	public void initialize(String filePath) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(filePath));
		int i = 0;
		if(filePath.equalsIgnoreCase("spambaseTest.txt")||filePath.equalsIgnoreCase("spambaseTrain.txt")){
			while(i < 25){
				String[] line = fileScanner.nextLine().split(" ");
				featureSets.add(new Features());
				for(int j = 0; j < line.length-1; j++){
					double random = Math.random()*100;
					if(random > 50){
						featureSets.get(i).featureSet.add(1);
					}else{
						featureSets.get(i).featureSet.add(0);
					}
				}
				featureSets.get(i).featureSet.trimToSize();
				i++;
			}
			removeDupes(); // remove duplicate entries.
			fileScanner.close();
		}else{
			while(fileScanner.hasNextLine() && i < 100){
				String[] line = fileScanner.nextLine().split(" ");
				featureSets.add(new Features());
				for(int j = 0; j < line.length-1; j++){
					double random = Math.random()*100;
					if(random > 50){
						featureSets.get(i).featureSet.add(1);
					}else{
						featureSets.get(i).featureSet.add(0);
					}
				}
				featureSets.get(i).featureSet.trimToSize();
				i++;
			}
			removeDupes(); // remove duplicate entries.
			fileScanner.close();
		}
	}	
	
	/**************************************************************************
	 Removes duplicate featureSets from featureSets arrayList.
	**************************************************************************/
	
	public void removeDupes(){
		HashSet<String> vals = new HashSet<String>();
		int i = 0;
		while(i < featureSets.size()){
			String feature = "";
			for(int j = 0; j < featureSets.get(i).featureSet.size(); j++){
				if(featureSets.get(i).featureSet.get(j) == 1){
					feature+=String.valueOf(j);
				}
			}
			if(vals.contains(feature) || feature.equals("")){
				featureSets.remove(i);
				continue;
			}
			vals.add(feature);
			i++;
		}
	}
	
	/**************************************************************************
	 Utility Methods
	 **************************************************************************/
	
	public void printFeatureSets(){
		for(Features f : featureSets){
			for(int val : f.featureSet){
				System.out.print(val + " ");
			}
			System.out.println();
		}
	}

	public void sort(){
		Collections.sort(featureSets, new Comparator<Features>() {
			@Override
			public int compare(Features o1, Features o2) {
				return (int) (o1.probability - o2.probability);
			}
		});
	}
}
