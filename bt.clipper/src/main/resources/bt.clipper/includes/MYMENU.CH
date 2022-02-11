#define _WIN_CONCURRENT_        1
#define _WIN_CONSOLE_           2
#define _WIN_CURSOR_            3
#define _WIN_ENABLE_CHOICE_     4
#define _WIN_ENABLE_MOVE_       5
#define _WIN_ENABLE_SIZE_       6
#define _WIN_IDLE_              7

//
#define VERSION_1      "ver.1993"


#define MY_HSEP            substr(_frame[1],18,2)
#define MY_CSEP            substr(_frame[1],20,1)
#define MY_FSEP            substr(_frame[1],21,2)

#define RECORD_UPDATED     chr(1)
#define RECORD_NOT_UPDATED chr(0)
#define RECORD_FREE        chr(0)
#define RECORD_DELETED     chr(2)
#define RECORD_TRACH       chr(3)

#define EXT_MEMO           ".fpt"
#define EXT_INDEX          ".idx"
#define EXT_CINDEX         ".cdx"

#command EDIT FIELDS <fields> TO <var>;
         [TITLE  <title>];
         [HEADER <soob>];
         [TOP    <row>];
         [LEFT   <col>];
         [LEN    <len>];
         [COLOR  <colorspec>];
         [HORCLICK <horclick>];
         [BLOCK  <block>]=>;
         <var>:=edBlank(<title>,<soob>,<fields>,<row>,<col>,<colorspec>,<block>,<horclick>,<len> )

//
#define H_BOTTOM   1
#define H_TOP      2
#command ARCHOICE <adoc> [TO     <var>];
                         [TITLE  <title>];
                         [HEADER <soob>];
                         [TOP    <row>];
                         [LEFT   <col>];
                         [LEN    <count>];
                         [BEGINPOS <beginpos>];
                         [BEGINAR  <beginar>];
                         [WIDTH  <width>];
                         [COLOR  <colortable>];
                         [FIELDS <fields>];
                         [BLOCK  <block>];
                         [COLORBLOCK <colorblock>];
                         [COLORSPEC  <colorspec>];
                         [HORCLICK  <horclick>];
                         [NUMBER <x:ON,OFF,&>];
                         [VERTCLICK <vertclick>];
                         [FILTER <filter>];
                         [SEARCH FIELD <searchField>];
                         [HORFLAG   <HorFlag>]=>;
  <var>:=ascroll(<title>,<soob>,<adoc>,<row>,<col>,<count>,;
          <width>,<colortable>,<fields>,<block>,<colorblock>,<colorspec>,;
          <horclick>,<vertclick>,<beginpos>,<beginar>,<HorFlag>,;
          if(_Upper(<(x)>)=="ON",.t.,.f.),;
          <filter>,<searchField>)

/*--------------------------------------------*/

// Команда создания нового указателя на bar-меню                  *
                                                                  *
// Синтаксис :                                                    *
                                                                  *
#command  NEW BAR  <menu>  ;
          => ;
          <menu>:=newbar()
/*--------------------------------------------*/                  
#command  NEW BAR TO <menu>  ;
          => ;
          <menu>:=newbar()
                                                                  *
// Передаваемые параметры :                                       *
                                                                  *
//               < menu>  - указатель на будующее bar-меню        *
                                                                  *
// Возвращаемые значения :                                        *
                                                                  *
//               < menu>  - указатель на сформированное bar-меню  *
                                                                  *
                                                                  *
*******************************************************************

// Команда добавления в bar-меню очередного промпта

// Синтаксис :

#command  @ <row>, <col> BAR <prompt> ;
                        [TO  <bar>  ];
                        [COLOR <col1> ] ;
          => ;
          addbar( <bar>, <row>, <col>, <prompt> , <col1> )

#command  @ <row>, <col> BAR <prompt> ;
                        [TO  <bar>  ];
                        [COLOR <col1> ,<col2>]  ;
          => ;
          addbar( <bar>, <row>, <col>, <prompt> , <col1> , <col2> )

#command  @ <row>, <col> BAR <prompt> ;
                        [TO  <bar>  ];
                        [COLOR <col1> ,<col2> ,<col3>]  ;
          => ;
          addbar( <bar>, <row>, <col>, <prompt> , <col1> , <col2> ,<col3>)

#command  @ <row>, <col> BAR <prompt> ;
                        [TO  <bar>  ];
                        [COLOR <col1> ,<col2> ,<col3> ,<col4>]  ;
          => ;
          addbar( <bar>, <row>, <col>, <prompt> , <col1> , <col2> ,<col3> ,<col4>)

// Передаваемые параметры :                                       *
//
//             <row>     -   строка выводимого промпта

//             <col>     -   колонка выводимого промпта

//             <prompt>  -   промпт

//             <bar>     -   указатель на bar-меню

//             <col1>    -   основной цвет
//             <col2>    -   цвет выбора
//             <col3>    -   цвет буквы на фоне основного
//             <col4>    -   цвет буквы на фоне цвета выбора


// Возвращаемые значения :                                        *

//             нет


/*---------------------------------------------*/

// Активизация bar-меню

// Синтаксис :


#command  BAR <menu> TO <v> [FUNCTION <f>] [BEGIN <b>] [MODE <m>];
          => ;
          <v> := activbar(<menu>,<f>,<b>,<m>)

// Передаваемые параметры :                                       *
*
*            <menu>    -  указатель на bar-меню
*
*            <v>       -  переменная в которую заносится номер выбранного промпта
*
*            <f>       -  функция определяемая пользователем
*
*            <b>       -  номер промпта, с которого начинать выбор
*
*            <m>       -  режим управления функцией пользователя
*
*                         1 :        1- появляется промпт
*                                    2- функция пользователя
*                                    3- ожидание промпта
*
*                         2:         1- появляется промпт
*                                    2- ожидание промпта
*                                    3- функция пользователя
*
*                         3:         1- функция пользователя
*                                    2- появляется промпт
*                                    3- ожидание промпта
*
*                         4:         немедленный выход
*

// Возвращаемые значения :                                        *

//          номер выбранного промпта

/*---------------------------------------------*/

//  Освобождает память под ранее занятый указатель на bar-меню


#command  CLOSE BAR <menu> ;
          => ;
           closebar(<menu>)

/*--------------------------------------------*/

#command  RELEASE BAR <menu> ;
          => ;
           closebar(<menu>)

/*--------------------------------------------*/

//  Перерисовывает bar-меню

#command  REFRESH BAR  => barrefresh()

/*--------------------------------------------*/

// Перерисовывает текущий промпт

#command  REFRESH CURBAR => refbarcur()

/*--------------------------------------------*/

// Удаляет промпт из bar-меню

#command  DELBAR FROM <array> TO <item>   => delbar(<array>,<item>)

/*--------------------------------------------*/

#command  CHBAR <item> FROM <array>  ;
                       [BAR <prompt>] ;
                       [COLOR <col1> ] ;
               => ;
               chbar( <array>, <item>,  <prompt> , <col1> )

/*--------------------------------------------*/

#command  CHBAR <item> FROM <array>  ;
                       [BAR <prompt>] ;
                       [COLOR <col1> , <col2> ] ;
               => ;
               chbar( <array>, <item>,  <prompt> , <col1> ,<col2>)

/*--------------------------------------------*/

#command  CHBAR <item> FROM <array>  ;
                       [BAR <prompt>] ;
                       [COLOR <col1> , <col2> , <col3> ] ;
               => ;
               chbar( <array>, <item>,  <prompt> , <col1> ,<col2>, <col3>)

/*--------------------------------------------*/

#command  CHBAR <item> FROM <array>  ;
                      [BAR <prompt>] ;
                      [COLOR <col1> , <col2> , <col3> ,<col4> ] ;
               => ;
               chbar( <array>, <item>, <prompt> ,<col1> ,<col2>, <col3> ,<col4>)

/*--------------------------------------------*/

#translate newWindow(<begx>,<begy>,<endx>,<endy>,<frame>,<title>,<mode>,<blink>,<color>) =>;
           new_Window(<begx>,<begy>,<endx>,<endy>,<frame>,<title>,<mode>,<blink>,<color>)

#translate newColor(<col1>,<col2>) => _setcolor(<col1>,<col2>)

#translate restColor() => _restcolor()

#translate restReport() => delReport()

//*****************************************************
#define BAZ      1
#define PRCREATE   1
#define PRRENAME   2
#define PRMOVE     3
#define PRCOPY     4
#define PRSEARCH   6
#define PRDELETE   7
**********************************************************
// Макро для определения режима при вводе

#define GT_TOP       1
#define GT_BOTTOM    2
#define GT_NORMAL    3
#define GT_EDIT      4
//
#define GT_PREBLOCK  10
#define GT_POSTBLOCK 11
#define GT_PREVAR    20
#define GT_POSTVAR   21
//
#define RET_NO_CHANGE 0
#define RET_CHANGE    1
#define RET_CONTINUE  2
#define RET_NO_EDIT   3
#define RET_NO_EXIT   4
//
***********************************
#define R_TYPE        1
#define R_NAME        2
#define R_FILE        3
#define R_FIELDS      4
#define R_FILTR       5
#define R_CORRECT     6
#define R_FREEZE      7
#define R_FUNCTION    8
#define R_WINDOW      9
#define R_COLOR       10
#define R_ACCESS      11
#define R_BLINK       12
#define R_QUERY       13
#define R_SWITCH      14
#define R_FRAME       15
#define R_PRINT       16
#define R_INDEX       17
#define R_USER_APPEND 18
#define R_SPISOK      19
#define R_RELATION_SON 20
#define R_SET_OBJECT   21
#define R_MAX          21
//
#define RF_TITLE     1
#define RF_NAME      2
#define RF_LAPPEND   3
#define RF_PROC      4
#define RF_SPRAV     5
#define RF_LEN       6
#define RF_TYPE      7
#define RF_WIDTH     8
#define RF_PERFORM   9
#define RF_PICTURE   10
#define RF_DECPOS    11
#define RF_REFRESH   12
#define RF_RELATION_PARENT  13
#define RF_RELATION_SON     14
#define RF_SEPARATORS       15
//
#define OB_MAIN      1
#define OB_RELATION  2
#define OB_TYPE_LOAD 3
//
#define SEP_HEAD     1
#define SEP_COLUMN   2
#define SEP_FOOT     3
//
 #define RQ_WHERE    1
 #define RQ_GROUP    2
//
 #define Q_WHERE     1
 #define Q_GROUP     2
//
 #define K_SCAN_FILES 1
 #define K_FIELD      2
 #define K_LOAD       3
 #define K_FORMS      4
 #define K_PRINT      5
 #define K_USER       6
 #define K_APPLIC     8
 #define K_QUIT       7
 #define K_SAVE       9
 #define K_INFORM     1.1
 #define K_UNLOAD     1.2
 #define K_FICUS      1.3
 #define K_SPISOK     1.4

//

#define M_FUNC       8
#define M_LOAD       1
#define M_FORMS      2
#define M_PRINT      3
#define M_USER       4
#define M_UNLOAD     5

//

 #define AF_HEADER    10
 #define AF_PLACE     11
 #define AF_WIDTH     12
 #define AF_MAKE      13
 #define AF_PICTURE   14
 #define AF_FORMULA   17
 #define AF_SPRAV     18
 #define AF_SPRINF    19
 #define AF_SPRKILL   20
 #define AF_FICUS     201
 #define AF_REFRESH   202
 #define AF_RELATION_PARENT  203
 #define AF_RELATION_SON     204
 #define AF_COPY             205
 #define AF_SEPARATORS       206

//
#define DB_FREEZE    21
#define DB_FILTR     22
#define DB_ACCESS    23
#define DB_APPREC    24
#define DB_COLOR     25
#define DB_BLINK     26
#define DB_FRAME     27
#define DB_PRINT     28
#define DB_INDEX     29
//
#define WN_PLACE     31
#define WN_SIZE      32
#define WN_FRAME     33
//
#define SP_NAME           1
#define SP_FILE           2
#define SP_NFIELD         3
#define SP_FIELD          4
#define SP_AFIELD         5
#define SP_INDEX          6
#define SP_APPLICATION    7
#define SP_FILTER         8
#define SP_ALIAS          9
//
#define SP_SON            2
#define SP_PARENT         1
//
 #define QU_WHERE    40
 #define QU_GROUP    41
//
 #define SET_ALL     100
//
 #define SW_READONLY     1
 #define SW_AUTOSKIP     4
 #define SW_PANEL        3
 #define SW_END          2
 #define SW_APPEND       5
 #define SW_TABLE_INKEY  6
 #define SW_ACTIVE_KEY   7
 #define SW_BLANK        8
 #define SW_GLOBAL       9
 #define SW_SHOW_RECORD  10
 #define SW_SHOW_DELETED 11

//
 #define SIZE_APPLICATION 14

*---------------------------------------------------------*
#command BININDEX  ON <(field)> TO <(file)> =>;
         CreatBinIndex(<(file)>,<(field)>)

#command SET BININDEX TO <(file)> =>;
         SetBinIndex(<(file)>)

#command CLOSE BININDEX <(file)> =>;
         CloseBinIndex(<(file)>)

#command BSEEK FOR <(field)> = <value> =>;
         SeekBinary(<(field)>,<value>)

#command BCONTINUE =>;
         bContinue()
*---------------------------------------------------------*

#define TYPE_MENU      1
#define TYPE_APPLIC    2
#define TYPE_TEXT      3
#define TYPE_QUERY     4
#define TYPE_USER      5
#define TYPE_UNDEFINE  0

*---------------------------------------------------------*

#define M_TITLE       1
#define M_ITEMS       2
#define M_ROW         3
#define M_COL         4
#define M_COLOR       5
#define M_ARGUMENT    6
#define M_MESSAGE     7

*---------------------------------------------------------*
#define WM_NORMAL   0
#define WM_QUERY    1
*---------------------------------------------------------*
#define U_EXCLUSIVE 1
#define U_SHARE     2
#define U_READONLY  3
*----------------------------------------------------------*
#define FILE_SETUP        "mirage.cnf"
#define S_PATHWORK     1
#define S_PATHSERVER   2
#define S_TABLEUSER    3
#define S_COLOR        4
#define S_QUERY_PASSW  5
*----------------------------------------------------------*
#define ST_SUMMA          2
#define ST_AVERAGE        3
#define ST_MAX            4
#define ST_MIN            5
#define ST_GRAN           5



#define MAX_ALIAS     30

#define CR             chr(13)+chr(10)
#define W_MAXCOL       132
#define PG_COUNT       1

#define PG_TOTAL       2
#define PG_RIGHT       3
#define PG_TOP         4
#define PG_BOTTOM      5
#define PG_TOPHEAD     6
#define PG_BOTTOMHEAD  7
#define PG_NUMBER      8
#define PG_PAUSE       9
#define PG_EJECT       10
#define PG_ORDER       11
#define PG_MODE        12

#define PAN_BROWSE     1
#define PAN_BLANK      2

#translate KEYBOARD(<x>) => __keyboard(<x>)

#define O_MAS                 1
#define O_BROWSE              2
#define O_BLANK               3
#define O_NAME                4
#define O_SIZE                5
#define O_DATE                6
#define O_APPEND              7
#define O_APPEND_RESET        8
#define O_TOOLS_MENU          9
#define O_INFORM              10
#define O_SORT                11
#define O_USER_PRINT          12
#define O_PRINT               13
#define O_USER_FORMS          14
#define O_USER_IMPORT         15
#define O_INCLUDE_KEYS        16
#define O_CUR_RECNO           17
#define O_POSITION            18
#define O_ROWPOS              19
#define O_COLPOS              20
#define O_COMMON              21
#define O_FICUS_EXIST         22
#define O_NFIELD              23
#define O_EXEPT_KEYS          24
#define O_WAIT_MODE           25
#define O_SWITCH_PANEL        26
#define O_SHOW_RECORD         27
#define O_BLOCK_SEARCH        28
#define O_CHOICE_PRINT        29
#define O_VIEW_PRINT          30
#define O_SACCESS             31
#define O_BUSER_FORMS         32
#define O_BUSER_PRINT         33
#define O_BUSER_IMPORT        34
#define O_BSORT               35
#define O_BPRINT              36
#define O_BAPPEND             37
#define O_BAPPEND_RESET       38
#define O_CACHE_FLAG          39
#define O_CURSOR              40
#define O_WALIAS              41
#define O_FILTER_BLOCK        42
#define O_FLAG_APPEND         43
#define O_RECORD_APPEND       44
#define O_FIND_RECORD         45
#define O_WINDOW              46
#define O_CODE                47
#define O_GOTOP               48
#define O_REFRESH             49
#define O_MANAGER_KEYS        50
#define O_MANAGER             51
#define O_STABILIZE           52
#define O_STABLE              53
#define O_FILTER_LOAD         54
#define O_KEYBOARD            55
#define O_EVENT               56
#define O_POS_MENU            57
#define O_GLOBAL              58
#define O_DEHILITE            59
#define O_HILITE              60
#define O_SELECT              61
#define O_REFRESH_ALL         62
#define O_REFRESH_CURRENT     63
#define O_SAVE_INDEX          64
#define O_COUNT_USE           65
#define O_FIRST_LOAD          66
#define O_GOTOP_BLOCK         67
#define O_GOBOTTOM_BLOCK      68
#define O_RELATION_BLOCK      69
#define O_MAX                 69
*********** MESSAGES ************
#define M_NEXT_OBJECT         "next"
#define M_COPY_RELATION       "WorkRelation"
*****************************
#define CODE_EXIT             0
//

#define GL_WIN_PLACE          1
#define GL_WIN_SIZE           2
#define GL_WIN_MAX            3
#define GL_WIN_REST           4
#define GL_EXIT               22

#define GL_SAVE                5
#define GL_INSERT              6
#define GL_CHANGE_VIEW         7
#define GL_PACK                8
#define GL_GOTO_RECORD         9
#define GL_FIND_RECORD         10

#define GL_DELETE_RECORD       11
#define GL_SELECT_ALL          12
#define GL_UNSELECT_ALL        13

#define GL_SORT                14
#define GL_IMPORT              15

#define GL_PRINT_BEGIN         16
#define GL_CHOICE_DEVICE_PRINT 17
#define GL_VIEW_PRN_FILE       18
#define GL_PRINT_FORMS         19
#define GL_PRINT_QUALITY       20
#define GL_PRINT_PARAM         21
#define SW_GLOBAL_MAX          22
//
#define RLP_NAME               1
#define RLP_FILE               2
#define RLP_FIELD              3
#define RLP_NAME_FIELD         4
#define RLP_FUNCTION           5
#define RLP_ISINDEX            6
#define RLP_INDEX_NAME         7
#define RLP_SPISOK             8
#define RLP_KEY                9
//
#define FA_NORMAL        0
#define FA_READONLY      1
#define FA_HIDDEN        2
#define FA_SYSTEM        4
#define FA_VOLUME        8
#define FA_DIRECTORY     16
#define FA_ARCHIVE       32
//
#command @ <x>,<y> SAY <string> INTO <window> ;
           [MARGIN <margin:CENTR,RIGHT,LEFT,&>] ;
           [COLOR <color>]    =>;
           SetPosWindow(<window>,<x>,<y>) ;;
           SayWindow(<window>,<string>,<color>,;
           if(_upper(<(margin)>)=="CENTR",1,if(_upper(<(margin)>)=="RIGHT",2,if(_upper(<(margin)>)=="LEFT",3,))))


#command  CREATE WINDOW [TOP <nTop>];
                        [LEFT <nLeft>];
                        [BOTTOM <nBottom>];
                        [RIGHT <nRight>];
                        [FRAME <frame>];
                        [TITLE <title>];
                        [SHOWMODE <shmode>];
                        [BLINK <blink>];
                        [COLOR <color>];
                        [FROM  <wParent>];
                        [WINPROC <winproc>];
                        [BANNERPROC  <winOpen>];
                        [FINISHPROC  <winClose>] TO <wHandle> =>;
          <wHandle>:=CreateWindow(<nTop>    ,;
                                  <nLeft>   ,;
                                  <nBottom> ,;
                                  <nRight>  ,;
                                  <frame>   ,;
                                  <title>   ,;
                                  <shmode>  ,;
                                  <blink>   ,;
                                  <color>   ,;
                                  <winproc> ,;
                                  <winOpen> ,;
                                  <winClose>,;
                                  <wParent>)

#command DELETE WINDOW <win> =>  DeleteWindow(<win>)


#command ? <str> INTO <win> ;
                 [WRAP  <wrap>];
                 [COLOR <color>]   ;
                 [MARGIN <margin:CENTR,RIGHT,LEFT,&>] => ;
                 QoutWindow(<win>,<str>,<color>,<wrap>,,;
                 if(_upper(<(margin)>)=="CENTR",1,if(_upper(<(margin)>)=="RIGHT",2,if(_upper(<(margin)>)=="LEFT",3,))),.t.)

#command ?? <str> INTO <win> ;
                 [WRAP  <wrap>];
                 [COLOR <color>]   ;
                 [MARGIN <margin:CENTR,RIGHT,LEFT,&>] => ;
                 QoutWindow(<win>,<str>,<color>,<wrap>,.t.,;
                 if(_upper(<(margin)>)=="CENTR",1,if(_upper(<(margin)>)=="RIGHT",2,if(_upper(<(margin)>)=="LEFT",3,))),.t.)

#command CLEAR WINDOW <win> => ClrWindow(<win>)
#command SET WINDOW CONCURRENT <x:ON,OFF,&>             => SetWin( _WIN_CONCURRENT_,   if(Upper(<(x)>) == "ON", .t., .f.) )
#command SET WINDOW CONSOLE    <x:ON,OFF,&>             => SetWin( _WIN_CONSOLE_,      if(Upper(<(x)>) == "ON", .t., .f.) )
#command SET WINDOW CURSOR  <WinHandle>   <x:ON,OFF,&>  => SetWin( _WIN_CURSOR_,       if(Upper(<(x)>) == "ON", .t., .f.),<WinHandle> )
#command SET WINDOW CHOICE <WinHandle> <x:ON,OFF,&>     => SetWin( _WIN_ENABLE_CHOICE_,if(Upper(<(x)>) == "ON", .t., .f.),<WinHandle>)
#command SET WINDOW MOVE   <WinHandle> <x:ON,OFF,&>     => SetWin( _WIN_ENABLE_MOVE_,  if(Upper(<(x)>) == "ON", .t., .f.),<WinHandle>)
#command SET WINDOW SIZE   <WinHandle> <x:ON,OFF,&>     => SetWin( _WIN_ENABLE_SIZE_,  if(Upper(<(x)>) == "ON", .t., .f.),<WinHandle>)
#command SET WINDOW IDLE   <WinHandle> <x:ON,OFF,&>     => SetWin( _WIN_IDLE_,  if(Upper(<(x)>) == "ON", .t., .f.),<WinHandle>)
