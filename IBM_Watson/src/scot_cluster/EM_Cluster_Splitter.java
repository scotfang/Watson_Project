package scot_cluster;
import weka.core.converters.ArffLoader;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.converters.ArffSaver;
import java.io.File;
import java.io.PrintWriter;

import weka.clusterers.EM;
import weka.core.Instances;
import java.lang.String;
import java.util.Arrays;
public class EM_Cluster_Splitter {

	public ArffLoader loader;
	public ArffSaver[] savers;
	public File input_f;
	public String output_prefix;
	
	public EM_Cluster_Splitter(File input_f, String output_prefix, EM model, int[] attIndexes) throws Exception {
		loader = new ArffLoader();
		loader.setFile(input_f);
		Instances iStruct = loader.getStructure();
			
		savers = new ArffSaver[model.numberOfClusters()];
		for (int i=0;i<savers.length;i++){
			savers[i] = new ArffSaver();
			savers[i].setStructure(iStruct);
			savers[i].setRetrieval(ArffSaver.INCREMENTAL);
			String out_f = String.format("%s_c%d.arff", output_prefix, i);
			System.out.printf("Initializing Arrfsaver to dump to %s...\n", out_f);
			savers[i].setFile(new File(out_f));
			
		}
		
		Instance inst = null;
		DenseInstance clean = null;
		double[] clean_vals = new double[attIndexes.length];
		
		int cluster = 0;
		int count = 0;
		int cluster_counts[] = new int[model.numberOfClusters()]; 
		for (int j=0; j<cluster_counts.length; j++) {
			cluster_counts[j] = 0;
		}
		
		while((inst=loader.getNextInstance(iStruct)) != null){
	
			for (int j=0;j<attIndexes.length;j++) {
				clean_vals[j] = inst.value(attIndexes[j]);
			}
			clean = new DenseInstance(inst.weight(), clean_vals);			
			cluster = model.clusterInstance(clean);
			savers[cluster].writeIncremental(inst);
			cluster_counts[cluster] = cluster_counts[cluster] + 1;
			count++;
			if (count%50000==0) {
				System.out.printf("Processed %d instances\n", count);
			}
		}
		//freaking rediculous that weka doesn't automatically flush end of the arffsaver for you
		for (ArffSaver s: savers) {
			if (s.getWriter() != null) {
			      PrintWriter outW = new PrintWriter(s.getWriter());
			      outW.flush();
			      outW.close();
			}
		}
		
		System.out.printf("Empirical cluster counts: ");
		for (int ct: cluster_counts) {
			System.out.printf("%d ", ct);
		}
		System.out.printf("\n");
	}
	
	public static void main(String[] args) throws Exception {
		//String input_f = "../data/num_data.arff";
		//String input_f = "../data/chunks/tiny_num_data.arff";
		String input_f = "../data/chunks/chunk1_l164301_num_data.arff";
		//String output_prefix = "../data/clusters/splits/full";
		//String output_prefix = "../data/clusters/splits/tiny_em500";
		String output_prefix = "../data/clusters/splits/chunk1";
		String model_f = "../data/clusters/full_em500.model";
		
		int[] attIndexes = {14,
		                    86,
		                    97,
		                    127,
		                    185,
		                    203,
		                    211};
		
		System.out.printf("attIndexes %s\n", Arrays.toString(attIndexes));
		
		EM model = (EM)weka.core.SerializationHelper.read(model_f);
		
		System.out.println(model.toString());
		System.out.printf("Cluster Priors: %s\n", Arrays.toString(model.getClusterPriors()));
		
		System.out.printf("Splitting clusters in %s with cluster model %s\n", input_f, model_f);
		new EM_Cluster_Splitter(new File(input_f), output_prefix, model, attIndexes);
		System.out.println("Finished splitting clusters");
	}
}
