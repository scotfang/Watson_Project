package classifiers;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.*;
import weka.filters.unsupervised.attribute.*;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.meta.RacedIncrementalLogitBoost;
import weka.core.converters.ArffLoader;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.DenseInstance;
import weka.classifiers.trees.*;
//perform cost sensitive logistic regression

public class Trainer {

	public String[] cluster_files;
	String dump_dir;	//directory to dump models to
					
	public Trainer(String[] cluster_files, String dump_dir) throws Exception {
		for (String f: cluster_files)
			assert(new File(f).exists());
		this.cluster_files = cluster_files;
		
		Discretize df = new Discretize();
		String[] df_options = {"-D", "-R last"};
		df.setOptions(df_options);
		
		new File(dump_dir).mkdirs();
		this.dump_dir = dump_dir;
		
		System.out.printf("Created new trainer with dump_dir %s\n\tClusterFiles %s\n", dump_dir, Arrays.toString(cluster_files));
	}
	
	public static String[] splitFext(String file) {
		return file.split("\\.(?=[^\\.]+$)");
	}
	
	public static boolean clusterEmpty(File clust_f) {
		return (clust_f.length() == 0); 
	}
	
	public static void setCostMatrix(CostSensitiveClassifier cs, double fn_cost) {
		CostMatrix cm = new CostMatrix(2);
		cm.initialize();
		cm.setElement(1, 0, fn_cost);
		cs.setCostMatrix(cm);
	}
	
	public Instances load_batch(String source_f) throws Exception {
		System.out.printf("\t\tLoading data_source %s....\n", source_f.toString());
		DataSource data_source = new DataSource(source_f.toString());
		Instances data = data_source.getDataSet();
		data.setClassIndex(data.numAttributes()-1);
		return data;
	}
	
	public static void dumpObj(File dump_f, Object obj) throws FileNotFoundException, IOException {
		System.out.printf("\t\tDumping classifier to %s...\n", dump_f);
		ObjectOutputStream obj_out = new ObjectOutputStream(new FileOutputStream(dump_f));
		obj_out.writeObject(obj);
	}
	
	public String cs_logreg(double fn_cost, int cluster) throws Exception { //fn_cost in cost matrix
		File clust_f = new File(cluster_files[cluster]);
		if (clusterEmpty(clust_f)) {
			System.out.printf("Cluster%d empty\n", cluster);
			return null;
		}
		
		CostSensitiveClassifier cs = new CostSensitiveClassifier();
		String cmd = String.format("-W \"weka.classifiers.functions.Logistic\"");
		String[] options = weka.core.Utils.splitOptions(cmd);
		cs.setOptions(options);
		setCostMatrix(cs, fn_cost);
		System.out.printf("CS with %s\ncost-matrix:\n%s\n", cmd, cs.getCostMatrix().toString());
		
		FilteredClassifier nomC = new FilteredClassifier();
		nomC.setClassifier(cs);
		//Discretize df = new Discretize();
		NumericToNominal num2nom = new NumericToNominal();
		num2nom.setAttributeIndices("last");
		nomC.setFilter(num2nom);
				
		Instances data = load_batch(clust_f.toString());
		System.out.printf("\t\tTraining classifier...\n");
		nomC.buildClassifier(data);
		File dump_f = new File(dump_dir, splitFext(clust_f.getName())[0] + String.format("_cs%dlogreg.model", (int)fn_cost));
		System.out.printf("\t\tSerializing model to %s...\n", dump_f.toString());	
		dumpObj(dump_f, nomC);
		return dump_f.toString();
	}
	
	//Note: simplelogistic in weka uses uses logitboost "regression tree" as a the weak classifier, 
	//However other papers have shown logitboost performs better with a stump classifier.
	public String cs_simplelog(double fn_cost, int cluster, int maxIt) throws Exception { //fn_cost in cost matrix
		File clust_f = new File(cluster_files[cluster]);
		if (clusterEmpty(clust_f)) {
			System.out.printf("Cluster%d empty\n", cluster);
			return null;
		}
		
		CostSensitiveClassifier cs = new CostSensitiveClassifier();
		
		SimpleLogistic simpleC = new SimpleLogistic();
		simpleC.setUseCrossValidation(false);
		simpleC.setMaxBoostingIterations(maxIt);
		
		cs.setClassifier(simpleC);
		setCostMatrix(cs, fn_cost);
		System.out.printf("CS with SimpleLogistic no cv, cost-matrix:\n%s\n", cs.getCostMatrix().toString());
				
		FilteredClassifier nomC = new FilteredClassifier();
		nomC.setClassifier(cs);
		NumericToNominal num2nom = new NumericToNominal();
		num2nom.setAttributeIndices("last");
		nomC.setFilter(num2nom);
				
		Instances data = load_batch(clust_f.toString());
		System.out.printf("\t\tTraining classifier...\n");
		nomC.buildClassifier(data);
		File dump_f = new File(dump_dir, splitFext(clust_f.getName())[0] + String.format("_cs%dSimpleLog_I%d.model", (int)fn_cost, maxIt));
		System.out.printf("\t\tSerializing model to %s...\n", dump_f.toString());	
		dumpObj(dump_f, nomC);
		return dump_f.toString();
	}
		
	public String logitBoost_stumps(int cluster, int its) throws Exception { //fn_cost in cost matrix
		File clust_f = new File(cluster_files[cluster]);
		if (clusterEmpty(clust_f)) {
			System.out.printf("Cluster%d empty\n", cluster);
			return null;
		}
		
		DecisionStump stump = new DecisionStump();
		LogitBoost lb = new LogitBoost();
		lb.setClassifier(stump);
		lb.setNumIterations(its);
		//lb.setWeightThreshold(weightmass) //90-100
		
		FilteredClassifier nomC = new FilteredClassifier();
		nomC.setClassifier(lb);
		NumericToNominal num2nom = new NumericToNominal();
		num2nom.setAttributeIndices("last");
		nomC.setFilter(num2nom);
				
		Instances data = load_batch(clust_f.toString());
		System.out.printf("\t\tLogitBoost Stump Cluster:%d Its:%d Training classifiers...\n", cluster, its);
		nomC.buildClassifier(data);
		File dump_f = new File(dump_dir, splitFext(clust_f.getName())[0] + String.format("_logitboost_I%d.model", its));
		System.out.printf("\t\tSerializing model to %s...\n", dump_f.toString());	
		dumpObj(dump_f, nomC);
		return dump_f.toString();
	}
	
	public String rBoost_M5P(int cluster, double minChunk_pct, double maxChunk_pct) throws Exception { //racedIncrementalBoost
		//THIS SUCKED
		M5P tree = new M5P();
		return rBoost(cluster, minChunk_pct, maxChunk_pct, tree);
	}
	
	public String rBoost_REP(int cluster, double minChunk_pct, double maxChunk_pct) throws Exception { //racedIncrementalBoost
		REPTree tree = new REPTree();
		return rBoost(cluster, minChunk_pct, maxChunk_pct, tree);
	}
	
	public String rBoost_stumps(int cluster, double minChunk_pct, double maxChunk_pct) throws Exception { //racedIncrementalBoost
		DecisionStump stump = new DecisionStump();
		return rBoost(cluster, minChunk_pct, maxChunk_pct, stump);
	}
	
	public String rBoost(int cluster, double minChunk_pct, double maxChunk_pct, weka.classifiers.Classifier weakClass) throws Exception { //racedIncrementalBoost
		File clust_f = new File(cluster_files[cluster]);
		if (clusterEmpty(clust_f)) {
			System.out.printf("Cluster%d empty\n", cluster);
			return null;
		}
		
		
		//FilteredClassifier nomC = new FilteredClassifier();
		//nomC.setClassifier(rBoost);
		//NumericToNominal num2nom = new NumericToNominal();
		//num2nom.setAttributeIndices("last");
		//nomC.setFilter(num2nom);
		
		// load data
		ArffLoader loader = new ArffLoader();
		loader.setFile(clust_f);
		Instances iStruct = loader.getStructure();
		iStruct.setClassIndex(iStruct.numAttributes() - 1);
		Instance inst;
		int N_inst = 0;
		while((inst = loader.getNextInstance(iStruct)) != null) {
			N_inst++;
		}
		loader.reset();
		
		RacedIncrementalLogitBoost rBoost = new RacedIncrementalLogitBoost();
		rBoost.setClassifier(weakClass);
		rBoost.setMinChunkSize((int)(minChunk_pct*N_inst)+1);
		rBoost.setMaxChunkSize((int)(maxChunk_pct*N_inst));
		System.out.printf("Chunksize min/max %d/%d\n", rBoost.getMinChunkSize(), rBoost.getMaxChunkSize());
		//System.out.printf("Prune type %s\n", rBoost.getPruningType().toString());
				
		FilteredClassifier nomC = new FilteredClassifier();
		nomC.setClassifier(rBoost);
		NumericToNominal num2nom = new NumericToNominal();
		num2nom.setAttributeIndices("last");
		nomC.setFilter(num2nom);
		FastVector truthVals = new FastVector(2);
		truthVals.addElement("0");
		truthVals.addElement("1");
		Attribute binNom = new Attribute("TRUTH", truthVals);
		Instances dummyStruct = iStruct;
		dummyStruct.setClassIndex(0);
		dummyStruct.deleteAttributeAt(dummyStruct.numAttributes()-1);
		dummyStruct.insertAttributeAt(binNom, dummyStruct.numAttributes());
		dummyStruct.setClassIndex(dummyStruct.numAttributes() - 1);
		nomC.buildClassifier(dummyStruct);
				
		iStruct = loader.getStructure();
		int N_att = iStruct.numAttributes();
		int its = 0;
		while ((inst = loader.getNextInstance(iStruct)) != null) {
			its++;
			if(its%100==0) {
				System.out.printf("Boosted %d instances\n", its);
			}
			//DenseInstance dummy = new DenseInstance(inst.weight(), inst.toDoubleArray());
			//dummy.insertAttributeAt(N_att);
			//if (dummy.value(N_att-1)==0.0) {
				//dummy.setValue(N_att, "0");
			//} else if (dummy.value(N_att-1) == 1.0) {
				//dummy.setValue(N_att, "1");
			//} else {
				//assert(false);
			//}
			//System.out.printf("class val %f/%f\n", inst.value(N_att-1), dummy.value(N_att));
			//assert(dummy.value(N_att) == inst.value(N_att=1));
			rBoost.updateClassifier(inst);
		}
		loader.reset();
 
		File dump_f = new File(dump_dir, splitFext(clust_f.getName())[0] + String.format("_rBoost_C%d_I%d.model", rBoost.getBestCommitteeChunkSize(),its));
		System.out.printf("\t\tSerializing model to %s...\n", dump_f.toString());	
		dumpObj(dump_f, rBoost);
		return dump_f.toString();
	}
	
	public static void main(String[] args) throws Exception {
		String[] full_train_files = {
				"../data/clusters/splits/full_em500_c0_train.arff",
				"../data/clusters/splits/full_em500_c1_train.arff",
				"../data/clusters/splits/full_em500_c2_train.arff",
				"../data/clusters/splits/full_em500_c3_train.arff",
				"../data/clusters/splits/full_em500_c4_train.arff",
				"../data/clusters/splits/full_em500_c5_train.arff",
				"../data/clusters/splits/full_em500_c6_train.arff",
				"../data/clusters/splits/full_em500_c7_train.arff",
				"../data/clusters/splits/full_em500_c8_train.arff",
				"../data/clusters/splits/full_em500_c9_train.arff",
				
		};
		
		String[] full_files = {
				"../data/clusters/splits/full_em500_c0.arff",
				"../data/clusters/splits/full_em500_c1.arff",
				"../data/clusters/splits/full_em500_c2.arff",
				"../data/clusters/splits/full_em500_c3.arff",
				"../data/clusters/splits/full_em500_c4.arff",
				"../data/clusters/splits/full_em500_c5.arff",
				"../data/clusters/splits/full_em500_c6.arff",
				"../data/clusters/splits/full_em500_c7.arff",
				"../data/clusters/splits/full_em500_c8.arff",
				"../data/clusters/splits/full_em500_c9.arff",
				
		};
		
		String[] chunk1_files = {
				"../data/clusters/splits/chunk1_c0_train.arff",
				"../data/clusters/splits/chunk1_c1_train.arff",
				"../data/clusters/splits/chunk1_c2_train.arff",
				"../data/clusters/splits/chunk1_c3_train.arff",
				"../data/clusters/splits/chunk1_c4_train.arff",
				"../data/clusters/splits/chunk1_c5_train.arff",
				"../data/clusters/splits/chunk1_c6_train.arff",
				"../data/clusters/splits/chunk1_c7_train.arff",
				"../data/clusters/splits/chunk1_c8_train.arff",
				"../data/clusters/splits/chunk1_c9_train.arff",
				
		};
		
		String[] tiny_files = {
				"../data/clusters/splits/tiny_em500_c0.arff",
				"../data/clusters/splits/tiny_em500_c1.arff",
				"../data/clusters/splits/tiny_em500_c2.arff",
				"../data/clusters/splits/tiny_em500_c3.arff",
				"../data/clusters/splits/tiny_em500_c4.arff",
				"../data/clusters/splits/tiny_em500_c5.arff",
				"../data/clusters/splits/tiny_em500_c6.arff",
				"../data/clusters/splits/tiny_em500_c7.arff",
				"../data/clusters/splits/tiny_em500_c8.arff",
				"../data/clusters/splits/tiny_em500_c9.arff",
				
		};
		String[] no_cluster_files = { "../data/num_data.arff" };
		String no_cluster_dump_dir = "../no_cluster_classifiers/rboost_max33";
		
		String full_dump_dir = "../full_classifiers/cs_logreg";
		String full_train_dump_dir = "../full_train_classifiers/lBoost_stumps";
		String tiny_dump_dir = "../tiny_classifiers";
		String chunk1_dump_dir = "../chunk1_train_classifiers/rboost_REP";
		//String[] cluster_files = tiny_files;
		//String dump_dir = tiny_dump_dir;
		
		String[] cluster_files = full_files;
		String dump_dir = full_dump_dir;
		
		Trainer t = new Trainer(cluster_files, dump_dir);
		int N_clust = cluster_files.length;
		File map_f;
		
		//int maxIts = 100;
		
		//String[] lBoost_map = new String[N_clust];
		//for (int j=0; j<cluster_files.length;j++) {
			//lBoost_map[j] = t.logitBoost_stumps(j, maxIts);
		//}
		//map_f = new File(dump_dir, String.format("_lBoost_I%d.map",maxIts));
		//System.out.printf("Dumping %s\n", map_f);
		//dumpObj(map_f, lBoost_map);
		
		//double maxChunk = 0.33;
		//double minChunk = 0.001;
	
		
		//String[] rBoost_map = new String[N_clust];
		//for (int j=0; j<cluster_files.length;j++) {
			//rBoost_map[j] = t.rBoost_stumps(j, minChunk, maxChunk);
		//}
		//map_f = new File(dump_dir, String.format("lBoost_stumps.map"));
		//System.out.printf("Dumping %s\n", map_f);
		//dumpObj(map_f, rBoost_map);
		
		//double[] fn_cost_array = { 10, 30, 50, 100 };
		double[] fn_cost_array = {10};
	
		
		for (double fn_cost: fn_cost_array) {
			String[] logreg_map = new String[N_clust];
			for (int j=0; j<cluster_files.length;j++) {
				logreg_map[j] = t.cs_logreg(fn_cost, j);
			}
			map_f = new File(dump_dir, String.format("cs%d_logreg.map",(int)fn_cost));
			System.out.printf("Dumping %s\n", map_f);
			dumpObj(map_f, logreg_map);
			
			//String[] simplelog_map = new String[N_clust];
			//for (int j=0; j<cluster_files.length;j++) {
				//simplelog_map[j] = t.cs_simplelog(fn_cost, j, maxIt);
			//}
			//map_f = new File(dump_dir, String.format("cs%d_simplelog.map",(int)fn_cost));
			//System.out.printf("Dumping %s\n", map_f);
			//dumpObj(map_f, simplelog_map);
		
		}
	
		
		System.out.println("Finished program.");
	}

}
