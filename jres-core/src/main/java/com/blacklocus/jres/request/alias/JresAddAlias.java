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
package com.blacklocus.jres.request.alias;

import com.blacklocus.jres.request.JresJsonRequest;
import com.blacklocus.jres.response.common.JresAcknowledgedReply;
import com.blacklocus.jres.response.common.JresErrorReplyException;
import com.blacklocus.jres.strings.JresPaths;
import org.apache.http.client.methods.HttpPut;

/**
 * <a href="http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-aliases.html#alias-adding">Add Single Index Alias API</a>
 * <p>
 * Can throw {@link JresErrorReplyException}.
 */
public class JresAddAlias extends JresJsonRequest<JresAcknowledgedReply> {

    private final String index;
    private final String alias;

    /**
     * @param index the actual index
     * @param alias another name by which the actual index may be addressed
     */
    public JresAddAlias(String index, String alias) {
        super(JresAcknowledgedReply.class);
        this.index = index;
        this.alias = alias;
    }

    @Override
    public String getHttpMethod() {
        return HttpPut.METHOD_NAME;
    }

    @Override
    public String getPath() {
        return JresPaths.slashedPath(index) + "_alias/" + alias;
    }

    @Override
    public Object getPayload() {
        return null;
    }

}
