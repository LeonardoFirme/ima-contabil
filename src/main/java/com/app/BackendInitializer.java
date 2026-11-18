package com.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BackendInitializer {

    private static Process backendProcess;

    public static void startBackend() {
        try {
            // Mata instâncias anteriores pendidas na porta 5000 para evitar conflito de socket
            killExistingBackendProcess();

            String path = BackendInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            File jarFile = new File(path);
            File currentDir = jarFile.getParentFile();

            File backendExe = new File(currentDir, "backend.exe");
            if (!backendExe.exists()) {
                throw new IllegalStateException("Binário do backend não localizado no diretório de instalação: " + backendExe.getAbsolutePath());
            }

            ProcessBuilder pb = new ProcessBuilder(backendExe.getAbsolutePath());
            pb.directory(currentDir);
            pb.redirectErrorStream(true);

            backendProcess = pb.start();

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
                throw new RuntimeException("O servidor backend .NET não respondeu na porta 5000.\n--- LOGS DO BACKEND ---\n" + backendLogs.toString());
            }

        } catch (Exception e) {
            System.err.println("Erro crítico na inicialização do subsistema backend: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void killExistingBackendProcess() {
        try {
            // Comando para encerrar forçadamente qualquer processo backend.exe anterior no Windows
            ProcessBuilder killPb = new ProcessBuilder("taskkill", "/F", "/IM", "backend.exe");
            killPb.redirectErrorStream(true);
            Process p = killPb.start();
            p.waitFor();
        } catch (Exception ignored) {
        }
    }

    private static boolean checkHealthEndpoint() {
        try {
            URL url = new URL("http://localhost:5000/api/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(600);
            connection.setReadTimeout(600);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static void stopBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            backendProcess.destroyForcibly();
        }
        killExistingBackendProcess();
    }

    public static void iniciarBackend() { startBackend(); }
    public static void encerrarBackend() { stopBackend(); }
}