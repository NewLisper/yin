package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Delimeter extends Node {
    public String shape;


    public Delimeter(String shape, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.shape = shape;
    }


    public Value interp(Scope s) {
        return null;
    }


    @Override
    public Value typecheck(Scope s) {
        return null;
    }


    // all delimeters
    public static final Set<String> delims = new HashSet<>();

    // map open delimeters to their matched closing ones
    public static final Map<String, String> delimMap = new HashMap<>();


    public static void addDelimiterPair(String open, String close) {
        delims.add(open);
        delims.add(close);
        delimMap.put(open, close);
    }


    public static void addDelimiter(String delim) {
        delims.add(delim);
    }


    public static boolean isDelimiter(char c) {
        return delims.contains(Character.toString(c));
    }


    public static boolean isOpen(Node c) {
        return (c instanceof Delimeter) && delimMap.keySet().contains(((Delimeter) c).shape);
    }


    public static boolean isClose(Node c) {
        return (c instanceof Delimeter) && delimMap.values().contains(((Delimeter) c).shape);
    }


    public static boolean match(Node open, Node close) {
        if (!(open instanceof Delimeter) ||
                !(close instanceof Delimeter))
        {
            return false;
        }
        String matched = delimMap.get(((Delimeter) open).shape);
        return matched != null && matched.equals(((Delimeter) close).shape);
    }


    public String toString() {
        return shape;
    }
}
