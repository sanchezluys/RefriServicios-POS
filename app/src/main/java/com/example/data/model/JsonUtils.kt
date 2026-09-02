package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object JsonUtils {
    fun usedPartsToJson(parts: List<UsedPartItem>): String {
        val array = JSONArray()
        for (part in parts) {
            val obj = JSONObject()
            obj.put("inventoryId", part.inventoryId)
            obj.put("partName", part.partName)
            obj.put("quantity", part.quantity)
            obj.put("unitPrice", part.unitPrice)
            obj.put("unit", part.unit)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToUsedParts(json: String?): List<UsedPartItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<UsedPartItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    UsedPartItem(
                        inventoryId = obj.optLong("inventoryId", 0L),
                        partName = obj.optString("partName", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        unit = obj.optString("unit", "uds")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun invoiceLinesToJson(items: List<InvoiceLineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("description", item.description)
            obj.put("quantity", item.quantity)
            obj.put("unitPrice", item.unitPrice)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToInvoiceLines(json: String?): List<InvoiceLineItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceLineItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceLineItem(
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }
}
