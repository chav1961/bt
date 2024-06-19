package chav1961.bt.etc;

import java.util.Arrays;

import chav1961.purelib.basic.Utils;

public class Test5 {
	public static void main(final String[] args) {
		final float[][]	source = new float[][]{new float[] {1, -2, 3}, new float[] {0, 4, -1}, new float[] {5, 0, 0}};
		inversion(source);
		
		for (float[] item : source) {
			System.err.println(Arrays.toString(item));
		}
	}
	
	public static void inversion(final float source[][]) {
		final int 		n	= source.length;
        final double  	identity[] = new double[n*n];
        double 			temp;

        Utils.fillArray(identity, 0f);
        for (int i = 0; i < n; i++) {		// Create identity matrix
           	identity[n*i+i] = 1f;
		}
        
        for (int k = 0; k < n; k++) {
            temp = 1.0/source[k][k];
            for (int j = 0; j < n; j++) {	// normalize selected line by [k][k] value
                source[k][j] *= temp;
                identity[n*k+j] *= temp;
            }
            
            for (int i = 0; i < n; i++) {	// Make zeroes on all [k] column except current [k][k]
            	if (i != k) {
	                temp = source[i][k];
	                for (int j = 0; j < n; j++) {
	                    source[i][j] -= source[k][j] * temp;
	                    identity[n*i+j] -= identity[n*k+j] * temp;
	                }
            	}
            }
        }
        
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < n; j++) {
        		source[i][j] = (float)identity[n*i+j]; 
        	}
        }
    }	
}
