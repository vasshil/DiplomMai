//package core
//
//import com.jme3.math.Vector3f
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.SerializationException
//import kotlinx.serialization.builtins.ListSerializer
//import kotlinx.serialization.descriptors.SerialDescriptor
//import kotlinx.serialization.descriptors.buildClassSerialDescriptor
//import kotlinx.serialization.descriptors.element
//import kotlinx.serialization.encoding.CompositeDecoder
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//
//// Создаем сериализатор для Vector3f
//object Vector3fSerializer : KSerializer<Vector3f> {
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vector3f") {
//        element<Float>("x")
//        element<Float>("y")
//        element<Float>("z")
//    }
//
//    override fun serialize(encoder: Encoder, value: Vector3f) {
//        val compositeEncoder = encoder.beginStructure(descriptor)
//        compositeEncoder.encodeFloatElement(descriptor, 0, value.x)
//        compositeEncoder.encodeFloatElement(descriptor, 1, value.y)
//        compositeEncoder.encodeFloatElement(descriptor, 2, value.z)
//        compositeEncoder.endStructure(descriptor)
//    }
//
//    override fun deserialize(decoder: Decoder): Vector3f {
//        val compositeDecoder = decoder.beginStructure(descriptor)
//        var x = 0f
//        var y = 0f
//        var z = 0f
//
//        loop@ while (true) {
//            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
//                0 -> x = compositeDecoder.decodeFloatElement(descriptor, 0)
//                1 -> y = compositeDecoder.decodeFloatElement(descriptor, 1)
//                2 -> z = compositeDecoder.decodeFloatElement(descriptor, 2)
//                CompositeDecoder.DECODE_DONE -> break@loop
//                else -> throw SerializationException("Unexpected index: $index")
//            }
//        }
//        compositeDecoder.endStructure(descriptor)
//        return Vector3f(x, y, z)
//    }
//}
//
//
//// Сериализатор для MutableList<Vector3f>
//object MutableListVector3fSerializer : KSerializer<MutableList<Vector3f>> {
//
//    private val listSerializer = ListSerializer(this)
//    override val descriptor: SerialDescriptor = listSerializer.descriptor
//
//    override fun serialize(encoder: Encoder, value: MutableList<Vector3f>) {
//        val compositeEncoder = encoder.beginCollection(descriptor, value.size)
//        value.forEachIndexed { index, vector3f ->
//            compositeEncoder.encodeSerializableElement(descriptor, index, Vector3fSerializer, vector3f)
//        }
//        compositeEncoder.endStructure(descriptor)
//    }
//
//    override fun deserialize(decoder: Decoder): MutableList<Vector3f> {
//        val compositeDecoder = decoder.beginStructure(descriptor)
//        val list = mutableListOf<Vector3f>()
//
//        loop@ while (true) {
//            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
//                CompositeDecoder.DECODE_DONE -> break@loop
//                else -> list.add(compositeDecoder.decodeSerializableElement(descriptor, index, Vector3fSerializer))
//            }
//        }
//        compositeDecoder.endStructure(descriptor)
//        return list
//    }
//}