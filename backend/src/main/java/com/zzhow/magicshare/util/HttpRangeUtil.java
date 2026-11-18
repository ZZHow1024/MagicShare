package com.zzhow.magicshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP Range 请求处理工具类
 *
 * @author ZZHow
 * create 2026/11/18
 */
public class HttpRangeUtil {
    private HttpRangeUtil() {
    }

    /**
     * 解析 Range 请求头
     *
     * @throws IllegalArgumentException 请求头非法或范围不可满足
     */
    public static Range parse(String rangeHeader, long fileLength) {
        if (rangeHeader == null || rangeHeader.isBlank())
            return Range.full();
        if (!rangeHeader.startsWith("bytes="))
            throw new IllegalArgumentException("Unsupported range unit");

        String rangeValue = rangeHeader.substring("bytes=".length());
        // Multipart ranges are unsupported; ignore the Range header per RFC 7233.
        if (rangeValue.contains(","))
            return Range.full();

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

    /**
     * 返回限定在 Range 范围内的输入流
     */
    public static InputStream limitedInputStream(File file, Range range) throws IOException {
        return new RangeInputStream(new FileInputStream(file), range.start(), range.length(file.length()));
    }

    public record Range(long start, long end, boolean partial) {
        public static Range full() {
            return new Range(0, -1, false);
        }

        Range(long start, long end) {
            this(start, end, true);
        }

        public long length(long fileLength) {
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