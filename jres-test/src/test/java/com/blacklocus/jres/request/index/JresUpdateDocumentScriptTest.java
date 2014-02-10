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
package com.blacklocus.jres.request.index;

import com.blacklocus.jres.BaseJresTest;
import com.blacklocus.jres.request.JresBulkable;
import com.blacklocus.jres.request.bulk.JresBulk;
import com.blacklocus.jres.request.document.JresGetDocument;
import com.blacklocus.jres.request.search.JresSearch;
import com.blacklocus.jres.response.common.JresErrorReplyException;
import com.blacklocus.jres.response.search.JresSearchReply;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JresUpdateDocumentScriptTest extends BaseJresTest {

    @Test(expected = JresErrorReplyException.class)
    public void testPartialUpdatesNoUpsert() {
        String index = "JresUpdateDocumentScriptTest.testPartialUpdatesNoUpsert".toLowerCase();
        String type = "test";
        String id1 = "Fotel 6";

        // Script doesn't matter because document does not exist and this is not an upsert.
        jres.quest(new JresUpdateDocumentScript(index, type, id1, ""));
    }

    @Test
    public void testPartialUpdatesWithUpsertOption() {
        String index = "JresUpdateDocumentScriptTest.testPartialUpdatesWithUpsertOption".toLowerCase();
        String type = "test";
        String id1 = "horchata";
        String id2 = "margarita";

        Object updateDoc1WithFood = ImmutableMap.of("description", Arrays.asList("Es horchata"));
        Object updateDoc2WithFood = ImmutableMap.of("description", Arrays.asList("Es margarita"));
        // Scripts don't matter, because this should fall back to insert since the docs don't exist.
        jres.quest(new JresUpdateDocumentScript(index, type, id1, "", Collections.<String, Object>emptyMap(), updateDoc1WithFood));
        jres.quest(new JresUpdateDocumentScript(index, type, id2, "", Collections.<String, Object>emptyMap(), updateDoc2WithFood));
        jres.quest(new JresRefresh(index));

        JresSearchReply searchReply = jres.quest(new JresSearch(index, type));
        Assert.assertEquals((Object) 2L, searchReply.getHits().getTotal());


        String updateDoc1Script = "ctx._source.description += description";
        Map<String, String> updateDoc1With2ndDesc = ImmutableMap.of("description", "¡Sí, es final!");
        jres.quest(new JresUpdateDocumentScript(index, type, id1, updateDoc1Script, updateDoc1With2ndDesc));
        jres.quest(new JresRefresh(index));

        searchReply = jres.quest(new JresSearch(index, type));
        Assert.assertEquals("Still 2 documents", (Object) 2L, searchReply.getHits().getTotal());

        Map<String, List<String>> doc1 = jres.quest(new JresGetDocument(index, type, id1))
                .getSourceAsType(new TypeReference<Map<String, List<String>>>() {});
        Assert.assertEquals(ImmutableMap.of(
                "description", Arrays.asList("Es horchata", "¡Sí, es final!")
        ), doc1);

        Map<String, List<String>> doc2 = jres.quest(new JresGetDocument(index, type, id2)).getSourceAsType(
                new TypeReference<Map<String, List<String>>>() {});
        Assert.assertEquals(ImmutableMap.of(
                "description", Arrays.asList("Es margarita")
        ), doc2);
    }

    @Test
    public void testBulkPartialUpdates() {
        String index = "JresUpdateDocumentScriptTest.testBulkPartialUpdates".toLowerCase();
        String type = "test";
        String id1 = "horchata";
        String id2 = "margarita";

        Map<String, List<String>> updateDoc1WithFood = ImmutableMap.of("description", Arrays.asList("Es horchata"));
        Map<String, List<String>> updateDoc2WithFood = ImmutableMap.of("description", Arrays.asList("Es margarita"));
        // Scripts don't matter, because this should fall back to insert since the docs don't exist.
        jres.quest(new JresBulk(index, type, Arrays.<JresBulkable>asList(
                new JresUpdateDocumentScript(index, type, id1, "", Collections.<String, Object>emptyMap(), updateDoc1WithFood),
                new JresUpdateDocumentScript(index, type, id2, "", Collections.<String, Object>emptyMap(), updateDoc2WithFood)
        )));
        jres.quest(new JresRefresh(index));

        JresSearchReply searchReply = jres.quest(new JresSearch(index, type));
        Assert.assertEquals((Object) 2L, searchReply.getHits().getTotal());


        String updateDoc1Script = "ctx._source.description += description";
        Map<String, String> updateDoc1With2ndDesc = ImmutableMap.of("description", "¡Sí, es final!");
        jres.quest(new JresBulk(index, type, Arrays.<JresBulkable>asList(
                // results in update of existing doc
                new JresUpdateDocumentScript(index, type, id1, updateDoc1Script, updateDoc1With2ndDesc),
                // results in insert of new doc
                new JresUpdateDocumentScript(index, type, "id3", "", Collections.<String, Object>emptyMap(), ImmutableMap.of(
                        "description", Arrays.asList("the 3rd document")
                ))
        )));
        jres.quest(new JresRefresh(index));

        searchReply = jres.quest(new JresSearch(index, type));
        Assert.assertEquals("3 total documents", (Object) 3L, searchReply.getHits().getTotal());


        Map<String, List<String>> doc1 = jres.quest(new JresGetDocument(index, type, id1))
                .getSourceAsType(new TypeReference<Map<String, List<String>>>() {});
        Assert.assertEquals(ImmutableMap.of(
                "description", Arrays.asList("Es horchata", "¡Sí, es final!")
        ), doc1);

        Map<String, List<String>> doc2 = jres.quest(new JresGetDocument(index, type, id2))
                .getSourceAsType(new TypeReference<Map<String, List<String>>>() {});
        Assert.assertEquals(ImmutableMap.of(
                "description", Arrays.asList("Es margarita")
        ), doc2);
    }
}
