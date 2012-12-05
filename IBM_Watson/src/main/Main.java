package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import classifiers.Classifiers;

import weka.classifiers.Classifier;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;
import data.DataLoader;

public class Main 
{
	public static final double THRESHOLD = 0.9700817495583556;
	
	public static void main(String[] args) throws Exception 
	{		
		
//		DataLoader.printTrainingTestingSets();
//		DataLoader.createWekaDataFile();

//		DataLoader.splitFile();
		
//		DataLoader.printTrainingFeatureSubset("15,87,98,128,129,186,204");
		
//		splitTrainingToClassifierSubsets();
		
//		int numClusters = 12;
//		buildClusterers(numClusters);
//		clusterInstances(numClusters);
//		separateDataToClusters(numClusters);
		
//		DataLoader.generateClusterSubset(3);
		
//		DataLoader.splitClusterFile(1,5);
//		DataLoader.splitClusterFile(2,5);
//		DataLoader.splitClusterFile(3,5);		
//		
//		Classifier classifier3 = Classifiers.buildNonUpdateableClassifier(3);
//		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("6clusters\\classifier3"));
//		oos.writeObject(classifier3);
//		oos.close();
//		System.gc();
//		
//		Classifier classifier4 = Classifiers.buildNonUpdateableClassifier(4);
//		oos = new ObjectOutputStream(new FileOutputStream("6clusters\\classifier4"));
//		oos.writeObject(classifier4);
//		oos.close();
//		System.gc();
//		
//		Classifier classifier0 = Classifiers.buildNonUpdateableClassifier(0);
//		oos = new ObjectOutputStream(new FileOutputStream("6clusters\\classifier0"));
//		oos.writeObject(classifier0);
//		oos.close();
//		System.gc();
//		
//		
//		Classifier classifier6 = Classifiers.buildNonUpdateableClassifier(6);
//		oos = new ObjectOutputStream(new FileOutputStream("7clusters\\classifier6"));
//		oos.writeObject(classifier6);
//		oos.close();
//		
//		Classifier classifier5 = Classifiers.buildNonUpdateableClassifier(5);
//		oos = new ObjectOutputStream(new FileOutputStream("7clusters\\classifier5"));
//		oos.writeObject(classifier5);
//		oos.close();
//		System.gc();
		
//		DataSource data = new DataSource("unlabelled\\unlabelledClusterSubset.csv");
//		clusterUnlabelled(data);
		
//		printTrainingFinalValues();
		
//		getThreshold();
		
//		splitFileToClassifierSubsets("unlabelled\\wekaUnlabelled.csv", "unlabelled\\wekaUnlabelled_classifier1_subset.csv", "unlabelled\\wekaUnlabelled_classifier2_subset.csv", "unlabelled\\wekaUnlabelled_classifier3_subset.csv");

//		DataLoader.splitUnlabelled();
		
		test("unlabelled\\unlabelled_1.csv", "unlabelled\\unlabelledClusterSubset.csv", 
				"unlabelled\\wekaUnlabelled_classifier1_subset.csv", "unlabelled\\wekaUnlabelled_classifier2_subset.csv", 
				"unlabelled\\wekaUnlabelled_classifier3_subset.csv", "unlabelled\\unlabelled_labelled_1.csv");
		
		test("unlabelled\\unlabelled_2.csv", "unlabelled\\unlabelledClusterSubset.csv", 
				"unlabelled\\wekaUnlabelled_classifier1_subset.csv", "unlabelled\\wekaUnlabelled_classifier2_subset.csv", 
				"unlabelled\\wekaUnlabelled_classifier3_subset.csv", "unlabelled\\unlabelled_labelled_2.csv");
	}

	
	public static EM buildClusterers(int n) throws Exception
	{
		DataSource	clusteringSrc	= new DataSource(DataLoader.clusterSubsetPath);
	
		
		String[] options = new String[2];
		
		options[0] = "-R";
		options[1] = "1";
		Remove removeQID = new Remove();
		removeQID.setOptions(options);
		removeQID.setInputFormat(clusteringSrc.getDataSet(0));
		
		options[0] ="-N";
		options[1] = String.valueOf(n);		
		
		EM em = new EM();
		em.setOptions(options);
		em.buildClusterer(Filter.useFilter(clusteringSrc.getDataSet(), removeQID));
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(n+"_clusterer"));
		oos.writeObject(em);
		oos.close();
		
		return em;
		
//		em = new EM();
//		options[1] = "10";
//		em.setOptions(options);
//		em.buildClusterer(Filter.useFilter(clusteringSrc.getDataSet(), removeQID));
//		
//		oos = new ObjectOutputStream(new FileOutputStream("10_clusterer"));
//		oos.writeObject(em);
//		oos.close();
	}
	
	public static void clusterInstances(int n) throws Exception
	{
		DataSource data = new DataSource(DataLoader.clusterSubsetPath);
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(n+"_clusterer"));
		EM clusterer = (EM) ois.readObject();
		ois.close();
		
		Instances instances	= data.getDataSet();
		
		//Map<qID, Map<cluster, count>>
		LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> clusters = new LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>>();
		
		String[] options = new String[2];		
		options[0] = "-R";
		options[1] = "1";
		Remove removeQID = new Remove();
		removeQID.setOptions(options);
		removeQID.setInputFormat(data.getDataSet(0));
				
		Iterator<Instance> instanceIt = Filter.useFilter(instances, removeQID).iterator();
		int i = 0;
		while(instanceIt.hasNext())
		{
			Instance curInstance = instanceIt.next();
			int cluster = clusterer.clusterInstance(curInstance);
			int qID = (int)instances.get(i).value(0);
//			System.out.println(qID + ":" + cluster);
			
			if(!clusters.containsKey(qID))
				clusters.put(qID, new LinkedHashMap<Integer, Integer>());
			if(!clusters.get(qID).containsKey(cluster))
				clusters.get(qID).put(cluster, 1);
			else
			{
				int curVal = clusters.get(qID).get(cluster) + 1;
				clusters.get(qID).put(cluster, curVal);
			}
			
			i++;
		}
		
		
		//print out clusters
		PrintWriter out = new PrintWriter(n+"_clusters.csv");
		for(Integer qID : clusters.keySet())
		{
			String line = qID + ",";
			
			//now find the best cluster
			int maxCount = 0;
			int bestCluster = 0;
			for(Integer cluster : clusters.get(qID).keySet())
			{
				int curCount = clusters.get(qID).get(cluster);
				if(curCount > maxCount)
				{
					maxCount = curCount;
					bestCluster = cluster;
				}
			}
			
			line += bestCluster;
			out.println(line);
		}
		out.close();
	}
	
	public static void separateDataToClusters(int numClusters) throws Exception
	{
		LinkedHashMap<Integer, Integer> clusters = new LinkedHashMap<Integer, Integer>();
		BufferedReader in = new BufferedReader(new FileReader(numClusters+"_clusters.csv"));
		
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[] nums = curLine.split(",");
			Integer qID = new Integer(nums[0]);
			Integer cluster = new Integer(nums[1]);
			clusters.put(qID, cluster);
		}
		in.close();
		
		in = new BufferedReader(new FileReader("wekaLabelled.csv"));
		String firstLine = in.readLine();
		
		ArrayList<PrintWriter> files = new ArrayList<PrintWriter>();
		for(int i = 0; i < numClusters; i++)
			files.add(new PrintWriter("cluster" + i + ".csv"));
		
		for(PrintWriter pw : files)
			pw.println(firstLine);
		
		while((curLine = in.readLine()) != null)
		{
			Integer qID = new Integer(new Double(curLine.split(",")[0]).intValue());
			int cluster = clusters.get(qID);
			if(cluster >= files.size())
			{	
				System.out.println("unknown cluster: " + cluster);
				System.out.println(curLine);
				continue;
			}
			
			files.get(cluster).println(curLine);
		}
		
		for(PrintWriter pw : files)
			pw.close();
	}
	
	public static Map<Integer, Double[]> clusterUnlabelled(DataSource data) throws Exception
	{
		Map<Integer, Double[]>	distributions	= new LinkedHashMap<Integer, Double[]>();
		Map<Integer, Integer>	counts			= new LinkedHashMap<Integer, Integer>();
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("6_clusterer"));
		EM clusterer = (EM) ois.readObject();
		ois.close();
		
		Instances instances	= data.getDataSet();
		
		String[] options = new String[2];		
		options[0] = "-R";
		options[1] = "1";
		Remove removeQID = new Remove();
		removeQID.setOptions(options);
		removeQID.setInputFormat(data.getDataSet(0));
		
		Iterator<Instance> instanceIt = Filter.useFilter(instances, removeQID).iterator();
		int i = 0;
		while(instanceIt.hasNext())
		{
			Instance curInst = instanceIt.next();			
			double[] distribution = clusterer.distributionForInstance(curInst);
			int qID = (int)instances.get(i).value(0);
			
			//update the distributions
			if(!distributions.containsKey(qID))
			{
				Double[] curDist = new Double[distribution.length];
				for(int j = 0; j < distribution.length; j++)
					curDist[j] = new Double(distribution[j]);
				distributions.put(qID, curDist);
			}
			else
			{
				Double[] curDist = distributions.get(qID);
				for(int j = 0; j < distribution.length; j++)
					curDist[j] += distribution[j];
				distributions.put(qID, curDist);
			}
			
			//update the counts
			if(!counts.containsKey(qID))
				counts.put(qID, 1);
			else
			{
				Integer val = counts.get(qID) + 1;
				counts.put(qID, val);
			}
			
			i++;
		}
		
		//now normalize the distributions and print them out
		for(Integer qID : distributions.keySet())
		{
			double count = counts.get(qID).doubleValue();
			Double[] dist = distributions.get(qID);
			
			String println = qID + ": [";
			for(int j = 0; j < dist.length; j++)
			{
				dist[j] = dist[j] / count;
				println += j + ":" + dist[j] + ",";
			}
			println += "]";
//			System.out.println(println);
		}
		
		return distributions;
	}
	
	public static void splitTrainingToClassifierSubsets() throws IOException
	{
		//Classifier 1
		for(int i = 1; i <= 8; i++)
			DataLoader.printTrainingFeatureSubset("labelled_" + i + ".csv", "labelled_" + i + "_classifier1_subset.csv", DataLoader.cluster1Subset);

		//Classifier 2
		for(int i = 1; i <= 8; i++)
			DataLoader.printTrainingFeatureSubset("labelled_" + i + ".csv", "labelled_" + i + "_classifier2_subset.csv", DataLoader.cluster2Subset);
		
		//Classifier 3
		for(int i = 1; i <= 8; i++)
			DataLoader.printTrainingFeatureSubset("labelled_" + i + ".csv", "labelled_" + i + "_classifier3_subset.csv", DataLoader.cluster3Subset);
	}
	
	public static void splitFileToClassifierSubsets(String filePath, String dest1, String dest2, String dest3) throws IOException
	{
		DataLoader.printTrainingFeatureSubset(filePath, dest1, DataLoader.cluster1Subset);
		DataLoader.printTrainingFeatureSubset(filePath, dest2, DataLoader.cluster2Subset);
		DataLoader.printTrainingFeatureSubset(filePath, dest3, DataLoader.cluster3Subset);
	}
	
	public static Classifier getClassifier(int n) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("6clusters\\classifier" + n));
		Classifier classifier = (Classifier)ois.readObject();
		ois.close();
		return classifier;
	}
	
	public static void test(String filePath, String clusterSubset, String class1Subset, String class2Subset, String class3Subset, String dest) throws Exception
	{
		DataSource data = new DataSource(clusterSubset);
		Map<Integer, Double[]> clusterDistributions = clusterUnlabelled(data);
		data = null;
		System.gc();
		
		data = new DataSource(class1Subset);
		Classifier classifier = getClassifier(1);
		ArrayList<Double[]> classifier1Dists = Classifiers.getDistributions(data, classifier, true);
		data = null;
		classifier = null;
		System.gc();
		
		data = new DataSource(class2Subset);
		classifier = getClassifier(2);
		ArrayList<Double[]> classifier2Dists = Classifiers.getDistributions(data, classifier, true);
		data = null;
		classifier = null;
		System.gc();
		
		data = new DataSource(class3Subset);
		classifier = getClassifier(3);
		ArrayList<Double[]> classifier3Dists = Classifiers.getDistributions(data, classifier, true);
		data = null;
		classifier = null;
		System.gc();
		
		data = new DataSource(filePath);
		
		classifier = getClassifier(0);
		ArrayList<Double[]> classifier0Dists = Classifiers.getDistributions(data, classifier, true);
		classifier = null;
		System.gc();
		
		classifier = getClassifier(4);
		ArrayList<Double[]> classifier4Dists = Classifiers.getDistributions(data, classifier, true);
		classifier = null;
		System.gc();
		
		classifier = getClassifier(5);
		ArrayList<Double[]> classifier5Dists = Classifiers.getDistributions(data, classifier, true);
		classifier = null;
		System.gc();
		
		PrintWriter out = new PrintWriter(dest);
		Instances instances = data.getDataSet();
		Iterator<Instance> instanceIterator = instances.iterator();
		int j = 0;
		while(instanceIterator.hasNext())
		{
			Instance curInst = instanceIterator.next();
			Integer qID = (int)curInst.value(0);
			
			double value = 0.0;
			Double[] weightVec = clusterDistributions.get(qID);
			
			double weight = weightVec[0];
			Double[] dist = classifier0Dists.get(j);
			value += (weight * dist[1]);
			
			weight = weightVec[1];
			dist = classifier1Dists.get(j);
			value += (weight * dist[1]);
			
			weight = weightVec[2];
			dist = classifier2Dists.get(j);
			value += (weight * dist[1]);
			
			weight = weightVec[3];
			dist = classifier3Dists.get(j);
			value += (weight * dist[1]);
			
			weight = weightVec[4];
			dist = classifier4Dists.get(j);
			value += (weight * dist[1]);
			
			weight = weightVec[5];
			dist = classifier5Dists.get(j);
			value += (weight * dist[1]);
			
			if(value >= THRESHOLD)
				out.println(qID + ",true");
			else
				out.println(qID + ",false");
							
			j++;
		}
		out.close();
	}
	
	public static void printTrainingFinalValues() throws Exception
	{
		DataSource data = new DataSource(DataLoader.clusterSubsetPath);
		Map<Integer, Double[]> clusterDistributions = clusterUnlabelled(data);
		data = null;
		System.gc();
		
		for(int i = 2; i <= 8; i++)
		{			
			DataSource data1 = new DataSource("labelled_" + i + "_classifier1_subset.csv");
			Classifier classifier = getClassifier(1);
			ArrayList<Double[]> classifier1Dists = Classifiers.getDistributions(data1, classifier, true);
			data1 = null;
			classifier = null;
			System.gc();
			System.out.println("used classifier 1");
			
			DataSource data2 = new DataSource("labelled_" + i + "_classifier2_subset.csv");
			classifier = getClassifier(2);
			ArrayList<Double[]> classifier2Dists = Classifiers.getDistributions(data2, classifier, true);
			data2 = null;
			classifier = null;
			System.gc();
			System.out.println("used classifier 2");
			
			DataSource data3 = new DataSource("labelled_" + i + "_classifier3_subset.csv");
			classifier = getClassifier(3);
			ArrayList<Double[]> classifier3Dists = Classifiers.getDistributions(data3, classifier, true);
			data3 = null;
			classifier = null;
			System.gc();
			System.out.println("used classifier 3");
			
			DataSource dataFull = new DataSource("labelled_" + i + ".csv");
						
			classifier = getClassifier(0);
			ArrayList<Double[]> classifier0Dists = Classifiers.getDistributions(dataFull, classifier, false);
			classifier = null;
			System.gc();
			System.out.println("used classifier 0");
			
			classifier = getClassifier(4);
			ArrayList<Double[]> classifier4Dists = Classifiers.getDistributions(dataFull, classifier, false);
			classifier = null;
			System.gc();
			System.out.println("used classifier 4");
			
			classifier = getClassifier(5);
			ArrayList<Double[]> classifier5Dists = Classifiers.getDistributions(dataFull, classifier, false);
			classifier = null;
			System.gc();
			System.out.println("used classifier 5");
			
			PrintWriter out = new PrintWriter("labelled_" + i + "_results.txt");
			Instances instances = dataFull.getDataSet();
			Iterator<Instance> instanceIterator = instances.iterator();
			int j = 0;
			while(instanceIterator.hasNext())
			{
				Instance curInst = instanceIterator.next();
				Integer qID = (int)curInst.value(0);
				double truth = curInst.value(curInst.numAttributes()-1);
				
				double[] value = new double[2];
				value[0] = 0.0;
				value[1] = 0.0;
				Double[] weightVec = clusterDistributions.get(qID);
				
				double weight = weightVec[0];
				Double[] dist = classifier0Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				weight = weightVec[1];
				dist = classifier1Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				weight = weightVec[2];
				dist = classifier2Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				weight = weightVec[3];
				dist = classifier3Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				weight = weightVec[4];
				dist = classifier4Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				weight = weightVec[5];
				dist = classifier5Dists.get(j);
				value[0] += (weight * dist[0]);
				value[1] += (weight * dist[1]);
				
				out.println(qID + "," + truth + "," + value[0] + "," + value[1]);
								
				j++;
			}
			
			
			out.close();
						 
		}
		
	}
	

	public static double getThreshold() throws Exception
	{
		TreeSet<Double> negatives = new TreeSet<Double>();
		TreeSet<Double> positives = new TreeSet<Double>();
		
		for(int i = 1; i <= 8; i++)
		{
			BufferedReader in = new BufferedReader(new FileReader("labelled_" + i + "_results.txt"));
			
			String curLine;
			while((curLine = in.readLine()) != null)
			{
				String[] lineArr = curLine.split(",");
				Double val = new Double(lineArr[3].trim());
				if(lineArr[1].trim().equals("1.0"))
					positives.add(val);
				else if(lineArr[1].trim().equals("0.0"))
					negatives.add(val);
				else
					System.out.println("Unknown class: " + lineArr[1] + " val=" + val);
			}
			in.close();
		}
		
//		System.out.println("max negatives:" + negatives.last());
//		System.out.println("min positives:" + positives.first());
//		System.out.println("max positives:" + positives.last());
		
		Iterator<Double> posItr = positives.iterator();
		Iterator<Double> negItr = negatives.descendingIterator();
		
		double threshold = 0.0;
		double curPosVal;
		double curNegVal;
		while((curPosVal = posItr.next()) <= (curNegVal = negItr.next()))
		{
			threshold = (curPosVal + curNegVal) / 2.0;
			System.out.println("cur threshold:" + threshold);
		}
		
		System.out.println("final threshold:" + threshold);
		return threshold;
	}
}
