package com.kaat.getgo;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;


public class RequestController {


    @FXML
    private ComboBox<String> method;
    @FXML
    private TextField url;

    @FXML
    private ComboBox<String> authorization;
    @FXML
    private TextArea settings;
    @FXML
    private TextArea response;
    @FXML
    private HBox basicAuthFields;
    @FXML
    private TextField basicAuthUsername;
    @FXML
    private PasswordField basicAuthPassword;

    @FXML
    private HBox apiKeyFields;

    @FXML
    private TextField apiKeyKeyField;

    @FXML
    private TextField apiKeyValueField;

    @FXML
    private ComboBox<String> apiKeyAddToComboBox;

    @FXML
    private HBox bearerTokenFields;

    @FXML
    private TextField bearerTokenField;

    @FXML
    private TableView<ParamRow> paramsTable;
    @FXML
    private TableColumn<ParamRow, Boolean> checkColumn;
    @FXML
    private TableColumn<ParamRow, String> keyColumn;
    @FXML
    private TableColumn<ParamRow, String> valueColumn;

    @FXML
    private TableView<HeaderRow> headersTable;
    @FXML
    private TableColumn<HeaderRow, Boolean> checkHeaderColumn;
    @FXML
    private TableColumn<HeaderRow, String> keyHeaderColumn;
    @FXML
    private TableColumn<HeaderRow, String> valueHeaderColumn;

    @FXML
    private Slider maxRedirectsSlider;
    @FXML
    private TextField maxRedirectsTextField;

    @FXML
    private ToggleSwitch followRedirectsToggle;


    public void initialize() {

        // Initialize the Param columns
        checkColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        checkColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkColumn));

        // Key and Value columns for Param table
        keyColumn.setCellValueFactory(cellData -> cellData.getValue().keyProperty());
        keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        // Check column for Header table
        checkHeaderColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        checkHeaderColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkHeaderColumn));

      // Key and Value columns for Header table
        keyHeaderColumn.setCellValueFactory(cellData -> cellData.getValue().keyProperty());
        keyHeaderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueHeaderColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueHeaderColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        // Create an example row (you can add more rows programmatically)
        List<ParamRow> paramRows = new ArrayList<>();
        paramRows.add(new ParamRow(true, "Param1", "Value1"));

        paramsTable.setItems(FXCollections.observableList(paramRows));


        // Create an example row for headers (you can add more rows programmatically)
        List<HeaderRow> headerRows = new ArrayList<>();
        headerRows.add(new HeaderRow(true, "Content-Type", "application/json"));

        headersTable.setItems(FXCollections.observableList(headerRows));

        //select authorization combobox
        authorization.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            handleAuthorizationTypeChange(newValue);
        });

        // Add a listener to update the text field when the slider value changes
        maxRedirectsSlider.valueChangingProperty().addListener((observable, oldValue, isChanging) -> {
            if (!isChanging) {
                maxRedirectsTextField.setText(Integer.toString((int) maxRedirectsSlider.getValue()));
            }
        });

        //Todo: we need to handle the redirects being changed and the redirection itself.
        followRedirectsToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Handle the toggle action here based on the 'newValue' value
            // If newValue is true, redirects are enabled; if false, they are disabled
        });

    }


    @FXML
    private void sendRequest(ActionEvent event) {
        String requestUrl = url.getText();

        // Check if the request URL is not null and is a valid URL
        if (requestUrl != null && isValidURL(requestUrl)) {
            StringBuilder request = new StringBuilder();
            String selectedMethod = method.getValue();
            if (selectedMethod == null || selectedMethod.isEmpty()) {
                // Default to GET if no method is selected
                selectedMethod = "GET";
            }
            request.append("Request Method: ").append(selectedMethod).append("\n");

            // Extract Params and Headers from their respective TableViews
            String requestParams = extractTableValues(paramsTable);
            String requestHeaders = extractTableValues(headersTable);
            String authType = (authorization != null) ? authorization.getValue() : null;
            String additionalSettings = (settings != null) ? settings.getText() : null;

            // Build the request based on whether each field has a value
            request.append("Request URL: ").append(requestUrl).append("\n");

            if (requestParams != null && !requestParams.isEmpty()) {
                request.append("Request Params: ").append(requestParams).append("\n");
            }

            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                request.append("Request Headers: ").append(requestHeaders).append("\n");
            }

            if (authType != null && !authType.isEmpty()) {
                request.append("Authorization Type: ").append(authType).append("\n");
            }

            if (additionalSettings != null && !additionalSettings.isEmpty()) {
                request.append("Additional Settings: ").append(additionalSettings);
            }

            // Handle the request and populate the response TextArea
            String responseText = sendHttpRequest(request.toString());
            response.setText(responseText);
        } else {
            // Handle the case where the request URL is missing or malformed
            // You can show an error message to the user or take appropriate action.
        }
    }

    public interface TableRow {
        StringProperty keyProperty();
        StringProperty valueProperty();
    }


    private String extractTableValues(TableView<? extends TableRow> tableView) {
        ObservableList<? extends TableRow> rows = tableView.getItems();
        StringBuilder values = new StringBuilder();

        for (TableRow row : rows) {
            String key = String.valueOf(row.keyProperty());
            String value = String.valueOf(row.valueProperty());

            if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                values.append(key).append(": ").append(value).append("\n");
            }
        }

        return values.toString().trim();
    }



    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private String sendHttpRequest(String request) {
        try {
            // Create a URL object from the request URL
            URL requestUrl = new URL(Objects.requireNonNull(getRequestURLFromRequest(request)));

            // Extract request method and headers from the request string
            String requestMethod = getRequestHeaderValue(request, "Request Method");
            String requestHeaders = getRequestHeaderValue(request, "Request Headers");

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

            // Set the request method
            connection.setRequestMethod(requestMethod);

            // Set request headers (if available)
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                connection.setRequestProperty("Content-Type", "application/json");
                // You can parse and set other headers as needed
            }

            // Enable input and output streams for the request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // If it's a POST or PUT request, write the request body (params) to the output stream
            if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
                // Extract and write requestParams to the output stream
                String requestParams = getRequestHeaderValue(request, "Request Params");
                // You can write the requestParams to the output stream
            }

            // Send the request and receive the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Return the response as a String
                return response.toString();
            } else {
                // Handle error response
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // Extract a specific header value from the request string
    private String getRequestHeaderValue(String request, String headerName) {
        int headerStart = request.indexOf(headerName);
        if (headerStart == -1) {
            return null; // Header not found
        }
        int headerValueStart = headerStart + headerName.length() + 1;
        int headerValueEnd = request.indexOf("\n", headerValueStart);
        if (headerValueEnd == -1) {
            headerValueEnd = request.length();
        }
        return request.substring(headerValueStart, headerValueEnd).trim();
    }

    // Extract the request URL from the request string
    private String getRequestURLFromRequest(String request) {
        int urlStart = request.indexOf("Request URL:");
        if (urlStart == -1) {
            return null; // URL not found
        }
        int urlValueStart = urlStart + "Request URL:".length() + 1;
        int urlValueEnd = request.indexOf("\n", urlValueStart);
        if (urlValueEnd == -1) {
            urlValueEnd = request.length();
        }
        return request.substring(urlValueStart, urlValueEnd).trim();
    }


    // Support passing apiKey either in Header or in URL
    private String apiKeyLocation;

    // Set apiKeyLocation based on user selection: Header or URL
    public void setApiKeyLocation(String location) {
        this.apiKeyLocation = location;
    }

    // handle combo box change to Auth Type: Data change
    private void handleAuthorization(HttpURLConnection connection, String authType, String headers) throws IOException {
        if ("Basic Auth".equals(authType)) {
            // Handle Basic Auth
            String[] credentials = headers.split(":");
            String username = credentials[0];
            String password = credentials[1];
            String authString = username + ":" + password;
            String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
            connection.setRequestProperty("Authorization", authHeaderValue);
        } else if ("Bearer Token".equals(authType)) {
            // Handle Bearer Token
            connection.setRequestProperty("Authorization", "Bearer " + headers);
        } else if ("API Key".equals(authType)) {
            if ("Header".equals(apiKeyLocation)) {
                // Handle API Key in Header
                connection.setRequestProperty("api-key", headers);
            } else if ("Query Params".equals(apiKeyLocation)) {
                // Handle API Key in Query Params
                String apiKeyValue = URLEncoder.encode(headers, StandardCharsets.UTF_8);
                String url = connection.getURL().toString();
                url += (url.contains("?") ? "&" : "?") + "api-key=" + apiKeyValue;
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
        } else {
            // Handle other authorization types or No Auth
            // No additional authorization headers needed
            //ToDo: Add No Auth as a handling to clear any accidental auth type selections
        }
    }


//handle combo box change to Auth Type: Display change
    private void handleAuthorizationTypeChange(String selectedAuthType) {
        if ("Basic Auth".equals(selectedAuthType)) {
            // Show Basic Auth input fields
            basicAuthFields.setVisible(true);
            apiKeyFields.setVisible(false);
            bearerTokenFields.setVisible(false);
        } else if ("API Key".equals(selectedAuthType)) {
            // Show API Key input fields
            basicAuthFields.setVisible(false);
            apiKeyFields.setVisible(true);
            bearerTokenFields.setVisible(false);
        } else if ("Bearer Token".equals(selectedAuthType)) {
            // Show Bearer Token input fields
            basicAuthFields.setVisible(false);
            apiKeyFields.setVisible(false);
            bearerTokenFields.setVisible(true);
        } else {
            // Hide all input fields for other authorization types or No Auth
            basicAuthFields.setVisible(false);
            apiKeyFields.setVisible(false);
            bearerTokenFields.setVisible(false);
        }
    }




}

