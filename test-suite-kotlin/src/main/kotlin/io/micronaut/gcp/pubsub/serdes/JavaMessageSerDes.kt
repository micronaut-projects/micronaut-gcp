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
package io.micronaut.gcp.pubsub.serdes
//tag::imports[]
import io.micronaut.core.serialize.exceptions.SerializationException
import io.micronaut.core.type.Argument
import java.io.*
import jakarta.inject.Singleton
//end::imports[]

// tag::clazz[]
@Singleton // <1>
class JavaMessageSerDes : PubSubMessageSerDes {

	override fun supportedType(): String { // <2>
		return "application/x.java"
	}

	override fun deserialize(data: ByteArray, type: Argument<*>): Any {
		val bin = ByteArrayInputStream(data)
		var result: Any? = null
		result = try {
			val reader = ObjectInputStream(bin)
			reader.readObject()
		} catch (e: Exception) {
			throw SerializationException("Failed to deserialize object", e)
		}
		return result
	}

	override fun serialize(data: Any): ByteArray {
		val baos = ByteArrayOutputStream()
		try {
			val writer = ObjectOutputStream(baos)
			writer.writeObject(data)
		} catch (e: IOException) {
			throw SerializationException("Failed to serialize object", e)
		}
		return baos.toByteArray()
	}
}
// end::clazz[]
