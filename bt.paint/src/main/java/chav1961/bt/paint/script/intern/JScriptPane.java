package chav1961.bt.paint.script.intern;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.bt.paint.script.intern.interfaces.LexTypes;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.Lexema;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.ui.HighlightItem;
import chav1961.purelib.ui.swing.useful.JTextPaneHighlighter;

public class JScriptPane extends JTextPaneHighlighter<LexTypes> {
	private static final long serialVersionUID = 1L;

	public JScriptPane() {
		final SimpleAttributeSet	sas = new SimpleAttributeSet();
	
		StyleConstants.setBold(sas,false);
		StyleConstants.setItalic(sas,false);
		StyleConstants.setForeground(sas,Color.RED);
		characterStyles.put(LexTypes.EOF,sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.BLUE);
		characterStyles.put(LexTypes.PART, sas);

		StyleConstants.setBold(sas, true);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.BLUE);
		characterStyles.put(LexTypes.STATEMENT, sas);

		StyleConstants.setBold(sas, true);
		StyleConstants.setItalic(sas, true);
		StyleConstants.setForeground(sas, Color.BLUE);
		characterStyles.put(LexTypes.STATEMENT, sas);

		StyleConstants.setBold(sas, true);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.BLACK);
		characterStyles.put(LexTypes.OPTION, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.DARK_GRAY);
		characterStyles.put(LexTypes.OPTION, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, true);
		StyleConstants.setForeground(sas, Color.DARK_GRAY);
		characterStyles.put(LexTypes.OPTION, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.GREEN);
		characterStyles.put(LexTypes.OPTION, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.DARK_GRAY);
		characterStyles.put(LexTypes.OPERATOR, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.OPEN, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.CLOSE, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.OPENB, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.CLOSEB, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.OPENF, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.LIGHT_GRAY);
		characterStyles.put(LexTypes.CLOSEF, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.COMMA, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.DOT, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.RANGE, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.COLON, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.CAST, sas);

		StyleConstants.setBold(sas, false);
		StyleConstants.setItalic(sas, false);
		StyleConstants.setForeground(sas, Color.CYAN);
		characterStyles.put(LexTypes.SEMICOLON, sas);
	}
	
	@Override
	protected HighlightItem<LexTypes>[] parseString(final String program) {
		try(final Reader						rdr = new StringReader(program)) {
			final List<HighlightItem<LexTypes>>	result = new ArrayList<>();
			final List<Lexema>					lex = ScriptParserUtil.parseLex(rdr, new OrdinalSyntaxTree<LexTypes>(), true);
			final Lexema[]						content = lex.toArray(new Lexema[lex.size()]);
			
			for(int index = 0; index < content.length -1; index++) {
				result.add(new HighlightItem<LexTypes>(content[index].getDispl() + content[index].getCol(), content[index + 1].getDispl() + content[index + 1].getCol(), content[index].getType()));
			}
			return result.toArray(new HighlightItem[result.size()]);
		} catch (IOException | SyntaxException e) {
			return new HighlightItem[0];
		}
	}
}
