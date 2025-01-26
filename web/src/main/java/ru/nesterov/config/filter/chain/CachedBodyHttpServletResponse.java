package ru.nesterov.config.filter.chain;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private final ServletOutputStream cachedStream = new CachedServletOutputStream(cachedBody, super.getOutputStream());
    private PrintWriter writer;

    public CachedBodyHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return cachedStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(cachedStream, true, StandardCharsets.UTF_8);
        }
        return writer;
    }

    public String getCachedBody() {
        return cachedBody.toString(StandardCharsets.UTF_8);
    }

    private static class CachedServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream cachedBody;
        private final ServletOutputStream originalStream;

        public CachedServletOutputStream(ByteArrayOutputStream cachedBody, ServletOutputStream originalStream) {
            this.cachedBody = cachedBody;
            this.originalStream = originalStream;
        }

        @Override
        public boolean isReady() {
            return originalStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            originalStream.setWriteListener(writeListener);
        }

        @Override
        public void write(int b) throws IOException {
            cachedBody.write(b); // Кэшируем тело ответа
            originalStream.write(b); // Записываем в оригинальный поток
        }
    }
}