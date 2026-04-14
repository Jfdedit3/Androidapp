package com.jfdedit3.robloxuserfinder

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object RobloxApi {
    fun searchUsers(keyword: String, limit: Int = 25): List<RobloxUser> {
        val encoded = URLEncoder.encode(keyword, Charsets.UTF_8.name())
        val searchUrl = "https://users.roblox.com/v1/users/search?keyword=$encoded&limit=$limit"
        val searchJson = getJson(searchUrl)
        val searchData = searchJson.optJSONArray("data") ?: JSONArray()

        val ids = mutableListOf<Long>()
        val baseUsers = mutableListOf<Pair<Long, Pair<String, String>>>()

        for (i in 0 until searchData.length()) {
            val item = searchData.getJSONObject(i)
            val id = item.optLong("id")
            val name = item.optString("name")
            val displayName = item.optString("displayName")
            ids.add(id)
            baseUsers.add(id to (name to displayName))
        }

        val avatarMap = fetchAvatarUrls(ids)

        return baseUsers.map { (id, names) ->
            RobloxUser(
                id = id,
                username = names.first,
                displayName = names.second,
                avatarUrl = avatarMap[id].orEmpty()
            )
        }
    }

    private fun fetchAvatarUrls(ids: List<Long>): Map<Long, String> {
        if (ids.isEmpty()) return emptyMap()
        val idsParam = ids.joinToString(",")
        val url = "https://thumbnails.roblox.com/v1/users/avatar-headshot?userIds=$idsParam&size=150x150&format=Png&isCircular=false"
        val json = getJson(url)
        val data = json.optJSONArray("data") ?: JSONArray()
        val result = mutableMapOf<Long, String>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            result[item.optLong("targetId")] = item.optString("imageUrl")
        }
        return result
    }

    private fun getJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/json")

        val text = connection.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }
}
