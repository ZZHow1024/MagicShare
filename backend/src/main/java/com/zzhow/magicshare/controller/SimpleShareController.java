package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.SimpleShareRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 简单分享模式控制类
 *
 * @author ZZHow
 * create 2026/08/12
 * update 2026/08/12
 */
@RestController
@ConditionalOnProperty(name = "magicshare.simple-mode", havingValue = "true")
public class SimpleShareController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index(HttpServletRequest request,
                                        @RequestParam(defaultValue = "false") boolean checksum) {
        File root = getShareRoot();
        if (root == null)
            return html(HttpStatus.NOT_FOUND, "No shared files", page("No shared files", "<p>The share list is empty.</p>"));

        if (root.isFile())
            return html(HttpStatus.OK, "MagicShare", renderFlatFileList(request, checksum));

        if (SimpleShareRepository.isShowDirectoryStructure())
            return html(HttpStatus.OK, "MagicShare", renderDirectory(request, root, "", checksum));

        return html(HttpStatus.OK, "MagicShare", renderFlatFileList(request, checksum));
    }

    @RequestMapping(value = "/browse/**", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> browse(HttpServletRequest request,
                                         @RequestParam(defaultValue = "false") boolean checksum) {
        if (!SimpleShareRepository.isShowDirectoryStructure())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        File root = getShareRoot();
        if (root == null || root.isFile())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        String relativePath = extractPath(request, "/browse/");
        File directory = resolveUnderRoot(root, relativePath);
        if (directory == null || !directory.isDirectory())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        return html(HttpStatus.OK, "MagicShare", renderDirectory(request, directory, normalizeRelativePath(relativePath), checksum));
    }

    @RequestMapping(value = "/files/**", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<InputStreamResource> file(HttpServletRequest request) {
        File root = getShareRoot();
        if (root == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        File file = resolveSharedFile(root, extractPath(request, "/files/"));
        if (file == null || !file.isFile() || !file.canRead())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            long fileLength = file.length();
            Range range = parseRange(request.getHeader(HttpHeaders.RANGE), fileLength);
            boolean headRequest = "HEAD".equalsIgnoreCase(request.getMethod());
            HttpHeaders headers = createDownloadHeaders(file, range, fileLength);

            if (range.partial()) {
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.start() + "-" + range.end() + "/" + fileLength);
                if (headRequest)
                    return new ResponseEntity<>(headers, HttpStatus.PARTIAL_CONTENT);

                return new ResponseEntity<>(new InputStreamResource(limitedInputStream(file, range)), headers, HttpStatus.PARTIAL_CONTENT);
            }

            if (headRequest)
                return new ResponseEntity<>(headers, HttpStatus.OK);

            return new ResponseEntity<>(new InputStreamResource(new FileInputStream(file)), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + file.length())
                    .build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private File getShareRoot() {
        String basePath = FileRepository.getBasePath();
        if (basePath == null || basePath.isBlank())
            return null;

        File root = new File(basePath);
        if (!root.exists())
            return null;

        return root;
    }

    private String renderDirectory(HttpServletRequest request, File directory, String relativePath, boolean checksum) {
        StringBuilder rows = new StringBuilder();
        String parentPath = parentPath(relativePath);
        if (parentPath != null)
            rows.append(directoryRow("..", browseUrl(request, parentPath, checksum), "", checksum));

        File[] children = directory.listFiles();
        if (children != null) {
            Arrays.sort(children, Comparator
                    .comparing(File::isFile)
                    .thenComparing(file -> file.getName().toLowerCase(Locale.ROOT)));

            for (File child : children) {
                String childRelativePath = appendRelativePath(relativePath, child.getName());
                if (child.isDirectory())
                    rows.append(directoryRow(child.getName() + "/", browseUrl(request, childRelativePath, checksum), formatModifiedTime(child), checksum));
                else
                    rows.append(fileRow(request, child, childRelativePath, checksum));
            }
        }

        String title = relativePath == null || relativePath.isBlank() ? "Index of /" : "Index of /" + relativePath;
        return page(title, table(rows, checksum));
    }

    private String renderFlatFileList(HttpServletRequest request, boolean checksum) {
        StringBuilder rows = new StringBuilder();
        File root = getShareRoot();
        for (FileDetail fileDetail : FileRepository.getFiles()) {
            String relativePath = toRelativeDownloadPath(root, fileDetail);
            File file = resolveSharedFile(root, relativePath);
            if (file != null && file.isFile())
                rows.append(fileRow(request, file, relativePath, checksum));
        }

        return page("Shared files", table(rows, checksum));
    }

    private String table(StringBuilder rows, boolean checksum) {
        return """
                <table>
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Size</th>
                        <th>Modified</th>
                        %s
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                """.formatted(checksum ? "<th>SHA-256</th>" : "", rows);
    }

    private String directoryRow(String name, String url, String modifiedTime, boolean checksum) {
        return """
                <tr>
                    <td><a href="%s">%s</a></td>
                    <td>-</td>
                    <td>%s</td>
                    %s
                    <td></td>
                </tr>
                """.formatted(url, escapeHtml(name), escapeHtml(modifiedTime), checksum ? "<td></td>" : "");
    }

    private String fileRow(HttpServletRequest request, File file, String relativePath, boolean checksum) {
        String fileUrl = fileUrl(request, relativePath);
        String wgetCommand = "wget \"" + fileUrl + "\"";
        return """
                <tr>
                    <td><a href="%s">%s</a></td>
                    <td>%s</td>
                    <td>%s</td>
                    %s
                    <td>
                        <button type="button" data-copy="%s">Copy link</button>
                        <button type="button" data-copy="%s">Copy wget</button>
                    </td>
                </tr>
                """.formatted(
                fileUrl,
                escapeHtml(displayName(relativePath, file)),
                formatSize(file.length()),
                formatModifiedTime(file),
                checksum ? "<td><code>" + escapeHtml(sha256(file)) + "</code></td>" : "",
                escapeHtml(fileUrl),
                escapeHtml(wgetCommand)
        );
    }

    private ResponseEntity<String> html(HttpStatus status, String title, String body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(body == null ? page(title, "") : body);
    }

    private String page(String title, String content) {
        return """
                <!doctype html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 24px; color: #202124; }
                        h1 { font-size: 24px; font-weight: 500; margin: 0 0 16px; }
                        table { border-collapse: collapse; width: 100%%; }
                        th, td { border-bottom: 1px solid #ddd; padding: 8px 10px; text-align: left; vertical-align: top; }
                        th { background: #f6f8fa; }
                        a { color: #0969da; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                        button { margin: 0 4px 4px 0; padding: 4px 8px; }
                        code { font-size: 12px; word-break: break-all; }
                    </style>
                </head>
                <body>
                    <h1>%s</h1>
                    %s
                    <script>
                        (function () {
                            var buttons = document.getElementsByTagName('button');
                            for (var i = 0; i < buttons.length; i++) {
                                buttons[i].onclick = function () {
                                    var text = this.getAttribute('data-copy');
                                    if (navigator.clipboard) {
                                        navigator.clipboard.writeText(text);
                                    } else {
                                        window.prompt('Copy', text);
                                    }
                                };
                            }
                        })();
                    </script>
                </body>
                </html>
                """.formatted(escapeHtml(title), escapeHtml(title), content);
    }

    private HttpHeaders createDownloadHeaders(File file, Range range, long fileLength) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(detectContentType(file));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentLength(range.length(fileLength));
        headers.setLastModified(file.lastModified());
        headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.getName()));
        return headers;
    }

    private MediaType detectContentType(File file) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null)
            return MediaType.APPLICATION_OCTET_STREAM;

        return MediaType.parseMediaType(contentType);
    }

    private InputStream limitedInputStream(File file, Range range) throws IOException {
        return new RangeInputStream(new FileInputStream(file), range.start(), range.length(file.length()));
    }

    private File resolveSharedFile(File root, String requestPath) {
        String normalizedPath = normalizeRelativePath(requestPath);
        if (root.isFile()) {
            if (normalizedPath.isBlank() || normalizedPath.equals(root.getName()))
                return root;
            return null;
        }

        return resolveUnderRoot(root, normalizedPath);
    }

    private File resolveUnderRoot(File root, String relativePath) {
        try {
            Path rootPath = root.toPath().toRealPath();
            Path resolvedPath = rootPath.resolve(normalizeRelativePath(relativePath).replace("/", File.separator)).normalize();
            if (!resolvedPath.startsWith(rootPath))
                return null;

            return resolvedPath.toFile();
        } catch (IOException e) {
            return null;
        }
    }

    private String extractPath(HttpServletRequest request, String prefix) {
        String uri = request.getRequestURI();
        int prefixIndex = uri.indexOf(prefix);
        if (prefixIndex == -1)
            return "";

        String path = uri.substring(prefixIndex + prefix.length());
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
    }

    private String toRelativeDownloadPath(File root, FileDetail fileDetail) {
        if (root != null && root.isFile())
            return root.getName();

        return normalizeRelativePath(fileDetail.getPath());
    }

    private String normalizeRelativePath(String relativePath) {
        if (relativePath == null)
            return "";

        String normalizedPath = relativePath.replace("\\", "/");
        while (normalizedPath.startsWith("/"))
            normalizedPath = normalizedPath.substring(1);

        return normalizedPath;
    }

    private String appendRelativePath(String basePath, String childName) {
        if (basePath == null || basePath.isBlank())
            return childName;

        return basePath + "/" + childName;
    }

    private String parentPath(String relativePath) {
        if (relativePath == null || relativePath.isBlank())
            return null;

        int index = relativePath.lastIndexOf('/');
        if (index == -1)
            return "";

        return relativePath.substring(0, index);
    }

    private String browseUrl(HttpServletRequest request, String relativePath, boolean checksum) {
        String url = baseUrl(request) + (relativePath == null || relativePath.isBlank() ? "/" : "/browse/" + encodePath(relativePath));
        return checksum ? url + "?checksum=true" : url;
    }

    private String fileUrl(HttpServletRequest request, String relativePath) {
        return baseUrl(request) + "/files/" + encodePath(relativePath);
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    private String encodePath(String path) {
        return Arrays.stream(normalizeRelativePath(path).split("/"))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
    }

    private String contentDisposition(String fileName) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }

    private String displayName(String relativePath, File file) {
        if (relativePath == null || relativePath.isBlank())
            return file.getName();

        return relativePath;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L)
            return String.format(Locale.ROOT, "%.1f MB", bytes / 1024.0 / 1024.0);

        return String.format(Locale.ROOT, "%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }

    private String formatModifiedTime(File file) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(file.lastModified()));
    }

    private String sha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file), digest)) {
                byte[] buffer = new byte[8192];
                while (inputStream.read(buffer) != -1) {
                    // DigestInputStream updates the digest while reading.
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            return "";
        }
    }

    private String escapeHtml(String value) {
        if (value == null)
            return "";

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private Range parseRange(String rangeHeader, long fileLength) {
        if (rangeHeader == null || rangeHeader.isBlank())
            return Range.full();
        if (!rangeHeader.startsWith("bytes="))
            throw new IllegalArgumentException("Unsupported range unit");

        String rangeValue = rangeHeader.substring("bytes=".length());
        if (rangeValue.contains(","))
            rangeValue = rangeValue.substring(0, rangeValue.indexOf(','));

        String[] parts = rangeValue.split("-", 2);
        long start;
        long end;

        if (parts[0].isBlank()) {
            long suffixLength = Long.parseLong(parts[1]);
            if (suffixLength <= 0)
                throw new IllegalArgumentException("Invalid suffix range");
            start = Math.max(0, fileLength - suffixLength);
            end = fileLength - 1;
        } else {
            start = Long.parseLong(parts[0]);
            end = parts.length > 1 && !parts[1].isBlank() ? Long.parseLong(parts[1]) : fileLength - 1;
        }

        if (start < 0 || end < start || start >= fileLength)
            throw new IllegalArgumentException("Invalid range");

        return new Range(start, Math.min(end, fileLength - 1));
    }

    private record Range(long start, long end, boolean partial) {
        static Range full() {
            return new Range(0, -1, false);
        }

        Range(long start, long end) {
            this(start, end, true);
        }

        long length(long fileLength) {
            if (!partial)
                return fileLength;

            return end - start + 1;
        }
    }

    private static class RangeInputStream extends InputStream {
        private final InputStream inputStream;
        private long remaining;

        private RangeInputStream(InputStream inputStream, long start, long length) throws IOException {
            this.inputStream = inputStream;
            this.remaining = length;
            long skipped = 0;
            while (skipped < start) {
                long current = inputStream.skip(start - skipped);
                if (current <= 0)
                    throw new IOException("Unable to skip to range start");
                skipped += current;
            }
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0)
                return -1;

            int value = inputStream.read();
            if (value != -1)
                remaining--;
            return value;
        }

        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            if (remaining <= 0)
                return -1;

            int count = inputStream.read(bytes, off, (int) Math.min(len, remaining));
            if (count != -1)
                remaining -= count;
            return count;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
