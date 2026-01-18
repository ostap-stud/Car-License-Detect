package com.github.ostap_stud.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateSerializer : KSerializer<Date> {

    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.ostap_stud.Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        val decoded = decoder.decodeString()
        return formatter.parse(decoded) ?: throw SerializationException("Invalid date format")
    }

    override fun serialize(encoder: Encoder, value: Date) {
        val formatted = formatter.format(value)
        encoder.encodeString(formatted)
    }

}