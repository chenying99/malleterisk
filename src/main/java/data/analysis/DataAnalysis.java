package data.analysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import types.email.IEmailMessage;
import types.email.IEmailParticipant;
import types.mallet.LabeledInstancesList;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.SparseVector;

public class DataAnalysis {
	
	// features
	
	public static int numTerms(InstanceList il) {
		return il.getDataAlphabet().size();
	}
	
	public static Map<Object, Double> termOccurrences(InstanceList il) {
		Alphabet dataAlphabet = il.getDataAlphabet();
		SparseVector sv = new SparseVector(new double[dataAlphabet.size()]);

		for (Instance i : il)
			sv.plusEqualsSparse((FeatureVector) i.getData());
		
		Map<Object, Double> counts = new HashMap<Object, Double>();

		int[] indices = sv.getIndices();
		for (int idx : indices)
			counts.put(dataAlphabet.lookupObject(idx), sv.value(idx));
		
		return counts;
	}	
	
	public static double averageTermsPerDocument(InstanceList il) {
		double i = 0;
		
		for (Instance instance : il)
			i +=  ((FeatureVector)instance.getData()).getIndices().length;
		
		return i / (double)il.size();
	}
	
	public static int minNumTermsInDocuments(InstanceList il) {
		int m = -1;
		
		for (Instance instance : il) {
			int i = ((FeatureVector) instance.getData()).getIndices().length;
			if(m == -1 || m > i) m = i;
		}
		
		return m;
	}
	
	public static int maxNumTermsInDocument(InstanceList il) {
		int m = 0;
		
		for (Instance instance : il) {
			int i = ((FeatureVector) instance.getData()).getIndices().length;
			if(i > m) m = i;
		}
		
		return m;
	}
	
	public static Map<Object, Double> averageTermsPerClass(LabeledInstancesList lil) {
		Map<Object, Double> avg = new HashMap<Object, Double>();
		
		// average length of messages per class
		for(int i=0; i<lil.getNumLabels(); ++i)
			avg.put(lil.getLabel(i), averageTermsPerDocument(lil.getLabelInstances(i)));
		
		return avg;
	}
	
	public static Map<Object, SparseVector> termClassFrequencies(LabeledInstancesList lil) {
		Map<Object, SparseVector> tcf = new HashMap<Object, SparseVector>();
		
		InstanceList[] instancelists = lil.getInstanceLists();
		InstanceList il = null;
		for(int i=0; i<lil.getNumLabels(); ++i) {
			il = instancelists[i];
			
			SparseVector sv = new SparseVector(new double[il.getDataAlphabet().size()]);
			for (Instance inst : il)
				sv.plusEqualsSparse((FeatureVector) inst.getData());
			
			tcf.put(lil.getLabel(i), sv);
		}
		
		return tcf;
	}
	
	public static void termClassSparsity() {
		
	}
	
	// documents / classes
	
	public static int numClasses(InstanceList il) {
		return il.getTargetAlphabet().size();
	}
	
	public static int numDocuments(InstanceList il) {
		return il.size();
	}

	public static double averageDocumentsPerClass(InstanceList il) {
		return (double)il.size()/(double)il.getTargetAlphabet().size();
	}
	
	public static int minNumDocumentsInClass(LabeledInstancesList lil) {
		int c = -1;

		for (InstanceList instances : lil.getInstanceLists()) {
			int i = instances.size();
			if(c==-1 || c > i) c = i;
		}

		return c;
	}
	
	public static int maxNumDocumentsInClass(LabeledInstancesList lil) {
		int c = 0;

		for (InstanceList instances : lil.getInstanceLists()) {
			int i = instances.size();
			if(i > c) c = i;
		}

		return c;
	}
	
	public static Map<Object, Integer> docClassDistribution(LabeledInstancesList lil) {
		Map<Object, Integer> hg = new HashMap<Object, Integer>();
		
		// number of documents per class
		for(int i=0; i<lil.getNumLabels(); ++i)
			hg.put(lil.getLabel(i), lil.getNumLabelInstances(i));
		
		return hg;
	}
	
	// participants
	
	public static Map<Object, Set<IEmailParticipant>> labelsUniqueParticipants(LabeledInstancesList lil) {
		// label - {unique participants}
		Map<Object, Set<IEmailParticipant>> lp = new HashMap<Object, Set<IEmailParticipant>>();
		
		for(int i=0; i<lil.getNumLabels(); ++i) {
			// retrieve participants
			Object lbl = lil.getLabel(i);
			Set<IEmailParticipant> participants = lp.get(lbl);
			if(participants==null) {
				participants = new HashSet<IEmailParticipant>();
				lp.put(lbl, participants);
			}
			
			// add unique participants
			InstanceList il = lil.getLabelInstances(i);
			for (Instance instance : il) {
				Object o = instance.getData();
				IEmailMessage m = (IEmailMessage) o;
				participants.addAll(m.getParticipants());
			}
		}
		
		return lp;
	}
	
	public static int totalUniqueParticipants(Map<Object, Set<IEmailParticipant>> lp) {
		// total unique participants in set
		Set<IEmailParticipant> p = new HashSet<IEmailParticipant>();
		
		for (Entry<Object, Set<IEmailParticipant>> entry : lp.entrySet())
			p.addAll(entry.getValue());
		
		return p.size();
	}
	
	public static Map<Object, Integer> labelsNumParticipants(Map<Object, Set<IEmailParticipant>> lp) {
		// just aggregates labelsUniqueParticipants
		Map<Object, Integer> hg = new HashMap<Object, Integer>();
				
		for (Entry<Object, Set<IEmailParticipant>> entry : lp.entrySet())
			hg.put(entry.getKey(), entry.getValue().size());
		
		return hg;
	}
	
	public static Map<Set<Integer>, Set<String>> participantsClassesCorrelation(InstanceList pil) {
		// find a correlation between participants and labels
		// participants are treated as groups (unique set of participants)
		
		Map<Set<Integer>, Set<String>> groupsLabels = new HashMap<Set<Integer>, Set<String>>();
		
		// iterate all instances and build the map<groups, labels> that associates
		// the unique groups to a set of classes
		for (Instance inst : pil) {
			IEmailMessage m = (IEmailMessage) inst.getData();
			
			// get the group of the message
			Set<Integer> g = new HashSet<Integer>();
			for (IEmailParticipant p : m.getParticipants()) // I could/should implement equals/hashcode on
				g.add(p.getParticipantId());				// EmailParticipant to avoid doing this
			
			// create and add if not exists
			Set<String> l;
			if((l = groupsLabels.get(g)) == null) {
				l = new HashSet<String>();
				groupsLabels.put(g, l);
			}
			
			// associate this class to the group
			l.add(inst.getTarget().toString());
		}
		
		return groupsLabels;
	}
	
	public static void printParticipantsClassesRatio(Map<Set<Integer>, Set<String>> groupsLabels) {
		int i = 1;
		for (Entry<Set<Integer>, Set<String>> entry : groupsLabels.entrySet()) {
			System.out.print((i++) + ": [ ");
			for (Integer p : entry.getKey())
				System.out.print(p + " ");
			System.out.print("]");

			System.out.print("\t\t\t");
			
			System.out.print("[ ");
			for (String l : entry.getValue())
				System.out.print(l + " ");
			System.out.print("]");
			
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println("#unique entries: " + groupsLabels.size()); // number of unique groups
	}
	
	public static double participantsClassesRatio(Map<Set<Integer>, Set<String>> groupsLabels) {
		double count = 0;
		for (Entry<Set<Integer>, Set<String>> entry : groupsLabels.entrySet()) 
			if(entry.getValue().size() == 1)
				count++;
		
//		System.out.println(count);
//		System.out.println(groupsClasses.entrySet().size());
		
		return count / (double) groupsLabels.entrySet().size();
	}
	
	// date
	
	public static Map<Object, TimeInterval> timeClassIntervals(LabeledInstancesList lil) {
		// instance's data must be of date type
		
		// time period for classes
		Map<Object, TimeInterval> hg = new HashMap<Object, TimeInterval>();
		
		for(int i=0; i<lil.getNumLabels(); ++i) {
			InstanceList il = lil.getLabelInstances(i);
			
			Date earliest = null, latest = null;
			for (Instance instance : il) {
				Date d = (Date) instance.getData();
				
				if(earliest == null && latest == null) earliest = latest = d;
				
				else if(d.compareTo(latest) == 1) latest = d;
				else if(d.compareTo(earliest) == -1) earliest = d;
			}
			
			hg.put(lil.getLabel(i), new TimeInterval(earliest, latest));
		}
		
		return hg;
	}
	
	public static Map<Month, Map<Object, Integer>> timeClassFrequencies(LabeledInstancesList lil) {
		Map<Month, Map<Object, Integer>> hg = new HashMap<Month, Map<Object, Integer>>();

		final SimpleDateFormat MONTH_SDF = new SimpleDateFormat("MM");
		final SimpleDateFormat YEAR_SDF = new SimpleDateFormat("yyyy");
		
		for(int i=0; i<lil.getNumLabels(); ++i) {
			InstanceList il = lil.getLabelInstances(i);
			
			for (Instance instance : il) {
				Date d = (Date) instance.getData();
				Month m = new Month(MONTH_SDF.format(d), YEAR_SDF.format(d));
				
				Map<Object, Integer> counts = hg.get(m);
				if(counts == null) {
					counts = new HashMap<Object, Integer>();
					hg.put(m, counts);
				}
				
				Object lbl = instance.getTarget();
				Integer cnt = counts.containsKey(lbl) ? counts.get(lbl) : 0;
				counts.put(lbl, cnt+1);
			}
		}
		
		return hg;
	}
	
	// utils
	
	public static void mapToCSV(String filename, Map<?, ?> data) throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(filename);
		PrintWriter pw = new PrintWriter(out);
		
		for (Entry<?, ?> entry : data.entrySet()) {
			pw.write(entry.getKey().toString());
			pw.write(", ");
			pw.write(entry.getValue().toString());
			pw.write('\n');
		}

		pw.flush();
		pw.close();
	}
	
	public static void mapMapToCSV(String filename, Map<Month, Map<Object, Integer>> map) throws FileNotFoundException {
		// for dates
		FileOutputStream out = new FileOutputStream(filename);
		PrintWriter pw = new PrintWriter(out);
		
		for (Entry<Month, Map<Object, Integer>> entry : map.entrySet()) {
			Map<Object, Integer> values = entry.getValue();
			for (Entry<Object, Integer> cf : values.entrySet()) {
				pw.write(entry.getKey().toString());
				pw.write(", ");
				pw.write(cf.getKey().toString());
				pw.write(", ");
				pw.write(cf.getValue().toString());
				pw.write('\n');
			}
		}

		pw.flush();
		pw.close();
	}
}

class TimeInterval {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
	
	public final Date startDate;
	public final Date endDate;
	
	public TimeInterval(Date sd, Date ed) {
		this.startDate = sd;
		this.endDate = ed;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(SDF.format(startDate));
		sb.append(", ");
		sb.append(SDF.format(endDate));
		
		return sb.toString();
	}
}
