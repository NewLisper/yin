package org.yinwang.yin.parser;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin.Util;
import org.yinwang.yin.ast.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Parser
 * parse S-expression-like structure into more structured data
 * with Classes, fields etc that can be easily accessed
 */
public class Parser {

    public static Node parse(String file) throws ParserException {
        PreParser preparser = new PreParser(file);
        Node prenode = preparser.parse();
        return parseNode(prenode);
    }


    public static Node parseNode(Node prenode) throws ParserException {

        if (!(prenode instanceof Tuple)) {
            // Case 1: node is not of form (..) or [..], return the node itself
            return prenode;
        } else {
            // Case 2: node is of form (..) or [..]
            Tuple tuple = ((Tuple) prenode);
            List<Node> elements = tuple.elements;

            if (delimType(tuple.open, Constants.SQUARE_BEGIN)) {
                // Case 2.1: node is of form [..]
                return new VectorLiteral(parseList(elements), tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
            } else {
                // Case 2.2: node is (..)
                if (elements.isEmpty()) {
                    // Case 2.2.1: node is (). This is not allowed
                    throw new ParserException("syntax error", tuple);
                } else {
                    // Case 2.2.2: node is of form (keyword ..)
                    Node keyNode = elements.get(0);

                    if (keyNode instanceof Name) {
                        switch (((Name) keyNode).id) {
                            case Constants.SEQ_KEYWORD:
                                return parseBlock(tuple);
                            case Constants.IF_KEYWORD:
                                return parseIf(tuple);
                            case Constants.DEF_KEYWORD:
                                return parseDef(tuple);
                            case Constants.ASSIGN_KEYWORD:
                                return parseAssign(tuple);
                            case Constants.DECLARE_KEYWORD:
                                return parseDeclare(tuple);
                            case Constants.FUN_KEYWORD:
                                return parseFun(tuple);
                            case Constants.RECORD_KEYWORD:
                                return parseRecordDef(tuple);
                            default:
                                return parseCall(tuple);
                        }
                    } else {
                        // applications whose operator is not a name
                        // e.g. ((foo 1) 2)
                        return parseCall(tuple);
                    }
                }
            }
        }
    }


    public static Block parseBlock(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        List<Node> statements = parseList(elements.subList(1, elements.size()));
        return new Block(statements, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static If parseIf(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        if (elements.size() != 4) {
            throw new ParserException("incorrect format of if", tuple);
        }
        Node test = parseNode(elements.get(1));
        Node conseq = parseNode(elements.get(2));
        Node alter = parseNode(elements.get(3));
        return new If(test, conseq, alter, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static Def parseDef(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        if (elements.size() != 3) {
            throw new ParserException("incorrect format of definition", tuple);
        }
        Node pattern = parseNode(elements.get(1));
        Node value = parseNode(elements.get(2));
        return new Def(pattern, value, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);

    }


    public static Assign parseAssign(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        if (elements.size() != 3) {
            throw new ParserException("incorrect format of definition", tuple);
        }
        Node pattern = parseNode(elements.get(1));
        Node value = parseNode(elements.get(2));
        return new Assign(pattern, value, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static Declare parseDeclare(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        if (elements.size() < 2) {
            throw new ParserException("syntax error in record type definition", tuple);
        }
        Scope properties = parseProperties(elements.subList(1, elements.size()));
        return new Declare(properties, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static Fun parseFun(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;

        if (elements.size() < 3) {
            throw new ParserException("syntax error in function definition", tuple);
        }

        // construct parameter list
        Node preParams = elements.get(1);
        if (!(preParams instanceof Tuple)) {
            throw new ParserException("incorrect format of parameters: " + preParams.toString(), preParams);
        }

        // parse the parameters, test whether it's all names or all tuples
        boolean hasName = false;
        boolean hasTuple = false;
        List<Name> paramNames = new ArrayList<>();
        List<Node> paramTuples = new ArrayList<>();

        for (Node p : ((Tuple) preParams).elements) {
            if (p instanceof Name) {
                hasName = true;
                paramNames.add((Name) p);
            } else if (p instanceof Tuple) {
                hasTuple = true;
                List<Node> argElements = ((Tuple) p).elements;
                if (argElements.size() == 0) {
                    throw new ParserException("illegal argument format: " + p.toString(), p);
                }
                if (!(argElements.get(0) instanceof Name)) {
                    throw new ParserException("illegal argument name : " + argElements.get(0), p);
                }

                Name name = (Name) argElements.get(0);
                if (!name.id.equals(Constants.RETURN_ARROW)) {
                    paramNames.add(name);
                }
                paramTuples.add(p);
            }
        }

        if (hasName && hasTuple) {
            throw new ParserException("parameters must be either all names or all tuples: " +
                    preParams.toString(), preParams);
        }

        Scope properties;
        if (hasTuple) {
            properties = parseProperties(paramTuples);
        } else {
            properties = null;
        }

        // construct body
        List<Node> statements = parseList(elements.subList(2, elements.size()));
        int start = statements.get(0).start;
        int end = statements.get(statements.size() - 1).end;
        Node body = new Block(statements, tuple.file, start, end, tuple.line, tuple.col);

        return new Fun(paramNames, properties, body,
                tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static RecordDef parseRecordDef(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        if (elements.size() < 2) {
            throw new ParserException("syntax error in record type definition", tuple);
        }

        Node name = elements.get(1);
        Node maybeParents = elements.get(2);

        List<Name> parents;
        List<Node> fields;

        if (!(name instanceof Name)) {
            throw new ParserException("syntax error in record name: " + name.toString(), name);
        }

        // check if there are parents (record A (B C) ...)
        if (maybeParents instanceof Tuple &&
                delimType(((Tuple) maybeParents).open, Constants.PAREN_BEGIN))
        {
            List<Node> parentNodes = ((Tuple) maybeParents).elements;
            parents = new ArrayList<>();
            for (Node p : parentNodes) {
                if (!(p instanceof Name)) {
                    throw new ParserException("parents can only be names", p);
                }
                parents.add((Name) p);
            }
            fields = elements.subList(3, elements.size());
        } else {
            parents = null;
            fields = elements.subList(2, elements.size());
        }

        Scope properties = parseProperties(fields);
        return new RecordDef((Name) name, parents, properties, tuple.file,
                tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static Call parseCall(Tuple tuple) throws ParserException {
        List<Node> elements = tuple.elements;
        Node func = parseNode(elements.get(0));
        List<Node> parsedArgs = parseList(elements.subList(1, elements.size()));
        Argument args = new Argument(parsedArgs);
        return new Call(func, args, tuple.file, tuple.start, tuple.end, tuple.line, tuple.col);
    }


    public static List<Node> parseList(List<Node> prenodes) throws ParserException {
        List<Node> parsed = new ArrayList<>();
        for (Node s : prenodes) {
            parsed.add(parseNode(s));
        }
        return parsed;
    }


    // treat the list of nodes as key-value pairs like (:x 1 :y 2)
    public static Map<String, Node> parseMap(List<Node> prenodes) throws ParserException {
        Map<String, Node> ret = new LinkedHashMap<>();
        if (prenodes.size() % 2 != 0) {
            throw new ParserException("must be of the form (:key1 value1 :key2 value2), but got: " +
                    prenodes.toString(), prenodes.get(0));
        }

        for (int i = 0; i < prenodes.size(); i += 2) {
            Node key = prenodes.get(i);
            Node value = prenodes.get(i + 1);
            if (!(key instanceof Keyword)) {
                throw new ParserException("key must be a keyword, but got: " + key.toString(), key);
            }
            ret.put(((Keyword) key).id, value);
        }
        return ret;
    }


    public static Scope parseProperties(List<Node> fields) throws ParserException {
        Scope properties = new Scope();
        for (Node field : fields) {
            if (!(field instanceof Tuple &&
                    delimType(((Tuple) field).open, Constants.SQUARE_BEGIN) &&
                    ((Tuple) field).elements.size() >= 2))
            {
                throw new ParserException("incorrect form of descriptor: " + field.toString(), field);
            } else {
                List<Node> elements = parseList(((Tuple) field).elements);
                Node nameNode = elements.get(0);
                if (!(nameNode instanceof Name)) {
                    throw new ParserException("expect a name, but got: " + nameNode.toString(), nameNode);
                }
                String id = ((Name) nameNode).id;
                if (properties.containsKey(id)) {
                    throw new ParserException("duplicated name: " + nameNode.toString(), nameNode);
                }

                Node typeNode = elements.get(1);
                if (!(typeNode instanceof Name)) {
                    throw new ParserException("type must be a name, but got: " + typeNode.toString(), typeNode);
                }
                properties.put(id, "type", typeNode);

                Map<String, Node> props = parseMap(elements.subList(2, elements.size()));
                Map<String, Object> propsObj = new LinkedHashMap<>();
                for (Map.Entry<String, Node> e : props.entrySet()) {
                    propsObj.put(e.getKey(), e.getValue());
                }
                properties.putProperties(((Name) nameNode).id, propsObj);
            }
        }
        return properties;
    }


    public static boolean delimType(Node c, String d) {
        return c instanceof Delimeter && ((Delimeter) c).shape.equals(d);
    }


    public static void main(String[] args) throws ParserException {
        Node tree = Parser.parse(args[0]);
        Util.msg(tree.toString());
    }

}
