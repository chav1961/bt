package chav1961.bt.etc;

public class Shpoor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final float[][]	matrix1 = new float[][] {{1,2},{3,4}};
		final float[][]	matrix2 = new float[][] {{0,5},{6,7}};
		final float[][]	matrix3 = new float[4][4];

		for(int i = 0; i < matrix1.length; i++) {
			for(int j = 0; j < matrix1[i].length; j++) {
				for(int ii = 0; ii < matrix2.length; ii++) {
					for(int jj = 0; jj < matrix2[ii].length; jj++) {
						final int	iIndex = i*matrix1.length + ii; 
						final int	jIndex = j*matrix1[i].length + jj; 
						
						matrix3[iIndex][jIndex] = matrix1[i][j] * matrix2[ii][jj]; 
					}
				}
			}
		}
		System.err.println("Completed: ");
		
		// 0, 5, 0, 10,
		// 6, 7, 12, 14,
		// 0, 15, 0, 20,
		// 18, 21, 24, 28
	}
}
