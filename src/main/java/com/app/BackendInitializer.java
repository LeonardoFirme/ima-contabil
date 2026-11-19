package com.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class BackendInitializer {

    private static Process backendProcess;
    private static boolean startedByUs = false;

    public static void startBackend() {
        try {
            if (checkHealthEndpoint()) {
                startedByUs = false;
                return;
            }

            killExistingBackendProcess();

            File backendExe = localizarBackendExe();
            if (backendExe == null || !backendExe.exists()) {
                throw new IllegalStateException(
                    "Binário do backend não localizado no diretório de instalação nem em ambiente de desenvolvimento."
                );
            }

            ProcessBuilder pb = new ProcessBuilder(backendExe.getAbsolutePath());
            pb.directory(backendExe.getParentFile());
            pb.redirectErrorStream(true);

            backendProcess = pb.start();
            startedByUs = true;

            StringBuilder backendLogs = new StringBuilder();
            Thread outputConsumer = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(backendProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("[Backend .NET] " + line);
                        backendLogs.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });
            outputConsumer.setDaemon(true);
            outputConsumer.start();

            boolean isRunning = false;
            for (int attempt = 1; attempt <= 30; attempt++) {
                Thread.sleep(500);
                if (checkHealthEndpoint()) {
                    isRunning = true;
                    break;
                }
                if (!backendProcess.isAlive()) {
                    break;
                }
            }

            if (!isRunning) {
                throw new RuntimeException(
                    "O servidor backend .NET não respondeu na porta 5000.\n--- LOGS DO BACKEND ---\n" + backendLogs
                );
            }

        } catch (Exception e) {
            System.err.println("Erro crítico na inicialização do subsistema backend: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static File localizarBackendExe() throws Exception {
        String path = BackendInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File locationFile = new File(path);
        File currentDir = locationFile.isDirectory() ? locationFile : locationFile.getParentFile();

        File candidate = new File(currentDir, "backend.exe");
        if (candidate.exists()) return candidate;

        if (currentDir.getName().equalsIgnoreCase("classes") && currentDir.getParentFile() != null) {
            File targetDir = currentDir.getParentFile();
            candidate = new File(targetDir, "backend.exe");
            if (candidate.exists()) return candidate;

            if (targetDir.getParentFile() != null && targetDir.getParentFile().getParentFile() != null) {
                File projectRoot = targetDir.getParentFile().getParentFile();
                File devPublishExe = new File(projectRoot, "backend\\bin\\Release\\net8.0\\win-x64\\publish\\backend.exe");
                if (devPublishExe.exists()) return devPublishExe;
            }
        }

        if (currentDir.getParentFile() != null) {
            candidate = new File(currentDir.getParentFile(), "backend.exe");
            if (candidate.exists()) return candidate;

            if (currentDir.getParentFile().getParentFile() != null) {
                candidate = new File(currentDir.getParentFile().getParentFile(), "backend.exe");
                if (candidate.exists()) return candidate;
            }
        }

        return candidate;
    }

    private static void killExistingBackendProcess() {
        try {
            ProcessBuilder killPb = new ProcessBuilder("taskkill", "/F", "/IM", "backend.exe");
            killPb.redirectErrorStream(true);
            Process p = killPb.start();
            p.waitFor();
        } catch (Exception ignored) {
        }
    }

    private static boolean checkHealthEndpoint() {
        try {
            URL url = URI.create("http://localhost:5000/api/health").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(600);
            connection.setReadTimeout(600);
            connection.connect();
            int code = connection.getResponseCode();
            connection.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static void stopBackend() {
        if (!startedByUs) {
            return;
        }
        if (backendProcess != null && backendProcess.isAlive()) {
            backendProcess.destroyForcibly();
        }
        killExistingBackendProcess();
    }

    public static void iniciarBackend() { startBackend(); }
    public static void encerrarBackend() { stopBackend(); }
}