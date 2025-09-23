package chav1961.bt.svgeditor.parser;

public class Command {
	// <Descriptor>	::= <Name>[<Parameter>...]
	// <Name>		::= <Letter>[<LetterOrDigit>...]
	// <Parameter>	::= <Name>':'<Type>['='[<DefaultValue>]]
	// <TypeName>	::= {'int'|'real'|'point'|'rect'|'text'|'color'|'enum('<EnumRef>')'}
	// <Type>		::= {<int>|<real>|<point>|<rect>|<text>|<color>|<enumRef>}
	// <int>		::= {'+'|'-'}<digit>[<digit>...]
	// <real>		::= {'+'|'-'}<int>['.'<int>]
	// <point>		::= <int>','<int>
	// <rect>		::= <int>','<int>','<int>','<int>
	// <text>		::= '"'<chars><escapes>'"'
	// <color>		::= {<Name>|'#"<int>}
	// <enumRef>	::= <Name>
	// Example:
	//	line from:point to:point color:color=green
	
	public Command(final String descriptor, final String helpPrefix, final String helpReference) {
		
	}
	
	
}
