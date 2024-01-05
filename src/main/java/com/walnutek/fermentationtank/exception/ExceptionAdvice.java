package com.walnutek.fermentationtank.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionAdvice {

    private static Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, String>> appException(AppException e) {
        logger.error("", e);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("code", e.getCode());
        resultMap.put("cause", e.getMessage());
        return ResponseEntity.badRequest().body(resultMap);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> validationException(BindException e) {
        logger.error("", e);
        String allErrorMessages = e.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\r\n"));
        AppException appException = new AppException(AppException.Code.E006, allErrorMessages);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("code", appException.getCode());
        resultMap.put("cause", appException.getMessage());
        return ResponseEntity.badRequest().body(resultMap);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generalException(Exception e) {
    	logger.error("", e);
    	Map<String, String> resultMap = new HashMap<>();
    	resultMap.put("code", AppException.Code.E000.name());
    	resultMap.put("cause", e.getMessage());
    	return ResponseEntity.badRequest().body(resultMap);
    }

}
