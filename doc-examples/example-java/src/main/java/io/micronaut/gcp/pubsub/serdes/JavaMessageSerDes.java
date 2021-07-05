/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.pubsub.serdes;
//tag::imports[]
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;

import jakarta.inject.Singleton;
import java.io.*;
//end::imports[]

// tag::clazz[]
@Singleton // <1>
public class JavaMessageSerDes implements PubSubMessageSerDes {

    @Override
    public String supportedType() { // <2>
        return "application/x.java";
    }

    @Override
    public Object deserialize(byte[] data, Argument<?> type) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Object result = null;
        try {
            ObjectInputStream reader = new ObjectInputStream(bin);
            result = reader.readObject();
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize object", e);
        }
        return result;
    }

    @Override
    public byte[] serialize(Object data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream writer = new ObjectOutputStream(baos);
            writer.writeObject(data);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize object", e);
        }
        return baos.toByteArray();
    }

}
// end::clazz[]
