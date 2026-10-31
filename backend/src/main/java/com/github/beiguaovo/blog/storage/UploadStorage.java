package com.github.beiguaovo.blog.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class UploadStorage {

    @Getter
    private Path root;

    @Value("${blog.upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        root = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            // 启动即建目录：既保证 toUri() 带尾斜杠，也避免首次上传才暴露权限问题
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录: " + root, e);
        }
        log.info("上传目录: {}  (user.dir={})", root, System.getProperty("user.dir"));
    }

    /** 给 addResourceLocations 用，保证以 / 结尾 */
    public String baseLocation() {
        String location = root.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    /** 解析 yyyy/MM 子目录，并防止路径穿越 */
    public Path resolveDateDir(String datePath) {
        Path dir = root.resolve(datePath).normalize();
        if (!dir.startsWith(root)) {
            throw new IllegalArgumentException("非法路径: " + datePath);
        }
        return dir;
    }
}