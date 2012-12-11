package scot_cluster;
import weka.clusterers.EM;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.Arrays;


public class EM_Cluster {
	
	public EM model;
	public String training_f;
	public String[] options;
	
	public EM_Cluster(String tf, String[] options) throws Exception {
		this.model = new EM();
		this.training_f = tf;
		this.options = options;
				
		BufferedReader r = new BufferedReader(new FileReader(tf));
		Instances data = new Instances(r);
				
		model.setOptions(options);
		model.buildClusterer(data);
	}
	
	public static void main(String[] args) throws Exception {
		String tf = "../data/clusters/full_selatt.arff";
		String[] options = {"-N 10", "-V"}; 
		String dump_f = "../data/clusters/full.model";
		
		System.out.printf("Building EM Cluster on training file:%s options%s\n", tf, Arrays.toString(options));
		EM_Cluster c = new EM_Cluster(tf, options);
		
		System.out.printf("Dumping EM cluster to %s\n", dump_f);
		FileOutputStream f_out = new FileOutputStream(dump_f);
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		obj_out.writeObject ( c.model );
		
		System.out.println("Finished EM Clustering");
	}

}
