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

package javax.json.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * 
 * @since 1.1
 *
 */
public class JsonCollectors {

    private JsonCollectors() {
    }

    public static Collector<JsonValue, JsonArrayBuilder, JsonArray> toJsonArray() {
        return Collector.of(
                Json::createArrayBuilder,
                JsonArrayBuilder::add,
                JsonArrayBuilder::addAll,
                JsonArrayBuilder::build);
    }

    public static Collector<JsonValue, JsonObjectBuilder, JsonObject> toJsonObject(Function<JsonValue, String> keyMapper, Function<JsonValue, JsonValue> valueMapper) {
        return Collector.of(
                Json::createObjectBuilder,
                (b, v) -> b.add(keyMapper.apply(v), valueMapper.apply(v)),
                JsonObjectBuilder::addAll,
                JsonObjectBuilder::build,
                Collector.Characteristics.UNORDERED);
    }

    public static Collector<JsonValue, Map<String, JsonArrayBuilder>, JsonObject> groupingBy(Function<JsonValue, String> classifier, Collector<JsonValue, JsonArrayBuilder, JsonArray> downstream) {

        BiConsumer<Map<String, JsonArrayBuilder>, JsonValue> accumulator =
                (map, value) -> {
                    String key = classifier.apply(value);
                    if (key == null) {
                        throw new JsonException("value cannot be mapped to a null key");
                    }
                    JsonArrayBuilder arrayBuilder = 
                        map.computeIfAbsent(key, v->downstream.supplier().get());
                    downstream.accumulator().accept(arrayBuilder, value);
                };
            Function<Map<String, JsonArrayBuilder>, JsonObject> finisher =
                map -> {
                    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                    map.forEach((k, v) -> {
                        JsonArray array = downstream.finisher().apply(v);
                        objectBuilder.add(k, array);
                    });
                    return objectBuilder.build();
                };
            BinaryOperator<Map<String, JsonArrayBuilder>> combiner =
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                };
            return Collector.of(HashMap::new, accumulator, combiner, finisher,
                Collector.Characteristics.UNORDERED);
    }

    public static Collector<JsonValue, Map<String, JsonArrayBuilder>, JsonObject> groupingBy(Function<JsonValue, String> classifier) {
        return groupingBy(classifier, toJsonArray());
    }
}

