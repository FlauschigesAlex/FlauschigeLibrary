package at.flauschigesalex.defaultLibrary.translation;

import at.flauschigesalex.defaultLibrary.exception.LibraryException;

public final class TranslationException extends LibraryException {

    static TranslationException DefaultLocaleAbsent = new TranslationException("Could not load find default translation: " + TranslationSource.defaultLocale.toLanguageTag());

    public TranslationException(String message) {
        super(message);
    }
}
