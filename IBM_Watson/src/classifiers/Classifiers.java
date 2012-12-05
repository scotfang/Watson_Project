package classifiers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

public class Classifiers 
{
//	public static Classifier buildClassifier(int cluster)
//	{
//		return null;
//	}
//
//	
//	public static Classifier buildUpdateableClassifier()
//	{
//		return null;
//	}
	
	public static Classifier buildNonUpdateableClassifier(int cluster) throws Exception
	{
		DataSource clusterSrc = new DataSource("6clusters\\cluster" + cluster + "_attr_subset.csv");
		
		//figure out cost to fix class imbalance
		int classIndex = clusterSrc.getDataSet().numAttributes()-1;
		int[] counts = clusterSrc.getDataSet().attributeStats(classIndex).nominalCounts;
		System.out.println(counts[0]);
		System.out.println(counts[1]);
		
		int max = 0;
		int min = Integer.MAX_VALUE;
		for(int i = 0; i < counts.length; i++)
		{
			if(counts[i] > max)
				max = counts[i];
			if(counts[i] < min)
				min = counts[i];
		}
		
		CostSensitiveClassifier csc = getCostSensitiveClassifier(((double)max) / ((double)min));
		
		//load instances w/ class index set
		Instances instances = clusterSrc.getDataSet(classIndex);
//		instances.attribute(classIndex).
		
		//remove qID from instances 
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "1";
		Remove remove = new Remove();
		remove.setOptions(options);
		remove.setInputFormat(instances);
		
		//build it and return
		csc.buildClassifier(Filter.useFilter(instances, remove));
		
		return csc;
	}
	
	private static CostSensitiveClassifier getCostSensitiveClassifier(double imbalanceRatio) throws Exception
	{
		String[] options = new String[2];
		
		CostSensitiveClassifier csc = new CostSensitiveClassifier();
		options[0] = "-cost-matrix";
		options[1] = "\"[0.0 1.0; " + imbalanceRatio + " 0.0]\"";
		
		csc.setOptions(options);
		
		SimpleLogistic logistic = new SimpleLogistic();
		//TODO any options?
		
		csc.setClassifier(logistic);
		
		return csc;
	}
	
//	public static void classifyInstances(DataSource data, Map<Integer, Double[]> distributions) throws Exception
//	{
//		ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
//		for(int i = 0; i < 6; i++)
//		{
//			if(i == 1 || i == 2 || i == 3)
//				continue;
//			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("6clusters\\classifier" + i));
//			Classifier curClassifier = (Classifier)ois.readObject();
//			ois.close();
//			classifiers.add(curClassifier);
//		}
//		
//		//TODO
//	}
//	
	public static ArrayList<Double[]> getDistributions(DataSource data, Classifier classifier, boolean addLabel) throws Exception
	{
		ArrayList<Double[]> distributions = new ArrayList<Double[]>();
		
		Instances instances = data.getDataSet();
		
		
		//remove qID from instances 
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "1";
		Remove remove = new Remove();
		remove.setOptions(options);
		remove.setInputFormat(instances);
		
		Instances filteredInstances = Filter.useFilter(instances, remove);
		
		if(addLabel)
		{
			options = new String[6];
			options[0] = "-T";
			options[1] = "NOM";
			options[2] = "-N";
			options[3] = "label";
			options[4] = "-L";
			options[5] = "false,true";
			
			Add add = new Add();
			add.setOptions(options);
			add.setInputFormat(filteredInstances);
			filteredInstances = Filter.useFilter(filteredInstances, add);
		}
		
		
		Iterator<Instance> instanceIt = filteredInstances.iterator();
		while(instanceIt.hasNext())
		{
			double[] curDist = classifier.distributionForInstance(instanceIt.next());
			Double[] curDistObj = new Double[curDist.length];
			for(int i = 0; i < curDist.length; i++)
				curDistObj[i] = curDist[i];
			distributions.add(curDistObj);
		}
		
		return distributions;
	}
}
