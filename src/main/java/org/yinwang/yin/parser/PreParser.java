package org.yinwang.yin.parser;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Util;
import org.yinwang.yin.ast.Delimeter;
import org.yinwang.yin.ast.Name;
import org.yinwang.yin.ast.Node;
import org.yinwang.yin.ast.Tuple;

import java.util.ArrayList;
import java.util.List;


/**
 * parse text into a meanlingless but more structured format
 * similar to S-expressions but with less syntax
 * (just matched (..) [..] and {..})
 */
public class PreParser {

    String file;
    Lexer lexer;


    public PreParser(String file) {
        this.file = Util.unifyPath(file);
        this.lexer = new Lexer(file);
    }


    /**
     * Get next node from token stream
     */
    public Node nextNode() throws ParserException {
        return nextNode1(0);
    }


    /**
     * Helper for nextNode, which does the real work
     *
     * @return a Node or null if file ends
     */
    public Node nextNode1(int depth) throws ParserException {
        Node first = lexer.nextToken();

        // end of file
        if (first == null) {
            return null;
        }

        if (Delimeter.isOpen(first)) {   // try to get matched (...)
            List<Node> elements = new ArrayList<>();
            Node next;
            for (next = nextNode1(depth + 1);
                 !Delimeter.match(first, next);
                 next = nextNode1(depth + 1))
            {
                if (next == null) {
                    throw new ParserException("unclosed delimeter till end of file: " + first.toString(), first);
                } else if (Delimeter.isClose(next)) {
                    throw new ParserException("unmatched closing delimeter: " +
                            next.toString() + " does not close " + first.toString(), next);
                } else {
                    elements.add(next);
                }
            }
            return new Tuple(elements, first, next, first.file, first.start, next.end, first.line, first.col);
        } else if (depth == 0 && Delimeter.isClose(first)) {
            throw new ParserException("unmatched closing delimeter: " + first.toString() +
                    " does not close any open delimeter", first);
        } else {
            return first;
        }
    }


    /**
     * Parse file into a Node
     *
     * @return a Tuple containing the file's parse tree
     */
    public Node parse() throws ParserException {
        List<Node> elements = new ArrayList<>();
        elements.add(Name.genName(Constants.SEQ_KEYWORD));      // synthetic block keyword

        Node s = nextNode();
        Node first = s;
        Node last = null;
        for (; s != null; last = s, s = nextNode()) {
            elements.add(s);
        }

        return new Tuple(
                elements,
                Name.genName(Constants.PAREN_BEGIN),
                Name.genName(Constants.PAREN_END),
                file,
                first == null ? 0 : first.start,
                last == null ? 0 : last.end,
                0, 0
        );
    }


    public static void main(String[] args) throws ParserException {
        PreParser p = new PreParser(args[0]);
        Util.msg("preparser result: " + p.parse());
    }

}
