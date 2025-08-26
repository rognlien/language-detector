package com.github.rognlien

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

object LanguageProfileCodec {
    @JvmStatic
    fun write(
        languageProfile: LanguageProfile,
        outputStream: OutputStream,
    ) {
        with(languageProfile) {
            outputStream.use { out ->
                DataOutputStream(out).use { dos ->
                    dos.writeUTF(language)
                    dos.writeInt(ngrams.size)
                    ngrams.forEach { (ngram, freq) ->
                        dos.writeUTF(ngram)
                        dos.writeDouble(freq)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun read(inputStream: InputStream): LanguageProfile {
        return inputStream.use { input ->
            DataInputStream(input).use { dis ->
                val lang = dis.readUTF()
                val size = dis.readInt()
                val ngrams = mutableMapOf<String, Double>()
                repeat(size) {
                    val ngram = dis.readUTF()
                    val freq = dis.readDouble()
                    ngrams[ngram] = freq
                }
                LanguageProfile(lang, ngrams)
            }
        }
    }
}
