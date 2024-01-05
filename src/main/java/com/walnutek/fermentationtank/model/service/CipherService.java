package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class CipherService {

	/**
	 * Encrypt use default algorithm - SHA3-256
	 * @param source
	 * @return
	 */
	public String encrypt(String source) {
		return encrypt(source, Algorithm.SHA3_256);
	}
	
	public String encrypt(String source, Algorithm algorithm) {
		return switch(algorithm) {
			case SHA_256 -> DigestUtils.sha256Hex(source);
			case SHA3_256 -> DigestUtils.sha3_256Hex(source);
			default -> throw new AppException(Code.E002, "不支援的演算法");
		};
	}
	
	public enum Algorithm {
		SHA_256,
		SHA3_256;
	}
	
}
