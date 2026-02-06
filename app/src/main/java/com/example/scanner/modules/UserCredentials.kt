package com.example.scanner.modules

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

data class UserCredentials(
    val login: String,
    val password: String,
    val uid: String
)

class SecureStorage(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_storage"
        private const val MASTER_KEY_ALIAS = "master_key_alias"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val KEY_SIZE = 256
    }

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val secretKey: SecretKey by lazy {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val encodedKey = sharedPrefs.getString(MASTER_KEY_ALIAS, null)

        return if (encodedKey != null) {
            val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
            SecretKeySpec(keyBytes, "AES")
        } else {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE)
            val newKey = keyGenerator.generateKey()

            val encodedNewKey = Base64.encodeToString(newKey.encoded, Base64.DEFAULT)
            sharedPrefs.edit()
                .putString(MASTER_KEY_ALIAS, encodedNewKey)
                .apply()

            newKey
        }
    }

    private fun encrypt(data: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv

        return Pair(
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
            Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }

    private fun decrypt(encryptedData: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))

        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun saveCredentials(uid: String, login: String, password: String): Boolean {
        return try {
            val data = "$login|$password"
            val (encryptedData, iv) = encrypt(data)

            sharedPrefs.edit()
                .putString("${uid}_data", encryptedData)
                .putString("${uid}_iv", iv)
                .apply()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCredentials(uid: String): UserCredentials? {
        return try {
            val encryptedData = sharedPrefs.getString("${uid}_data", null)
            val iv = sharedPrefs.getString("${uid}_iv", null)

            if (encryptedData != null && iv != null) {
                val decryptedData = decrypt(encryptedData, iv)
                val parts = decryptedData.split("|")

                if (parts.size == 2) {
                    UserCredentials(
                        login = parts[0],
                        password = parts[1],
                        uid = uid
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun clearAll() {
        sharedPrefs.edit().clear().apply()
    }

    // Метод для отладки
    fun debugGetAllUids(): List<String> {
        val allEntries = sharedPrefs.all
        val uids = mutableListOf<String>()

        allEntries.forEach { (key, _) ->
            if (key.endsWith("_data")) {
                val uid = key.removeSuffix("_data")
                uids.add(uid)
            }
        }

        return uids
    }

    fun removeCredentials(uid: String) {
        sharedPrefs.edit().apply {
            remove("${uid}_data")  // Удаляем зашифрованные данные
            remove("${uid}_iv")    // Удаляем IV
            apply()
        }
    }
}