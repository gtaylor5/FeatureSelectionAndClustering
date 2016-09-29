import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class HAC {
	
	/**************************************************************************
	 Supporting attributes for HAC.
	**************************************************************************/
	
	ArrayList<ArrayList<Double[]>> clusters = new ArrayList<ArrayList<Double[]>>(); //clusters similar to those found in KMeans
	ArrayList<Double[]> newMeans = new ArrayList<Double[]>(); //means of each clusters.
	ArrayList<ArrayList<Double[]>> betterClusters = new ArrayList<ArrayList<Double[]>>(); //reorganized clusters.
	int dataLength = 0; // length of data instance.
	
	/**************************************************************************
	 HAC Constructor
	**************************************************************************/
	
	public HAC(int k, int length){
		for(int i = 0; i < k; i++){
			ArrayList<Double[]> temp = new ArrayList<Double[]>();
			betterClusters.add(temp); // initialize cluster size.
		}
		dataLength = length; //set dataInstance lenght
	}

	/**************************************************************************
	 Initializes clusters. Each data instance is initially its own cluster.
	**************************************************************************/
	
	public void generateClusters(String trainPath) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(trainPath));
		while(fileScanner.hasNextLine()){
			ArrayList<Double[]> cluster = new ArrayList<Double[]>();
			String[] line = fileScanner.nextLine().split(" ");
			if(line.length == 0){
				break;
			}
			Double[] dataInstance = convertData(line); // convert string array to double array.
			cluster.add(dataInstance);
			clusters.add(cluster);
		}
		fileScanner.close();
	}

	/**************************************************************************
	 Merges clusters based on closest cluster mean. Not clean. Clusters generated
	 are unbalanced. Also, generates k clusters. One for each class.
	**************************************************************************/
	
	public void mergeClusters(int k, HashSet<Integer> indexes){
		while(clusters.size() > k){ // one cluster for each class.
			ArrayList<Double> centroids = calculateCentroids(indexes); // calculate centroid values for each data instance.
			double minDistance = Double.MIN_VALUE;
			int from = 0; // transfer value from location
			int to = 0; // transfer value to location
			to = 0;
			for(int i = 0; i < clusters.size(); i++){ // iterate through clusters
				to = i; // to location is set to current cluster.
				for(int j = 0; j < centroids.size(); j++){
					if(j == i){ // skip current "to" location
						continue;
					}
					double distance = euclidDistance(centroids.get(i),centroids.get(j)); //calculate distances between two centroids.
					if(minDistance > distance){
						minDistance = distance;
						from = j; // set from value based on minimum distance.
					}
				}
				if(to >= clusters.size() || from >= clusters.size()){ // error handling "array index out of bounds" possibility.
					break;
				}
				clusters.get(to).addAll(clusters.get(from)); // transfer clusters.
				clusters.remove(from); // remove "from" cluster.
				centroids.removeAll(centroids); // remove corresponding centroids
				centroids = calculateCentroids(indexes); //recalculate centroids.
			}
		}
	}
	
	/**************************************************************************
	 perfectClusters cleans up the clusters from the mergeClusters method. 
	 Makes sure dataInstances with similar means are clustered together.
	**************************************************************************/
	
	public void perfectClusters(HashSet<Integer> indexes){
		ArrayList<Double> means = new ArrayList<Double>();
		for(int i = 0; i < clusters.size(); i++){
			means.add(calculateCentroid(clusters.get(i), indexes));// stores means of each cluster in arraylist.
		}
		for(int i = 0; i < clusters.size(); i++){ // iterate through clusters
			for(int j = 0; j < clusters.get(i).size();j++){ // iterate through cluster dataInstances
				int index = 0;
				Double minDistance = Double.MAX_VALUE;
				for(int k = 0; k < means.size(); k++){ // recalculate correct index to place current data instance based on featureSet
					if(euclidDistance(means.get(k), calcMean(clusters.get(i).get(j), indexes)) < minDistance){
						index = k;
						minDistance = euclidDistance(means.get(k), calcMean(clusters.get(i).get(j), indexes));
					}
				}
				betterClusters.get(index).add(clusters.get(i).get(j)); // place current dataInstance in betterCluster correct cluster.
			}
		}
		setClusterMeans(); // set the means of each cluster and store them in newMeans.
	}
	
	/**************************************************************************
	 Identical method to the KMeans version.
	**************************************************************************/
	
	public double testAlgo(String filePath,ArrayList<Double[]> calcMeans, HashSet<Integer> indexes) throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(filePath));
		double fileSize = 0;
		double correct = 0;
		while(fileScanner.hasNextLine()){
			fileSize++;
			String[] line = fileScanner.nextLine().split(" ");
			if(line.length == 0){
				break;
			}
			Double[] dataInstance = convertData(line);
			double minDistance = Double.MAX_VALUE;
			int index = 0;
			for(int i = 0; i < calcMeans.size(); i++){
				double distance = euclidDistance(dataInstance,calcMeans.get(i), indexes);
				if(minDistance > distance){
					minDistance = distance;
					index = i;
				}
			}
			if(filePath.equalsIgnoreCase("irisTest.txt")){
				if((index == 0 && line[line.length-1].equalsIgnoreCase("Iris-setosa")) || (index == 1 && line[line.length-1].equalsIgnoreCase("Iris-versicolor") ||(index == 2 && line[line.length-1].equalsIgnoreCase("Iris-virginica")))){
						correct++;
				}
			}else if(filePath.equalsIgnoreCase("glassTest.txt")){
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
				}else if(filePath.equalsIgnoreCase("spambaseTest.txt")){
					if((index == 0 && line[line.length-1].equals("1")) || (index == 1 && line[line.length-1].equals("0"))){
						correct++;
					}
				}
			}
		fileScanner.close();
		return (correct/fileSize)*100;
	}

	/**************************************************************************
	 Identical to the KMeans version.
	**************************************************************************/
	
	public void setClusterMeans(){
		newMeans.removeAll(newMeans);
		for(int i = 0; i < betterClusters.size(); i++){
			Double[] meanValues = new Double[dataLength];
			for(int j = 0; j < dataLength; j++){
				double meanVal = 0;
				for(int f = 0; f < betterClusters.get(i).size(); f++){
					meanVal+= betterClusters.get(i).get(f)[j];
				}
				meanVal /= betterClusters.get(i).size();
				meanValues[j] = meanVal;
			}
			newMeans.add(meanValues);
		}
	}
	
	/**************************************************************************
	 calculates centroid of each cluster.
	**************************************************************************/
	
	public ArrayList<Double> calculateCentroids(HashSet<Integer> indexes){
		int i = 0;
		ArrayList<Double> centroids = new ArrayList<Double>();
		while(i < clusters.size()){
			centroids.add(calculateCentroid(clusters.get(i), indexes));
			i++;
		}
		return centroids;
	}
	
	/**************************************************************************
	 Calculate euclidean distance of two scalar quantities
	**************************************************************************/
	
	public Double euclidDistance(Double x, Double m){
		Double val = 0.0;
		val += Math.pow((x-m), 2);
		return Math.sqrt(val);
	}
	
	/**************************************************************************
	 Calculate euclidean distance of two vectors given a featureSet (indexes)
	**************************************************************************/
	
	public Double euclidDistance(Double[] x, Double[] m, HashSet<Integer> indexes){
		Double val = 0.0; // final value to be returned.
		if(x.length != m.length){
			return -1.0;
		}else{
			for(int vals : indexes){
				val += Math.pow((x[vals]-m[vals]), 2); // vals are the indexes to be used for calculating the distance.
			}
			return Math.sqrt(val);
		}
	}

	/**************************************************************************
	Calculate centroid of a cluster.
	**************************************************************************/
	
	public double calculateCentroid(ArrayList<Double[]> array, HashSet<Integer> indexes){
		double val = 0.0;
		for(int i = 0; i < array.size(); i++){
			val += calcMean(array.get(i), indexes); // calculates the mean of a double array.
		}
		return val/array.size(); // returns centroid of cluster.
	}

	/**************************************************************************
	 Calculate mean of an array given a featureSet(indexes).
	**************************************************************************/
	
	public double calcMean(Double[] array, HashSet<Integer> indexes){
		double val = 0;
		for(int vals : indexes){
			val+=array[vals];
		}
		return val/indexes.size();
	}
	
	/**************************************************************************
	Indentical to KMeans version.
	**************************************************************************/
	
	public Double[] convertData(String[] array){
		Double[] vals = new Double[array.length-1];
		for(int i = 0; i < vals.length; i++){
			vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}
	
	/**************************************************************************
	 Prints data instance and associated mean of the dataInstance.
	 Prints clusterMean at end.
	**************************************************************************/
	
	public void printClusters1(HashSet<Integer> indexes){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			for(int j = 0; j < clusters.get(i).size(); j++){
				for(int f = 0; f < clusters.get(i).get(j).length; f++){
					System.out.print(clusters.get(i).get(j)[f]+ " ");
				}
				System.out.print(calcMean(clusters.get(i).get(j), indexes)); // print data Instance mean.
				System.out.println();
			}
			System.out.println(calculateCentroid(clusters.get(i), indexes)); // print cluster mean.
		}
	}

	/**************************************************************************
	 Print a particular cluster.
	**************************************************************************/
		
	public void printClusters(ArrayList<ArrayList<Double[]>> clusters, HashSet<Integer> indexes){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			for(int j = 0; j < clusters.get(i).size(); j++){
				for(int f = 0; f < clusters.get(i).get(j).length; f++){
					System.out.print(clusters.get(i).get(j)[f]+ " ");
				}
				System.out.print(calcMean(clusters.get(i).get(j), indexes));
				System.out.println();
			}
		}
	}

	/**************************************************************************
	 Get means of each centroid.
	**************************************************************************/
	
	public ArrayList<Double> getMeans(HashSet<Integer> indexes){
		ArrayList<Double> means = new ArrayList<Double>();
		for(int i = 0; i < clusters.size(); i++){
			means.add(calculateCentroid(clusters.get(i), indexes));
		}
		return means;
	}
	
	/**************************************************************************
	 Reset clusters.
	**************************************************************************/
	
	public void pruneClusters(){
		clusters.removeAll(clusters);
	}
	
	/**************************************************************************
	 Identical to KMeans version.
	**************************************************************************/
	
	public void pruneClusters1(){
		for(int i = 0; i < betterClusters.size(); i++){
			for(int j = 0; j < betterClusters.get(i).size(); j++){
				betterClusters.get(i).remove(betterClusters.get(i).get(j));
			}
		}
	}
	
}
