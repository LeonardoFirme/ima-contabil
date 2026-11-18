package com.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainApp extends Application {

    private static final String VERSAO_ATUAL = "1.1.1";
    private static final String GITHUB_VERSION_URL = "https://raw.githubusercontent.com/LeonardoFirme/ima-contabil/main/version.json";
    private static final String API_BASE_URL = "http://localhost:5000/api";

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
    public void init() throws Exception {
        super.init();
        // Define explicitamente o padrão brasileiro antes de carregar qualquer componente
        Locale.setDefault(Locale.of("pt", "BR"));
        BackendInitializer.startBackend();
    }

    @Override
    public void stop() throws Exception {
        BackendInitializer.stopBackend();
        super.stop();
    }

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
        progressBar.setStyle("-fx-accent: #097f29;");

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
            "-fx-background-color: #097f29; " +
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
        overlay.setPrefSize(1024, 768);

        StackPane welcomeLayout = new StackPane();
        ImageView bgImageView = new ImageView();
        try {
            var resource = MainApp.class.getResource("/IMA-1024x768.png");
            if (resource != null) {
                Image bgImage = new Image(resource.toExternalForm());
                bgImageView.setImage(bgImage);
                bgImageView.setPreserveRatio(false);
                bgImageView.fitWidthProperty().bind(welcomeLayout.widthProperty());
                bgImageView.fitHeightProperty().bind(welcomeLayout.heightProperty());
            }
        } catch (Exception ignored) {}

        welcomeLayout.getChildren().addAll(bgImageView, overlay, contentBox);

        Scene scene = new Scene(welcomeLayout, 1024, 768);
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

        lblTituloApp = new Label("Editor de Lotes (XLS, XLSX, CSV)");
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
            "-fx-background-color: #097f29; " +
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

                    // Instancia o formatador monetário estritamente no padrão brasileiro (ponto no milhar, vírgula no decimal)
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("pt", "BR"));
                    symbols.setDecimalSeparator(',');
                    symbols.setGroupingSeparator('.');
                    DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

                    if (nomeArquivo.endsWith(".xls") || nomeArquivo.endsWith(".xlsx")) {
                        try (Workbook workbook = WorkbookFactory.create(file)) {
                            Sheet sheet = workbook.getSheetAt(0);
                            DataFormatter formatter = new DataFormatter(Locale.of("pt", "BR"));

                            // Detecta o layout analisando o cabeçalho/primeiras linhas
                            boolean isFinancas = false;
                            for (int rIdx = 0; rIdx < Math.min(sheet.getLastRowNum(), 5); rIdx++) {
                                Row rowCheck = sheet.getRow(rIdx);
                                if (rowCheck != null) {
                                    for (Cell c : rowCheck) {
                                        String val = formatter.formatCellValue(c).toLowerCase();
                                        if (val.contains("conta corrente") || val.contains("valor (r$)") || val.contains("movimentação")) {
                                            isFinancas = true;
                                            break;
                                        }
                                    }
                                }
                                if (isFinancas) break;
                            }

                            for (Row row : sheet) {
                                if (row == null || row.getPhysicalNumberOfCells() == 0) continue;

                                if (isFinancas) {
                                    // Layout de Finanças / Extrato (Coluna 2 = Descrição, Coluna 5 = Valor, Coluna 6 = Saldo)
                                    Cell cellDesc = row.getCell(2);
                                    Cell cellValor = row.getCell(5);
                                    Cell cellSaldo = row.getCell(6);

                                    String desc = cellDesc != null ? extrairValorCelularRobust(cellDesc, formatter).trim() : "";
                                    String valorStr = cellValor != null ? extrairValorCelularRobust(cellValor, formatter).trim() : "";
                                    String saldoStr = cellSaldo != null ? extrairValorCelularRobust(cellSaldo, formatter).trim() : "";

                                    if (desc.isEmpty() || desc.equalsIgnoreCase("cliente ou fornecedor") || desc.equalsIgnoreCase("descrição")) {
                                        continue;
                                    }

                                    String saldoFormatado = formatarValorMonetario(valorStr, df);
                                    String saldoAntFormatado = formatarValorMonetario(saldoStr, df);

                                    if (!desc.isEmpty()) {
                                        dataList.add(new RowData(desc, saldoFormatado, saldoAntFormatado));
                                    }
                                } else {
                                    // Layout DRE / Balancete
                                    String desc = "";
                                    String saldoVal = "";
                                    String saldoAntVal = "";

                                    Cell cellDesc0 = row.getCell(0);
                                    Cell cellDesc2 = row.getCell(2);
                                    Cell cellSaldoDRE = row.getCell(8);
                                    Cell cellSaldoAntDRE = row.getCell(11);

                                    if (cellSaldoDRE != null || cellSaldoAntDRE != null) {
                                        String d0 = cellDesc0 != null ? extrairValorCelularRobust(cellDesc0, formatter).trim() : "";
                                        String d2 = cellDesc2 != null ? extrairValorCelularRobust(cellDesc2, formatter).trim() : "";
                                        desc = !d0.isEmpty() ? d0 : d2;

                                        if (cellSaldoDRE != null) {
                                            saldoVal = formatarValorMonetario(extrairValorCelularRobust(cellSaldoDRE, formatter), df);
                                        }
                                        if (cellSaldoAntDRE != null) {
                                            saldoAntVal = formatarValorMonetario(extrairValorCelularRobust(cellSaldoAntDRE, formatter), df);
                                        }
                                    } else {
                                        // Varredura genérica para outros tipos de planilhas
                                        List<String> valoresEncontrados = new ArrayList<>();
                                        List<String> textosDescricao = new ArrayList<>();

                                        for (int i = 0; i < row.getLastCellNum(); i++) {
                                            Cell cell = row.getCell(i);
                                            if (cell == null) continue;
                                            String val = extrairValorCelularRobust(cell, formatter).trim();
                                            if (val.isEmpty()) continue;

                                            if (isValorMonetarioEstrito(val)) {
                                                valoresEncontrados.add(val);
                                            } else {
                                                textosDescricao.add(val);
                                            }
                                        }

                                        desc = String.join(" - ", textosDescricao);
                                        if (!valoresEncontrados.isEmpty()) {
                                            saldoVal = formatarValorMonetario(valoresEncontrados.get(0), df);
                                            if (valoresEncontrados.size() > 1) {
                                                saldoAntVal = formatarValorMonetario(valoresEncontrados.get(1), df);
                                            }
                                        }
                                    }

                                    if (!desc.isEmpty() || !saldoVal.isEmpty() || !saldoAntVal.isEmpty()) {
                                        String dUpper = desc.toUpperCase();
                                        if (!dUpper.contains("EMPRESA:") && !dUpper.contains("C.N.P.J.") && !dUpper.contains("PERÍODO:") && !dUpper.contains("INSC. JUNTA") && !dUpper.equals("DESCRIÇÃO")) {
                                            dataList.add(new RowData(desc, saldoVal, saldoAntVal));
                                        }
                                    }
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
                                    formatarValorMonetario(colunas[1].trim(), df),
                                    formatarValorMonetario(colunas[2].trim(), df)
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
            "-fx-background-color: #097f29; " +
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
            if (dataList.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aviso");
                alert.setHeaderText("Tabela vazia");
                alert.setContentText("Importe ou adicione itens à tabela antes de processar.");
                alert.showAndWait();
                return;
            }

            btnProcess.setDisable(true);
            String tituloRelatorio = lblTituloApp.getText().replace("Arquivo: ", "");

            List<RowData> snapshotItens = new ArrayList<>(dataList);

            Task<HttpResult> taskProcessamento = new Task<>() {
                @Override
                protected HttpResult call() throws Exception {
                    com.google.gson.JsonObject rootJson = new com.google.gson.JsonObject();
                    rootJson.addProperty("titulo", tituloRelatorio);
                    com.google.gson.JsonArray itensArray = new com.google.gson.JsonArray();

                    for (RowData r : snapshotItens) {
                        com.google.gson.JsonObject itemObj = new com.google.gson.JsonObject();
                        itemObj.addProperty("descricao", r.getDescricao());
                        itemObj.addProperty("saldo", r.getSaldo());
                        itemObj.addProperty("saldoAnterior", r.getSaldoAnterior());
                        itensArray.add(itemObj);
                    }
                    rootJson.add("itens", itensArray);

                    return enviarRequisicaoJson(API_BASE_URL + "/processar", "POST", rootJson.toString());
                }
            };

            taskProcessamento.setOnSucceeded(event -> {
                btnProcess.setDisable(false);
                HttpResult response = taskProcessamento.getValue();
                if (response.statusCode == 200 || response.statusCode == 201) {
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
                    alert.setContentText("Código HTTP: " + response.statusCode + "\nResposta: " + response.body);
                    alert.showAndWait();
                }
            });

            taskProcessamento.setOnFailed(event -> {
                btnProcess.setDisable(false);
                Throwable ex = taskProcessamento.getException();
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro de Conexão com o Backend");
                alert.setHeaderText("Não foi possível comunicar com o servidor local.");
                alert.setContentText("Verifique se o backend .NET está rodando na porta 5000.\nDetalhe: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
                alert.showAndWait();
            });

            new Thread(taskProcessamento).start();
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
                btnAbrirPdf.setDisable(true);

                Task<File> taskAbrir = new Task<>() {
                    @Override
                    protected File call() throws Exception {
                        String encodedName = URLEncoder.encode(nomeArquivo, StandardCharsets.UTF_8).replace("+", "%20");
                        File tempPdf = new File(System.getProperty("java.io.tmpdir"), nomeArquivo);
                        baixarArquivoHttp(API_BASE_URL + "/downloads/" + encodedName, tempPdf);
                        return tempPdf;
                    }
                };

                taskAbrir.setOnSucceeded(ev -> {
                    btnAbrirPdf.setDisable(false);
                    try {
                        File tempPdf = taskAbrir.getValue();
                        if (tempPdf.exists()) {
                            java.awt.Desktop.getDesktop().open(tempPdf);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                taskAbrir.setOnFailed(ev -> {
                    btnAbrirPdf.setDisable(false);
                    Throwable ex = taskAbrir.getException();
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Não foi possível abrir o PDF.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });

                new Thread(taskAbrir).start();
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
                btnSalvarComo.setDisable(true);
                String nomeArquivo = selecionado.trim();

                Task<Void> taskSalvar = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        String encodedName = URLEncoder.encode(nomeArquivo, StandardCharsets.UTF_8).replace("+", "%20");
                        baixarArquivoHttp(API_BASE_URL + "/downloads/" + encodedName, arquivoDestino);
                        return null;
                    }
                };

                taskSalvar.setOnSucceeded(ev -> {
                    btnSalvarComo.setDisable(false);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText("Arquivo salvo com sucesso!");
                    alert.setContentText("Local: " + arquivoDestino.getAbsolutePath());
                    alert.showAndWait();
                });

                taskSalvar.setOnFailed(ev -> {
                    btnSalvarComo.setDisable(false);
                    Throwable ex = taskSalvar.getException();
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Não foi possível salvar o arquivo.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });

                new Thread(taskSalvar).start();
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

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                btnExcluir.setDisable(true);

                Task<Void> taskExcluir = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        for (String sel : selecionados) {
                            String[] partes = sel.split("_");
                            if (partes.length >= 2) {
                                String idStr = partes[1];
                                enviarRequisicaoJson(API_BASE_URL + "/importacao/" + idStr, "DELETE", null);
                            }
                        }
                        return null;
                    }
                };

                taskExcluir.setOnSucceeded(ev -> {
                    btnExcluir.setDisable(false);
                    carregarDownloads();
                });

                taskExcluir.setOnFailed(ev -> {
                    btnExcluir.setDisable(false);
                    taskExcluir.getException().printStackTrace();
                });

                new Thread(taskExcluir).start();
            }
        });

        VBox rightPane = new VBox(12, lblDownloads, stackDownloads, btnAtualizar, btnVerificarAtualizacao, btnAbrirPdf, btnSalvarComo, btnExcluir);
        rightPane.setPadding(new Insets(15));
        rightPane.setMinWidth(280);
        rightPane.setStyle("-fx-background-color: #f8f9fa;");

        SplitPane splitPane = new SplitPane(leftPane, rightPane);
        splitPane.setDividerPositions(0.75);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

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

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setBottom(footerBox);

        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        atualizarPlaceholderEditor();
        carregarDownloads();
        verificarAtualizacaoSilenciosa(primaryStage);
    }

    private static class HttpResult {
        final int statusCode;
        final String body;

        HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    private HttpResult enviarRequisicaoJson(String urlStr, String metodo, String jsonBody) throws Exception {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(metodo);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "IMA-Contabil-Desktop");

        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder responseText = new StringBuilder();
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseText.append(line.trim());
                }
            }
        }
        conn.disconnect();
        return new HttpResult(status, responseText.toString());
    }

    private void baixarArquivoHttp(String urlStr, File destino) throws Exception {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "IMA-Contabil-Desktop");

        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) {
            String novaUrl = conn.getHeaderField("Location");
            conn.disconnect();
            baixarArquivoHttp(novaUrl, destino);
            return;
        }

        if (status != 200) {
            conn.disconnect();
            throw new Exception("Falha no download. Código HTTP: " + status);
        }

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }
    }

    private void verificarAtualizacaoSilenciosa(Stage ownerStage) {
        Task<String[]> task = new Task<>() {
            @Override
            protected String[] call() throws Exception {
                try {
                    HttpResult response = enviarRequisicaoJson(GITHUB_VERSION_URL, "GET", null);
                    if (response.statusCode == 200) {
                        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(response.body).getAsJsonObject();
                        String versaoRemota = obj.has("versao") ? obj.get("versao").getAsString() : null;
                        String urlDownload = obj.has("urlDownload") ? obj.get("urlDownload").getAsString() : null;

                        if (versaoRemota != null && urlDownload != null) {
                            return new String[]{versaoRemota, urlDownload};
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            String[] resultado = task.getValue();
            if (resultado != null && resultado[0] != null) {
                String versaoRemota = resultado[0];
                String urlDownload = resultado[1];

                if (!versaoRemota.equals(VERSAO_ATUAL)) {
                    Platform.runLater(() -> exibirNotificacaoAtualizacao(versaoRemota, urlDownload, ownerStage));
                }
            }
        });

        new Thread(task).start();
    }

    private void verificarAtualizacaoSistema(Stage ownerStage) {
        Task<String[]> task = new Task<>() {
            @Override
            protected String[] call() throws Exception {
                HttpResult response = enviarRequisicaoJson(GITHUB_VERSION_URL, "GET", null);
                if (response.statusCode == 200) {
                    String versaoRemota = extrairValorJson(response.body, "versao");
                    String urlDownload = extrairValorJson(response.body, "urlDownload");
                    if (versaoRemota == null || urlDownload == null) {
                        throw new Exception("Estrutura inválida no arquivo version.json.");
                    }
                    return new String[]{versaoRemota, urlDownload};
                }
                throw new Exception("Falha ao consultar servidor (Código HTTP: " + response.statusCode + ")");
            }
        };

        task.setOnSucceeded(e -> {
            String[] resultado = task.getValue();
            String versaoRemota = resultado[0];
            String urlDownload = resultado[1];

            if (!versaoRemota.equals(VERSAO_ATUAL)) {
                Platform.runLater(() -> exibirNotificacaoAtualizacao(versaoRemota, urlDownload, ownerStage));
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(ownerStage);
                    alert.setTitle("Sistema Atualizado");
                    alert.setHeaderText(null);
                    alert.setContentText("Você já está utilizando a versão mais recente (" + VERSAO_ATUAL + ").");
                    alert.showAndWait();
                });
            }
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setTitle("Erro de Conexão");
            alert.setHeaderText("Não foi possível verificar atualizações.");
            alert.setContentText("Detalhe: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    private void exibirNotificacaoAtualizacao(String versaoRemota, String urlDownload, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(ownerStage);
        alert.setTitle("Nova Atualização Disponível");
        alert.setHeaderText("A versão v" + versaoRemota + " está disponível.");
        alert.setContentText("Deseja realizar a atualização agora?");

        ButtonType btnSim = new ButtonType("Sim", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNao = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnSim) {
            baixarEAtualizarJar(urlDownload, ownerStage);
        }
    }

    private void baixarEAtualizarJar(String urlDownload, Stage ownerStage) {
        Task<File> downloadTask = new Task<>() {
            @Override
            protected File call() throws Exception {
                File pastaDownloads = new File(System.getProperty("user.home"), "Downloads");
                File installerFile = new File(pastaDownloads, "ima-contabil-update.msi");
                baixarArquivoHttp(urlDownload, installerFile);
                return installerFile;
            }
        };

        downloadTask.setOnSucceeded(e -> {
            File installerFile = downloadTask.getValue();
            Platform.runLater(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder("msiexec.exe", "/i", installerFile.getAbsolutePath(), "/qb");
                    pb.start();

                    Platform.exit();
                    System.exit(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro ao Executar Instalador");
                    alert.setHeaderText("Não foi possível iniciar a instalação automática.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            });
        });

        downloadTask.setOnFailed(e -> {
            Throwable ex = downloadTask.getException();
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro na Atualização");
                alert.setHeaderText("Não foi possível baixar o instalador.");
                alert.setContentText(ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido.");
                alert.showAndWait();
            });
        });

        new Thread(downloadTask).start();
    }

    private String extrairValorJson(String json, String chave) {
        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (obj.has(chave)) {
                return obj.get(chave).getAsString();
            }
            return null;
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
        t = t.replace("R$", "").replace("(", "").replace(")", "").trim();
        return t.matches("^-?\\d{1,3}(\\.\\d{3})*,\\d{2}$") ||
               t.matches("^-?\\d+([.,]\\d+)?$");
    }

    private String extrairValorCelularRobust(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double val = cell.getNumericCellValue();
                // Retorna o valor numérico puro no formato internacional (ex: 23475318.44)
                return String.format(Locale.US, "%.2f", val);
            } else if (cell.getCellType() == CellType.FORMULA) {
                try {
                    if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return String.format(Locale.US, "%.2f", cell.getNumericCellValue());
                    }
                } catch (Exception ignored) {}
            }
            return formatter.formatCellValue(cell).trim();
        } catch (Exception e) {
            return formatter.formatCellValue(cell).trim();
        }
    }

    private String formatarValorMonetario(String valorStr, DecimalFormat df) {
        if (valorStr == null || valorStr.trim().isEmpty()) return "";
        try {
            String limpo = valorStr.trim();
            boolean isNegativo = limpo.startsWith("(") && limpo.endsWith(")");

            limpo = limpo.replace("(", "").replace(")", "").replace("R$", "").trim();

            double num;
            // Identifica se a string tem vírgula (padrão brasileiro: 23.475.318,44) ou apenas ponto decimal (padrão US/POI: 23475318.44)
            if (limpo.contains(",")) {
                limpo = limpo.replace(".", "").replace(",", ".");
                num = Double.parseDouble(limpo);
            } else {
                num = Double.parseDouble(limpo);
            }

            if (isNegativo && num > 0) {
                num = -num;
            }

            String formatado = df.format(Math.abs(num));
            return (num < 0 ? "-R$ " : "R$ ") + formatado;
        } catch (Exception e) {
            return valorStr;
        }
    }

    private void carregarDadosNoEditor(String id) {
        Task<List<RowData>> taskCarregarItens = new Task<>() {
            @Override
            protected List<RowData> call() throws Exception {
                HttpResult response = enviarRequisicaoJson(API_BASE_URL + "/importacao/" + id + "/itens", "GET", null);
                List<RowData> itensCarregados = new ArrayList<>();
                if (response.statusCode == 200) {
                    com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(response.body).getAsJsonArray();
                    for (com.google.gson.JsonElement el : jsonArray) {
                        com.google.gson.JsonObject obj = el.getAsJsonObject();
                        String descVal = obj.has("descricao") ? obj.get("descricao").getAsString() : "";
                        String saldoVal = obj.has("saldo") ? obj.get("saldo").getAsString() : "";
                        String saldoAntVal = obj.has("saldoAnterior") ? obj.get("saldoAnterior").getAsString() : "";

                        if (!descVal.isEmpty() || !saldoVal.isEmpty() || !saldoAntVal.isEmpty()) {
                            itensCarregados.add(new RowData(descVal, saldoVal, saldoAntVal));
                        }
                    }
                }
                return itensCarregados;
            }
        };

        taskCarregarItens.setOnSucceeded(e -> {
            dataList.setAll(taskCarregarItens.getValue());
            atualizarPlaceholderEditor();
        });

        taskCarregarItens.setOnFailed(e -> taskCarregarItens.getException().printStackTrace());

        new Thread(taskCarregarItens).start();
    }

    private void carregarDownloads() {
        Task<List<String>> taskDownloads = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                HttpResult response = enviarRequisicaoJson(API_BASE_URL + "/downloads", "GET", null);
                List<String> nomes = new ArrayList<>();
                if (response.statusCode == 200) {
                    com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(response.body).getAsJsonArray();
                    for (com.google.gson.JsonElement el : jsonArray) {
                        com.google.gson.JsonObject obj = el.getAsJsonObject();
                        if (obj.has("nome")) {
                            nomes.add(obj.get("nome").getAsString());
                        }
                    }
                }
                return nomes;
            }
        };

        taskDownloads.setOnSucceeded(e -> {
            downloadsList.setAll(taskDownloads.getValue());
            if (lblPlaceholderDownloads != null) {
                lblPlaceholderDownloads.setVisible(downloadsList.isEmpty());
            }
        });

        taskDownloads.setOnFailed(e -> {
            if (lblPlaceholderDownloads != null) {
                lblPlaceholderDownloads.setVisible(downloadsList.isEmpty());
            }
        });

        new Thread(taskDownloads).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}