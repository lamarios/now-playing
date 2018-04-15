package com.ftpix.nowplaying.controllers;

import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@SparkController
public class IndexController {


    /**
     * Serves the content of the index file;
     *
     * @return
     */
    @SparkGet("/")
    public String serveIndex() throws IOException {
        try (
                InputStream inputStream = getClass().getClassLoader().getResource("web/public/index.html").openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            return reader.lines().collect(Collectors.joining(""));
        }
    }
}
