package eu.reportincident.incident_service.service;

public interface TranslationService {

    String detectLanguage(String text);

    String translateText(String text, String targetLanguageCode);

    void closeClient();
}