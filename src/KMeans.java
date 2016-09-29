import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

//Number of clusters & means are equal to the number of classes

/**************************************************************************
The Kmeans class is a class that executes the KMeans clustering algorithm 
in order to help with SFS and GAS.
**************************************************************************/

public class KMeans {

	/**************************************************************************
	The DataConfig class is used to hold the configurations of the dataSets for
	easy iteration through the algorithms. See implementation in Main.class
	**************************************************************************/
	
	ArrayList<Double[]> means; // Average values of each cluster.
	ArrayList<Double[]> newMeans; // means that are calculated after each iteration of the respective algorithms.
	ArrayList<ArrayList<Double[]>> clusters; // set of clusters. Set to the amount of classes in each data set.
	String filePath; //path to the training set.
	int dataLength = 0; // i.e number of features.
	int k = 0; // number of classes
	int val = 0;
	
	/**************************************************************************
	KMeans constructor. Sets attribute values above.
	**************************************************************************/
	
	public KMeans(String filePath, int k) throws FileNotFoundException{
		this.filePath = filePath;
		Scanner fileScanner = new Scanner(new File(this.filePath));
		if(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" ");
			this.dataLength = line.length-1;
		}
		means = new ArrayList<Double[]>(k);
		newMeans = new ArrayList<Double[]>(k);
		clusters = new ArrayList<ArrayList<Double[]>>(k); // clusters matches # classes.
		for(int i = 0; i < k; i++){ // initialize each internal arraylist or double[] array.
			clusters.add(new ArrayList<Double[]>());
			newMeans.add(new Double[dataLength]);
		}
		this.k = k; // set k.
		fileScanner.close(); // close file scanner.
	}
	
	/**************************************************************************
	 testAlgo returns the performance given a set of means and featureSets.
	 the filePath that is usually passed in is the test file.
	**************************************************************************/
	
	public double testAlgo(String filePath,ArrayList<Double[]> calcMeans, HashSet<Integer> indexes) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(this.filePath)); // initialize file scanner
		double fileSize = 0; // for mean calculation see line 122
		double correct = 0; // number of correct classifications.
		while(fileScanner.hasNextLine()){
			fileSize++;
			String[] line = fileScanner.nextLine().split(" ");
			if(line.length == 0){
				break;
			}
			Double[] dataInstance = convertData(line); // convert data from string array to double array ignoring class value at line.length-1.
			double minDistance = Double.MAX_VALUE;
			int index = 0;
			//Cluster at "index" is closest cluster.
			for(int i = 0; i < calcMeans.size(); i++){
				double distance = euclidDistance(dataInstance,calcMeans.get(i), indexes);
				if(minDistance > distance){
					minDistance = distance;
					index = i;
				}
			}
			if(filePath.equalsIgnoreCase("irisTest.txt")){//check that index aligns with the correct class. If so, increment correct. else do nothing.
				if((index == 0 && line[line.length-1].equalsIgnoreCase("Iris-setosa")) || (index == 1 && line[line.length-1].equalsIgnoreCase("Iris-versicolor") ||(index == 2 && line[line.length-1].equalsIgnoreCase("Iris-virginica")))){
						correct++;
				}
			}else if(filePath.equalsIgnoreCase("glassTest.txt")){//check that index aligns with the correct class. If so, increment correct. else do nothing.
				switch(index){
					case 0:
						if(line[line.length-1].equals("1")){
							correct++;
						}
						break;
					case 1:
						if(line[line.length-1].equals("2")){
							correct++;
						}
						break;
					case 2:
						if(line[line.length-1].equals("3")){
							correct++;
						}
						break;
					case 3:
						if(line[line.length-1].equals("5")){
							correct++;
						}
						break;
					case 4:
						if(line[line.length-1].equals("6")){
							correct++;
						}
						break;
					case 5:
						if(line[line.length-1].equals("7")){
							correct++;
						}
						break;
					}
				}else if(filePath.equalsIgnoreCase("spambaseTest.txt")){//check that index aligns with the correct class. If so, increment correct. else do nothing.
					if((index == 0 && line[line.length-1].equals("1")) || (index == 1 && line[line.length-1].equals("0"))){
						correct++;
					}
				}
			}
		fileScanner.close(); // close fileScanner
		return (correct/fileSize)*100; //return fitness/effectiveness
	}
	
	/**************************************************************************
	 generateMeans generates the means for each cluster until the means don't 
	 change.
	**************************************************************************/
	
	public void generateMeans(HashSet<Integer> indexes) throws FileNotFoundException{
		while(true){
			Scanner fileScanner = new Scanner(new File(this.filePath));
			while(fileScanner.hasNextLine()){
				String[] line = fileScanner.nextLine().split(" ");
				if(line.length == 0){
					break;
				}
				Double[] dataInstance = convertData(line); // convert string array to double array.
				assignCluster(dataInstance, indexes); //assign data instance to correct cluster basd on the featureSet (indexes).
			}
			setClusterMeans(); // get the means for each cluster.
			if(meansDidChange()){ // if the means changed update "means"
				means = newMeans;
			}else{ // otherwise break
				fileScanner.close();
				break;
			}
			fileScanner.close();
		}
	}
	
	/**************************************************************************
	 Iterate through each cluster and each double array and each element in 
	 the double array and generate averages for each attribute in each cluster.
	 Only one attribute is averaged at a time. (i.e top down).
	**************************************************************************/
	
	public void setClusterMeans(){
		newMeans.removeAll(newMeans);
		for(int i = 0; i < clusters.size(); i++){ // iterate through clusters (classes)
			Double[] meanValues = new Double[dataLength];
			for(int j = 0; j < dataLength; j++){ // iterate through feature attributes.
				double meanVal = 0;
				for(int f = 0; f < clusters.get(i).size(); f++){ // iterate through all data instances.
					meanVal+= clusters.get(i).get(f)[j];
				}
				meanVal /= clusters.get(i).size();
				meanValues[j] = meanVal;
			}
			newMeans.add(meanValues);
		}
	}
	
	/**************************************************************************
	 Assigns a data instance to the proper cluster based on the featureSet(indexes).
	 Assigns data instance to closest cluster.
	**************************************************************************/
	
	public void assignCluster(Double[] x, HashSet<Integer> indexes){
		double minDistance = Double.MAX_VALUE;
		int index = 0;
		for(int i = 0; i < means.size(); i++){//Cluster at "index" is closest cluster.
			double distance = euclidDistance(x,means.get(i), indexes);
			if(minDistance > distance){
				minDistance = distance;
				index = i;
			}
		}
		clusters.get(index).add(x); // add data instance to proper cluster
	}
	
	/**************************************************************************
	 pruneClusters removes all of the double arrays from each cluster. This is 
	 necessary when continually running through the SFS and GAS algo.
	**************************************************************************/
	
	public void pruneClusters(){
		for(int i = 0; i < clusters.size(); i++){
			for(int j = 0; j < clusters.get(i).size(); j++){
				clusters.get(i).remove(clusters.get(i).get(j));
			}
		}
	}
	
	/**************************************************************************
	 Checks to see if the means are different.
	**************************************************************************/
	
	public boolean meansDidChange(){
		for(int i = 0; i < newMeans.size(); i++){
			for(int j = 0; j < newMeans.get(i).length; j++){
				if(means.get(i)[j] != newMeans.get(i)[j]){ // if values are different return true.
					return true;
				}
			}
		}
		return false;
	}
	
	/**************************************************************************
	 calculate euclidean distance between two vectors with a given featureSet.
	**************************************************************************/
	
	public Double euclidDistance(Double[] x, Double[] m, HashSet<Integer> indexes){
		Double val = 0.0;
		if(x.length != m.length){
			return -1.0;
		}else{
			for(int vals : indexes){
				val += Math.pow((x[vals]-m[vals]), 2);
			}
			return Math.sqrt(val);
		}
	}
	
	/**************************************************************************
	 convert string array to double array.
	**************************************************************************/
	
	public Double[] convertData(String[] array){
		Double[] vals = new Double[array.length-1]; // ignore last element in the array (class).
		for(int i = 0; i < vals.length; i++){
			vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}

	/**************************************************************************
	 Initialize means. pick a data instance from each class as the cluster mean.
	**************************************************************************/
	
	public void initializeMeans() throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(this.filePath));
		String currentClass = "";
		if(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" ");
			currentClass = line[line.length-1];
			means.add(convertData(line));
		}
		while(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" ");
			if(line.length == 0){
				break;
			}
			if(line[line.length-1].equals(currentClass)){
				continue;
			}
			currentClass = line[line.length-1];
			means.add(convertData(line));
		}
		fileScanner.close();
	}
	
	/**************************************************************************
	 Prints a string array as space separated values.
	**************************************************************************/
	
	public void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	 Prints a double array as space separated values.
	**************************************************************************/
	
	public void printArray(Double[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	 Prints all data instances in a cluster.
	**************************************************************************/
	
	public void printClusters(){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			for(int j = 0; j < clusters.get(i).size(); j++){
				for(int f = 0; f < clusters.get(i).get(j).length; f++){
					System.out.print(clusters.get(i).get(j)[f]+ " ");
				}
				System.out.println();
			}
		}
	}
	
	/**************************************************************************
	Prints cluster sizes.
	**************************************************************************/
	
	public void printClusters2(){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			System.out.println(clusters.get(i).size());
		}
	}
	
	/**************************************************************************
	 returns average value of a double array.
	**************************************************************************/
	
	public Double getAverage(Double[] array){
		double sum = 0;
		for(int i = 0; i < array.length; i++){
			sum+=array[i];
		}
		return sum/array.length;
	}
	
}
