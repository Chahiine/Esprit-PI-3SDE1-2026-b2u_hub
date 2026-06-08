package com.example.pi.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiPropertiesTest {

    @Test
    void isConfigured_falseWhenDisabled() {
        GeminiProperties props = new GeminiProperties();
        props.setEnabled(false);
        props.setApiKey("my-key");

        assertThat(props.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_falseWhenApiKeyBlank() {
        GeminiProperties props = new GeminiProperties();
        props.setEnabled(true);
        props.setApiKey("  ");

        assertThat(props.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_trueWhenEnabledWithKey() {
        GeminiProperties props = new GeminiProperties();
        props.setEnabled(true);
        props.setApiKey("AIza-test-key");
        props.setModel("gemini-2.5-flash");

        assertThat(props.isConfigured()).isTrue();
        assertThat(props.getModel()).isEqualTo("gemini-2.5-flash");
    }
}
