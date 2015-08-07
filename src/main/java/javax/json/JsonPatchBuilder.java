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

/**
 * 
 * @since 1.1
 *
 */
public class JsonPatchBuilder {

    
    private JsonArrayBuilder builder; 

    public JsonPatchBuilder(JsonArray patch) {
        builder = Json.createArrayBuilder(patch);
    }

    public JsonPatchBuilder() {
        builder = Json.createArrayBuilder();
    }

    public JsonStructure apply(JsonStructure target) {
        return new JsonPatch(build()).apply(target);
    }
    
    public JsonObject apply(JsonObject target) {
        return new JsonPatch(build()).apply(target);
    }

    public JsonArray apply(JsonArray target) {
        return new JsonPatch(build()).apply(target);
    }

    public JsonPatchBuilder add(String path, JsonValue value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.ADD)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                   );
        return this;
    }

    public JsonPatchBuilder add(String path, String value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.ADD)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                   );
        return this;
    }

    public JsonPatchBuilder add(String path, int value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.ADD)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder add(String path, boolean value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.ADD)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                   );
        return this;
    }

    public JsonPatchBuilder remove(String path) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.REMOVE)
                           .add(JsonPatch.PATH, path)
                    );
        return this;
    }

    public JsonPatchBuilder replace(String path, JsonValue value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.REPLACE)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder replace(String path, String value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.REPLACE)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder replace(String path, int value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.REPLACE)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder replace(String path, boolean value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.REPLACE)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder move(String path, String from) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.MOVE)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.FROM, from)
                  );
        return this;
    }
 
    public JsonPatchBuilder copy(String path, String from) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.COPY)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.FROM, from)
                  );
        return this;
    }
 
    public JsonPatchBuilder test(String path, JsonValue value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.TEST)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder test(String path, String value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.TEST)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder test(String path, int value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.TEST)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }

    public JsonPatchBuilder test(String path, boolean value) {
        builder.add(Json.createObjectBuilder()
                           .add(JsonPatch.OP, JsonPatch.TEST)
                           .add(JsonPatch.PATH, path)
                           .add(JsonPatch.VALUE, value)
                  );
        return this;
    }
    
    JsonPatchBuilder addPatches(JsonArray patches) {
        for (JsonValue jsonValue : patches) {
            builder.add(jsonValue);
        }
        
        
        return this;
    }

    public JsonArray build() {
        return builder.build();
    }
}

