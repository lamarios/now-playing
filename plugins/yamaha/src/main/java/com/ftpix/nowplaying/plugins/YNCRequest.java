package com.ftpix.nowplaying.plugins;

public class YNCRequest {
    public static int TYPE_GET = 1, TYPE_PUT = 0;
    public int type = 1;
    private String responsePreValue, responsePostValue;
    private String request;

    public void buildRequest(String param, String... nodes) {
        StringBuilder requestBody = new StringBuilder();
        StringBuilder responseBody = new StringBuilder();

        requestBody
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"");
        requestBody.append(type == TYPE_GET ? "GET" : "PUT");
        requestBody.append("\">");

        responseBody.append("<YAMAHA_AV rsp=\"");
        responseBody.append(type == TYPE_GET ? "GET" : "PUT");
        responseBody.append("\" RC=\"0\">");

        for (int i = 0; i < nodes.length; i++) {
            requestBody.append("<");
            requestBody.append(nodes[i]);
            requestBody.append(">");

            responseBody.append("<");
            responseBody.append(nodes[i]);
            responseBody.append(">");
        }

        responsePreValue = responseBody.toString();

        responseBody = new StringBuilder();

        requestBody.append(param);

        for (int i = nodes.length - 1; i >= 0; i--) {
            requestBody.append("</");
            requestBody.append(nodes[i]);
            requestBody.append(">");

            responseBody.append("</");
            responseBody.append(nodes[i]);
            responseBody.append(">");
        }

        requestBody.append("</YAMAHA_AV>");
        responseBody.append("</YAMAHA_AV>");

        request = requestBody.toString();
        responsePostValue = responseBody.toString();
    }

    public String getResponseValue(String response) {
        response = response.replace(responsePreValue, "");
        response = response.replace(responsePostValue, "");

        return response;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
