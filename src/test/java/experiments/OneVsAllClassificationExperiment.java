package experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import classifiers.LibLinearTrainer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import types.mallet.LabeledInstancesList;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import execution.ExecutionResult;
import execution.ExecutionUtils;

public class OneVsAllClassificationExperiment {
	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<File> files = new ArrayList<File>();
		files.add(new File("instances+1+1+body"));
//		files.add(new File("instances+2+2+body"));
//		files.add(new File("instances+2+3+body"));
//		files.add(new File("instances+2+4+body"));
//		files.add(new File("instances+2+5+body"));
//		files.add(new File("instances+2+6+body"));
//		files.add(new File("instances+2+7+body"));
//		files.add(new File("instances+2+8+body"));
		
		for (File file : files) {
			InstanceList instancelist = InstanceList.load(file);
			OneVersusAll ova = new OneVersusAll(instancelist);
			
			while(ova.hasNext()) {
				
				ExecutionResult r = new ExecutionResult(file.getName(), null, null, "");
				r.trials.put(0, ExecutionUtils.crossValidate(ova.next(), 10, new LibLinearTrainer()));

				r.outputTrials();
				r.outputAccuracies();
			}
		}
	}
}

/**
 * Implements the One vs All strategy for classification.
 * The collection is iterated and each iteration, a different class is picked as the "One",
 * while the others are transformed into "All" (or rest).
 * 
 * @author tt
 *
 */
class OneVersusAll implements Iterable<InstanceList>, Iterator<InstanceList> {
	private final InstanceList instances;
	private final InstanceList[] instanceLists;
	private int currentOneClassIndex;

	public OneVersusAll(InstanceList instances) {
		this.instances = instances;

		this.instanceLists = new LabeledInstancesList(instances).getInstanceLists();
		this.currentOneClassIndex = 0;
	}

	public Object getCurrentOneClass() {
		return instances.getTargetAlphabet().lookupObject(currentOneClassIndex-1);
	}

	@Override
	public boolean hasNext() {
		return currentOneClassIndex < instanceLists.length;
	}

	@Override
	public InstanceList next() {
		// create new instancelist with changed target alphabet (empty for now, will be filled internally)
		Alphabet features = this.instances.getDataAlphabet();
		Alphabet labels = this.instances.getTargetAlphabet();
		LabelAlphabet newLabels = new LabelAlphabet();
		InstanceList newInstances = new InstanceList(features, newLabels);

		// get the "current" label
		Object currentLabel = labels.lookupObject(currentOneClassIndex++);
		Object labelOne = newLabels.lookupLabel(currentLabel, true);
		Object labelAll = newLabels.lookupLabel(-1, true);

		// set the new targets for all the instances
		for (Instance instance : this.instances) {
			newInstances.add(new Instance(
				instance.getData(), 
				((Label) instance.getTarget()).getEntry().equals(currentLabel) ? labelOne : labelAll, 
				instance.getName(), 
				instance.getSource())
			);
		}

		return newInstances;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<InstanceList> iterator() {
		return this;
	}
}
