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
import java.net.*;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

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
        // Retrieve the request URL from the user input
        String requestUrl = url.getText();

        // Check if the request URL is not null and is a valid URL
        if (requestUrl != null && isValidURL(requestUrl)) {
            // Initialize a StringBuilder to build the request details
            StringBuilder request = new StringBuilder();

            // Determine the selected HTTP method, defaulting to GET if none is selected
            String selectedMethod = method.getValue();
            if (selectedMethod == null || selectedMethod.isEmpty()) {
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


    /**
     * Extracts key-value pairs from a TableView and constructs a formatted string.
     *
     * @param tableView The TableView containing the data to be extracted.
     * @return A formatted string of key-value pairs extracted from the TableView.
     */
    private String extractTableValues(TableView<? extends TableRow> tableView) {
        // Retrieve the list of rows from the TableView
        ObservableList<? extends TableRow> rows = tableView.getItems();

        // Initialize a StringBuilder to build the formatted string
        StringBuilder values = new StringBuilder();

        // Iterate through each row in the TableView
        for (TableRow row : rows) {
            // Extract key and value properties from the row
            String key = String.valueOf(row.keyProperty());
            String value = String.valueOf(row.valueProperty());

            // Check if both key and value are not empty before appending to the result
            if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                values.append(key).append(": ").append(value).append("\n");
            }
        }

        // Return the formatted string, trimming any leading or trailing whitespace
        return values.toString().trim();
    }


    /**
     * Checks if the given string is a valid URL.
     *
     * @param url The string to be checked for URL validity.
     * @return {@code true} if the string is a valid URL; otherwise, {@code false}.
     */
    private boolean isValidURL(String url) {
        try {
            // Attempt to create a URL object from the provided string and convert it to a URI
            new URL(url).toURI();

            // If the above line doesn't throw an exception, the URL is considered valid
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            // An exception is caught if the URL is malformed or cannot be converted to a URI
            return false;
        }
    }


    /**
     * Sends an HTTP request and returns the response as a String.
     *
     * @param request The formatted request string containing URL, method, headers, and parameters.
     * @return The response received from the server, or an error message if the request fails.
     */
    private String sendHttpRequest(String request) {
        try {
            // Extract the URL from the request string
            String requestUrlString = Objects.requireNonNull(getRequestURLFromRequest(request));
            URL requestUrl = new URL(requestUrlString);

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
        } catch (MalformedURLException | ProtocolException e) {
            // Handle URL or protocol-related exceptions
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } catch (IOException e) {
            // Handle general input/output exceptions
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }


    /**
     * Extracts the value of a specific header from the given request string.
     *
     * @param request    The formatted request string containing headers.
     * @param headerName The name of the header whose value needs to be extracted.
     * @return The value of the specified header, or {@code null} if the header is not found.
     */
    private String getRequestHeaderValue(String request, String headerName) {
        // Find the starting position of the specified header in the request string
        int headerStart = request.indexOf(headerName);

        // If the header is not found, return null
        if (headerStart == -1) {
            return null; // Header not found
        }

        // Calculate the starting position of the header value
        int headerValueStart = headerStart + headerName.length() + 1;

        // Find the end position of the header value, considering a newline character as the delimiter
        int headerValueEnd = request.indexOf("\n", headerValueStart);

        // If the end position is not found, set it to the end of the request string
        if (headerValueEnd == -1) {
            headerValueEnd = request.length();
        }

        // Extract and trim the header value from the request string
        return request.substring(headerValueStart, headerValueEnd).trim();
    }


    /**
     * Extracts the request URL from the given formatted request string.
     *
     * @param request The formatted request string containing the request URL.
     * @return The extracted request URL, or {@code null} if the URL is not found.
     */
    private String getRequestURLFromRequest(String request) {
        // Find the starting position of the "Request URL:" tag in the request string
        int urlStart = request.indexOf("Request URL:");

        // If the "Request URL:" tag is not found, return null
        if (urlStart == -1) {
            return null; // URL not found
        }

        // Calculate the starting position of the request URL value
        int urlValueStart = urlStart + "Request URL:".length() + 1;

        // Find the end position of the request URL value, considering a newline character as the delimiter
        int urlValueEnd = request.indexOf("\n", urlValueStart);

        // If the end position is not found, set it to the end of the request string
        if (urlValueEnd == -1) {
            urlValueEnd = request.length();
        }

        // Extract and trim the request URL from the request string
        return request.substring(urlValueStart, urlValueEnd).trim();
    }


    // Support passing apiKey either in Header or in URL
    private String apiKeyLocation;

    // Set apiKeyLocation based on user selection: Header or URL
    public void setApiKeyLocation(String location) {
        this.apiKeyLocation = location;
    }

    /**
     * Handles changes in authorization type for an HTTP connection.
     *
     * @param connection The HttpURLConnection object for the request.
     * @param authType   The selected authorization type (e.g., Basic Auth, Bearer Token, API Key).
     * @param headers    The credentials or token associated with the selected authorization type.
     * @throws IOException If an I/O exception occurs while setting authorization headers.
     */
    private void handleAuthorization(HttpURLConnection connection, String authType, String headers) throws IOException {
        if ("Basic Auth".equals(authType)) {
            // Handle Basic Authentication
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
                // Handle API Key in Query Parameters
                String apiKeyValue = URLEncoder.encode(headers, StandardCharsets.UTF_8);
                String url = connection.getURL().toString();
                url += (url.contains("?") ? "&" : "?") + "api-key=" + apiKeyValue;
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
        } else {
            // Handle other authorization types or No Auth
            // No additional authorization headers needed
            // ToDo: Add No Auth handling to clear any accidental auth type selections
        }
    }


    /**
     * Handles the change in the selected authorization type in the user interface.
     * Adjusts the visibility of input fields based on the selected authorization type.
     *
     * @param selectedAuthType The newly selected authorization type (e.g., Basic Auth, API Key, Bearer Token).
     */
    private void handleAuthorizationTypeChange(String selectedAuthType) {
        if ("Basic Auth".equals(selectedAuthType)) {
            // Display input fields for Basic Auth, hide others
            basicAuthFields.setVisible(true);
            apiKeyFields.setVisible(false);
            bearerTokenFields.setVisible(false);
        } else if ("API Key".equals(selectedAuthType)) {
            // Display input fields for API Key, hide others
            basicAuthFields.setVisible(false);
            apiKeyFields.setVisible(true);
            bearerTokenFields.setVisible(false);
        } else if ("Bearer Token".equals(selectedAuthType)) {
            // Display input fields for Bearer Token, hide others
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


