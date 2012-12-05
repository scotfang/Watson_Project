package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

public class DataLoader 
{
	public static final String trainingFilePath		= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled.csv";
	public static final String trainingFilePath1	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_1.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_1.csv";
	public static final String trainingFilePath2	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_2.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_2.csv";
	public static final String trainingFilePath3	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_3.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_3.csv";
	public static final String trainingFilePath4	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_4.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_4.csv";
	public static final String trainingFilePath5	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_5.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_5.csv";
	public static final String trainingFilePath6	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_6.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_6.csv";
	public static final String trainingFilePath7	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_7.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_7.csv";
	public static final String trainingFilePath8	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\labelled_8.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\labelled_8.csv";
	public static final String wekaTrainingFilePath	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\wekaLabelled.csv";
	public static final String numCandidatesPath	= "C:\\Users\\Alex\\Dropbox\\Docs\\CSM276A\\numCandidates.txt";
	public static final String clusterSubsetPath	= "C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\clusterSubset.csv";//"C:\\Users\\Alex\\Documents\\school\\CSM276A\\clusterSubset.csv";
	
	//NOTE these are attribute numbers, not indices (i.e. index+1)
	public static final String cluster1Subset		= "13,17,47,50,54,61,62,67,90,94,98,99,238,242,248,249,268,271,273,274,276,280,293,295,300,304,312,316,333,335,16,18,65,72,88,288,294,317,21,91,8,64,109,114,244,281,239,247,287,291";
	public static final String cluster2Subset		= "22,23,67,74,271,274,276,286,288,293,294,300,306,316,45,287,319,342,64,253,328,21,42,50,61,93,291";
	public static final String cluster3Subset		= "8,13,16,17,47,50,54,68,88,100,109,271,274,280,287,293,294,300,316,317,340,67,74,238,333,341,56,62,242,247,260,273,288,306,6,18,45,98,107,250,276,290";
	
	public static Map<Long, Integer> loadData() throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(trainingFilePath));
		
//		Map<Long, List<CandidateAnswer>> candidateAnswers = new LinkedHashMap<Long, List<CandidateAnswer>>(); 
		
		Map<Long, Integer> numCandidates = new LinkedHashMap<Long, Integer>();
		
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[]		lineArr		= curLine.split(",");
			Long			qID			= (new Double(lineArr[0])).longValue();
//			Boolean			isCorrect	= new Boolean(lineArr[lineArr.length-1]);
//			Vector<Double>	features	= new Vector<Double>();
			
//			for(int i = 1; i < lineArr.length-1; i++)
//				features.add(new Double(lineArr[i]));
//			
//			CandidateAnswer ca = new CandidateAnswer(qID, features, isCorrect);
			
			if(numCandidates.containsKey(qID))
			{
				Integer i = numCandidates.get(qID);
				numCandidates.put(qID, new Integer(i.intValue()+1));
			}
			else
			{
				numCandidates.put(qID, new Integer(1));
			}
		}
		in.close();
		
		return numCandidates;
	}
	
	public static void splitTrainingFile() throws IOException
	{
		BufferedReader	in 		= new BufferedReader(new FileReader(trainingFilePath));
		PrintWriter		out1	= new PrintWriter(trainingFilePath1);
		PrintWriter		out2	= new PrintWriter(trainingFilePath2);
		PrintWriter		out3	= new PrintWriter(trainingFilePath3);
		PrintWriter		out4	= new PrintWriter(trainingFilePath4);
		PrintWriter		out5	= new PrintWriter(trainingFilePath5);
		PrintWriter		out6	= new PrintWriter(trainingFilePath6);
		PrintWriter		out7	= new PrintWriter(trainingFilePath7);
		PrintWriter		out8	= new PrintWriter(trainingFilePath8);
		
		String curLine = in.readLine();
		String[] attributes = curLine.split(",");
		
		String firstLine = "qId";
		for(int i = 1; i <attributes.length-1; i++)
			firstLine += ",attr" + i;
		firstLine += ",label";
		out1.println(firstLine);
		out2.println(firstLine);
		out3.println(firstLine);
		out4.println(firstLine);
		out5.println(firstLine);
		out6.println(firstLine);
		out7.println(firstLine);
		out8.println(firstLine);
				
		out1.println(curLine);
		int i = 1;
		
		while((curLine = in.readLine()) != null)
		{
			switch(i%8)
			{
			case 0:
				out1.println(curLine);
				break;
			case 1:
				out2.println(curLine);
				break;
			case 2:
				out3.println(curLine);
				break;
			case 3:
				out4.println(curLine);
				break;
			case 4:
				out5.println(curLine);
				break;
			case 5:
				out6.println(curLine);
				break;
			case 6:
				out7.println(curLine);
				break;
			case 7:
				out8.println(curLine);
				break;
			default:
//				out1.println(curLine);
				System.out.println("PROBLEM DEFAULT SWITCH REACHED");
				break;
			}
			
			i++;
		}
		in.close();
		out1.close();
		out2.close();
		out3.close();
		out4.close();
		out5.close();
		out6.close();
		out7.close();
		out8.close();
	}
	
	public static void splitClusterFile(int cluster, int n) throws IOException
	{
		BufferedReader			in 		= new BufferedReader(new FileReader("6clusters\\cluster" + cluster + ".csv"));
		ArrayList<PrintWriter>	files	= new ArrayList<PrintWriter>();
		for(int i = 0; i < n; i++)
			files.add(new PrintWriter("6clusters\\cluster" + cluster + "_" + i + ".csv"));
		
		String firstLine = in.readLine();
		
		for(PrintWriter pw : files)
			pw.println(firstLine);
				
		int i = 1;
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			PrintWriter pw = files.get(i%n);
			pw.println(curLine);			
			i++;
		}
		in.close();
		for(PrintWriter pw : files)
			pw.close();
	}
	
	public static Map<Long, Vector<String>> getQuestionLines() throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(trainingFilePath));
		
		Map<Long, Vector<String>> lines = new LinkedHashMap<Long, Vector<String>>();
		
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[]		lineArr		= curLine.split(",");
			Long			qID			= (new Double(lineArr[0])).longValue();
			
			if(lines.containsKey(qID))
			{
				lines.get(qID).add(curLine);
			}
			else
			{
				lines.put(qID, new Vector<String>());
				lines.get(qID).add(curLine);
			}
		}
		in.close();
		
		return lines;
	}
	
	public static Vector<String> getQuestionLines(Long qID, int numLines) throws NumberFormatException, IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(trainingFilePath));
		Vector<String> lines = new Vector<String>();
		
		int curNumLines = 0;
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[]		lineArr		= curLine.split(",");
			Long			curQID			= (new Double(lineArr[0])).longValue();
			
			if(qID.equals(curQID))
			{
				lines.add(curLine);
				curNumLines++;
			}
			if(curNumLines >= numLines)
				break;
		}
		in.close();
		return lines;
	}
	
	public static void printTrainingTestingSets() throws IOException
	{
//		BufferedReader in = new BufferedReader(new FileReader(numCandidatesPath));
		
		PrintWriter trainOut	= new PrintWriter("C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\trainingSet.csv");
		PrintWriter	testOut		= new PrintWriter("C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\testingSet.csv");
		
		Map<Long, Vector<String>> lines = getQuestionLines();
		double curDone = 0;
		double total = lines.keySet().size();
		for(Long qID : lines.keySet())
		{			
			Vector<String> curLines = lines.get(qID);
			for(int j = 0; j < curLines.size(); j++)
			{
				if(j%2 == 0)
					trainOut.println(curLines.get(j));
				else
					testOut.println(curLines.get(j));
			}
			lines.put(qID, null);
			
			curDone++;
			System.out.println(curDone/total);
		}
		
//		String curLine;
//		while((curLine = in.readLine()) != null)
//		{
//			String[] keyVals = curLine.split(",");
//			for(int i = 0; i < keyVals.length; i++)
//			{
//				System.out.println(((double)i)/((double)keyVals.length));
//				
//				String[] qIDNum = keyVals[i].trim().split("=");
//				
//				Vector<String> lines = getQuestionLines(new Long(qIDNum[0]), new Integer(qIDNum[1]));
//				for(int j = 0; j < lines.size(); j++)
//				{
//					if(j%2 == 0)
//						trainOut.println(lines.get(j));
//					else
//						testOut.println(lines.get(j));
//				}
//			}
//		}
//		in.close();
		trainOut.close();
		testOut.close();
	}
	
	public static void createWekaDataFile() throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader("unlabelled\\unlabelled.csv"));
		PrintWriter	out = new PrintWriter("C:\\Users\\Alex\\Documents\\School\\CSM276A\\IBM_Watson_project\\unlabelled\\wekaUnlabelled.csv");
		
		String curLine = in.readLine();
		String[] attributes = curLine.split(",");
		
		String firstLine = "qId";
		for(int i = 1; i <attributes.length-1; i++)
			firstLine += ",attr" + i;
		firstLine += ",label";
		out.println(firstLine);
		out.println(curLine);
		
		while((curLine = in.readLine()) != null)
			out.println(curLine);
		in.close();
		out.close();
	}
	
	public static void printTrainingFeatureSubset(String src, String dest, String subsetIndCSV) throws IOException
	{
		String[] indices = subsetIndCSV.split(",");
		
		BufferedReader in = new BufferedReader(new FileReader(src));
		PrintWriter out = new PrintWriter(dest);
		
//		String firstLine = "qId";
//		for(int i = 0; i < indices.length; i++)
//			firstLine += ",attr" + indices[i].trim();
//		firstLine += ",label";
//		out.println(firstLine);

		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[] lineArr = curLine.trim().split(",");
			String line2write = lineArr[0];
			for(int i = 0; i < indices.length; i++)
			{
				Integer ind = new Integer(indices[i].trim());
				line2write += "," + lineArr[ind.intValue()-1].trim();
			}
			out.println(line2write);
		}
		
		in.close();
		out.close();
	}
	
	public static void generateClusterSubset(int cluster) throws IOException
	{
		BufferedReader	in	= new BufferedReader(new FileReader("6clusters\\cluster" + cluster + "_attr.txt"));
		PrintWriter		out	= new PrintWriter("6clusters\\cluster" + cluster + "_attr_subset.csv");
		
		//create set of attribute numbers
		LinkedHashSet<Integer> attributeSet = new LinkedHashSet<Integer>();		
		String curLine;
		while((curLine = in.readLine()) != null)
		{
			String[] curAttrs = curLine.trim().split(",");
			for(String s : curAttrs)
				attributeSet.add(new Integer(s.trim()));
		}
		Object[] attributes = attributeSet.toArray();
		for(int i = 0; i < attributes.length; i++)
			System.out.print((Integer)attributes[i] + ",");
				
		//first line for weka
//		String firstLine = "qId";
//		for(int i = 0; i < attributes.length; i++)
//			firstLine += ",attr" + attributes[i];
//		firstLine += ",label";
//		out.println(firstLine);
		
		//begin parsing lines
		in.close();
		in = new BufferedReader(new FileReader("6clusters\\cluster" + cluster + ".csv"));
		while((curLine = in.readLine()) != null)
		{
			String[] lineArr = curLine.trim().split(",");
			String line2write = lineArr[0];
			for(int i = 0; i < attributes.length; i++)
			{
				Integer ind = ((Integer)attributes[i]) - 1;
				line2write += "," + lineArr[ind].trim();
			}
			line2write += "," + lineArr[lineArr.length-1];
			out.println(line2write);
		}
		
		in.close();
		out.close();
	}
	
	public static void splitUnlabelled() throws Exception
	{
		int numLines = 0;
		BufferedReader in = new BufferedReader(new FileReader("unlabelled\\unlabelled.csv"));
		while(in.readLine() != null)
			numLines++;
		in.close();
		System.out.println("Num Lines=" + numLines);
				
		in = new BufferedReader(new FileReader("unlabelled\\unlabelled.csv"));
		PrintWriter out = new PrintWriter("unlabelled\\unlabelled_1.csv");
		for(int i = 0; i < numLines/2; i++)
		{
			out.println(in.readLine());
		}
		out.close();
		
		out = new PrintWriter("unlabelled\\unlabelled_2.csv");
		String curLine;
		while((curLine = in.readLine()) != null)
			out.println(curLine);
		out.close();
	}
}
