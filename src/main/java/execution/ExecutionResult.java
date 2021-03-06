package execution;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import types.mallet.LabeledInstancesList;
import types.mallet.classify.ExtendedTrial;
import cc.mallet.classify.Classification;
import cc.mallet.types.Instance;
import cc.mallet.types.Labeling;

/**
 * Holds the results of an executon run (several cross validation runs).
 * Results are kept in the form of extended trials with respect to the 
 * corresponding fold.
 * 
 * @author tt
 */
public class ExecutionResult {
	public final String name;
	private final Date datetime;
	
	public final String transformer;
	public final String filter;
	public final String classifier;
	
	public Map<Integer, Collection<ExtendedTrial>> trials;
	
	public ExecutionResult(String n, String t, String f, String c) {
		this.name = n;
		this.datetime = new Date();
		this.transformer = t;
		this.filter = f;
		this.classifier = c;
		this.trials = new HashMap<Integer, Collection<ExtendedTrial>>();
	}
	
	/**
	 * Writes the results of the classification into the output stream in a formatted manner.
	 * (instance, real_class_idx, real_class, class_n1_idx, class_n1, class_n1_val, ..., class_nK_idx, class_nK, class_nK_val)
	 */
	public void outputTrials() throws FileNotFoundException {
		Instance instance;
		Labeling labeling; // ATTN: same variable used in 2 different contexts (why? cause i'm a cheap bastard)
		
		for (Integer n : this.trials.keySet()) {
			for (ExtendedTrial trial : this.trials.get(n)) {
				FileOutputStream out = new FileOutputStream(getTrialOutputName(trial, n));
				PrintWriter pw = new PrintWriter(out);
				
				LabeledInstancesList trainLabeledInstances = trial.getTrainLabeledInstances();
				LabeledInstancesList testLabeledInstances = trial.getTestLabeledInstances();
				for (Classification classification : trial) {
					// write out results in the form of:
					// instance, real_class_idx, real_class, num_train_docs, num_test_docs, class_n1_idx, class_n1, val_n1, ..., class_nK_idx, class_nK, val_nK
					// class_nN = class classified at position N
					// val_nN = value of class at position N
					
					// instance
					instance = classification.getInstance();
					pw.write(instance.getName() + ", ");
					
					// real class
					labeling = instance.getLabeling(); // ATTN: first use here, instance's labeling
					int classIdx = labeling.getBestIndex();
					pw.write(classIdx + ", ");
					pw.write(labeling.getBestLabel() + ", ");
					
					// write out training set and testing set size
					pw.write(trainLabeledInstances.getNumLabelInstances(classIdx) + ".0, ");
					pw.write(testLabeledInstances.getNumLabelInstances(classIdx) + ".0, ");
					
					// pairs of class_nN, val_nN
					labeling = classification.getLabeling(); // ATTN: second use here, classification's labeling
					int nl = labeling.numLocations();
					for(int i=0; i < nl; ++i) {
						pw.write(labeling.indexAtLocation(i) + ", ");
						pw.write(labeling.labelAtLocation(i) + ", ");
						pw.write(String.valueOf(labeling.valueAtLocation(i)));
						
						if(i+1 < nl) pw.write(", ");
					}
					pw.write('\n');
				}
				
				pw.flush();
				pw.close();
			}
		}
	}
	
	/**
	 * Writes the accuracies of the trials into a file, in a formatted manner.
	 * (number_of_features, trial1_accuracy, trial2_accuracy, ..., trialN_accuracy)
	 * 
	 * Used to plot feature selection/classification accuracy graph.
	 * 
	 */
	public void outputAccuracies() throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(getAccuraciesOutputName());
		PrintWriter pw = new PrintWriter(out);
		
		for (Integer n : this.trials.keySet()) {
			pw.write(n.toString());
			pw.write(", ");
			
			Collection<ExtendedTrial> trialsN = this.trials.get(n);
			int i = 0;
			int ts = trialsN.size();
			for (ExtendedTrial trial : trialsN) {
				pw.write(String.valueOf(trial.getAccuracy()));
				if(++i < ts) pw.write(", ");
			}
			pw.write('\n');
		}
		
		pw.flush();
		pw.close();
	}
	
	private String getTrialOutputName(ExtendedTrial trial, int n) {
		StringBuffer sb = new StringBuffer();
		sb.append("trial");
		sb.append("+");
		sb.append(this.name);
		if(this.transformer!=null) {
			sb.append("+");
			sb.append(this.transformer);
		}
		if(this.filter!=null) {
			sb.append("+");
			sb.append(this.filter);
		}
		sb.append("+");
		sb.append(n);
		sb.append("+");
		sb.append(this.classifier);
		sb.append("+");
		sb.append(new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(this.datetime));
		sb.append("+");
		sb.append("fold");
		sb.append("+");
		sb.append(trial.fold);
		
		return sb.toString();
	}
	
	private int accuracyCounter = 0;
	
	private String getAccuraciesOutputName() {
		StringBuffer sb = new StringBuffer();
		sb.append("accuracies");
		sb.append("+");
		sb.append(this.name);
		if(this.transformer!=null) {
			sb.append("+");
			sb.append(this.transformer);
		}
		if(this.filter!=null) {
			sb.append("+");
			sb.append(this.filter);
		}
		sb.append("+");
		sb.append(this.classifier);
		sb.append("+");
		sb.append(new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(this.datetime));
		sb.append("+");
		sb.append(accuracyCounter++);
		
		return sb.toString();
	}
}
