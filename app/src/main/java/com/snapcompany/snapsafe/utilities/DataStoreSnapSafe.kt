package com.grupoLucich.snapdoit.app.Utilities

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.auth.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.utilities.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore("userPreferences")


@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSnapSafe(context: Context): ViewModel(){


    private val dataStore = context.dataStore

    private inline fun <reified T> Gson.fromJson(json: String): T =
        fromJson(json, object : TypeToken<T>() {}.type)

    private fun <K, V> Map<K, V>.toJson(): String = Gson().toJson(this)

    private inline fun <reified K, reified V> String.toMap(): Map<K, V> =
        Gson().fromJson<Map<K, V>>(this)





    fun saveKey(saveAs: String, value: String){
        val key = stringPreferencesKey(saveAs)
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun saveMap(key: String, map: Map<String, String>, context: Context) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = map.toJson()
            }
        }
    }

    fun saveUserData(userData: UserData){

        saveKey("userEmail", userData.email)
        saveKey("profileImageUrl", userData.profileImageUrl ?: "")
        saveKey("name", userData.name)
        saveKey("phone", userData.phone)
        saveKey("subscription", userData.subscription.toString())

        /*viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("userData")] = Gson().toJson(userData)
            }
        }*/
    }

    suspend fun getUserDataInline(): UserData {
        val objectType = object : TypeToken<UserData>() {}.type
        var userDataRetrieve = UserData()

        val deferred = viewModelScope.async {
            val preferences = dataStore.data.first()
            val userDataJson = preferences[stringPreferencesKey("userData")] ?: "{}"


            if (userDataJson != "{}") {
                try {
                    userDataRetrieve =  Gson().fromJson(userDataJson, objectType)
                } catch (e: Exception) {
                    Log.e("getUserData", e.message.toString())

                }
            }
            else{
                Log.e("getUserData", "User data is empty")
            }
        }

        deferred.join()

        return userDataRetrieve

    }


    fun deleteUserData(){
        deleteKey(stringPreferencesKey("userEmail"))
        deleteKey(stringPreferencesKey("name"))
        deleteKey(stringPreferencesKey("profileImageUrl"))
        deleteKey(stringPreferencesKey("phone"))
        deleteKey(stringPreferencesKey("subscription"))
    }

    fun getUserData(): UserData{
        val email = getString(stringPreferencesKey("userEmail"))
        val name = getString(stringPreferencesKey("name"))
        val profileImageUrl = getString(stringPreferencesKey("profileImageUrl"))
        val phone = getString(stringPreferencesKey("phone"))
        val subscription = getString(stringPreferencesKey("subscription"))

        return UserData(
            email = email,
            name = name,
            profileImageUrl = profileImageUrl,
            phone = phone,
            subscription = subscription.toBoolean()
        )


        /*val objectType = object : TypeToken<UserData>() {}.type

        viewModelScope.launch(Dispatchers.Default) {
            val preferences = dataStore.data.first()
            val userData = preferences[stringPreferencesKey("userData")] ?: "{}"

            if (userData != "{}") {
                try {
                    callback(Gson().fromJson(userData, objectType), true)
                } catch (e: Exception) {
                    Log.e("getUserData", e.message.toString())
                    callback(UserData(), false)
                }
            }
            else{
                callback(UserData(), false)
            }
        }*/
    }

    fun saveGateList(gateList: List<GateData>){
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("gateList")] = Gson().toJson(gateList)
            }
        }
    }

    fun getGateList(context: Context, callback: (List<GateData>) -> Unit){
        val listType = object : TypeToken<List<GateData>>() {}.type

        viewModelScope.launch(Dispatchers.Default) {
            val preferences = context.dataStore.data.first()
            val gateList = preferences[stringPreferencesKey("gateList")] ?: "{}"
            if (gateList != "{}") {
                try {
                    callback(Gson().fromJson(gateList, listType))
                }
                catch (e: Exception){
                    Log.e("getGateList", e.message.toString())
                }

            }
            else {
                Log.d("getGateList", "List empty")
            }
        }
    }

    fun getMap(key: String, index: Int, context: Context, callback: (map: Map<String, String>, index: Int) -> Unit) {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            val mapJson = preferences[stringPreferencesKey(key)] ?: "{}"
            callback(mapJson.toMap(), index)
        }
    }



    fun getString(key: Preferences.Key<String>): String {
        return viewModelScope.async {
            runBlocking {
                return@runBlocking dataStore.data.first().toPreferences()[key].orEmpty()
            }
        }.getCompleted()
    }

    fun deleteKey(key: Preferences.Key<String>){
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(key)
            }
        }
    }

    fun deleteAllGates(done: () -> Unit){
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey("gateList"))
                done()
            }
        }
    }
}