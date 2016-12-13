package org.yinwang.yin.ast;


import org.yinwang.yin.Scope;
import org.yinwang.yin.Util;
import org.yinwang.yin.value.Value;

public class Name extends Node {
    public String id;


    public Name(String id, String file, int start, int end, int line, int col) {
        super(file, start, end, line, col);
        this.id = id;
    }


    /**
     * Generate a name without location info
     */
    public static Name genName(String id) {
        return new Name(id, null, 0, 0, 0, 0);
    }


    public Value interp(Scope s) {
        return s.lookup(id);
    }


    @Override
    public Value typecheck(Scope s) {
        Value v = s.lookup(id);
        if (v != null) {
            return v;
        } else {
            Util.abort(this, "unbound variable: " + id);
            return Value.VOID;
        }
    }


    public String toString() {
        return id;
    }
}
