package chav1961.bt.clipper.inner.streams;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class PreprocessingXReaderTest {
	@Test
	public void basicTest() throws IOException {
		Assert.assertEquals("test\ntest\n", readContent("test\ntest\n", Utils.mkMap()));
		Assert.assertEquals("X\n", readContent("#define X 10\nX\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, false)));
		Assert.assertEquals("10\n", readContent("#define X 10\nX\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true)));
		Assert.assertEquals("X\n", readContent("#define X 10\n#undef X\nX\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true)));
		Assert.assertEquals("presents\n", readContent("#define X 10\n#ifdef X\npresents\n#else\nmissing\n#end\n", Utils.mkMap()));
		Assert.assertEquals("missing\n", readContent("#define X 10\n#ifndef X\npresents\n#else\nmissing\n#end\n", Utils.mkMap()));
		Assert.assertEquals("presents\n/*missing\n*/\n", readContent("#define X 10\n#ifdef X\npresents\n#else\nmissing\n#end\n", Utils.mkMap(PreprocessingXReader.HIDING_METHOD, PreprocessingXReader.HidingMethod.MULTILINE_COMMENTED, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("presents\n//missing\n", readContent("#define X 10\n#ifdef X\npresents\n#else\nmissing\n#end\n", Utils.mkMap(PreprocessingXReader.HIDING_METHOD, PreprocessingXReader.HidingMethod.SINGLE_LINE_COMMENTED, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		
		Assert.assertEquals("test message\n", readContent("#stdout test message//\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true)));
		try{readContent("#error test message//tail \n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true));
			Assert.fail("Mandatory exception was not detected (#error directive inside)");
		} catch (IOException exc) {
			Assert.assertEquals("Line 1, pos 0: test message", exc.getCause().getLocalizedMessage());
		}
		
//		Assert.assertEquals("test message\n", readContent("#include "+URIUtils.convert2selfURI("test string".toCharArray(), PureLibSettings.DEFAULT_CONTENT_ENCODING)+"\ntest", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true)));
	}

	@Test
	public void commentAndContinuationTest() throws IOException {
		Assert.assertEquals("simple\n", readContent("simple\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("simple \n", readContent("simple // comment\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		
		Assert.assertEquals("line1line2\n", readContent("line1;\nline2\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("line1line2line3\n", readContent("line1;\nline2;\nline3\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("line1line2line3\n", readContent("line1;//comment\nline2;//comment\nline3//comment\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		
		Assert.assertEquals("before  after\n", readContent("before /*comment*/ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("before  after\n", readContent("before /*comment1\ncomment2*/ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("before  after\n", readContent("before /*comment1\ncomment2\ncomment3*/ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		
		Assert.assertEquals("before \n", readContent("before // /*comment*/ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("before  after\n", readContent("before /*comment // */ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("before  after\n", readContent("before /*comment \ncomment1 // comment*/ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("before  after\n", readContent("before /*comment \ncomment1 // comment \ncomment2 */ after\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));

		Assert.assertEquals("line1line2\n", readContent("line1;/*comment*/\n/*comment*/line2\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("line1line2\n", readContent("line1;/*comment\ncomment*/line2\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
		Assert.assertEquals("line1line2line3\n", readContent("line1;/*comment\ncomment*/line2;\nline3\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true, PreprocessingXReader.COMMENT_SEQUENCE, "//\n/*\t*/")));
	}
	
	@Test
	public void commandTest() throws IOException {
		Assert.assertEquals("X 20\n", readContent("#command X <item> => (<item>)\nX 20\n", Utils.mkMap()));
		Assert.assertEquals("(20)\n", readContent("#command X <item> => (<item>)\nX 20\n", Utils.mkMap(PreprocessingXReader.INLINE_SUBSTITUTION, true)));
	}
	
	private String readContent(final String content, final Map<String,Object> options) throws IOException {
		try(final Writer		wr = new StringWriter()) {
			try(final Reader	rdr = new StringReader(content);
				final Reader	pr = new PreprocessingXReader(rdr,options)) {
				
				Utils.copyStream(pr, wr);
			}
			return wr.toString();
		}
	}
}
