package com.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.concurrent.Task;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainApp extends Application {

    // Versão atual do aplicativo instalado
    private static final String VERSAO_ATUAL = "1.0.0";
    // Substitua pelo seu token PAT do GitHub e URL do arquivo version.json no repo privado
    private static final String GITHUB_VERSION_URL = "https://raw.githubusercontent.com/LeonardoFirme/ima-contabil/main/version.json";
    private static final String GITHUB_PAT_TOKEN = "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";

    public static class RowData {
        private final SimpleStringProperty descricao;
        private final SimpleStringProperty saldo;
        private final SimpleStringProperty saldoAnterior;

        public RowData(String descricao, String saldo, String saldoAnterior) {
            this.descricao = new SimpleStringProperty(descricao != null ? descricao : "");
            this.saldo = new SimpleStringProperty(saldo != null ? saldo : "");
            this.saldoAnterior = new SimpleStringProperty(saldoAnterior != null ? saldoAnterior : "");
        }

        public String getDescricao() { return descricao.get(); }
        public void setDescricao(String desc) { this.descricao.set(desc); }
        public String getSaldo() { return saldo.get(); }
        public void setSaldo(String sal) { this.saldo.set(sal); }
        public String getSaldoAnterior() { return saldoAnterior.get(); }
        public void setSaldoAnterior(String salAnt) { this.saldoAnterior.set(salAnt); }
    }

    private final ObservableList<RowData> dataList = FXCollections.observableArrayList();
    private final ObservableList<String> downloadsList = FXCollections.observableArrayList();
    private Label lblPlaceholderDownloads;
    private VBox placeholderEditorBox;
    private Label lblTituloApp;

    @Override
    public void start(Stage primaryStage) {
        mostrarTelaCarregamento(primaryStage);
    }

    private void mostrarTelaCarregamento(Stage primaryStage) {
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        try {
            var iconStream = MainApp.class.getResourceAsStream("/logo.png");
            if (iconStream != null) {
                splashStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        Label lblTitulo = new Label("IMA Contábil");
        lblTitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblSub = new Label("Desktop Lite - Carregando sistema...");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #ecf0f1;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(260);
        progressBar.setStyle("-fx-accent: #00db57;");

        VBox contentBox = new VBox(15, lblTitulo, lblSub, progressBar);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        overlay.setPrefSize(640, 420);

        StackPane splashLayout = new StackPane();
        ImageView bgImageView = new ImageView();
        try {
            var resource = MainApp.class.getResource("/splash-bg.png");
            if (resource != null) {
                Image bgImage = new Image(resource.toExternalForm());
                bgImageView.setImage(bgImage);
                bgImageView.setPreserveRatio(false);
                bgImageView.fitWidthProperty().bind(splashLayout.widthProperty());
                bgImageView.fitHeightProperty().bind(splashLayout.heightProperty());
            }
        } catch (Exception ignored) {}

        splashLayout.getChildren().addAll(bgImageView, overlay, contentBox);

        Scene splashScene = new Scene(splashLayout, 640, 420);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        Task<Void> taskCarregamento = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1500);
                return null;
            }
        };

        taskCarregamento.setOnSucceeded(e -> {
            splashStage.close();
            mostrarTelaBoasVindas(primaryStage);
        });

        new Thread(taskCarregamento).start();
    }

    private void mostrarTelaBoasVindas(Stage primaryStage) {
        Stage welcomeStage = new Stage();
        welcomeStage.setTitle("Bem-vindo - IMA Contábil");

        try {
            var iconStream = MainApp.class.getResourceAsStream("/logo.png");
            if (iconStream != null) {
                welcomeStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        Label lblBemVindo = new Label("Bem-vindo ao IMA Contábil - Desktop Lite");
        lblBemVindo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblDescricao = new Label("Editor de lotes e conversor universal .xls / .xlsx / .csv.");
        lblDescricao.setStyle("-fx-font-size: 13px; -fx-text-fill: #ecf0f1;");

        Button btnEntrar = new Button("Acessar Sistema ➔");
        btnEntrar.setStyle(
            "-fx-background-color: #00db57; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 22; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );

        btnEntrar.setOnAction(e -> {
            welcomeStage.close();
            abrirTelaPrincipal(primaryStage);
        });

        VBox contentBox = new VBox(15, lblBemVindo, lblDescricao, btnEntrar);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        overlay.setPrefSize(640, 420);

        StackPane welcomeLayout = new StackPane();
        ImageView bgImageView = new ImageView();
        try {
            var resource = MainApp.class.getResource("/splash-bg.png");
            if (resource != null) {
                Image bgImage = new Image(resource.toExternalForm());
                bgImageView.setImage(bgImage);
                bgImageView.setPreserveRatio(false);
                bgImageView.fitWidthProperty().bind(welcomeLayout.widthProperty());
                bgImageView.fitHeightProperty().bind(welcomeLayout.heightProperty());
            }
        } catch (Exception ignored) {}

        welcomeLayout.getChildren().addAll(bgImageView, overlay, contentBox);

        Scene scene = new Scene(welcomeLayout, 640, 420);
        welcomeStage.setScene(scene);
        welcomeStage.centerOnScreen();
        welcomeStage.show();

        Platform.runLater(() -> welcomeLayout.requestFocus());
    }

    private void abrirTelaPrincipal(Stage primaryStage) {
        primaryStage.setTitle("IMA Contábil - Desktop Lite");

        try {
            var iconStream = MainApp.class.getResourceAsStream("/logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        lblTituloApp = new Label("IMA Contábil - Editor de Lotes (XLS, XLSX, CSV)");
        lblTituloApp.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        TableView<RowData> table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 4px;");

        TableColumn<RowData, String> colDesc = new TableColumn<>("Descrição / Conteúdo");
        colDesc.setCellValueFactory(cellData -> cellData.getValue().descricao);
        colDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        colDesc.setOnEditCommit(t -> t.getRowValue().setDescricao(t.getNewValue()));

        TableColumn<RowData, String> colSaldo = new TableColumn<>("Saldo");
        colSaldo.setCellValueFactory(cellData -> cellData.getValue().saldo);
        colSaldo.setCellFactory(TextFieldTableCell.forTableColumn());
        colSaldo.setOnEditCommit(t -> t.getRowValue().setSaldo(t.getNewValue()));

        TableColumn<RowData, String> colSaldoAnt = new TableColumn<>("Saldo Anterior");
        colSaldoAnt.setCellValueFactory(cellData -> cellData.getValue().saldoAnterior);
        colSaldoAnt.setCellFactory(TextFieldTableCell.forTableColumn());
        colSaldoAnt.setOnEditCommit(t -> t.getRowValue().setSaldoAnterior(t.getNewValue()));

        table.getColumns().addAll(colDesc, colSaldo, colSaldoAnt);
        table.setItems(dataList);
        VBox.setVgrow(table, Priority.ALWAYS);

        String estiloBotaoNeutro =
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #2f3640; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: #dcdde1; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8 14; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;";

        Button btnImportTop = new Button("📁 Importar Arquivo (.xls / .xlsx / CSV)");
        btnImportTop.setMaxWidth(Double.MAX_VALUE);
        btnImportTop.setStyle(estiloBotaoNeutro);

        Button btnImportCenter = new Button("📁 Importar Arquivo (.xls / .xlsx / CSV)");
        btnImportCenter.setStyle(
            "-fx-background-color: #00db57; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );

        Runnable acaoImportar = () -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar Arquivo Contábil");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Todos os suportados", "*.csv", "*.xls", "*.xlsx", "*.txt"),
                new FileChooser.ExtensionFilter("Arquivos Excel (*.xls, *.xlsx)", "*.xls", "*.xlsx"),
                new FileChooser.ExtensionFilter("Arquivos CSV / Texto (*.csv, *.txt)", "*.csv", "*.txt")
            );

            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    dataList.clear();
                    String nomeArquivo = file.getName().toLowerCase();
                    lblTituloApp.setText("Arquivo: " + file.getName());

                    if (nomeArquivo.endsWith(".xls") || nomeArquivo.endsWith(".xlsx")) {
                        try (Workbook workbook = WorkbookFactory.create(file)) {
                            Sheet sheet = workbook.getSheetAt(0);
                            DataFormatter formatter = new DataFormatter();
                            NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
                            nf.setMinimumFractionDigits(2);
                            nf.setMaximumFractionDigits(2);

                            boolean isExtratoBancario = false;
                            int rowIndex = 0;
                            for (Row row : sheet) {
                                if (row == null) continue;
                                String pCell = formatter.formatCellValue(row.getCell(0)).toLowerCase();
                                String sCell = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)).toLowerCase() : "";
                                if (pCell.contains("situação") || sCell.contains("data") || pCell.contains("histórico")) {
                                    isExtratoBancario = true;
                                    break;
                                }
                                rowIndex++;
                                if (rowIndex > 5) break;
                            }

                            for (Row row : sheet) {
                                if (row == null || row.getPhysicalNumberOfCells() == 0) continue;

                                String desc = "";
                                String saldoVal = "";
                                String saldoAntVal = "";

                                if (isExtratoBancario) {
                                    String data = formatter.formatCellValue(row.getCell(1)).trim();
                                    String favorecido = formatter.formatCellValue(row.getCell(2)).trim();
                                    String categoria = formatter.formatCellValue(row.getCell(4)).trim();
                                    String valor = formatter.formatCellValue(row.getCell(5)).trim();
                                    String saldo = formatter.formatCellValue(row.getCell(6)).trim();

                                    if (data.equalsIgnoreCase("data") || favorecido.toLowerCase().contains("cliente ou fornecedor")) {
                                        continue;
                                    }

                                    if (!data.isEmpty() || !favorecido.isEmpty()) {
                                        StringBuilder sb = new StringBuilder();
                                        if (!data.isEmpty()) sb.append(data);
                                        if (!favorecido.isEmpty()) sb.append(" - ").append(favorecido);
                                        if (!categoria.isEmpty() && !categoria.contains("%")) sb.append(" - ").append(categoria);

                                        desc = sb.toString().trim();
                                        if (!valor.isEmpty()) {
                                            saldoVal = formatarValorMonetario(valor, nf);
                                        }
                                        if (!saldo.isEmpty()) {
                                            saldoAntVal = formatarValorMonetario(saldo, nf);
                                        }
                                    }
                                } else {
                                    List<String> celulasPreenchidas = new ArrayList<>();
                                    int lastCell = row.getLastCellNum();

                                    for (int i = 0; i < lastCell; i++) {
                                        String val = formatter.formatCellValue(row.getCell(i)).trim();
                                        if (!val.isEmpty()) {
                                            celulasPreenchidas.add(val);
                                        }
                                    }

                                    if (celulasPreenchidas.isEmpty()) continue;

                                    List<String> textosDescricao = new ArrayList<>();

                                    for (String textoCelula : celulasPreenchidas) {
                                        String textoLimpo = textoCelula.replace("\n", " ").replace("\r", " ").trim();
                                        if (isValorMonetarioEstrito(textoLimpo)) {
                                            if (saldoVal.isEmpty()) {
                                                saldoVal = formatarValorMonetario(textoLimpo, nf);
                                            } else if (saldoAntVal.isEmpty()) {
                                                saldoAntVal = formatarValorMonetario(textoLimpo, nf);
                                            } else {
                                                textosDescricao.add(textoLimpo);
                                            }
                                        } else {
                                            textosDescricao.add(textoLimpo);
                                        }
                                    }

                                    desc = String.join(" - ", textosDescricao);
                                }

                                if (!desc.isEmpty() || !saldoVal.isEmpty() || !saldoAntVal.isEmpty()) {
                                    dataList.add(new RowData(desc, saldoVal, saldoAntVal));
                                }
                            }
                        }
                    } else {
                        String conteudo = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                        String[] registros = conteudo.split("\\r?\\n|;");
                        for (String reg : registros) {
                            String linhaLimpa = reg.trim();
                            if (linhaLimpa.isEmpty()) continue;
                            String[] colunas = linhaLimpa.split(",");
                            if (colunas.length >= 3) {
                                dataList.add(new RowData(
                                    colunas[0].trim(),
                                    formatarValorMonetario(colunas[1].trim(), NumberFormat.getInstance(new Locale("pt", "BR"))),
                                    formatarValorMonetario(colunas[2].trim(), NumberFormat.getInstance(new Locale("pt", "BR")))
                                ));
                            } else {
                                dataList.add(new RowData(colunas[0].trim(), "", ""));
                            }
                        }
                    }
                    atualizarPlaceholderEditor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro de Importação");
                    alert.setHeaderText("Não foi possível ler o arquivo selecionado.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        };

        btnImportTop.setOnAction(e -> acaoImportar.run());
        btnImportCenter.setOnAction(e -> acaoImportar.run());

        Button btnProcess = new Button("⚡ Salvar na Lista & Gerar PDF");
        btnProcess.setMaxWidth(Double.MAX_VALUE);
        btnProcess.setStyle(
            "-fx-background-color: #00db57; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 10 14; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        btnProcess.setOnAction(e -> {
            try {
                StringBuilder json = new StringBuilder();
                json.append("{\"titulo\":\"Relatório Contábil Completo\",\"itens\":[");
                for (int i = 0; i < dataList.size(); i++) {
                    RowData r = dataList.get(i);
                    String descSegura = r.getDescricao()
                            .replace("\t", " ")
                            .replace("\n", " ")
                            .replace("\r", " ")
                            .replace("\"", "\\\"");

                    String saldoSeguro = r.getSaldo()
                            .replace("\t", "")
                            .replace("\"", "\\\"");

                    String saldoAntSeguro = r.getSaldoAnterior()
                            .replace("\t", "")
                            .replace("\"", "\\\"");

                    json.append(String.format("{\"descricao\":\"%s\",\"saldo\":\"%s\",\"saldoAnterior\":\"%s\"}", descSegura, saldoSeguro, saldoAntSeguro));
                    if (i < dataList.size() - 1) json.append(",");
                }
                json.append("]}");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:5000/api/processar"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText("Operação Concluída com Sucesso");
                    alert.setContentText("Dados salvos no SQLite e relatório PDF gerado.");
                    alert.showAndWait();
                    carregarDownloads();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro no Servidor");
                    alert.setHeaderText("Falha ao processar requisição.");
                    alert.setContentText("Resposta: " + response.body());
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label lblMsgEditor = new Label("Não há conteúdo na tabela");
        lblMsgEditor.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        placeholderEditorBox = new VBox(10, lblMsgEditor, btnImportCenter);
        placeholderEditorBox.setAlignment(Pos.CENTER);
        placeholderEditorBox.setMouseTransparent(false);

        StackPane stackEditor = new StackPane(table, placeholderEditorBox);
        VBox.setVgrow(stackEditor, Priority.ALWAYS);

        VBox leftPane = new VBox(12, lblTituloApp, btnImportTop, stackEditor, btnProcess);
        leftPane.setPadding(new Insets(15));
        leftPane.setStyle("-fx-background-color: #f8f9fa;");

        // --- PAINEL DIREITO: CENTRAL DE DOWNLOADS ---
        Label lblDownloads = new Label("Relatórios Gerados (Ctrl+Clique para múltiplos)");
        lblDownloads.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        ListView<String> listViewDownloads = new ListView<>(downloadsList);
        listViewDownloads.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewDownloads.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 4px;");
        VBox.setVgrow(listViewDownloads, Priority.ALWAYS);

        lblPlaceholderDownloads = new Label("Nenhum relatório gerado");
        lblPlaceholderDownloads.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        StackPane stackDownloads = new StackPane(listViewDownloads, lblPlaceholderDownloads);
        VBox.setVgrow(stackDownloads, Priority.ALWAYS);

        listViewDownloads.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selecionado = listViewDownloads.getSelectionModel().getSelectedItem();
                if (selecionado != null) {
                    try {
                        String[] partes = selecionado.split("_");
                        if (partes.length >= 2) {
                            String idStr = partes[1];
                            carregarDadosNoEditor(idStr);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        Button btnAtualizar = new Button("🔄 Atualizar Lista");
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setStyle(estiloBotaoNeutro);
        btnAtualizar.setOnAction(e -> carregarDownloads());

        Button btnVerificarAtualizacao = new Button("✨ Verificar Atualizações");
        btnVerificarAtualizacao.setMaxWidth(Double.MAX_VALUE);
        btnVerificarAtualizacao.setStyle(
            "-fx-background-color: #9b59b6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8 14; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        btnVerificarAtualizacao.setOnAction(e -> verificarAtualizacaoSistema(primaryStage));

        Button btnAbrirPdf = new Button("📂 Abrir PDF Selecionado");
        btnAbrirPdf.setMaxWidth(Double.MAX_VALUE);
        btnAbrirPdf.setStyle(estiloBotaoNeutro);
        btnAbrirPdf.setOnAction(e -> {
            String selecionado = listViewDownloads.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                String nomeArquivo = selecionado.trim();
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:5000/api/downloads/" + nomeArquivo))
                            .GET()
                            .build();

                    File tempPdf = new File(System.getProperty("java.io.tmpdir"), nomeArquivo);
                    client.send(request, HttpResponse.BodyHandlers.ofFile(tempPdf.toPath()));

                    if (tempPdf.exists()) {
                        java.awt.Desktop.getDesktop().open(tempPdf);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Não foi possível abrir o PDF.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        Button btnSalvarComo = new Button("💾 Salvar PDF Como...");
        btnSalvarComo.setMaxWidth(Double.MAX_VALUE);
        btnSalvarComo.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8 14; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        btnSalvarComo.setOnAction(e -> {
            String selecionado = listViewDownloads.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aviso");
                alert.setHeaderText("Nenhum relatório selecionado.");
                alert.setContentText("Selecione um relatório na lista para salvar.");
                alert.showAndWait();
                return;
            }

            FileChooser saveChooser = new FileChooser();
            saveChooser.setTitle("Salvar Relatório PDF");
            saveChooser.setInitialFileName(selecionado);
            saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF (*.pdf)", "*.pdf"));

            File arquivoDestino = saveChooser.showSaveDialog(primaryStage);
            if (arquivoDestino != null) {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:5000/api/downloads/" + selecionado))
                            .GET()
                            .build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    Files.copy(response.body(), arquivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText("Arquivo salvo com sucesso!");
                    alert.setContentText("Local: " + arquivoDestino.getAbsolutePath());
                    alert.showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Não foi possível salvar o arquivo.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        Button btnExcluir = new Button("🗑️ Excluir Selecionado(s)");
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8 14; " +
            "-fx-cursor: hand; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        btnExcluir.setOnAction(e -> {
            List<String> selecionados = new ArrayList<>(listViewDownloads.getSelectionModel().getSelectedItems());

            if (selecionados.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aviso");
                alert.setHeaderText("Nenhum item selecionado.");
                alert.setContentText("Segure Ctrl e clique para selecionar um ou mais relatórios.");
                alert.showAndWait();
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmação de Exclusão Definitiva");
            confirm.setHeaderText("Deseja apagar definitivamente os itens selecionados?");
            confirm.setContentText("Os registros serão removidos do SQLite e os PDFs apagados da máquina.");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    for (String sel : selecionados) {
                        String[] partes = sel.split("_");
                        if (partes.length >= 2) {
                            String idStr = partes[1];
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create("http://localhost:5000/api/importacao/" + idStr))
                                    .DELETE()
                                    .build();
                            client.send(request, HttpResponse.BodyHandlers.ofString());
                        }
                    }

                    carregarDownloads();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox rightPane = new VBox(12, lblDownloads, stackDownloads, btnAtualizar, btnVerificarAtualizacao, btnAbrirPdf, btnSalvarComo, btnExcluir);
        rightPane.setPadding(new Insets(15));
        rightPane.setMinWidth(280);
        rightPane.setStyle("-fx-background-color: #f8f9fa;");

        SplitPane splitPane = new SplitPane(leftPane, rightPane);
        splitPane.setDividerPositions(0.75);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- RODAPÉ DA APLICAÇÃO (FOOTER) ---
        HBox footerBox = new HBox();
        footerBox.setPadding(new Insets(8, 15, 8, 15));
        footerBox.setStyle("-fx-background-color: #2c3e50; -fx-alignment: center-left;");

        Label lblFooterDev = new Label("Desenvolvido por Leonardo Firme");
        lblFooterDev.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 11px; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblFooterVer = new Label("Versão " + VERSAO_ATUAL);
        lblFooterVer.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px;");

        footerBox.getChildren().addAll(lblFooterDev, spacer, lblFooterVer);

        // Layout Principal contendo SplitPane no centro e Footer embaixo
        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setBottom(footerBox);

        Scene scene = new Scene(root, 1050, 620);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        atualizarPlaceholderEditor();
        carregarDownloads();
    }

    // Método para verificar atualizações no repositório privado do GitHub usando o Token PAT
    private void verificarAtualizacaoSistema(Stage ownerStage) {
        Task<String[]> task = new Task<>() {
            @Override
            protected String[] call() throws Exception {
                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_VERSION_URL))
                        .header("Authorization", "Bearer " + GITHUB_PAT_TOKEN)
                        .header("Accept", "application/vnd.github.v3+json")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String json = response.body();
                    // Extração simples do JSON sem dependências extras de biblioteca
                    String versaoRemota = extrairValorJson(json, "versao");
                    String urlDownload = extrairValorJson(json, "urlDownload");
                    return new String[]{versaoRemota, urlDownload};
                }
                throw new Exception("Falha ao consultar servidor (Código HTTP: " + response.statusCode() + ")");
            }
        };

        task.setOnSucceeded(e -> {
            String[] resultado = task.getValue();
            String versaoRemota = resultado[0];
            String urlDownload = resultado[1];

            if (versaoRemota != null && !versaoRemota.equals(VERSAO_ATUAL)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initOwner(ownerStage);
                alert.setTitle("Atualização Disponível");
                alert.setHeaderText("Nova versão v" + versaoRemota + " disponível!");
                alert.setContentText("Você está na versão v" + VERSAO_ATUAL + ". Deseja abrir o navegador para baixar a atualização?");

                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new URI(urlDownload));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(ownerStage);
                alert.setTitle("Sistema Atualizado");
                alert.setHeaderText(null);
                alert.setContentText("Você já está utilizando a versão mais recente (" + VERSAO_ATUAL + ").");
                alert.showAndWait();
            }
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setTitle("Erro de Conexão");
            alert.setHeaderText("Não foi possível verificar atualizações.");
            alert.setContentText("Verifique sua conexão com a internet. Detalhe: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    private String extrairValorJson(String json, String chave) {
        try {
            String searchKey = "\"" + chave + "\":\"";
            int start = json.indexOf(searchKey);
            if (start == -1) return null;
            start += searchKey.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private void atualizarPlaceholderEditor() {
        if (placeholderEditorBox != null) {
            placeholderEditorBox.setVisible(dataList.isEmpty());
        }
    }

    private boolean isValorMonetarioEstrito(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;
        String t = texto.trim();

        if (t.matches(".*[A-Za-zÀ-ÿ].*") || t.contains("%")) {
            return false;
        }

        t = t.replace("R$", "").replace("(", "").replace(")", "").trim();

        return t.matches("^-?\\d{1,3}(\\.\\d{3})*,\\d{2}$") ||
               t.matches("^-?\\d+([.,]\\d+)?$");
    }

    private String formatarValorMonetario(String valorStr, NumberFormat nf) {
        if (valorStr == null || valorStr.trim().isEmpty()) return "";
        try {
            String limpo = valorStr.trim();
            boolean isNegativo = limpo.startsWith("(") && limpo.endsWith(")");

            limpo = limpo.replace("(", "").replace(")", "").replace("R$", "").trim();
            limpo = limpo.replace(".", "").replace(",", ".");

            double num = Double.parseDouble(limpo);
            if (isNegativo) {
                num = -num;
            }

            String formatado = nf.format(Math.abs(num));
            return (num < 0 ? "-R$ " : "R$ ") + formatado;
        } catch (Exception e) {
            return valorStr;
        }
    }

    private void carregarDadosNoEditor(String id) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/api/importacao/" + id + "/itens"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            dataList.clear();
            if (body.startsWith("[")) {
                String cleanBody = body.substring(1, body.length() - 1);
                if (!cleanBody.trim().isEmpty()) {
                    String[] items = cleanBody.split("\\},\\{");
                    for (String item : items) {
                        String cleanedItem = item.replace("{", "").replace("}", "");
                        String descVal = "";
                        String saldoVal = "";
                        String saldoAntVal = "";

                        String[] pairs = cleanedItem.split(",");
                        for (String pair : pairs) {
                            String[] keyValue = pair.split(":");
                            if (keyValue.length >= 2) {
                                String key = keyValue[0].replace("\"", "").trim();
                                String val = keyValue[1].replace("\"", "").trim();
                                if (key.equals("descricao")) descVal = val;
                                if (key.equals("saldo")) saldoVal = val;
                                if (key.equals("saldoAnterior")) saldoAntVal = val;
                            }
                        }

                        if (!descVal.isEmpty() || !saldoVal.isEmpty() || !saldoAntVal.isEmpty()) {
                            dataList.add(new RowData(descVal, saldoVal, saldoAntVal));
                        }
                    }
                }
            }
            atualizarPlaceholderEditor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void carregarDownloads() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/api/downloads"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            List<String> nomes = new ArrayList<>();
            if (body != null && body.contains("\"nome\":\"")) {
                String[] partes = body.split("\"nome\":\"");
                for (int i = 1; i < partes.length; i++) {
                    String nome = partes[i].split("\"")[0];
                    nomes.add(nome);
                }
            }

            Platform.runLater(() -> {
                downloadsList.setAll(nomes);
                if (lblPlaceholderDownloads != null) {
                    lblPlaceholderDownloads.setVisible(downloadsList.isEmpty());
                }
            });
        } catch (Exception ex) {
            Platform.runLater(() -> {
                if (lblPlaceholderDownloads != null) {
                    lblPlaceholderDownloads.setVisible(downloadsList.isEmpty());
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}