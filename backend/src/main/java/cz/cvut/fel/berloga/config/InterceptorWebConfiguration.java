package cz.cvut.fel.berloga.config;

import cz.cvut.fel.berloga.interceptors.ExecutionTimeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class InterceptorWebConfiguration implements WebMvcConfigurer {

    private List<String> patterns = new ArrayList<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        patterns.add("/calendar");
        patterns.add("/chat/all");
        patterns.add("/chat/find/**");
        patterns.add("/files/my-files");
        patterns.add("/files/get-file/**");
        patterns.add("/forum/all");
        patterns.add("/chat/**");
        patterns.add("/forum/all-search");
        patterns.add("/forum/question/**");
        patterns.add("/subject/list-all");
        patterns.add("/user/list");
        registry.addInterceptor(new ExecutionTimeInterceptor()).addPathPatterns(patterns);
    }
}
