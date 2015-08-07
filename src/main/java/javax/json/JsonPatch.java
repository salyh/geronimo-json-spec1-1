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

import java.io.StringReader;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.naming.PartialResultException;

/**
 * 
 * @since 1.1
 *
 */
public final class JsonPatch {

    static final String TEST = "test";
    static final String COPY = "copy";
    static final String MOVE = "move";
    static final String REPLACE = "replace";
    static final String REMOVE = "remove";
    static final String ADD = "add";
    static final String OP = "op";
    static final String PATH = "path";
    static final String VALUE = "value";
    static final String FROM = "from";
    
    private final JsonArray patch;
    
    public JsonPatch(JsonArray patch) {
       this.patch = Objects.requireNonNull(patch);
    }

    @Override
    public boolean equals(Object obj) {
        return patch.equals(obj);
    }

    @Override
    public int hashCode() {
        return patch.hashCode();
    }

    @Override
    public String toString() {
        return patch.toString();
    }

    /*
     * 
   { "op": "add", "path": "/a/b/c", "value": "foo" }
   { "path": "/a/b/c", "op": "add", "value": "foo" }
   { "value": "foo", "path": "/a/b/c", "op": "add" }
     * 
     */
    public JsonStructure apply(JsonStructure target) {
        System.out.println("start: "+target);
        for (JsonValue jsonValue : patch) {
            System.out.println(jsonValue);
            JsonObject patchLine = jsonValue.asJsonObject();
            String path = patchLine.getString(PATH);
            JsonValue value = null;
            String from = null;
            System.out.println("patchline; "+patchLine);
            System.out.println("path "+path);
            switch(patchLine.getString(OP)) {
            case ADD:
                value = patchLine.get(VALUE);
                target = new JsonPointer(path).add(target, value);
                System.out.println("after add "+target);
                break;
            case REMOVE:
                target = new JsonPointer(path).remove(target);
                System.out.println("after remove "+target);
                break;
            case REPLACE:
                value = patchLine.get(VALUE);
                target = new JsonPointer(path).replace(target, value);
                System.out.println("after replace "+target);
                break;
            case MOVE:
                from = patchLine.getString(FROM);
                if(from.equals(path)) break;
                JsonStructure result = new JsonPointer(path).add(target, new JsonPointer(from).getValue(target));
                target = new JsonPointer(from).remove(result);
                System.out.println("after move "+target);
                break;
            case TEST:
                value = patchLine.get(VALUE);
                if(!value.equals(new JsonPointer(path).getValue(target))) {
                    throw new JsonException("values do not match");
                }
                break;
            case COPY:
                from = patchLine.getString(FROM);
                target = new JsonPointer(path).add(target, new JsonPointer(from).getValue(target));
                System.out.println("after copy "+target);
                break;
            default: throw new JsonException("unknown op");
            
            }
            
        }
        System.out.println("applied "+patch.size());
        return target;
    }
    

    public static JsonArray diff(JsonStructure source, JsonStructure target) {
        System.out.println("overall source "+source);
        System.out.println("overall target "+target);
        return diff("", source, target);
    }
    
    private static JsonArray diff(String path, JsonValue source, JsonValue target) {
        
        if(source instanceof JsonObject && target instanceof JsonObject) {
            return diff(path, source.asJsonObject(), target.asJsonObject());
        }
        
        if(source instanceof JsonArray && target instanceof JsonArray) {
            return diff(path, source.asJsonArray(), target.asJsonArray());
        }
        
        /*if(source instanceof JsonObject && target instanceof JsonArray) {
            return new JsonPatchBuilder()
            .replace(path, target)
            .build();
        }
        
        if(source instanceof JsonArray && target instanceof JsonObject) {
            return new JsonPatchBuilder()
            .replace(path, target)
            .build();
        }*/
        
        System.out.println("replace "+path+" with "+target.getClass()+"  "+target);
        
        return new JsonPatchBuilder()
        .replace(path, target)
        .build();
        
        
    }
    
    private static JsonArray diff(String path, JsonArray source, JsonArray target) {
        System.out.println("replace arrays on "+path);
        System.out.println("source "+source);
        System.out.println("target "+target);
        JsonPatchBuilder pb = new JsonPatchBuilder();
        int offset=0;
        for (int i = 0; i < Math.max(source.size(), target.size()); i++) {
            if (target.size() - 1 >= i) {

                JsonValue tv = target.get(i);

                // source has an value at this index
                if (source.size() - 1 >= i) {
                    JsonValue sv = source.get(i);
                    if (!tv.equals(sv)) {
                        pb.addPatches(diff(path + "/" + i, sv, tv));
                    }
                } else {
                    pb.add(path+ "/" + (i + offset), tv);
                    System.out.println("add on "+path+ "/" + (i + offset) +" --> "+tv);
                }
            } else {
                pb.remove(path+ "/" + (i + offset));
                System.out.println("remove on "+path+ "/" + (i + offset));
                offset--;
            }
        }

        return pb.build();
    }
    
    private static JsonArray diff(String path, JsonObject source, JsonObject target) {
        System.out.println("replace objects on "+path);
        System.out.println("source "+source);
        System.out.println("target "+target);
        JsonPatchBuilder pb = new JsonPatchBuilder();
        Set<String> allKeys = new HashSet<String>(source.keySet());
        allKeys.addAll(target.keySet());
        for(String key:allKeys) {
            if (target.containsKey(key)) {

                JsonValue tv = target.get(key);

                // source has an value at this index
                if (source.containsKey(key)) {
                    JsonValue sv = source.get(key);
                    if (!tv.equals(sv)) {
                        pb.addPatches(diff(path + "/" + key, sv, tv));
                    }
                } else {
                    pb.add(path+ "/" + key, tv);
                    System.out.println("add on "+path+ "/" + key +" --> "+tv);
                }
            } else {
                pb.remove(path+ "/" + key);
                System.out.println("remove on "+path+ "/" + key);
            }
        }

        return pb.build();
    }
    
    public JsonObject apply(JsonObject target) {
        return (JsonObject) apply((JsonStructure)target);
    }

    public JsonArray apply(JsonArray target) {
        return (JsonArray) apply((JsonStructure)target);
    }
}

