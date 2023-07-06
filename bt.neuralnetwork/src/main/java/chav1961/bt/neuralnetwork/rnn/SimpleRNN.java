package chav1961.bt.neuralnetwork.rnn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.bt.neuralnetwork.math.NNMath;
import chav1961.bt.neuralnetwork.math.NNMath.RandomType;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableFloatArray;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

// https://bookflow.ru/vvedenie-v-rnn-rekurrentnye-nejronnye-seti-dlya-nachinayushhih/
public class SimpleRNN implements JsonSerializable<SimpleRNN> {
	private static final String	INPUT_VECTOR_SIZE = "inputVectorSize";
	private static final String	HIDDEN_VECTOR_SIZE = "hiddenVectorSize";
	private static final String	OUTPUT_VECTOR_SIZE = "outputVectorSize";
	private static final String	INPUT_HIDDEN_WEIGHT = "inputHiddenWeight";
	private static final String	HIDDEN_HIDDEN_WEIGHT = "hiddenHiddenWeight";
	private static final String	HIDDEN_OUTPUT_WEIGHT = "hiddenOutputWeight";
	private static final String	HIDDEN_DISPLACEMENT = "hiddenDisplacement";
	private static final String	OUTPUT_DISPLACEMENT = "outputDisplacement";

	private int			inputVectorSize = 0;
	private int			hiddenVectorSize = 0;
	private int			outputVectorSize = 0;
    private float[] 	inputHiddenWeight = null;
    private float[] 	hiddenHiddenWeight = null;
    private float[] 	hiddenOutputWeight = null;
    private float[] 	hiddenDisplacement = null;
    private float[] 	outputDisplacement = null;
    private float[][]	hiddens;
    private boolean	prepared = false;
    
	public SimpleRNN() {
		
	}

	@Override
	public void fromJson(final JsonStaxParser parser) throws SyntaxException, IOException {
		// TODO Auto-generated method stub
		if (parser == null) {
			throw new NullPointerException("Parser can't be null"); 
		}
		else {
			final GrowableFloatArray	gfa = new GrowableFloatArray(false);
			String 						name = "";
			int 						forInputVectorSize = 0, forHiddenVectorSize = 0, forOutputVectorSize = 0;
			float[]						forInputHiddenWeight = null, forHiddenHiddenWeight = null, forHiddenOutputWeight = null;   
			float[]						forHiddenDisplacement = null, forOutputDisplacement = null;   
			int 						depth = 0;
			
			for (JsonStaxParserLexType type : parser) {
				switch (type) {
					case INTEGER_VALUE	:
						if (depth == 1) {
							switch (name) {
								case INPUT_VECTOR_SIZE	:
									forInputVectorSize = (int)parser.intValue();
									break;
								case HIDDEN_VECTOR_SIZE	:
									forHiddenVectorSize = (int)parser.intValue();
									break;
								case OUTPUT_VECTOR_SIZE	:
									forOutputVectorSize = (int)parser.intValue();
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported field name"); 
							}
						}
						break;
					case LIST_SPLITTER	:
						if (depth != 1 && depth != 2) {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case NAME			:
						if (depth == 1) {
							name = parser.name();
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal name in the input"); 
						}
						break;
					case NAME_SPLITTER	:
						if (depth != 1) {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case REAL_VALUE		:
						if (depth == 2) {
							gfa.append((float)parser.realValue());
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal name in the input"); 
						}
						break;
					case START_ARRAY	:
						if (depth == 1) {
							depth = 2;
							gfa.clear();
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case END_ARRAY		:
						if (depth == 2) {
							depth = 1;
							switch (name) {
								case INPUT_HIDDEN_WEIGHT 	:
									forInputHiddenWeight = gfa.extract();
									break;
								case HIDDEN_HIDDEN_WEIGHT 	:
									forHiddenHiddenWeight = gfa.extract();
									break;
								case HIDDEN_OUTPUT_WEIGHT 	:
									forHiddenOutputWeight = gfa.extract();
									break;
								case HIDDEN_DISPLACEMENT 	:
									forHiddenDisplacement = gfa.extract();
									break;
								case OUTPUT_DISPLACEMENT 	:
									forOutputDisplacement = gfa.extract();
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported field name"); 
							}
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case START_OBJECT	:
						if (depth == 0) {
							depth = 1;
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case END_OBJECT		:
						if (depth == 1) {
							depth = 0;
						}
						else {
							throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
						}
						break;
					case ERROR : case STRING_VALUE : case NULL_VALUE : case BOOLEAN_VALUE :
					default:
						throw new SyntaxException(parser.row(), parser.col(), "Illegal lexema in the input source"); 
				}
			}
			checkValue(inputVectorSize, INPUT_VECTOR_SIZE);
			checkValue(hiddenVectorSize, HIDDEN_VECTOR_SIZE);
			checkValue(outputVectorSize, OUTPUT_VECTOR_SIZE);
			checkArray(forInputHiddenWeight, inputVectorSize * hiddenVectorSize, INPUT_HIDDEN_WEIGHT);
			checkArray(forHiddenHiddenWeight, hiddenVectorSize * hiddenVectorSize, HIDDEN_HIDDEN_WEIGHT);
			checkArray(forHiddenOutputWeight, hiddenVectorSize * outputVectorSize, HIDDEN_OUTPUT_WEIGHT);
			checkArray(forHiddenDisplacement, hiddenVectorSize, HIDDEN_DISPLACEMENT);
			checkArray(forOutputDisplacement, outputVectorSize, OUTPUT_DISPLACEMENT);
			
			this.inputVectorSize = forInputVectorSize;
			this.hiddenVectorSize = forHiddenVectorSize;
			this.outputVectorSize = forOutputVectorSize;
			this.inputHiddenWeight = forInputHiddenWeight;
			this.hiddenHiddenWeight = forHiddenHiddenWeight;
			this.hiddenOutputWeight = forHiddenOutputWeight;
			this.hiddenDisplacement = forHiddenDisplacement;
			this.outputDisplacement = forOutputDisplacement;
			this.prepared = true;
		}
	}

	private void checkValue(final int size, final String fieldName) throws SyntaxException {
		if (size <= 0) {
			throw new SyntaxException(0, 0, "Field ["+fieldName+"] is missing or has illegal value ["+size+"], must be greater than 0");
		}
	}

	private void checkArray(final float[] array, final int size, final String fieldName) throws SyntaxException {
		if (array == null) {
			throw new SyntaxException(0, 0, "Field ["+fieldName+"] is missing");
		}
		else if (array.length != size) {
			throw new SyntaxException(0, 0, "Field ["+fieldName+"] has wring number if items ["+array.length+"], must be ["+size+"]");
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (prepared) {
			throw new IllegalStateException("Unprepared network can't be unloaded. Call pepare(...) before"); 
		}
		else if (printer == null) {
			throw new NullPointerException("Printer can't be null");
		}
		else {
			printer.startObject();
				printer.name(INPUT_VECTOR_SIZE).value(inputVectorSize).splitter();
				printer.name(HIDDEN_VECTOR_SIZE).value(hiddenVectorSize).splitter();
				printer.name(OUTPUT_VECTOR_SIZE).value(outputVectorSize).splitter();
				printer.name(INPUT_HIDDEN_WEIGHT).startArray();
				for(int index = 0; index < inputHiddenWeight.length; index++) {
					if (index > 0) {
						printer.splitter();
					}
					printer.value(inputHiddenWeight[index]);
				}
				printer.endArray().splitter();
				printer.name(HIDDEN_HIDDEN_WEIGHT).startArray();
				for(int index = 0; index < hiddenHiddenWeight.length; index++) {
					if (index > 0) {
						printer.splitter();
					}
					printer.value(hiddenHiddenWeight[index]);
				}
				printer.endArray().splitter();
				printer.name(HIDDEN_OUTPUT_WEIGHT).startArray();
				for(int index = 0; index < hiddenOutputWeight.length; index++) {
					if (index > 0) {
						printer.splitter();
					}
					printer.value(hiddenOutputWeight[index]);
				}
				printer.endArray().splitter();
				printer.name(HIDDEN_DISPLACEMENT).startArray();
				for(int index = 0; index < hiddenDisplacement.length; index++) {
					if (index > 0) {
						printer.splitter();
					}
					printer.value(hiddenDisplacement[index]);
				}
				printer.endArray().splitter();
				printer.name(OUTPUT_DISPLACEMENT).startArray();
				for(int index = 0; index < outputDisplacement.length; index++) {
					if (index > 0) {
						printer.splitter();
					}
					printer.value(outputDisplacement[index]);
				}
				printer.endArray();
			printer.endObject();
			printer.flush();
		}
	}

	public void prepare(final int inputVectorSize, final int hiddenVectorSize, final int outputVectorSize) {
		if (inputVectorSize <= 0) {
			throw new IllegalArgumentException("Input vector size must be greater than 0");
		}
		else if (hiddenVectorSize <= 0) {
			throw new IllegalArgumentException("Hidden vector size must be greater than 0");
		}
		else if (outputVectorSize <= 0) {
			throw new IllegalArgumentException("Output vector size must be greater than 0");
		}
		else {
			this.inputVectorSize = inputVectorSize;
			this.hiddenVectorSize = hiddenVectorSize;
			this.outputVectorSize = outputVectorSize;
			this.inputHiddenWeight = new float[inputVectorSize * hiddenVectorSize];
			this.hiddenHiddenWeight = new float[hiddenVectorSize * hiddenVectorSize];
			this.hiddenOutputWeight = new float[hiddenVectorSize * outputVectorSize];
			this.hiddenDisplacement = new float[hiddenVectorSize];
			this.outputDisplacement = new float[outputVectorSize];
			
			NNMath.random(RandomType.UNIFORM, inputHiddenWeight);
			NNMath.random(RandomType.UNIFORM, hiddenHiddenWeight);
			NNMath.random(RandomType.UNIFORM, hiddenOutputWeight);
			this.prepared = true;
		}
	}
	
	public float[] forward(final float[][] source) {
		if (!prepared) {
			throw new IllegalStateException("Netword in not prepared yet. Call prepare(...) before"); 
		}
		else if (source == null || source.length != inputVectorSize) {
			throw new IllegalArgumentException("Source vector is null or have size differ to awaited size ["+inputVectorSize+"]");
		}
		else {
			final float[] result = new float[outputVectorSize];
			
			forward(source, result);
			return result;
		}
	}

	public void forward(final float[][] source, final float[] target) {
		if (!prepared) {
			throw new IllegalStateException("Netword in not prepared yet. Call prepare(...) before"); 
		}
		else if (source == null || source.length != inputVectorSize) {
			throw new IllegalArgumentException("Source vector is null or have size differ to awaited size ["+inputVectorSize+"]");
		}
		else if (target == null || target.length != outputVectorSize) {
			throw new IllegalArgumentException("Output vector is null or have size differ to awaited size ["+outputVectorSize+"]");
		}
		else {
			float[]	temp = null;
			
			hiddens = new float[source.length + 1][];
			hiddens[0] = new float[hiddenVectorSize];
			
			for(int index = 0; index < source.length; index++) {
				for(int item = 0; item < source[index].length; item++) {
					temp = NNMath.function(
							NNMath.matrixAdd(
									NNMath.matrixAdd(
										NNMath.matrixMul(inputHiddenWeight, hiddenVectorSize, inputVectorSize, source[index], inputVectorSize, 1),
										hiddenVectorSize, 
										1,
										NNMath.matrixMul(hiddenHiddenWeight, hiddenVectorSize, hiddenVectorSize, hiddens[index], hiddenVectorSize, 1)
									),
									hiddenVectorSize, 
									1,
									hiddenDisplacement
							),
							(t)->Math.tanh(t)
						);
					hiddens[index + 1] = temp;
				}
			}
			NNMath.matrixAdd(
				NNMath.matrixMul(hiddenOutputWeight, outputVectorSize, hiddenVectorSize, temp, hiddenVectorSize, 1),
				outputVectorSize, 
				1,
				outputDisplacement,
				target
			);
		}
	}
	
	public float[] backPropagation(final float[] error, final float rate) {
		final float[] inputHiddenDelta = new float[hiddenHiddenWeight.length];
		final float[] hiddenHiddenDelta = new float[hiddenHiddenWeight.length];
		final float[] hiddenDisplacementDelta = new float[hiddenDisplacement.length];

		final float[] hiddenOutputDelta = NNMath.matrixMul(error, outputVectorSize, 1, hiddens[hiddens.length-1], 1, hiddenVectorSize);
		final float[] outputDisplacementDelta = error.clone();
		final float[] hiddenDelta = NNMath.matrixMul(hiddenOutputWeight, outputVectorSize, hiddenVectorSize, error, hiddenVectorSize, 1);
		
		for(int index = hiddens.length - 1; index >= 0; index--) {
			final float[]	temp = NNMath.matrixMul(NNMath.function(hiddens[index], (t)->(1 - t*t)), 1, hiddenVectorSize, hiddenDelta, 1, hiddenVectorSize);
			
			NNMath.matrixAdd(outputDisplacementDelta, hiddenVectorSize, 1, temp);
			NNMath.matrixAdd(outputDisplacementDelta, hiddenVectorSize, 1, temp);
		}
		
		return null;
	}
	
/*	
	
	// Цикл тренировки
	
	for x, y in train_data.items():
	    inputs = createInputs(x)
	    target = int(y)

	    # Прямое распространение
	    out, _ = rnn.forward(inputs)
	    probs = softmax(out)

	    # Создание dL/dy
	    d_L_d_y = probs
	    d_L_d_y[target] -= 1

	    # Обратное распространение
	    rnn.backprop(d_L_d_y)
	    
	    
	    
	// Обратное распространение
	
	 def backprop(self, d_y, learn_rate=2e-2):
	        """
	        Выполнение фазы обратного распространения RNN.
	        - d_y (dL/dy) имеет форму (output_size), 1).
	        - learn_rate является вещественным числом float.
	        """
	        n = len(self.last_inputs)

	        # Вычисление dL/dWhy и dL/dby.
	        d_Why = d_y @ self.last_hs[n].T
	        d_by = d_y

	        # Инициализация dL/dWhh, dL/dWxh, и dL/dbh к нулю.
	        d_Whh = np.zeros(self.Whh.shape)
	        d_Wxh = np.zeros(self.Wxh.shape)
	        d_bh = np.zeros(self.bh.shape)

	        # Вычисление dL/dh для последнего h.
	        d_h = self.Why.T @ d_y

	        # Обратное распространение во времени.
	        for t in reversed(range(n)):
	            # Среднее значение: dL/dh * (1 - h^2)
	            temp = ((1 - self.last_hs[t + 1] ** 2) * d_h)

	            # dL/db = dL/dh * (1 - h^2)
	            d_bh += temp

	            # dL/dWhh = dL/dh * (1 - h^2) * h_{t-1}
	            d_Whh += temp @ self.last_hs[t].T

	            # dL/dWxh = dL/dh * (1 - h^2) * x
	            d_Wxh += temp @ self.last_inputs[t].T

	            # Далее dL/dh = dL/dh * (1 - h^2) * Whh
	            d_h = self.Whh @ temp

	        # Отсекаем, чтобы предотвратить разрыв градиентов.
	        for d in [d_Wxh, d_Whh, d_Why, d_bh, d_by]:
	            np.clip(d, -1, 1, out=d)

	        # Обновляем вес и смещение с использованием градиентного спуска.
	        self.Whh -= learn_rate * d_Whh
	        self.Wxh -= learn_rate * d_Wxh
	        self.Why -= learn_rate * d_Why
	        self.bh -= learn_rate * d_bh
	        self.by -= learn_rate * d_by
	
	
	// Обработка данных (тест)
	
	
	def processData(data, backprop=True):
	    """
	    Возврат потери рекуррентной нейронной сети и точности для данных
	    - данные представлены как словарь, что отображает текст как True или False.
	    - backprop определяет, нужно ли использовать обратное распределение
	    """
	    items = list(data.items())
	    random.shuffle(items)

	    loss = 0
	    num_correct = 0

	    for x, y in items:
	        inputs = createInputs(x)
	        target = int(y)

	        # Прямое распределение
	        out, _ = rnn.forward(inputs)
	        probs = softmax(out)

	        # Вычисление потери / точности 
	        loss -= np.log(probs[target])
	        num_correct += int(np.argmax(probs) == target)

	        if backprop:
	            # Создание dL/dy
	            d_L_d_y = probs
	            d_L_d_y[target] -= 1

	            # Обратное распределение
	            rnn.backprop(d_L_d_y)

	    return loss / len(data), num_correct / len(data)
	
	// тренировка
	for epoch in range(1000):
	    train_loss, train_acc = processData(train_data)

	    if epoch % 100 == 99:
	        print("--- Epoch %d" % (epoch + 1))
	        print("Train:\tLoss %.3f | Accuracy: %.3f" % (train_loss, train_acc))

	        test_loss, test_acc = processData(test_data, backprop=False)
	        print("Test:\tLoss %.3f | Accuracy: %.3f" % (test_loss, test_acc))
*/

	private static final Data[]	TRAINING_SET = {
									new Data("good", true),
									new Data("bad", false),
									new Data("happy", true),
									new Data("sad", false),
									new Data("not good", false),
									new Data("not bad", true),
									new Data("not happy", false),
									new Data("not sad", true),
									new Data("very good", true),
									new Data("very bad", false),
									new Data("very happy", true),
									new Data("very sad", false),
									new Data("i am happy", true),
									new Data("this is good", true),
									new Data("i am bad", false),
									new Data("this is bad", false),
									new Data("i am sad", false),
									new Data("this is sad", false),
									new Data("i am not happy", false),
									new Data("this is not good", false),
									new Data("i am not bad", true),
									new Data("this is not sad", true),
									new Data("i am very happy", true),
									new Data("this is very good", true),
									new Data("i am very bad", false),
									new Data("this is very sad", false),
									new Data("this is very happy", true),
									new Data("i am good not bad", true),
									new Data("this is good not bad", true),
									new Data("i am bad not good", false),
									new Data("i am good and happy", true),
									new Data("this is not good and not happy", false),
									new Data("i am not at all good", false),
									new Data("i am not at all bad", true),
									new Data("i am not at all happy", false),
									new Data("this is not at all sad", true),
									new Data("this is not at all happy", false),
									new Data("i am good right now", true),
									new Data("i am bad right now", false),
									new Data("this is bad right now", false),
									new Data("i am sad right now", false),
									new Data("i was good earlier", true),
									new Data("i was happy earlier", true),
									new Data("i was bad earlier", false),
									new Data("i was sad earlier", false),
									new Data("i am very bad right now", false),
									new Data("this is very good right now", true),
									new Data("this is very sad right now", false),
									new Data("this was bad earlier", false),
									new Data("this was very good earlier", true),
									new Data("this was very bad earlier", false),
									new Data("this was very happy earlier", true),
									new Data("this was very sad earlier", false),
									new Data("i was good and not bad earlier", true),
									new Data("i was not good and not happy earlier", false),
									new Data("i am not at all bad or sad right now", true),
									new Data("i am not at all good or happy right now", false),
									new Data("this was not happy and not good earlier", false)	
								};
	private static final Data[]	TEST_SET = {
									new Data("this is happy", true),
									new Data("i am good", true),
									new Data("this is not happy", false),
									new Data("i am not good", false),
									new Data("this is not bad", true),
									new Data("i am not sad", true),
									new Data("i am very good", true),
									new Data("this is very bad", false),
									new Data("i am very sad", false),
									new Data("this is bad not good", false),
									new Data("this is good and happy", true),
									new Data("i am not good and not happy", false),
									new Data("i am not at all sad", true),
									new Data("this is not at all good", false),
									new Data("this is not at all bad", true),
									new Data("this is good right now", true),
									new Data("this is sad right now", false),
									new Data("this is very bad right now", false),
									new Data("this was good earlier", true),
									new Data("i was not happy and not good earlier", false)			
								};
	
	private static final String[]	WORDS;
	
	static {
		final Set<String> temp = new HashSet<>();
		
		for(Data item : TRAINING_SET) {
			temp.addAll(Arrays.asList(item.word.split(" ")));
		}
		WORDS = temp.toArray(new String[temp.size()]);
	}
	
	public static void main(final String[] args) {
		final SimpleRNN	rnn = new SimpleRNN();
		
		rnn.prepare(WORDS.length, 100, 2);

		for(Data item : TRAINING_SET) {
			final float[][] source = createInputs(item.word); 
			final float[] result = NNMath.softmax(rnn.forward(source));
			
			result[item.marker ? 1 : 0] = -1;
			
			rnn.backPropagation(result, 2e-2f);
		}
		
		for(Data item : TEST_SET) {
			final float[][] source = createInputs(item.word); 
			final float[] result = NNMath.softmax(rnn.forward(source));
			
			System.err.println("Source: "+item.word+", result="+Arrays.toString(result));
		}		
	}
	
	private static float[][] createInputs(final String source) {
		final String[]	content = source.split(" ");
		final float[][]	result = new float[content.length][];
		
		for(int index = 0; index < result.length; index++) {
			result[index] = new float[WORDS.length];
			result[index][word2index(content[index])] = 1;
		}
		return result;
	}
	
	private static int word2index(final String word) {
		for(int index = 0; index < WORDS.length; index++) {
			if (WORDS[index].equals(word)) {
				return index;
			}
		}
		throw new IllegalArgumentException(); 
	}
	
	private static String index2word(final int index) {
		return WORDS[index];
	}
	
	static class Data {
		public final String		word;
		public final boolean	marker;

		public Data(String word, boolean marker) {
			this.word = word;
			this.marker = marker;
		}

		@Override
		public String toString() {
			return "Data [word=" + word + ", marker=" + marker + "]";
		}
	}
}
