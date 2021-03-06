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
package com.blacklocus.jres.request;

import com.blacklocus.jres.request.index.JresIndexDocument;
import com.blacklocus.jres.request.index.JresUpdateDocument;
import com.blacklocus.jres.request.index.JresUpdateDocumentScript;
import com.blacklocus.jres.strings.ObjectMappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This test class must be run in total to work properly. It tests itself to the extent that it covers all the classes
 * that it must to fulfill its existence.
 */
public class JsonSerializableTest {

    static final Set<Class<? extends JresBulkable>> BULKABLE_CLASSES_SERIAL =
            Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends JresBulkable>, Boolean>());
    static final Set<Class<? extends JresBulkable>> BULKABLE_CLASSES_POLY =
            Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends JresBulkable>, Boolean>());

    /**
     * In supporting scenarios where users may want to serialize jres bulkable requests as a message format, the request
     * classes need to actually be serializable. This is a check on minimum viable functionality. If additional
     * properties are detected that are not actually fields, this test is OK with that. Actual request fields must make
     * it back to the deserialized object; the rest can be ignored (such as non-field, GETTER-inferred properties).
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestObjectsSerializable() {
        // In lexicographical order of class hierarchy under JresBulkable
        forSerial(new JresIndexDocument("index", "type", "id", "document", false), new TypeReference<JresIndexDocument>() {});
        forSerial(new JresUpdateDocument("index", "type", "id", "document", false, 3), new TypeReference<JresUpdateDocument>() {});
        forSerial(new JresUpdateDocumentScript("index", "type", "id", "the script", ImmutableMap.of(
                "field", "value",
                "another", Arrays.asList("two", "values")
        ), ImmutableMap.of(
                "fields", "initial",
                "another", Arrays.asList("one", "two")
        ), 5), new TypeReference<JresUpdateDocumentScript>() {});
    }

    @Test
    public void testPolymorphismSerializable() {
        List<JresBulkable> bulkActions = Lists.<JresBulkable>newArrayList(
                forPoly(new JresIndexDocument("index", "type", "id", "document", false)),
                forPoly(new JresUpdateDocument("index", "type", "id", "document", false, 3)),
                forPoly(new JresUpdateDocumentScript("index", "type", "id", "the script", ImmutableMap.of(
                        "field", "value",
                        "another", Arrays.asList("two", "values")
                ), ImmutableMap.of(
                        "fields", "initial",
                        "another", Arrays.asList("one", "two")
                ), 10))
        );
        String json = ObjectMappers.toJsonPretty(bulkActions);
        List<JresBulkable> backAgain = ObjectMappers.fromJson(json, new TypeReference<List<JresBulkable>>() {});
        Assert.assertEquals(bulkActions, backAgain);
    }

    /**
     * Serializes to json, serializes back to object, serializes back to json - the two json strings should match.
     * Depends on equals being correctly implemented and exhaustive of all core parameters of the request.
     * <p/>
     * Notes that the bulkable class has been included in {@link #testRequestObjectsSerializable()}
     */
    @SuppressWarnings("unchecked")
    <T extends JresBulkable> void forSerial(T bulkableRequest, TypeReference<T> typeReference) {
        T roundTrip = ObjectMappers.fromJson(ObjectMappers.toJson(bulkableRequest), typeReference);
        Assert.assertEquals(bulkableRequest, roundTrip);
        BULKABLE_CLASSES_SERIAL.add((Class<? extends JresBulkable>) ObjectMappers.NORMAL.getTypeFactory().constructType(typeReference.getType()).getRawClass());
    }

    /**
     * Notes that the bulkable class has been included in {@link #testPolymorphismSerializable()}
     */
    <T extends JresBulkable> T forPoly(T bulkableRequest) {
        BULKABLE_CLASSES_POLY.add(bulkableRequest.getClass());
        return bulkableRequest;
    }


    /**
     * Reflectively ensures that all {@link JresRequest} concrete classes were covered by this unit test
     * {@link #testRequestObjectsSerializable()} and {@link #testPolymorphismSerializable()}
     */
    @AfterClass
    public static void ensureClassesTested() {
        Reflections reflections = new Reflections("com.blacklocus.jres");

        Set<Class<? extends JresBulkable>> requestTypes = reflections.getSubTypesOf(JresBulkable.class);
        if (!BULKABLE_CLASSES_SERIAL.containsAll(requestTypes)) {
            System.err.println("Some JresBulkable types were not tested for JSON serializability");
            System.err.println(Sets.difference(requestTypes, BULKABLE_CLASSES_SERIAL));
            Assert.fail("All JresBulkable classes tested for JSON serialization");
        }
        if (!BULKABLE_CLASSES_POLY.containsAll(requestTypes)) {
            System.err.println("Some JresBulkable types were not tested for JSON polymorphic serializability");
            System.err.println(Sets.difference(requestTypes, BULKABLE_CLASSES_SERIAL));
            Assert.fail("All JresBulkable classes tested for JSON polymorphic serializability");
        }

    }

}
