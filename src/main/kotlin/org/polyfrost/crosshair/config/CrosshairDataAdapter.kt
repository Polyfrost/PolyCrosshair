package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.serialize.ObjectSerializer
import org.polyfrost.oneconfig.api.config.v1.serialize.adapter.ComplexAdapter

class CrosshairDataAdapter : ComplexAdapter<CrosshairData>() {
    override fun serialize(data: CrosshairData): Map<String, Any?> {
        val out = LinkedHashMap<String, Any?>(2)
        out["current"] = ObjectSerializer.INSTANCE.serialize(data.current, true, false)
        out["presets"] = data.presets.map { ObjectSerializer.INSTANCE.serialize(it, true, false) }
        return out
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(map: Map<String, Any?>): CrosshairData {
        val data = CrosshairData()
        (map["current"] as? Map<String, Any?>)?.let {
            data.current = ObjectSerializer.INSTANCE.deserialize(it) as CrosshairEntry
        }
        (map["presets"] as? Collection<*>)?.forEach { e ->
            (e as? Map<String, Any?>)?.let {
                data.presets.add(ObjectSerializer.INSTANCE.deserialize(it) as CrosshairEntry)
            }
        }
        return data
    }

    override fun getTargetClass(): Class<CrosshairData> = CrosshairData::class.java
}
