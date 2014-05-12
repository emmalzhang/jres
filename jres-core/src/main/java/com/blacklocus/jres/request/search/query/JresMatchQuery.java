/**
 * Copyright 2013 BlackLocus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blacklocus.jres.request.search.query;

import com.blacklocus.jres.strings.ObjectMappers;

import java.util.HashMap;

public class JresMatchQuery extends HashMap<String, Object> implements JresQuery {

    public JresMatchQuery() {
    }

    public JresMatchQuery(String field, Object subquery) {
        put(field, subquery);
    }

    public JresMatchQuery addField(String field, Object subquery) {
        put(field, subquery);
        return this;
    }

    @Override
    public String queryType() {
        return "match";
    }

    @Override
    public String toString() {
        return ObjectMappers.toJson(this);
    }

}