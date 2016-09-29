import java.util.ArrayList;

/**************************************************************************
This Features class is used as a helper class for GAS. We have attributes
for the fitness of the featureSet as well as the probability. Additionally,
we hold a featureSet. The featureSet holds array locations for each feature.
**************************************************************************/

public class Features implements Comparable<Features>{
	
	ArrayList<Integer> featureSet = new ArrayList<Integer>();
	double fitness;
	double probability;
	int flag = 0;
	
	/**************************************************************************
	**************************************************************************/
	
	@Override
	public int compareTo(Features feature) {
		return Double.compare(this.fitness, feature.fitness);
	}
}
