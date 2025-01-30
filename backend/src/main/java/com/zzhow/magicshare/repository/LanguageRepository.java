package com.zzhow.magicshare.repository;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class LanguageRepository {
    public static ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", Locale.of("zh", "Hans"));
    private static String language = "zh_Hans";

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String language) {
        LanguageRepository.language = language;
        bundle = ResourceBundle.getBundle("MessagesBundle", Locale.of(language.split("_")[0], language.split("_")[1]));
    }
}
