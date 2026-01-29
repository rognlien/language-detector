package com.github.rognlien

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object LanguageProfileCodec {
    @JvmStatic
    fun write(
        languageProfile: LanguageProfile,
        outputStream: OutputStream,
    ) {
        with(languageProfile) {
            GZIPOutputStream(outputStream).use { gz ->
                DataOutputStream(gz).use { dos ->
                    dos.writeUTF(language)
                    dos.writeInt(ngrams.size)
                    ngrams.forEach { (ngram, freq) ->
                        dos.writeUTF(ngram)
                        dos.writeFloat(freq.toFloat())
                    }
                }
            }
        }
    }

    @JvmStatic
    fun read(inputStream: InputStream): LanguageProfile {
        return GZIPInputStream(inputStream).use { gz ->
            DataInputStream(gz).use { dis ->
                val lang = dis.readUTF()
                val size = dis.readInt()
                val ngrams = mutableMapOf<String, Double>()
                repeat(size) {
                    val ngram = dis.readUTF()
                    val freq = dis.readFloat().toDouble()
                    ngrams[ngram] = freq
                }
                LanguageProfile(lang, ngrams)
            }
        }
    }
}
