package net.minecraft.client.resources;

public class I18n {
   private static Locale i18nLocale;

   static void setLocale(Locale i18nLocaleIn) {
      i18nLocale = i18nLocaleIn;
   }

   public static String format(String translateKey, Object... parameters) {
      return i18nLocale.formatMessage(translateKey, parameters);
   }

   public static boolean hasKey(String key) {
      return i18nLocale.hasKey(key);
   }
}
