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

import javax.json.JsonValue.ValueType;

/**
 * 
 * @since 1.1
 *
 */
public class JsonMergePatch {
    /*

     define MergePatch(Target, Patch):
     if Patch is an Object:
       if Target is not an Object:
         Target = {} # Ignore the contents and set it to an empty Object
       for each Name/Value pair in Patch:
         if Value is null:
           if Name exists in Target:
             remove the Name/Value pair from Target
         else:
           Target[Name] = MergePatch(Target[Name], Value)
       return Target
     else:
       return Patch

    There are a few things to note about the function.  If the patch is
    anything other than an object, the result will always be to replace
    the entire target with the entire patch.  Also, it is not possible to
    patch part of a target that is not an object, such as to replace just
    some of the values in an array.


     */

    public static JsonValue mergePatch(JsonValue target, final JsonValue patch) {
        if (patch.getValueType() == ValueType.OBJECT) {
            if (target.getValueType() != ValueType.OBJECT) {
                target = JsonValue.EMPTY_JSON_OBJECT;
            }
            final JsonObject targetObject = target.asJsonObject();
            final JsonObject patchObject = patch.asJsonObject();
            final JsonObjectBuilder builder = Json.createObjectBuilder(targetObject);
            for (final String key : patchObject.keySet()) {
                final JsonValue value = patchObject.get(key);
                if (JsonValue.NULL == value) {
                    if (targetObject.containsKey(key)) {
                        builder.remove(key);
                    }
                } else {
                    if (targetObject.containsKey(key)) {
                        builder.add(key, mergePatch(targetObject.get(key), value));
                    } else {
                        builder.add(key, mergePatch(JsonValue.EMPTY_JSON_OBJECT, value));
                    }

                }
            }

            return builder.build();

        } else {
            return patch;
        }
    }

    public static JsonValue diff(final JsonValue source, final JsonValue target) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
