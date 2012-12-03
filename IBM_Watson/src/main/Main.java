package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

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
	
	
	public static void main(String[] args) throws Exception 
	{
//		DataLoader.printTrainingTestingSets();
//		DataLoader.createWekaDataFile();
		
//		System.out.println(DataLoader.loadData());
		

		
//		DataLoader.splitFile();
		
//		DataLoader.printFeatureSubset("15,87,98,128,129,186,204");
		
////		System.out.println(dataSource.isIncremental());
//		Instances	instances	= dataSource.getDataSet();
//	
//		SimpleKMeans kMeans = new SimpleKMeans();
		
//		clusterInstances();
		
//		buildClusterers(12);
//		clusterInstances(12);
//		separateDataToClusters();
		
		DataLoader.splitClusterFile(3);
		
		
//		Classifier classifier2 = Classifiers.buildNonUpdateableClassifier(2);
//		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("7clusters\\classifier2"));
//		oos.writeObject(classifier2);
//		oos.close();
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
	
	public static void separateDataToClusters() throws Exception
	{
		LinkedHashMap<Integer, Integer> clusters = new LinkedHashMap<Integer, Integer>();
		BufferedReader in = new BufferedReader(new FileReader("4_clusters.csv"));
		
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
		
		PrintWriter cluster1 = new PrintWriter("cluster1.csv");
		PrintWriter cluster2 = new PrintWriter("cluster2.csv");
		PrintWriter cluster3 = new PrintWriter("cluster3.csv");
		PrintWriter cluster4 = new PrintWriter("cluster4.csv");
		PrintWriter cluster5 = new PrintWriter("cluster5.csv");
		PrintWriter cluster6 = new PrintWriter("cluster6.csv");
		PrintWriter cluster7 = new PrintWriter("cluster7.csv");
		
		cluster1.println(firstLine);
		cluster2.println(firstLine);
		cluster3.println(firstLine);
		cluster4.println(firstLine);
		cluster5.println(firstLine);
		cluster6.println(firstLine);
		cluster7.println(firstLine);
		
		while((curLine = in.readLine()) != null)
		{
			Integer qID = new Integer(new Double(curLine.split(",")[0]).intValue());
			int cluster = clusters.get(qID);
			switch(cluster)
			{
			case 1:
				cluster1.println(curLine);
				break;
			case 2:
				cluster2.println(curLine);
				break;
			case 3:
				cluster3.println(curLine);
				break;
			case 4:
				cluster4.println(curLine);
				break;
			case 5:
				cluster5.println(curLine);
				break;
			case 6:
				cluster6.println(curLine);
				break;
			case 7:
				cluster7.println(curLine);
				break;
			default:
				System.out.println("unknown cluster: " + cluster);
				System.out.println(curLine);
				break;
			}
		}
		
		cluster1.close();
		cluster2.close();
		cluster3.close();
		cluster4.close();
		cluster5.close();
		cluster6.close();
		cluster7.close();
	}
}
