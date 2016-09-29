import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**************************************************************************
One time use class to process data into usable format.
**************************************************************************/

public class DataHandler {
	
	/**************************************************************************
	 Helper attributes.
	**************************************************************************/

	String[] unProcessedFiles = {"glass.data.txt", "iris.data.txt", "spambase.data.txt"};
	String[] dataSets = {"glass","iris","spambase"};
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	ArrayList<String[]> testData = new ArrayList<String[]>();
	ArrayList<String[]> trainingData = new ArrayList<String[]>();
	HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	String dataSetName = "";
	
	/**************************************************************************
	 Splits data into training and test sets and writes them to separate files.
	**************************************************************************/
	
	public void processData() throws IOException {
		try {
			for(int i = 0; i < dataSets.length; i++){
				splitData(unProcessedFiles[i]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 Constructor
	**************************************************************************/
	
	public DataHandler(String name){
		this.dataSetName = name;
	}
	
	/**************************************************************************
	 Splits data into training and test sets and writes them to separate files.
	**************************************************************************/
	
	public void splitData(String filePath) throws IOException{
		File theFile = new File(filePath);
		Scanner fileScanner = new Scanner(theFile); // fileScanner.
		int fileIterator = 0;
		while(fileScanner.hasNextLine()){
			fileAsArray.add(fileScanner.nextLine().split(",")); // add dataInstance to ArrayList.
		}
		
		fileAsArray.trimToSize(); //trim file size
		setClassCounts(fileAsArray); // count unique classes.
		 
		while(fileIterator < fileAsArray.size()){
			int testDataIterator = 0;//iterator for testSet.
			String currentClass = fileAsArray.get(fileIterator)[fileAsArray.get(fileIterator).length-1];
			while(fileIterator < fileAsArray.size() && currentClass.equals(fileAsArray.get(fileIterator)[fileAsArray.get(fileIterator).length-1])){
				if(testDataIterator < classCounts.get(currentClass)/3){ // test set is only 1/3 of data.
					testData.add(fileAsArray.get(fileIterator));
					testDataIterator++;
					fileIterator++;
				}else{//training set is 2/3 data.
					trainingData.add(fileAsArray.get(fileIterator));
					fileIterator++;
				}
			}
		}
		writeFiles(); // write data to files
		fileScanner.close();
	}
	
	/**************************************************************************
	 Counts classes (last element in the arrays).
	**************************************************************************/
	
	public void setClassCounts(ArrayList<String[]> file){
		for(int i = 0; i < file.size(); i++){
			if(classCounts.containsKey(file.get(i)[file.get(i).length-1])){
				int currentVal = classCounts.get(file.get(i)[file.get(i).length-1]);
				currentVal++;
				classCounts.put(file.get(i)[file.get(i).length-1], currentVal);
			}else{
				classCounts.put(file.get(i)[file.get(i).length-1], 1);
			}
		}
	}
	
	/**************************************************************************
	 Prints string array.
	**************************************************************************/
	
	public void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	Writes testSet and trainSet to different files.
	**************************************************************************/
	
	public void writeFiles() throws IOException{
		File testFile = new File(this.dataSetName+"Test.txt");
		FileWriter testWriter = new FileWriter(testFile);
		File trainFile = new File(this.dataSetName+"Train.txt");
		FileWriter trainWriter = new FileWriter(trainFile);
		if(dataSetName.equalsIgnoreCase("glass")){
			for(int i = 0; i < testData.size(); i++){
				for(int j = 1; j < testData.get(i).length; j++){
					testWriter.write(testData.get(i)[j] + " ");
				}
				testWriter.write(System.getProperty("line.separator"));
			}
			
			testWriter.close();
			
			for(int i = 0; i < trainingData.size(); i++){
				for(int j = 1; j < trainingData.get(i).length; j++){
					trainWriter.write(trainingData.get(i)[j] + " ");
				}
				trainWriter.write(System.getProperty("line.separator"));
			}
			trainWriter.close();
			return;
		}
		for(int i = 0; i < testData.size(); i++){
			for(int j = 0; j < testData.get(i).length; j++){
				testWriter.write(testData.get(i)[j] + " ");
			}
			testWriter.write(System.getProperty("line.separator"));
		}
		
		testWriter.close();
		
		for(int i = 0; i < trainingData.size(); i++){
			for(int j = 0; j < trainingData.get(i).length; j++){
				trainWriter.write(trainingData.get(i)[j] + " ");
			}
			trainWriter.write(System.getProperty("line.separator"));
		}
		trainWriter.close();
	}
	
}
