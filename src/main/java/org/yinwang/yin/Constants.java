package org.yinwang.yin;


import java.util.Arrays;
import java.util.List;

public class Constants {

    // delimiters and delimeter pairs
    public static final String LINE_COMMENT = "--";

    public static final String PAREN_BEGIN = "(";
    public static final String PAREN_END = ")";

    public static final String CURLY_BEGIN = "{";
    public static final String CURLY_END = "}";

    public static final String SQUARE_BEGIN = "[";
    public static final String SQUARE_END = "]";

    public static final String ATTRIBUTE_ACCESS = ".";
    public static final String RETURN_ARROW = "->";

    public static final String STRING_START = "\"";
    public static final String STRING_END = "\"";
    public static final String STRING_ESCAPE = "\\";


    // keywords
    public static final String SEQ_KEYWORD = "seq";
    public static final String FUN_KEYWORD = "fun";
    public static final String IF_KEYWORD = "if";
    public static final String DEF_KEYWORD = "define";
    public static final String ASSIGN_KEYWORD = "set!";
    public static final String RECORD_KEYWORD = "record";
    public static final String DECLARE_KEYWORD = "declare";
    public static final String UNION_KEYWORD = "U";

    public static List<Character> IDENT_CHARS =
            Arrays.asList('~', '!', '@', '#', '$', '%', '^', '&', '*', '-', '_', '=', '+', '|',
                    ':', ';', ',', '<', '>', '?', '/');


}
