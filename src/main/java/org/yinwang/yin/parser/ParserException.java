package org.yinwang.yin.parser;


import org.yinwang.yin.ast.Node;

public class ParserException extends Exception {
    public int line;
    public int col;
    public int start;


    public ParserException(String message, int line, int col, int start) {
        super(message);
        this.line = line;
        this.col = col;
        this.start = start;
    }


    public ParserException(String message, Node node) {
        super(message);
        this.line = node.line;
        this.col = node.col;
        this.start = node.start;
    }


    @Override
    public String toString() {
        return (line + 1) + ":" + (col + 1) + " parsing error " + getMessage();
    }
}
