package com.liucai.http.core;

import com.liucai.http.bulider.LcaiHttpRequestBulider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * multipart/form-data 写入工具。
 */
final class LcaiMultipartWriter {

    private static final String CRLF = "\r\n";

    private LcaiMultipartWriter() {
    }

    static void write(OutputStream os, LcaiHttpRequestBulider builder) throws IOException {
        String boundary = builder.boundary;

        // 文本参数
        if (builder.mapParams != null) {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, builder.encode), true);
            for (Map.Entry<String, Object> e : builder.mapParams.entrySet()) {
                w.append("--").append(boundary).append(CRLF);
                w.append("Content-Disposition: form-data; name=\"").append(e.getKey()).append("\"").append(CRLF);
                w.append(CRLF);
                w.append(String.valueOf(e.getValue())).append(CRLF);
                w.flush();
            }
        }

        // 文件参数
        if (builder.fileParams != null) {
            for (Map.Entry<String, File> e : builder.fileParams.entrySet()) {
                File f = e.getValue();
                if (!f.exists()) {
                    continue;
                }
                PrintWriter w = new PrintWriter(new OutputStreamWriter(os, builder.encode), true);
                w.append("--").append(boundary).append(CRLF);
                w.append("Content-Disposition: form-data; name=\"").append(e.getKey())
                        .append("\"; filename=\"").append(f.getName()).append("\"").append(CRLF);
                w.append("Content-Type: ").append(getMimeType(f.getName())).append(CRLF);
                w.append("Content-Transfer-Encoding: binary").append(CRLF);
                w.append(CRLF);
                w.flush();

                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    byte[] buf = new byte[8 * 1024];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        os.write(buf, 0, n);
                    }
                    os.flush();
                }

                os.write(CRLF.getBytes(builder.encode));
            }
        }

        // 结束
        PrintWriter w = new PrintWriter(new OutputStreamWriter(os, builder.encode), true);
        w.append("--").append(boundary).append("--").append(CRLF);
        w.flush();
    }

    private static String getMimeType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}