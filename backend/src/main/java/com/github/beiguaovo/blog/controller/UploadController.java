package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.storage.UploadStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "zip", "md");

    private final UploadStorage uploadStorage;

    @Value("${blog.upload.public-prefix}")
    private String publicPrefix;

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (original.contains("..")) {
            throw new BizException("非法文件名");
        }
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED.contains(ext)) {
            throw new BizException("不支持的文件类型: " + ext);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path dir = uploadStorage.resolveDateDir(datePath);
        Path target = dir.resolve(fileName).toAbsolutePath();
        try {
            Files.createDirectories(dir);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("写入文件失败: {}", target, e);
            throw new BizException(500, "文件保存失败");
        }
        log.info("上传成功: {} ({} bytes) -> {}", original, file.getSize(), target);

        String url = publicPrefix + "/" + datePath + "/" + fileName;
        return Result.ok(Map.of("url", url, "name", original));
    }
}