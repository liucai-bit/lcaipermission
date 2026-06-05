package com.liucai.http.core;

import com.alibaba.fastjson.JSONObject;
import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.http.bulider.LcaiHttpRequestBulider;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.http.thread.LcaiRunnableUtils;
import com.liucai.http.xml.LcaiHttpXmlHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public class LcaiHttpRequest {

    private LcaiHttpRequestBulider bulider;

    private HttpURLConnection connection;

    public LcaiHttpRequest(LcaiHttpRequestBulider bulider) {
        this.bulider = bulider;
        GlobalThreadPool.execute(new LcaiRunnableUtils() {
            @Override
            public void run() {
                excute();
            }
        });
    }

    private void excute() {
        // 检查是否已取消
        if (bulider.responseResult!=null && bulider.responseResult.cancelRequest()) {
            handleCancel();
            return;
        }

        if (TextUtils.equals(bulider.method, LcaiHttpMethod.DOWNLOAD)) {
            bulider.method = LcaiHttpMethod.GET;
        }

        this.openConnect();
    }

    private void openConnect() {

        // 检查是否已取消
        if (bulider.responseResult!=null && bulider.responseResult.cancelRequest()) {
            handleCancel();
            return;
        }

        if (TextUtils.isEmpty(bulider.url)) {
            if (bulider.responseResult != null) {
                bulider.responseResult.error(-1, "请求地址为空");
            }
        } else {
            LcaiLogUtils.d("request url", bulider.url);
            if (bulider.headers != null) {
                LcaiLogUtils.d("request header",bulider.headers.toString());
            }
            try {
                URL url = new URL(bulider.url + (TextUtils.equals(bulider.method, LcaiHttpMethod.GET) ? appendUrl() : ""));
                this.connection = (HttpURLConnection) url.openConnection();
                this.connection.setConnectTimeout(this.bulider.connectTimeout);
                this.connection.setReadTimeout(this.bulider.readTimeout);
                this.connection.setRequestMethod(this.bulider.method);
                if (bulider.httpSend == LcaiHttpSend.JSON) {
                    this.connection.setRequestProperty("Content-type", "application/json; charset=" + bulider.encode);
                } else if (bulider.httpSend == LcaiHttpSend.MAP) {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + this.bulider.encode);
                }else if (this.bulider.httpSend == LcaiHttpSend.FORM_DATA) {
                    String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                    this.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                }
                if (this.bulider.headers != null && this.bulider.headers.size() > 0) {
                    Iterator var2 = this.bulider.headers.entrySet().iterator();
                    while (var2.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry) var2.next();
                        this.connection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                startRequest();
            } catch (IOException exception) {
                LcaiLogUtils.d("构建请求失败", exception.getMessage());
                if (bulider.responseResult != null) {
                    bulider.responseResult.error(-1, "构建请求失败");
                }
            }
        }
    }

    private void startRequest() {
        OutputStreamWriter writer = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        InputStream in = null;
        InputStream errorStream = null;
        FileOutputStream fos = null;
        try {
            // 检查是否已取消
            if (bulider.responseResult!=null && bulider.responseResult.cancelRequest()) {
                handleCancel();
                return;
            }

            if (TextUtils.equals(bulider.method,LcaiHttpMethod.POST,LcaiHttpMethod.PUT)){
                if (bulider.httpSend == LcaiHttpSend.JSON) {
                    String uploadParams = getUploadParams();
                    LcaiLogUtils.d("request bofy",uploadParams);
                    if (!TextUtils.isEmpty(uploadParams)) {
                        writer = new OutputStreamWriter(connection.getOutputStream());
                        writer.write(uploadParams);
                        writer.flush();
                    }
                }

                if (this.bulider.httpSend == LcaiHttpSend.MAP) {
                    String fromBody = getFormBody();
                    LcaiLogUtils.d("request bofy",fromBody);
                    if (!TextUtils.isEmpty(fromBody)) {
                        if (this.bulider.mapParams != null && this.bulider.mapParams.size() > 0) {
                            writer = new OutputStreamWriter(connection.getOutputStream());
                            writer.write(fromBody);
                            writer.flush();
                        }
                    }
                }

                // 检查是否已取消
                if (bulider.responseResult!=null && bulider.responseResult.cancelRequest()) {
                    handleCancel();
                    return;
                }

                if (this.bulider.httpSend == LcaiHttpSend.FORM_DATA) {
                    // 处理 multipart/form-data（文件上传）
                    writeMultipartFormData(connection.getOutputStream());
                }


            }



            int responseCode = connection.getResponseCode();

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            if (responseCode >= 200 && responseCode <= 300) {
                if (this.bulider.httpCall == LcaiHttpCall.RESPONSE) {
                    if (this.bulider.responseResult != null) {
                        if (this.bulider.withHeader) {
                            this.bulider.responseResult.success(headerFields, connection.getInputStream());
                        } else {
                            this.bulider.responseResult.success(connection.getInputStream());
                        }
                    }
                } else if (this.bulider.httpCall == LcaiHttpCall.JSON) {
                    if (this.bulider.backXml) {
                        List<Map<String, String>> map = this.parseXML(this.bulider.qName, new InputSource(new InputStreamReader(connection.getInputStream())));
                        if (this.bulider.responseResult != null) {
                            if (this.bulider.withHeader) {
                                this.bulider.responseResult.success(headerFields, map.toString());
                            } else {
                                this.bulider.responseResult.success(map.toString());
                            }
                        }
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        ir = new InputStreamReader(connection.getInputStream(), this.bulider.encode);
                        br = new BufferedReader(ir);
                        String line;
                        while ((line = br.readLine()) != null) {
                            // 在读取循环中检查取消状态
                            if (bulider.responseResult != null && bulider.responseResult.cancelRequest()) {
                                handleCancel();
                                throw new InterruptedException("请求已被取消");
                            }
                            buffer.append(line);
                        }

                        String bf = buffer.toString();
                        if (TextUtils.isEmpty(bf)) {
                            throw new LcaiHttpException("DATA IS NOT NULL!");
                        }

                        bf = bf.replaceAll("\\s*|\r|\n|\t", "");
                        if (this.bulider.responseResult != null) {
                            if (this.bulider.withHeader) {
                                this.bulider.responseResult.success(headerFields, bf);
                            } else {
                                this.bulider.responseResult.success(bf);
                            }
                        }
                    }
                } else if (this.bulider.httpCall == LcaiHttpCall.DOWNLOAD) {
                    in = connection.getInputStream();
                    if (TextUtils.isEmpty(this.bulider.savePath) || TextUtils.isEmpty(this.bulider.fileName)) {
                        if (this.bulider.responseResult != null) {
                            this.bulider.responseResult.error(-1, "存储地址或文件名称不能为空");
                        }
                        return;
                    }

                    File file = new File(this.bulider.savePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    fos = new FileOutputStream(file + File.separator + bulider.fileName);
                    long size = connection.getContentLength();
                    int count = 0;
                    byte[] buf = new byte[1024];
                    int oldProgress = 0;
                    do {
                        // 在下载循环中检查取消状态
                        if (bulider.responseResult != null && bulider.responseResult.cancelRequest()) {
                            break;
                        }
                        int numread = in.read(buf);
                        count += numread;
                        int progress = (int) ((float) count / (float) size * 100.0F);
                        if (this.bulider.responseResult != null && progress > oldProgress) {
                            oldProgress = progress;
                            this.bulider.responseResult.success(progress, false, this.bulider);
                        }

                        if (numread <= 0) {
                            if (this.bulider.responseResult != null) {
                                this.bulider.responseResult.success(progress, true, this.bulider);
                            }
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (!this.bulider.stopDownload);

                    // 处理取消后的清理
                    if (bulider.responseResult!=null && bulider.responseResult.cancelRequest()) {
                        File download = new File(this.bulider.savePath + File.separator + this.bulider.fileName);
                        if (download.exists()) {
                            download.delete();
                        }
                        throw new InterruptedException("下载已被取消");
                    }

                    if (this.bulider.stopDownload) {
                        File download = new File(this.bulider.savePath + File.separator + this.bulider.fileName);
                        if (download.exists()) {
                            download.delete();
                        }
                    }
                }
            } else {
                errorStream = connection.getErrorStream();
                String errorBody = "";
                if (errorStream != null) {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, this.bulider.encode))) {
                        StringBuilder errorBuffer = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorBuffer.append(line);
                        }
                        errorBody = errorBuffer.toString();
                    }
                }
                if (bulider.responseResult != null) {
                    bulider.responseResult.error(responseCode, errorBody);
                }
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            try {
                if (connection != null) connection.disconnect();
                if (writer != null) writer.close();
                if (ir != null) ir.close();
                if (br != null) br.close();
                if (in != null) in.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                LcaiLogUtils.d("流关闭失败");
            }
        }
    }

    private String appendUrl() {
        if (this.bulider.mapParams != null && this.bulider.mapParams.size() >= 1) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("?");
            Iterator var2 = this.bulider.mapParams.entrySet().iterator();

            while (var2.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry) var2.next();
                buffer.append(entry.getKey());
                buffer.append("=");
                buffer.append(entry.getValue());
                buffer.append("&");
            }

            String append = buffer.toString();
            return append.substring(0, append.length() - 1);
        } else {
            return "";
        }
    }

    private List<Map<String, String>> parseXML(String qName, InputSource inputSource) {
        LcaiHttpXmlHandler xmlHandler;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            xmlHandler = new LcaiHttpXmlHandler(qName);
            parser.parse(inputSource, xmlHandler);
            List<Map<String, String>> maps = xmlHandler.getMaps();
            return maps;
        } catch (ParserConfigurationException var6) {
            throw new RuntimeException(var6);
        } catch (SAXException var7) {
            throw new RuntimeException(var7);
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }
    }

    private String getUploadParams() {
        if (!TextUtils.isEmpty(bulider.jsonParams)) {
            return bulider.jsonParams;
        }

        if (bulider.mapParams != null && bulider.mapParams.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : bulider.mapParams.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            return jsonObject.toJSONString();
        }
        return "";
    }

    private String getFormBody() {
        if (bulider.mapParams != null && bulider.mapParams.size() > 0) {
            StringBuilder formBody = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, Object> entry : bulider.mapParams.entrySet()) {
                if (!first) {
                    formBody.append("&");
                }
                try {
                    // URL 编码参数值
                    String encodedKey = URLEncoder.encode(entry.getKey(), this.bulider.encode);
                    String encodedValue = URLEncoder.encode(entry.getValue().toString(), this.bulider.encode);
                    formBody.append(encodedKey).append("=").append(encodedValue);
                } catch (UnsupportedEncodingException e) {
                    // 使用默认编码
                    formBody.append(entry.getKey()).append("=").append(entry.getValue());
                }
                first = false;
            }
            return formBody.toString();
        }
        return "";
    }

    private void writeMultipartFormData(OutputStream outputStream) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, this.bulider.encode), true);

        // 写入普通参数
        if (bulider.mapParams != null) {
            for (Map.Entry<String, Object> entry : bulider.mapParams.entrySet()) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
                writer.append("Content-Type: text/plain; charset=").append(this.bulider.encode).append("\r\n");
                writer.append("\r\n");
                writer.append(entry.getValue().toString()).append("\r\n");
                writer.flush();
            }
        }

        // 写入文件参数
        if (bulider.fileParams != null) {
            for (Map.Entry<String, File> entry : bulider.fileParams.entrySet()) {
                File file = entry.getValue();
                if (file.exists()) {
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey())
                            .append("\"; filename=\"").append(file.getName()).append("\"\r\n");
                    writer.append("Content-Type: ").append(getMimeType(file.getName())).append("\r\n");
                    writer.append("Content-Transfer-Encoding: binary\r\n");
                    writer.append("\r\n");
                    writer.flush();

                    // 写入文件内容
                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }

                    writer.append("\r\n");
                    writer.flush();
                }
            }
        }
        writer.append("--").append(boundary).append("--\r\n");
        writer.flush();
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "application/octet-stream";
        }
    }

    private void handleException(Exception e) {
        String errorMsg;
        int errorCode = -1;

        if (e instanceof IOException) {
            errorMsg = "网络连接错误: " + e.getMessage();
            errorCode = -2; // 网络错误代码
        } else if (e instanceof LcaiHttpException) {
            errorMsg = "HTTP处理错误: " + e.getMessage();
            errorCode = -3; // HTTP处理错误代码
        } else {
            errorMsg = "请求失败: " + e.getMessage();
            errorCode = -1; // 通用错误代码
        }

        if (bulider.responseResult != null) {
            bulider.responseResult.error(errorCode, errorMsg);
        }

        LcaiLogUtils.d("请求异常", errorMsg);
    }

    private void handleCancel() {
        // 回调取消事件
        if (bulider != null && bulider.responseResult != null) {
            bulider.responseResult.error(-4, "请求已被取消");
        }

        // 清理资源
        try {
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            // 忽略异常
        }

        // 如果是下载请求，删除临时文件
        if (this.bulider != null && this.bulider.httpCall == LcaiHttpCall.DOWNLOAD) {
            File download = new File(this.bulider.savePath + File.separator + this.bulider.fileName);
            if (download.exists()) {
                download.delete();
            }
        }

        LcaiLogUtils.d("请求取消");
    }
}
