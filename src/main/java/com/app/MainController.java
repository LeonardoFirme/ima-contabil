package com.app;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private Button btnSalvarGerarPdf;

    @FXML
    private void handleSalvarEGerarPdf() {
        new Thread(() -> {
            try {
                RelatorioDto dto = new RelatorioDto();
                dto.Titulo = "D. R. E.";
                dto.Itens = new ArrayList<>();

                ItemRelatorioDto dummy = new ItemRelatorioDto();
                dummy.Descricao = "RECEITA BRUTA";
                dummy.Saldo = "R$ 0,00";
                dummy.SaldoAnterior = "R$ 0,00";
                dto.Itens.add(dummy);

                Gson gson = new Gson();
                String jsonPayload = gson.toJson(dto);

                URL url = new URL("http://localhost:5000/api/processar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                Platform.runLater(() -> {
                    if (responseCode == 200 || responseCode == 201) {
                        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório salvo e PDF gerado com sucesso!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erro", "O backend retornou o código de status: " + responseCode);
                    }
                });
            } catch (Exception e) {
                String mensagemErro = e.getMessage();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erro de Conexão", "Não foi possível comunicar com o backend .NET:\n" + mensagemErro);
                });
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class RelatorioDto {
        public String Titulo;
        public List<ItemRelatorioDto> Itens;
    }

    public static class ItemRelatorioDto {
        public String Descricao;
        public String Saldo;
        public String SaldoAnterior;
    }
}