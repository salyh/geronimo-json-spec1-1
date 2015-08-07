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

/**
 * 
 * @since 1.1
 *
 */
public class JsonUtil {

    private static final char BACKSLASH = '\\';
    private static final String BACKSLASH_AS_STRING = String.valueOf(BACKSLASH);
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';

    public static JsonValue toJson(final String jsonString) {
        if (jsonString.indexOf("'") == -1) {
            return Json.createReader(new StringReader(jsonString)).readValue();
        }

        final StringBuilder sb = new StringBuilder();
        final int len = jsonString.length();
        boolean inStringContext = false;
        boolean bySingleQuote = false;
        for (int i = 0; i < len; i++) {
            final char c = jsonString.charAt(i);
            if (c != SINGLE_QUOTE && c != DOUBLE_QUOTE) {
                
                if(!inStringContext || c != BACKSLASH) {
                    sb.append(c);
                }
                continue;
            }

            // c is ' or "

            if (inStringContext) {
                final char p = jsonString.charAt(i - 1);
                if (p == BACKSLASH) {
                    if(c==DOUBLE_QUOTE) {
                        sb.append(BACKSLASH_AS_STRING+c);
                    } else {
                        sb.append(c);
                    }
                } else {
                    if (bySingleQuote) {
                        if (c == SINGLE_QUOTE) {
                            inStringContext = false;
                            sb.append(DOUBLE_QUOTE);
                        }
                    } else {
                        if (c == DOUBLE_QUOTE) {
                            inStringContext = false;
                            sb.append(DOUBLE_QUOTE);
                        }
                    }
                }
            } else {
                inStringContext = true;
                bySingleQuote = c == SINGLE_QUOTE;
                sb.append(DOUBLE_QUOTE);
            }
        }
        return Json.createReader(new StringReader(sb.toString())).readValue();
    }
}
