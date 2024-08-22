package edu.tcu.cs.hogwartsartifactsonline.client.ai.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwartsartifactsonline.client.ai.chat.dto.ChatResponse;
import edu.tcu.cs.hogwartsartifactsonline.client.ai.chat.dto.Choice;
import edu.tcu.cs.hogwartsartifactsonline.client.ai.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(MetaLlamaChatClient.class)
class MetaLlamaChatClientTest {

    @Autowired
    private MetaLlamaChatClient metaLlamaChatClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private String url;

    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        this.url = "https://api-inference.huggingface.co/models/meta-llama/Meta-Llama-3-8B-Instruct/v1/chat/completions";

        this.chatRequest = new ChatRequest("meta-llama/Meta-Llama-3-8B-Instruct", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array")
        ));
    }

    @Test
    void testGenerateSuccess() throws JsonProcessingException {
        // Given
        ChatResponse chatResponse = new ChatResponse(List.of(new Choice(0, new Message("assistant", "The summary includes six artifacts, owned by three different wizards."))));

        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", startsWith("Bearer ")))
                .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest)))
                .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));

        // When
        ChatResponse generatedChatResponse = this.metaLlamaChatClient.generate(this.chatRequest);

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect and andExpect were indeed performed.
        assertThat(generatedChatResponse.choices().get(0).message().content()).isEqualTo("The summary includes six artifacts, owned by three different wizards.");
    }

    @Test
    void testGenerateUnauthorizedRequest() throws JsonProcessingException {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.metaLlamaChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockServer.verify();
        assertThat(thrown)
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    /**
     * This test simulates a scenario where the service receives a 429 Quota Exceeded response.
     */
    @Test
    void testGenerateQuotaExceeded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withTooManyRequests());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse chatResponse = this.metaLlamaChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    /**
     * This test simulates receiving a 500 Internal Server Error response.
     */
    @Test
    void testGenerateServerError() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse chatResponse = this.metaLlamaChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    /**
     * This test simulates receiving a 503 Service Unavailable response.
     */
    @Test
    void testGenerateServerOverloaded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServiceUnavailable());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse chatResponse = this.metaLlamaChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
}