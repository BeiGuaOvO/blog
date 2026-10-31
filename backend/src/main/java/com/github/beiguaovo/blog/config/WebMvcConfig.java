package com.github.beiguaovo.blog.config;
import com.github.beiguaovo.blog.storage.UploadStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final UploadStorage uploadStorage;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadStorage.baseLocation();
        log.info("静态资源映射: /uploads/** -> {}", location);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);   // 文件名是 UUID，不会变，可以放心缓存
    }
}