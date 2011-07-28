/* Copyright (C) 2006 University of Pennsylvania.
 This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
 http://www.cs.umass.edu/~mccallum/mallet
 This software is provided under the terms of the Common Public License,
 version 1.0, as published by http://www.opensource.org.  For further
 information, see the file `LICENSE' included with this distribution. */

package struct.solver;

/** QPOutput.
 * 
 * @version 07/15/2006
 */
public class QPOutput {
	
	public double[] alpha;
	public double[] w1;
	public double[] w2;
	
	public int dim1;
	public int dim2;
	
	
	public QPOutput() {
		alpha=null; 
		w1=null; 
		w2=null;
		dim1=-1; dim2=-1;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		if (w1 != null)
			str.append("w1 length : " + w1.length  + " \n");
		else
			str.append("w1 is null\n");
		
		if (w2 != null)
			str.append("w2 length : " + w2.length + " \n");
		else
			str.append("w2 is null\n");		
		
		if (alpha != null)
			str.append("alpha length : " + alpha.length + " \n");
		else
			str.append("alpha is null\n");		
		
		str.append("dim1 : " + String.valueOf(dim1) + "\n");
		str.append("dim2 : " + String.valueOf(dim2) + "\n");
		return (str.toString());		
	}
};
