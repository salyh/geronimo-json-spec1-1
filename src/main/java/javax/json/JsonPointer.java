/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package javax.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * https://tools.ietf.org/html/rfc6901
 * 
 * @since 1.1
 *
 */
public final class JsonPointer {

    /*

     The ABNF syntax of a JSON Pointer is:

      json-pointer    = *( "/" reference-token )
      reference-token = *( unescaped / escaped )
      unescaped       = %x00-2E / %x30-7D / %x7F-10FFFF
         ; %x2F ('/') and %x7E ('~') are excluded from 'unescaped'
      escaped         = "~" ( "0" / "1" )
        ; representing '~' and '/', respectively

     */
    private static final Pattern POINTER_PATTERN = Pattern.compile("(/([^~/]|(~(0|1)))*)*");
    private final String jsonPointer;
    private volatile List<String> tokens;

    public JsonPointer(final String jsonPointer) {
        if (jsonPointer == null || !POINTER_PATTERN.matcher(jsonPointer).matches()) {
            throw new JsonException("Invalid Json pointer '"+jsonPointer+"'");
        }

        this.jsonPointer = jsonPointer;
    }

    @Override
    public boolean equals(final Object obj) {
        return jsonPointer.equals(obj);
    }

    @Override
    public int hashCode() {
        return jsonPointer.hashCode();
    }

    @Override
    public String toString() {
        return jsonPointer;
    }

    private void ensureTokenized() {
        if (tokens == null) {
            final String[] splits = jsonPointer.split("/");
            tokens = new ArrayList<String>(splits.length);
            Arrays.stream(splits).filter(t -> !t.isEmpty()).forEach(t -> tokens.add(t.replace("~1", "/").replace("~0", "~")));
        }
    }

    public JsonValue getValue(final JsonStructure target) {
        final NodeRef nr = getValue0(target);
        if (nr.exists()) {
            return nr.val;
        } else {
            throw new JsonException("No such element");
        }
    }
    
    Optional<JsonValue> testValue(final JsonStructure target) {
        final NodeRef nr = getValue0(target);
        if (nr.exists()) {
            return Optional.of(nr.val);
        } else {
            return Optional.empty();
        }
    }

    private static class NodeRef {
        private NodeRef parent;
        private int index = -2;
        private String key;
        private JsonValue val; // leaf or structure

        public void setVal(final JsonValue val) {
            this.val = val;
        }

        private final List<NodeRef> children = new ArrayList<NodeRef>();
        private final int nestLevel;

        public NodeRef(final JsonValue val) {
            this(val, 0);
        }

        public void rebuildFromChildren() {

            if (isLeaf()) {
                return;
            }

            if (children.isEmpty()) {
                return;
            }

            for (final Iterator iterator = children.iterator(); iterator.hasNext();) {
                final NodeRef nodeRef = (NodeRef) iterator.next();
                nodeRef.rebuildFromChildren();

            }

            if (isObject()) {
                final JsonObjectBuilder b = Json.createObjectBuilder();

                for (final Iterator iterator = children.iterator(); iterator.hasNext();) {
                    final NodeRef nodeRef = (NodeRef) iterator.next();
                    b.add(nodeRef.key, nodeRef.val);

                }
                val = b.build();
            }

            if (isArray()) {
                final JsonArrayBuilder b = Json.createArrayBuilder();
                children.sort(new Comparator<NodeRef>() {
                    @Override
                    public int compare(NodeRef o1, NodeRef o2) {
                        // TODO Auto-generated method stub
                        return o1.index==o2.index?0:(o1.index<o2.index?-1:1);
                    }
                });
                
                System.out.println("sorted children "+children);

                int i=0;
                for (final Iterator iterator = children.iterator(); iterator.hasNext();) {
                    final NodeRef nodeRef = (NodeRef) iterator.next();
                    b.add(i++, nodeRef.val);
                    //i++
                    //nodeRef.index

                }
                val = b.build();
                System.out.println("as build "+val );
            }

        }

        public NodeRef(final JsonValue val, final int level) {
            super();
            this.val = val;
            this.nestLevel = level;

            if (val instanceof JsonObject) {
                final JsonObject o = val.asJsonObject();
                final Set<Entry<String, JsonValue>> es = o.entrySet();
                for (final Entry<String, JsonValue> entry : es) {
                    addChild0(entry.getValue(), -2, entry.getKey());
                }

            } else if (val instanceof JsonArray) {
                final JsonArray a = val.asJsonArray();
                for (int i = 0; i < a.size(); i++) {
                    addChild0(a.get(i), i, null);
                }
            }
        }

        private NodeRef addChild0(final JsonValue val, final int index, final String key) {
            if (!exists()) {
                throw new JsonException("cannot add to non existing");
            }
            
            
            
            
            final NodeRef c = new NodeRef(val, nestLevel + 1);
            c.index = index;
            c.key = key;
            c.parent = this;
            if(index >= 0) {
                children.add(index, c);
            } else
            children.add(c);
            return c;
        }

        private NodeRef getParent() {
            return parent;
        }

        private List<NodeRef> getChildren() {
            return children;
        }

        private Optional<NodeRef> getChild(final int index) {
            NodeRef r;
            if (index < children.size() && (r = children.get(index)) != null) {
                return Optional.of(r);
            }

            return Optional.empty();
        }

        private Optional<NodeRef> getChild(final String key) {
            return children.stream().filter(r -> r.key.equals(key)).findFirst();
        }

        private NodeRef getRoot() {

            if (parent == null) {
                return this;
            }

            NodeRef root = parent;

            while (root.parent != null) {
                root = root.parent;
            }

            return root;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (final Iterator iterator = children.iterator(); iterator.hasNext();) {
                final NodeRef nodeRef = (NodeRef) iterator.next();
                sb.append(nodeRef.toString());
            }
            final StringBuilder id = new StringBuilder();
            for (int i = 0; i < nestLevel; i++) {
                id.append("-");
            }

            final String ident = id.toString();

            return ident
                    + (parent == null ? "ROOT" : (!exists() ? "<non exist>" : val.getValueType()) + "(" + (index == -2 ? key : index)
                            + ")l" + nestLevel) + System.lineSeparator() + sb;

        }

        public boolean exists() {
            return val != null;
        }

        public boolean isStructure() {
            return exists() && (val instanceof JsonObject || val instanceof JsonArray);
        }

        public boolean isObject() {
            return exists() && val instanceof JsonObject;
        }

        public boolean isArray() {
            return exists() && val instanceof JsonArray;
        }

        public boolean isLeaf() {
            return !isStructure() && exists();
        }

        public boolean parentIsObject() {
            return key != null;
        }

        public int getIndex() {
            return index;
        }

        public String getKey() {
            return key;
        }

        public JsonValue getVal() {
            return val;
        }

        public int getNestLevel() {
            return nestLevel;
        }

    }

    private NodeRef getValue0(final JsonStructure target) {
        Objects.requireNonNull(target);
        ensureTokenized();
        final NodeRef root = new NodeRef(target);
        System.out.println(jsonPointer+" :: "+tokens);
        if (jsonPointer.isEmpty()) {
            return root;
        }
        
        if (tokens.isEmpty()) {
            tokens.add("");
        }

        NodeRef curref = root;

        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            
            //System.out.println("evaluate " + token + " on " + curref.getNestLevel()
            //        + (curref.getVal() == null ? "NULL" : curref.getVal().getValueType()));

            if (curref.isObject()) {
                System.out.println("evaluate token "+token+" on object");
                final Optional<NodeRef> tmp = curref.getChild(token);
                if (tmp.isPresent()) {
                    curref = tmp.get();
                } else {

                    if (i != tokens.size() - 1) {
                        throw new JsonException("end reached");
                    }

                    return curref.addChild0(null, -2, token);
                }
            } else if (curref.isArray()) {
                System.out.println("evaluate token "+token+" on array");
                final int size = curref.getVal().asJsonArray().size();

                if (token.equals("-")) {
                    // dash
                    if (i != tokens.size() - 1) {
                        throw new JsonException("end reached");
                    }
                    return curref.addChild0(null, size, null); 
                }

                try {
                    final int index = Integer.parseUnsignedInt(token);

                    if (index > size) {
                        throw new JsonException("No such element (" + index + " is out of bounds (" + size + "))");
                    } else if (index == size) {
                        // dash
                        if (i != tokens.size() - 1) {
                            throw new JsonException("end reached");
                        }
                        return curref.addChild0(null, size, null);
                    }

                    curref = curref.getChildren().get(index);
                } catch (NumberFormatException e) {
                    throw new JsonException("Cannot parse '"+token+"' to an unsigned integer");
                }
            } else if (curref.isLeaf()) {
                
                if(!curref.exists()) {
                    throw new RuntimeException("leaf does not exist");
                }
                
                if (i != tokens.size() - 1) {
                    throw new JsonException("end reached");
                }
                
                if(curref.getParent().isArray()) {
                    try {
                        int index = Integer.parseUnsignedInt(token);
                    } catch (NumberFormatException e) {
                        throw new JsonException("Cannot parse '"+token+"' to an unsigned integer");
                    }
                }
                
                if(curref.getParent().isObject()) {
                   
                        throw new JsonException("Cannot parse '"+token+"' to an unsigned integer");
                    
                }
                
            } else {
                throw new JsonException("does not exists");
            }
        }

        return curref;
    }

    public JsonStructure add(final JsonStructure target, final JsonValue value) {
        final NodeRef node = getValue0(target);
        System.out.println(node.toString());
        System.out.println("newval: " + value);
        
        if(node.exists()) {
            if(node.getParent().isArray()) {
                node.getParent().addChild0(value, node.index, null);
            } else if (node.getParent().isObject()) {
                node.getParent().addChild0(value, -2, node.key);
            } else {
                throw new JsonException("cannot add to value");
            }
            
        } else {
            node.setVal(value);
        }
        
        
        return foldNodesFrom(node);
    }

    private JsonStructure foldNodesFrom(final NodeRef node) {
        //System.out.println(node.getRoot());
        node.getRoot().rebuildFromChildren();
        JsonValue v = node.getRoot().getVal();
        
        if(v instanceof JsonStructure) return  (JsonStructure) v;
        throw new JsonException("simple val not supported");
    }

    public JsonStructure replace(final JsonStructure target, final JsonValue value) {
        final NodeRef node = getValue0(target);
        System.out.println(node.toString());
        System.out.println("repl: " + value);
        if (!node.exists()) {
            throw new JsonException("node does not exist");
        }

        //System.out.println(node.getRoot().toString());
        node.setVal(value);
        node.getChildren().clear();

        return foldNodesFrom(node);

    }

    public JsonStructure remove(final JsonStructure target) {
        final NodeRef node = getValue0(target);
        System.out.println(node.toString());
        if (!node.exists()) {
            throw new JsonException("node does not exist");
        }

        //System.out.println(node.getRoot().toString());

        if (node.getParent() == null) {
            return node.isObject() ? JsonValue.EMPTY_JSON_OBJECT : JsonValue.EMPTY_JSON_ARRAY;
        }
        
        System.out.println(node.getParent().toString());
        
        node.getParent().setVal(node.getParent().isArray()?JsonValue.EMPTY_JSON_ARRAY:JsonValue.EMPTY_JSON_OBJECT);
        if(!node.getParent().getChildren().remove(node)) {
            throw new JsonException("could not remove node");
        }
        return foldNodesFrom(node.getParent());
    }

    public JsonObject add(final JsonObject target, final JsonValue value) {
        return (JsonObject) add((JsonStructure) target, value);
    }

    public JsonArray add(final JsonArray target, final JsonValue value) {
        return (JsonArray) add((JsonStructure) target, value);
    }

    public JsonObject replace(final JsonObject target, final JsonValue value) {
        return (JsonObject) replace((JsonStructure) target, value);
    }

    public JsonArray replace(final JsonArray target, final JsonValue value) {
        return (JsonArray) replace((JsonStructure) target, value);
    }

    public JsonObject remove(final JsonObject target) {
        return (JsonObject) remove((JsonStructure) target);
    }

    public JsonArray remove(final JsonArray target) {
        return (JsonArray) remove((JsonStructure) target);
    }
}
