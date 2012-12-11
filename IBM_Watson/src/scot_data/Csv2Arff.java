/**
 * 
 */
package scot_data;

/**
 * @author sfang
 * Randomize the rows in a CSV file and 
 * convert it to ARFF format.
 */

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

import java.util.Random;
 

public class Csv2Arff {

	public File csv_src;
	public File arff_dest;
	public	int pct_test;

	public Csv2Arff(File csv_src, File arff_dest) throws IOException{
		this.csv_src = csv_src;
		this.arff_dest = arff_dest;		
		
		System.out.printf("Loading %s", csv_src);
		CSVLoader loader = new CSVLoader();
	    loader.setSource(csv_src);
	    Instances data = loader.getDataSet();
	    
	    data.randomize(new Random());
	    	    
	    System.out.printf("Saving to %s", arff_dest);
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(arff_dest);
	    saver.setDestination(arff_dest);
	    saver.writeBatch();
	}
	
		
	public static void main(String[] args) throws IOException {
		String csv_f = "../data/num_data.csv";
		String arff_f = "../data/num_data.arff";
		
		File csv_src = new File(csv_f);
		File arff_dest = new File(arff_f);
		
		assert(csv_src.exists());
		
		new Csv2Arff(csv_src, arff_dest);
	}

}
