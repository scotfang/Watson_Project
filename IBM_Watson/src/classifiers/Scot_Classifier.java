package classifiers;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import weka.classifiers.Classifier;
import weka.clusterers.EM;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;


public class Scot_Classifier {
	
	EM clusterer;
	Classifier[] classPerClust;
	int[] clusterIdxs;
	String output_prefix;
	//[QID, best_score, 2nd_best_score, top_score_idx, total_count]
	private static final int QID = 0;
	private static final int BEST1 = 1;
	private static final int BEST2 = 2;
	private static final int BEST_IDX = 3;
	private static final int IDX_CT = 4;
	private static final int BEST_LABEL = 5;

	
	public Scot_Classifier(String output_prefix, String cluster_f, String clust2class_f, int[] clusterIdxs) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		this.clusterIdxs = clusterIdxs;
		this.output_prefix = output_prefix;
		
        if (cluster_f == null) {
            clusterer = null;
        } else {
            clusterer = (EM) weka.core.SerializationHelper.read(cluster_f);
        }
		String[] cluster_files = (String[]) new ObjectInputStream(new FileInputStream(clust2class_f)).readObject();
		classPerClust = new Classifier[cluster_files.length];
		for (int j=0; j<cluster_files.length; j++) {
			//System.out.printf("Reading %s classifier model for cluster %d\n", cluster_files[j], j);
			if (cluster_files[j]==null) {
				classPerClust[j] = null;
			} else {
				classPerClust[j] = (Classifier) weka.core.SerializationHelper.read(cluster_files[j]);
			}
		}
		System.out.printf("Initialized Classifier\n");
	}
	
	public double getScore(Instance inst) throws Exception {
		double true_prob = 0;
		DenseInstance clean;
		double[] clean_vals = new double[clusterIdxs.length];
		
		for (int j=0;j<clusterIdxs.length;j++) {
			clean_vals[j] = inst.value(clusterIdxs[j]);
		}
		
		clean = new DenseInstance(inst.weight(), clean_vals);			
        double[] clust_dist = {1};
        if (clusterer != null) {
            clust_dist = clusterer.distributionForInstance(clean);
            //System.out.printf("Clust_Dist N%d %s\n", clust_dist.length, Arrays.toString(clust_dist));
            //double max = 0;
            //int maxi=0;
            //for (int ci=0;ci<clust_dist.length;ci++) {
                //if (clust_dist[ci]>max) {
                    //max = clust_dist[ci];
                    //maxi = ci;
                //}
            //}
            //System.out.printf("Max cluster p: %f.2\n", max);
        }
		
		for (int ci=0;ci<clust_dist.length;ci++) {
			if (classPerClust[ci] != null && clust_dist[ci] > 0) {
				true_prob += classPerClust[ci].distributionForInstance(inst)[1] * clust_dist[ci];
			}
		}
		//System.out.printf("true_prob %f\n", true_prob);
		//assert(true_prob>0);
		return true_prob;
	}

	ArffLoader initLoader(String input) throws IOException {
		File input_f = new File(input);
		assert(input_f.exists());

		if (input_f.length() == 0) {
			System.out.printf("input file %s is empty, skipping\n", input_f);
			return null;
		}

		ArffLoader loader = new ArffLoader();
		loader.setFile(input_f);
		return loader;
	}

	public void eval_labelled(String input, double threshold) throws IOException, Exception {
		//decision threshold to determine FP and FN rates
		System.out.printf("Evaluating unlabelled dataset %s @ thresh %f\n", input, threshold);
		ArffLoader loader = initLoader(input);		
		if (loader==null) { return; }

		Instances iStruct = loader.getStructure();
		int N_att = iStruct.numAttributes();
		iStruct.setClassIndex(N_att-1);

		int inst_ct=0;
		int FP=0, TP=0, FN=0, TN=0;
		Instance inst;
		while ((inst=loader.getNextInstance(iStruct))!=null) {
			inst_ct++;
			if (inst_ct%100000==0) { System.out.printf("\tProcessed %d instances\n",inst_ct); }
			
			double true_prob = getScore(inst);
			double decision = (true_prob > threshold)? 1.0:0.0;

			if (inst.value(N_att-1) == decision) {		
				if (decision == 1) { TP += 1; } 
				else {TN += 1;}
			} else {
				if (decision == 1) { FP += 1; }
				else { FN += 1; }
			}
		}
			
			float fn_rate = (float)FN/(TP+FN);
			float fp_rate = (float)(FP)/(TN+FP);
			System.out.printf("\tTP: %d FN: %d fn_rate: %f\n", TP, FN, fn_rate);
			System.out.printf("\tTN: %d FP: %d fp_rate: %f\n", TN, FP, fp_rate);
	} 

	public int predict_abs_thresh(double abs_thresh, List<Double[]> id_stats, String output_f, boolean validate) throws IOException {
		assert(abs_thresh >=0);
		BufferedWriter writer = new BufferedWriter(new FileWriter(output_f));
		System.out.printf("\tDumping predictions to %s\n", output_f);
	
		int unanswered_QIDS= 0;
		int FP=0, TP=0, FN=0, TN=0;
		for (Iterator<Double[]> it = id_stats.iterator(); it.hasNext();) { 
			Double[] stat = it.next();
			double id = stat[QID];
			boolean valid_truth = (stat[BEST1] > abs_thresh);
			if (!valid_truth) { unanswered_QIDS += 1; } 
			if (validate) {
				if (valid_truth && stat[BEST_LABEL]==1.0) {TP+=1;}
				else if (!valid_truth && stat[BEST_LABEL]==1.0) {FN+=1;}
				else if (valid_truth && stat[BEST_LABEL]==0.0) {FP+=1;}
				else {TN += 1;}
			}

			for (int i=0; i<stat[IDX_CT]+1;i++){
				String csvLine;
				if (i == stat[BEST_IDX] && valid_truth) {
					csvLine = String.format("%d,true\n", (int)id);
				} else {
					csvLine = String.format("%d,false\n", (int)id);
				} 
				writer.write(csvLine);
			}
		}
		System.out.printf("\tUnanswered QIDS:%d/%d\n", unanswered_QIDS, id_stats.size());
        int score = 0;
		if (validate) {
			float fn_rate = (float)FN/(TP+FN);
			float fp_rate = (float)(FP)/(TN+FP);
            score = TP-FP;
			System.out.printf("\tTP: %d FN: %d fn_rate: %f\n", TP, FN, fn_rate);
			System.out.printf("\tTN: %d FP: %d fp_rate: %f\n", TN, FP, fp_rate);
            System.out.printf("\tFINAL SCORE: %d\n", score);
		}
		writer.close();
        return score;
	}

	public void predict_class(String input, double[] abs_thresh, double[] std_thresh, boolean validate) throws Exception { 
		//assumes that data is ordered by QID	
		ArffLoader loader = initLoader(input);		
		if (loader==null) { return; }

		Instances iStruct = loader.getStructure();
		int N_att = iStruct.numAttributes();
		iStruct.setClassIndex(N_att-1);
		
		Instance inst;
		int inst_ct = 0;
		System.out.printf("\tProcessing instances from %s\n",input);
		
		List<Double[]> id_stats = new ArrayList<Double[]>(); 
		Double[] cur_stats = { -1.0, -1.0, -1.0, -1.0, -1.0, -1.0};
		//[QID, BEST1, BEST2, BEST_IDX, IDX_CT, BEST_LABEL]

		while ((inst=loader.getNextInstance(iStruct))!=null) {
            if (inst_ct == 0) {
				cur_stats[QID] = inst.value(0);
            } else if (cur_stats[0] != inst.value(0)) {
				//we hit a new QID
				//stat collection assumes we have a dataset
				//ordered by QID
				assert(cur_stats[BEST1] - cur_stats[BEST2] >= 0);
				id_stats.add(cur_stats);
				//System.out.printf("%s\n", Arrays.toString(cur_stats));

				cur_stats = new Double[cur_stats.length];
				cur_stats[QID] = inst.value(0);
				cur_stats[BEST1] = -1.0;
				cur_stats[BEST2] = -1.0;
				cur_stats[BEST_IDX] = -1.0;
				cur_stats[IDX_CT] = -1.0;
				cur_stats[BEST_LABEL] = -1.0;
			}

			double tmp = cur_stats[IDX_CT];
			cur_stats[IDX_CT]++;
			assert(tmp+1 == cur_stats[IDX_CT]); //because I'm java ignorant 

			inst_ct++;
			if (inst_ct%100000==0) { System.out.printf("\tProcessed %d instances\n",inst_ct); }
			
			double true_prob = getScore(inst);
						
			if (true_prob > cur_stats[BEST1]) {
				cur_stats[BEST2] = (cur_stats[BEST1] < 0)? true_prob : cur_stats[BEST1];
				cur_stats[BEST1] = true_prob;
				cur_stats[BEST_IDX] = cur_stats[IDX_CT];
				if (validate) {
					cur_stats[BEST_LABEL] = inst.classValue();
				}
			} else if (true_prob > cur_stats[BEST2]) {
				cur_stats[BEST2] = true_prob;
			}
		}
		//Add last stat in at end of loop
		assert(cur_stats[BEST1] - cur_stats[BEST2] >= 0);
		id_stats.add(cur_stats);
		//System.out.printf("%s\n", Arrays.toString(cur_stats));

		//avg delta of best 2 scores
		double u_delta = 0; 
		double u_score = 0;
		double all_counts = 0;
		for (Iterator<Double[]> it = id_stats.iterator(); it.hasNext();) { 
				Double[] stat = it.next();
				u_delta += stat[BEST1] - stat[BEST2];
				u_score += stat[BEST1];
				all_counts += stat[IDX_CT] + 1;
		}
		assert(inst_ct == all_counts);
		u_delta /= id_stats.size();
		u_score /= id_stats.size();

		//variance of delta of best 2 scores
		double var_delta = 0;
		double var_score = 0;
		for (Iterator<Double[]> it = id_stats.iterator(); it.hasNext();) { 
				Double[] stat = it.next();
				double delta = stat[BEST1] - stat[BEST2];
				var_delta += (u_delta-delta)*(u_delta-delta);
				var_score += (stat[BEST1]-u_score)*(stat[BEST1]-u_score);
		}
		var_delta /= id_stats.size();
		var_score /= id_stats.size();
		System.out.printf("Best2_Delta mean %.2f variance %.2f\n", u_delta, var_delta);
		System.out.printf("Best1 mean %.2f std_dev %.2f\n", u_score, Math.sqrt(var_score));
		
		loader.reset();

        double best_thresh = -1.0;
        int best_score = -1;
        for (double st: std_thresh) {
            double converted_thresh =  u_score + st*Math.sqrt(var_score);
            if (converted_thresh < 1) {
                System.out.printf("Predicting class @ std_dev_thresh %f ==> %f\n", st, converted_thresh);
                String output_f = String.format("%s_std_thresh%.4f_%.4f.csv", output_prefix, st, converted_thresh);
                int score = predict_abs_thresh(converted_thresh, id_stats, output_f, validate); 
                if (best_score < score) {
                    best_score = score;
                    best_thresh = converted_thresh;
                }
            } else {
                System.out.printf("Can't predict class @ std_dev_thresh %f ==> %f\n", st, converted_thresh);
            }
		}
        for (double at: abs_thresh) {
            if (at >= 0) { 
                System.out.printf("Predicting class @ abs_thresh %f\n", at);
                String output_f = String.format("%s_abs_thresh%.4f.csv", output_prefix, at);
                int score = predict_abs_thresh(at, id_stats,output_f, validate); 

                if (best_score < score) {
                    best_score = score;
                    best_thresh = at;
                }
            }
        }
        if (validate) {
            System.out.printf("BEST SCORE: %d BEST_THRESH %f\n", best_score, best_thresh);
        }
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int[] clusterIdxs = {14,
                86,
                97,
                127,
                185,
                203,
                211};	
		//double [] thresholds = { .0001, .0005, .001, .005, 0.01};
		String output_prefix = "../submissions/rBoost_clustered_min10000_max10";
		//String cluster_f = "../data/clusters/full_em500.model";
        String cluster_f = null;
		//String clust2class_f = "../full_train_classifiers/cs10_logreg/cs10_logreg.map";
        String clust2class_f = "/home/sfang/Watson_Project/no_cluster_classifiers/rboost_max33/lBoost_stumps.map";
		
		Scot_Classifier c = new Scot_Classifier(output_prefix, cluster_f, clust2class_f, clusterIdxs);

		//String input_data = "../data/unlabelled.arff";
		//String input_data = "../data/num_data.arff";
		String input_data = "../data/chunks/chunk1_sorted.arff";
        //double[] std_thresh = {2, 2.3, 2.6, 2.9};
        double[] std_thresh = {};
        double[] abs_thresh = {.9991, .9992, .9995, .9996, .9999};

		c.predict_class(input_data, abs_thresh, std_thresh, true);

		//for (double t: thresholds) {
			
		//}
			
		//double [] thresholds = { .05 };

		//Scot_Classifier c = new Scot_Classifier("../data/unlabelled.arff", "../submissions/stump_rBoost_minC10_maxC100", "../data/clusters/full_em500.model", "../full_train_classifiers/rBoost_min1000_max10/rBoost.map", clusterIdxs, false);	
		
		//double[] thresholds = {.15};
		//int[] clusters = {0,1,2,3,4,5,6,7,8,9};
		//for (int cluster: clusters) {
			//Scot_Classifier c = new Scot_Classifier(String.format("../data/clusters/splits/full_em500_c%d_test.arff", cluster), "../submissions/stump_rBoost_minC10_maxC100", "../data/clusters/full_em500.model", "../full_train_classifiers/rBoost_REP/rBoost.map", clusterIdxs, true);	
			
			//for (double t: thresholds) {
				//c.eval(t);
			//}
		//}
		//Scot_Classifier c = new Scot_Classifier("../data/unlabelled.arff", "../submissions/cs10_logreg_EM7", "../data/clusters/full_em500.model", "../full_train_classifiers/cs10_logreg.map", clusterIdxs, false);	
		//for (double t: thresholds) {
			//c.eval(t);
		//}
		System.out.printf("Finished Program.\n");
	}
}
