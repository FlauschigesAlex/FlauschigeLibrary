package at.flauschigesalex.defaultLibrary.translation

object TranslationValidator {
    fun validateKey(input: String): KeyResponse {
        var translationKey = input

        if (translationKey.isEmpty() || translationKey.isBlank())
            throw TranslationException("TranslationKey cannot be empty.")

        translationKey = translationKey.trim()
        if (!translationKey.contains(".")) {
            System.err.println("TranslationKey must have sub-key: Required : main.sub -> Provided : $translationKey")
            return KeyResponse(false, translationKey)
        }

        if (translationKey.startsWith(".") || translationKey.endsWith(".")) {
            System.err.println("TranslationKey is malformed: Cannot start or end with \".\"")
            return KeyResponse(false, translationKey)
        }

        return KeyResponse(true, translationKey)
    }

    data class KeyResponse(val success: Boolean, val translationKey: String) {
        val failure = !success
    }
}
