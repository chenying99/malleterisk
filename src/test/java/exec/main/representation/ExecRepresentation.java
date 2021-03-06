package exec.main.representation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import execution.ExecutionUtils;
import ft.selection.IFilter;
import ft.selection.methods.FilterByRankedIG;
import ft.selection.methods.FilterByRankedTW;
import ft.weighting.IWeighter;
import ft.weighting.methods.FeatureWeighting;

public class ExecRepresentation {
	public static void main(String[] args) throws FileNotFoundException, InstantiationException, IllegalAccessException {
		ArrayList<File> files = new ArrayList<File>();
		files.add(new File("instances+1+1+body"));
		files.add(new File("instances+2+2+body"));
		files.add(new File("instances+2+3+body"));
		files.add(new File("instances+2+4+body"));
		files.add(new File("instances+2+5+body"));
		files.add(new File("instances+2+6+body"));
		files.add(new File("instances+2+7+body"));
		files.add(new File("instances+2+8+body"));
		
		ArrayList<IWeighter> transformers = new ArrayList<IWeighter>();
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_BOOLEAN, FeatureWeighting.IDF_NONE, FeatureWeighting.NORMALIZATION_NONE));	// boolean
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_NONE, FeatureWeighting.IDF_NONE, FeatureWeighting.NORMALIZATION_NONE));		// nnn
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_MAX_NORM, FeatureWeighting.IDF_NONE, FeatureWeighting.NORMALIZATION_NONE));	// mnn
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_LOG, FeatureWeighting.IDF_NONE, FeatureWeighting.NORMALIZATION_NONE));		// lnn
		
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_NONE, FeatureWeighting.IDF_IDF, FeatureWeighting.NORMALIZATION_NONE));		// ntn
		transformers.add(new FeatureWeighting(FeatureWeighting.TF_LOG, FeatureWeighting.IDF_IDF, FeatureWeighting.NORMALIZATION_COSINE));		// ltc
		
		ArrayList<IFilter> filters = new ArrayList<IFilter>();
		filters.add(new FilterByRankedTW());
		filters.add(new FilterByRankedIG());
		
		ArrayList<ClassifierTrainer<? extends Classifier>> classifiers = new ArrayList<ClassifierTrainer<? extends Classifier>>();
		classifiers.add(new NaiveBayesTrainer());
		
		int step = 5;
		int folds = 10;
		
		ExecutionUtils.runWeightersFiltersClassifiers(files, transformers, filters, classifiers, step, folds);
	}
}
