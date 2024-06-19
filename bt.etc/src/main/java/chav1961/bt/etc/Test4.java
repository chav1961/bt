package chav1961.bt.etc;

public class Test4 {
    public static void main(String[] args) {
        int n = 1000;
        float mA[] = new float[n*n];
        float mB[] = new float[n*n];
        float res[] = new float[n*n];
        for (int i=0; i<n*n; i++) {
            mA[i] = i;
            mB[i] = i;
        }

        final long	time=  System.nanoTime();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    res[n*i+j] += mA[n*i+k] * mB[n*k+j]; 
                }
            }
        }
        System.err.print("Duration="+(System.nanoTime()-time));
    }
}