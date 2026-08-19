package com.example.ui

object TelexConverter {
    fun convert(input: String): String {
        if (input.isEmpty()) return input
        val words = input.split(" ")
        val convertedWords = words.map { convertWord(it) }
        return convertedWords.joinToString(" ")
    }

    private fun convertWord(word: String): String {
        if (word.isEmpty()) return word
        
        var cleanWord = word
        
        // Handle dd -> đ / DD -> Đ
        cleanWord = cleanWord.replace("dd", "đ").replace("Dd", "Đ").replace("DD", "Đ")
        
        // Handle double vowel additions in Telex
        cleanWord = cleanWord.replace("aa", "â").replace("Aa", "Â")
        cleanWord = cleanWord.replace("ee", "ê").replace("Ee", "Ê")
        cleanWord = cleanWord.replace("oo", "ô").replace("Oo", "Ô")
        
        // Handle w transformations
        cleanWord = cleanWord.replace("aw", "ă").replace("Aw", "Ă")
        cleanWord = cleanWord.replace("ow", "ơ").replace("Ow", "Ơ")
        cleanWord = cleanWord.replace("uw", "ư").replace("Uw", "Ư")
        
        // If there's a lone 'w' remaining after vowels
        if (cleanWord.contains("w") || cleanWord.contains("W")) {
            if (cleanWord.contains("u", ignoreCase = true)) {
                cleanWord = cleanWord.replace("uw", "ư", ignoreCase = true)
                    .replace("u", "ư", ignoreCase = true)
                    .replace("w", "", ignoreCase = true)
            } else if (cleanWord.contains("o", ignoreCase = true)) {
                cleanWord = cleanWord.replace("ow", "ơ", ignoreCase = true)
                    .replace("o", "ơ", ignoreCase = true)
                    .replace("w", "", ignoreCase = true)
            } else if (cleanWord.contains("a", ignoreCase = true)) {
                cleanWord = cleanWord.replace("aw", "ă", ignoreCase = true)
                    .replace("a", "ă", ignoreCase = true)
                    .replace("w", "", ignoreCase = true)
            } else {
                cleanWord = cleanWord.replace("w", "ư", ignoreCase = true)
            }
        }

        // Now handle tone marks: s, f, r, x, j
        val toneKeys = listOf('s', 'f', 'r', 'x', 'j', 'z')
        var foundToneKey: Char? = null
        var tempWord = cleanWord
        
        for (key in toneKeys) {
            if (tempWord.endsWith(key, ignoreCase = true)) {
                foundToneKey = key.lowercaseChar()
                tempWord = tempWord.substring(0, tempWord.length - 1)
                break
            }
        }
        
        if (foundToneKey == null) {
            return cleanWord
        }

        // Sếp rule: "như phần dấu thì ko bắt buộc khi s là dấu sắc nếu 1 lần nữa là ra s"
        // Let's generalize it: if the tempWord already has the exact tone mark corresponding to the foundToneKey,
        // we remove that tone mark (restore to base vowel) and append the literal toneKey character at the end!
        val toneIndex = when (foundToneKey) {
            's' -> 1 // dấu sắc
            'f' -> 2 // dấu huyền
            'r' -> 3 // dấu hỏi
            'x' -> 4 // dấu ngã
            'j' -> 5 // dấu nặng
            else -> 0
        }
        
        if (toneIndex > 0) {
            val vowelWithTones = mapOf(
                'a' to listOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
                'ă' to listOf('ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ'),
                'â' to listOf('â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
                'e' to listOf('e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'),
                'ê' to listOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'),
                'i' to listOf('i', 'í', 'ì', 'ỉ', 'ĩ', 'ị'),
                'o' to listOf('o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'),
                'ô' to listOf('ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'),
                'ơ' to listOf('ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'),
                'u' to listOf('u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'),
                'ư' to listOf('ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'),
                'y' to listOf('y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ'),
                'A' to listOf('A', 'Á', 'À', 'Ả', 'Ã', 'Ạ'),
                'Ă' to listOf('Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ'),
                'Â' to listOf('Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ'),
                'E' to listOf('E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ'),
                'Ê' to listOf('Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ'),
                'I' to listOf('I', 'Í', 'Ì', 'Ỉ', 'Ĩ', 'Ị'),
                'O' to listOf('O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ'),
                'Ô' to listOf('Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ'),
                'Ơ' to listOf('Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ'),
                'U' to listOf('U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ'),
                'Ư' to listOf('Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'),
                'Y' to listOf('Y', 'Ý', 'Ỳ', 'Ỷ', 'Ỹ', 'Ỵ')
            )
            
            var alreadyHasThisTone = false
            var markedCharIndex = -1
            var baseChar: Char = ' '
            
            for (i in tempWord.indices) {
                val c = tempWord[i]
                for ((base, list) in vowelWithTones) {
                    if (list.getOrNull(toneIndex) == c) {
                        alreadyHasThisTone = true
                        markedCharIndex = i
                        baseChar = base
                        break
                    }
                }
                if (alreadyHasThisTone) break
            }
            
            if (alreadyHasThisTone && markedCharIndex != -1) {
                val sb = java.lang.StringBuilder(tempWord)
                sb.setCharAt(markedCharIndex, baseChar)
                return sb.toString() + foundToneKey
            }
        }
        
        return applyToneMark(tempWord, foundToneKey)
    }

    private fun applyToneMark(word: String, toneKey: Char): String {
        val vowelWithTones = mapOf(
            'a' to listOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
            'ă' to listOf('ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ'),
            'â' to listOf('â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
            'e' to listOf('e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'),
            'ê' to listOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'),
            'i' to listOf('i', 'í', 'ì', 'ỉ', 'ĩ', 'ị'),
            'o' to listOf('o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'),
            'ô' to listOf('ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'),
            'ơ' to listOf('ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'),
            'u' to listOf('u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'),
            'ư' to listOf('ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'),
            'y' to listOf('y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ'),
            
            'A' to listOf('A', 'Á', 'À', 'Ả', 'Ã', 'Ạ'),
            'Ă' to listOf('Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ'),
            'Â' to listOf('Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ'),
            'E' to listOf('E', 'É', 'È', 'Ẻ', 'E', 'Ẹ'),
            'Ê' to listOf('Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ'),
            'I' to listOf('I', 'Í', 'Ì', 'Ỉ', 'Ĩ', 'Ị'),
            'O' to listOf('O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ'),
            'Ô' to listOf('Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ'),
            'Ơ' to listOf('Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ'),
            'U' to listOf('U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ'),
            'Ư' to listOf('Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'),
            'Y' to listOf('Y', 'Ý', 'Ỳ', 'Ỷ', 'Ỹ', 'Ỵ')
        )

        val toneIndex = when (toneKey) {
            's' -> 1
            'f' -> 2
            'r' -> 3
            'x' -> 4
            'j' -> 5
            else -> 0
        }
        
        val vowels = listOf('a', 'ă', 'â', 'e', 'ê', 'i', 'o', 'ô', 'ơ', 'u', 'ư', 'y', 'A', 'Ă', 'Â', 'E', 'Ê', 'I', 'O', 'Ô', 'Ơ', 'U', 'Ư', 'Y')
        val vowelIndices = mutableListOf<Int>()
        for (i in word.indices) {
            if (word[i] in vowels) {
                vowelIndices.add(i)
            }
        }
        
        if (vowelIndices.isEmpty()) return word
        
        val targetIndex = when (vowelIndices.size) {
            1 -> vowelIndices[0]
            2 -> {
                val pair = word.substring(vowelIndices[0], vowelIndices[1] + 1).lowercase()
                if (pair == "oa" || pair == "oe" || pair == "uy" || pair == "ue" || pair == "uơ") {
                    vowelIndices[1]
                } else {
                    vowelIndices[0]
                }
            }
            3 -> vowelIndices[1]
            else -> vowelIndices.last()
        }
        
        val charToMark = word[targetIndex]
        val markedList = vowelWithTones[charToMark] ?: return word
        val markedChar = markedList.getOrNull(toneIndex) ?: charToMark
        
        val sb = java.lang.StringBuilder(word)
        sb.setCharAt(targetIndex, markedChar)
        return sb.toString()
    }
}
