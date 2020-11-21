package moe.tristan.kmdah;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public final class MockWebServerSupport {

    private MockWebServer mockWebServer;

    public String start() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String hostName = mockWebServer.getHostName();
        int port = mockWebServer.getPort();
        return "http://" + hostName + ":" + port;
    }

    public void stop() throws IOException {
        mockWebServer.close();
    }

    public void enqueue(MockResponse mockResponse) {
        mockWebServer.enqueue(mockResponse);
    }

    public RecordedRequest takeRequest() {
        try {
            return mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
