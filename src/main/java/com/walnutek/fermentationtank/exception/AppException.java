package com.walnutek.fermentationtank.exception;

import java.util.Optional;

public class AppException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3578808369508669363L;
	
	private Code code;

    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(Code code) {
        super(code.getDescription());
        this.code = code;
    }
    public AppException(Code code,  String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return Optional.ofNullable(code)
                .orElse(Code.E000)
                .name();
    }

    public enum Code {
        E000("發生錯誤"),
        E001("登入驗證無效"),
        E002("請求參數有誤"),
        E003("您無權限請求該資源"),
        E004("查無資料"),
        E005("請勾選至少一個項目"),
        E006("資料驗證失敗"),
        E007("字串長度超出資料表欄位限制"),
        E008(""),
        E009(""),
        E010("");

        private  String description;

        public String getDescription() {
            return description;
        }

        private Code(String description) {
            this.description = description;
        }
    }

}
