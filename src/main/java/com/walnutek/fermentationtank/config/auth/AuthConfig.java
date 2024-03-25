package com.walnutek.fermentationtank.config.auth;

import com.walnutek.fermentationtank.config.interceptor.AuthInterceptor;
import com.walnutek.fermentationtank.config.interceptor.CorsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

	@Autowired
    private CorsInterceptor corsInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

	@Value("${app.api-root-path:}")
    private String apiRootPath;

	@Value("${app.dev-mode}")
	private Boolean isDevMode;

	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(corsInterceptor)
                .addPathPatterns("/**")
                .order(0);

        registry.addInterceptor(authInterceptor)
                .addPathPatterns(apiRootPath + "/**")
                .excludePathPatterns(getExcludePatternList())
                .order(1);
    }

	private List<String> getExcludePatternList(){
		List<String> excludePathList = new ArrayList<>();
        excludePathList.add("/user/login");
        excludePathList.add("/line-notify");

        if(isDevMode) {
        	excludePathList.add("/docs");
        	excludePathList.add("/swagger-ui/**");
        	excludePathList.add("/**/docs");
        	excludePathList.add("/**/docs/**");
        }

        return excludePathList.stream()
        		.map(path -> apiRootPath + path)
        		.toList();
	}

	@Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
    	if(StringUtils.hasText(apiRootPath)) {
    		configurer.setPatternParser(new PathPatternParser())
    				  .addPathPrefix(apiRootPath, HandlerTypePredicate.forAnnotation(RestController.class));
    	}
    }

}
