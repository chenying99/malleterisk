/* Copyright (C) 2006 University of Pennsylvania.
 This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
 http://www.cs.umass.edu/~mccallum/mallet
 This software is provided under the terms of the Common Public License,
 version 1.0, as published by http://www.opensource.org.  For further
 information, see the file `LICENSE' included with this distribution. */

package struct.types;

/** A generic label.
 * 
 * @version 08/15/2006
 */
public interface SLLabel {

	/** Returns the feature-vector representation
	 * of this label.
	 */
    public SLFeatureVector getFeatureVectorRepresentation();

    /** Evaluates a prediction against this label.
     */
    public double loss(SLLabel pred);

    //public int hammingDistance(Label pred);

    /** 1 - hammingDistance */
    //public int correct(Label pred);
}
