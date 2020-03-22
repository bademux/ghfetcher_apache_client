package com.github.bademux.ghfetcher.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static java.util.Objects.nonNull;

public final class RecordingWireMock extends TestWatcher {

    private final WireMockServer wireMock;
    private final String serverUrl;
    private final boolean forceRecord;
    private final FileSource mappingsDir;

    public RecordingWireMock(WireMockServer wireMock, String targetServerUrl, Boolean forceRecord) {
        this.wireMock = wireMock;
        this.serverUrl = targetServerUrl;
        this.forceRecord = Boolean.TRUE.equals(forceRecord);
        mappingsDir = wireMock.getOptions().filesRoot().child(MAPPINGS_ROOT);
    }

    public RecordingWireMock(WireMockServer wireMock, String targetServerUrl) {
        this(wireMock, targetServerUrl, forceRecordDefault());
    }

    private static boolean forceRecordDefault() {
        return nonNull(System.getProperty("--force-record"));
    }

    @Override
    protected void starting(Description description) {
        handleForceRecord();
        if (isRecordingMode()) {
            prepareFolders();
            startRecordingForDefinedServer();
        }
    }

    @Override
    protected void finished(Description description) {
        stopRecordingIfNeeded();
    }

    private void stopRecordingIfNeeded() {
        if (RecordingStatus.Recording.equals(wireMock.getRecordingStatus().getStatus())) {
            mappingsDir.createIfNecessary();
            wireMock.getOptions().filesRoot().child(FILES_ROOT).createIfNecessary();
            wireMock.snapshotRecord();
        }
    }

    private void handleForceRecord() {
        if (forceRecord) {
            try {
                cleanupMappings();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void prepareFolders() {
        mappingsDir.createIfNecessary();
    }

    private boolean isRecordingMode() {
        return !mappingsDir.exists() || mappingsDir.listFilesRecursively().isEmpty();
    }

    private void startRecordingForDefinedServer() {
        wireMock.startRecording(serverUrl);
    }

    private void cleanupMappings() throws IOException {
        if (!mappingsDir.exists()) {
            return;
        }
        for (TextFile textFile : mappingsDir.listFilesRecursively()) {
            Files.delete(Paths.get(textFile.getPath()));
        }
    }

}