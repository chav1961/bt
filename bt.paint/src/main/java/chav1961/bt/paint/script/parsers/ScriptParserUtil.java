package chav1961.bt.paint.script.parsers;


//<prog>::=<anon_block>[{<function>|<procedure>]}]...
//<anon_block>::=[<declarations>][<body>]
//<declarations>::='var'<declaration_list>
//<declaration_list>::=<declaration>[,<declaration>]...
//<body>::='begin'<statements>'end'
//<declaration>::=<@name>':'<type>[':='<initial>]
//<type>::={<simple_type>|<complex_type>}
//<simple_type>::={'int'|'real'|'str'|'bool'|'color'|'point'|'rect'|'font'|'transform'|'stroke'|'image'}
//<complex_type>::={'array''of'<simple_type>|'map''of'<simple_type>}
//<statements>::=<statement>[';'<statement>]...>
//<statement>::={<assignment>|<if>|<while>|<do>|<for>|<case>|<continue>|<break>|<call>|<return>|<sequence>}
//<sequence>::='{'<statements>'}'
//<assignment>::=<left_part>':='<right_part>
//<if>::='if'<cond>'then'<statement>['else'<statement>]
//<while>::='while'<cond>'do'<statement>
//<do>::='do'<statement>'while'<cond>
//<for>::='for'<var>{':='<right_part>'to'<right_part>['step'<right_part>]|':'<right_part>}'do'<statement>
//<case>::='case'<right_part>['of'<range_list>':'<statements>]...['default'':'<statements>]'end'
//<continue>::='continue'[<@int>]
//<break>::='break'[<@int>]
//<call>::='call'<name>'('<list>')'
//<return>::='return'[<right_part>]
//<range_list>::=<range>[','<range_list>
//<range>::=<right_part>'..'<right_part>
//<list>::=<right_part>[','<list>
//<right_part>::=<andNode>['||'<andNode>]...
//<and_node>::=<notNode>['&&'<notNode>]...
//<not_node>::='!'{<comparison>|<boolean>}
//<comparison>::=<concat>{{'>'|'>='|'<'|'<='|'='|'<>'}<concat>|'in'<range_list>}
//<concat>::=<addNode>[#<addNode>]...
//<addNode>::=<mulNode>[{'+'|'-'|'|'}<mulNode>]...
//<mulNode>::=<negNode>[{'*'|'/'|'%'|'&'|'^'}<negNode>]...
//<negNode>::={'-'|'+'|'~'}<term>
//<term>::={<var>|<const>|<func>|<predefined>}['::'<simple_type>]
//<var>::=<@name>['['<right_part>']'['.'<@name>]
//<const>::={<@int>|<@real>|<@string>|'true'|'false'|'`'<sequence>'`'}
//<func>::=<complex_name>'('[<list>]')'
//<complexName>::=<var>['.'<complexName>]...
//<left_part>::=<var>
//<predefined>::={'system'|'clipboard'|'image'|'args'}
//<function>::='func'<@name>'('[<declaration_list>]')'':'<{<simple_type>:<complex_type>}>';'[<declarations>]<body>
//<procedure>::='proc'<@name>'('[<declaration_list>]')'';'[<declarations>]<body>


public class ScriptParserUtil {

}
