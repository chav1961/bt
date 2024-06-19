package chav1961.bt.etc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import chav1961.purelib.basic.Utils;

public class KepstrTest {
	private static final int	NUMBER_OF_PIECES = 128; 
	private static final int	NUMBER_OF_MKK = 24; 

	// https://habr.com/ru/articles/144491/
	
	public static void main(String[] args) throws IOException {
		final double[] content; 
		
		try(final InputStream is = KepstrTest.class.getResourceAsStream("coin8.raw");
			final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			content = normalize(toShort(baos.toByteArray()));
		}
		
		final double[][] pieces = split(content, NUMBER_OF_PIECES);
		
		for(int index = 0; index < pieces.length; index++) {
			pieces[index] = discreteFourierTransform(pieces[index]);
			for(int pos = 0; pos < pieces[index].length; pos++) {
				pieces[index][pos] = freq2MEL(pieces[index][pos]);
			}
		}
		
		final double[][] mels = new double[pieces.length][];
		
		for(int index = 0; index < pieces.length; index++) {
			mels[index] = getMKK(pieces[index], NUMBER_OF_MKK);
		}
	}

	private static short[] toShort(final byte[] byteArray) throws IOException {
		final short[] result = new short[(byteArray.length + 1)/2];
		
		try(final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
			final DataInputStream dis = new DataInputStream(bais)) {

			for(int index = 0; index < result.length; index++) {
				result[index] = dis.readShort();
			}
		}
		return result;
	}

	private static double[] normalize(final short[] source) {	// https://ru.wikipedia.org/wiki/%D0%9D%D0%BE%D1%80%D0%BC%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F_%D0%B7%D0%B2%D1%83%D0%BA%D0%B0
		final double[] result = new double[source.length];
		double maxValue = 0;
		
		for(short val : source) {
			maxValue = Math.max(maxValue, Math.abs(val));
		}
		for(int index = 0; index < result.length; index++) {
			result[index] = source[index] / maxValue;
		}
		return result;
	}
	
	private static double[][] split(final double[] source, final int pieceSize) {
		final int numberOfPieces = source.length / pieceSize;
		final double[][] result = new double[numberOfPieces][];
		int startPiece = -pieceSize/2;
		
		for(int index = 0; index < numberOfPieces; index++) {
			final double[] temp = new double[pieceSize];
			
			for (int pos = 0; pos < pieceSize; pos++) {
				if (startPiece + pos < 0 || startPiece + pos >= source.length) {
					temp[pos] = 0;
				}
				else {
					temp[pos] = hammingWeight(pos, source[startPiece+pos], pieceSize);
				}
			}
			result[index] = temp;
			startPiece += pieceSize;
		}
		return result;
	}

	private static double hammingWeight(final int sequentialNumber, final double value, final int pieceSize) {
		return 0.53836 + 0.46164 * value * Math.cos(Math.PI * sequentialNumber / (pieceSize - 1)); 
	}
	
	private static double[] discreteFourierTransform(final double[] source) {
		final double[] result = new double[source.length];
		int i = 0;
		
		for(int index = 0; index < source.length; index++) {
			double sum = 0;
			
			for(int pos = 0; pos < source.length; pos++) {
				sum += source[pos] * Math.exp(-2 * Math.PI * i * index * pos / source.length);				
			}
			result[index] = sum;
		}
		return result;
	}
	
	private static double freq2MEL(final double freq) {
		return 1127 * Math.log(1 + freq/700);
	}	
	
	private static double[] getMKK(final double[] source, final int numberOfMKK) {
		final double[] result = new double[numberOfMKK];
		
		for(int index = 0; index < numberOfMKK; index++) {
			double sum = 0;
			
			for(int pos = 0; pos < source.length; pos++) {
				sum += Math.log(Math.PI * index * (pos - 0.5) * numberOfMKK);
			}
			result[index] = sum;
		}
		return result;
	}
}
