package org.yinwang.yin.value.primitives;


import org.yinwang.yin.ast.Node;
import org.yinwang.yin.value.PrimFun;
import org.yinwang.yin.value.Value;

import java.util.List;

public class Print extends PrimFun {

    public Print() {
        super("print", 1);
    }


    @Override
    public Value apply(List<Value> args, Node location) {
        boolean first = true;
        for (Value v : args) {
            if (!first) {
                System.out.print(", ");
            }
            System.out.print(v);
            first = false;
        }
        System.out.println();
        return Value.VOID;
    }


    public Value typecheck(List<Value> args, Node location) {
        return Value.VOID;
    }
}
