package com.zzhow.magicshare.util;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ZZHow
 * create 2026/11/15
 */
class FileUtilTest {
    @TempDir
    Path tempDir;

    @Test
    void findKeepsSubdirectoryPrefixInRelativePath() throws IOException {
        Files.createDirectories(tempDir.resolve("sub/deep"));
        Files.writeString(tempDir.resolve("top.txt"), "top");
        Files.writeString(tempDir.resolve("sub/nested.txt"), "nested");
        Files.writeString(tempDir.resolve("sub/deep/leaf.txt"), "leaf");

        List<FileDetail> files = new ArrayList<>();
        FileUtil.find(tempDir.toString(), files);

        assertEquals(3, files.size());
        assertEquals(File.separator + "top.txt", pathOf(files, "top.txt"));
        assertEquals(File.separator + "sub" + File.separator + "nested.txt", pathOf(files, "nested.txt"));
        assertEquals(File.separator + "sub" + File.separator + "deep" + File.separator + "leaf.txt", pathOf(files, "leaf.txt"));
    }

    @Test
    void findSingleFileUsesEmptyRelativePath() throws IOException {
        Path file = Files.writeString(tempDir.resolve("single.txt"), "single");

        List<FileDetail> files = new ArrayList<>();
        FileUtil.find(file.toString(), files);

        assertEquals(1, files.size());
        assertEquals("", files.get(0).getPath());
    }

    private String pathOf(List<FileDetail> files, String name) {
        return files.stream()
                .filter(file -> file.getName().equals(name))
                .findFirst()
                .orElseThrow()
                .getPath();
    }
}