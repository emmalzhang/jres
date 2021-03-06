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
package com.blacklocus.jres.handler;

import com.blacklocus.jres.response.JresReply;
import org.apache.http.client.ResponseHandler;

abstract class AbstractJresResponseHandler<REPLY extends JresReply> implements ResponseHandler<REPLY> {

    private final Class<REPLY> replyClass;

    AbstractJresResponseHandler(Class<REPLY> replyClass) {
        this.replyClass = replyClass;
    }

    public Class<REPLY> getReplyClass() {
        return replyClass;
    }

}
