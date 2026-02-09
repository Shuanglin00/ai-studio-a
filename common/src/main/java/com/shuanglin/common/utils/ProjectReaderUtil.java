package com.shuanglin.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 用于读取 ZIP 压缩包中项目文件的工具类。
 * 能够识别常见的文本文件（代码、配置文件、文档等）并提取其内容。
 */
public class ProjectReaderUtil {

    private static final String[] TEXT_EXTENSIONS = {
            ".java", ".kt", ".py", ".js", ".ts", ".jsx", ".tsx", ".html", ".htm", ".css", ".scss", ".sass", ".less",
            ".xml", ".json", ".yml", ".yaml", ".properties", ".env", ".ini", ".conf",
            ".md", ".txt", ".rst", ".adoc",
            ".sql",
            ".sh", ".bash", ".ksh", ".zsh",
            ".bat", ".cmd",
            ".gradle", ".groovy", ".gradle.kts",
            ".rb",
            ".php",
            ".cs",
            ".go",
            ".rs",
            ".scala",
            ".c", ".cpp", ".h", ".hpp", ".cxx", ".hxx",
            ".feature", ".feature.md", ".feature.txt", ".feature.json", ".feature.yaml", ".feature.yml", ".gherkin"
    };

    private static final String[] SPECIAL_TEXT_FILENAMES = {
            "makefile", "makefile.unix", "makefile.windows",
            "dockerfile", "docker-compose.yml",
            ".gitignore", ".gitattributes", ".gitmodules",
            ".travis.yml", ".gitlab-ci.yml", "jenkinsfile",
            "pom.xml",
            "build.gradle", "build.gradle.kts",
            "package.json", "package-lock.json",
            "requirements.txt", "setup.py", "pyproject.toml",
            "composer.json", "composer.lock",
            ".editorconfig", ".prettierrc", ".eslintrc"
    };

    /**
     * 读取 ZIP 压缩包内所有被识别为文本的文件内容，并组合成一个字符串。
     */
    public static String readAllProjectTextFiles(File zipFile) throws IOException {
        StringBuilder allContent = new StringBuilder();

        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IllegalArgumentException("指定的 ZIP 文件不存在或不是一个有效文件: " + zipFile.getAbsolutePath());
        }

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    String lowerCaseEntryName = entryName.toLowerCase();

                    if (isTextFile(entryName, lowerCaseEntryName)) {
                        allContent.append("--- File: ").append(entryName).append(" ---").append(System.lineSeparator());

                        try (InputStream entryInputStream = zip.getInputStream(entry)) {
                            String fileContent = readInputStreamAsText(entryInputStream, StandardCharsets.UTF_8);
                            allContent.append(fileContent);
                        } catch (IOException e) {
                            allContent.append("Error reading file content: ").append(e.getMessage()).append(System.lineSeparator());
                            allContent.append("--- END File: ").append(entryName).append(" (Read Error) ---").append(System.lineSeparator());
                        }
                    }
                    allContent.append(System.lineSeparator());
                }
            }
        }
        return allContent.toString();
    }

    /**
     * 判断一个文件是否是文本文件，基于其文件名和扩展名。
     */
    public static boolean isTextFile(String entryName, String lowerCaseEntryName) {
        int dotIndex = entryName.lastIndexOf('.');

        for (String specialFilename : SPECIAL_TEXT_FILENAMES) {
            if (lowerCaseEntryName.equals(specialFilename) || lowerCaseEntryName.startsWith(specialFilename + ".")) {
                return true;
            }
        }

        if (dotIndex > 0 && dotIndex < entryName.length() - 1) {
            String extension = entryName.substring(dotIndex);
            for (String textExt : TEXT_EXTENSIONS) {
                if (lowerCaseEntryName.endsWith(textExt)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String readInputStreamAsText(InputStream inputStream, Charset charset) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
            char[] buffer = new char[8192];
            int charsRead;
            while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                stringBuilder.append(buffer, 0, charsRead);
            }
        } catch (IOException e) {
            throw e;
        }
        return stringBuilder.toString();
    }
}
