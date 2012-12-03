package classifiers;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Classifiers 
{
	public static Classifier buildClassifier(int cluster)
	{
		return null;
	}

	
	public static Classifier buildUpdateableClassifier()
	{
		return null;
	}
	
	public static Classifier buildNonUpdateableClassifier(int cluster) throws Exception
	{
		DataSource clusterSrc = new DataSource("7clusters\\cluster" + cluster + ".csv");
		
		//figure out cost to fix class imbalance
		int classIndex = clusterSrc.getDataSet().numAttributes()-1;
		int[] counts = clusterSrc.getDataSet().attributeStats(classIndex).nominalCounts;
		
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
}
