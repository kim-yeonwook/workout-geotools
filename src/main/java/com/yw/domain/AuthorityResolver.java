package com.yw.domain;

import com.yw.infrastructure.enums.Constants;
import com.yw.infrastructure.exception.InternalServerException;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * prj 파일을 읽었을때 Authority 가 없는 경우 존재
 * 방지하기위해 CrsNameToAuthorityResolver 추가 crs 의 이름을 읽어 Authority 를 반환
 * -
 * 이슈가 존재 했던 crs만 추가, 나머지는 굳이?
 */
public class AuthorityResolver {

    private static final Map<String, String> CRS_NAME_AUTHORITY_MAP = new HashMap<>();

    public static void init() {
        if (!CRS_NAME_AUTHORITY_MAP.isEmpty())
            return;

        try {
            ClassPathResource resource = new ClassPathResource("resolver.properties");
            try (InputStream in = resource.getInputStream(); InputStreamReader inr = new InputStreamReader(in); BufferedReader br = new BufferedReader(inr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(Constants.EQUALS);
                    if (split.length == 2) {
                        CRS_NAME_AUTHORITY_MAP.put(split[0], split[1]);
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalServerException("AuthorityResolver 초기화 실패!");
        }
    }

    public static String get(String name) {
        return CRS_NAME_AUTHORITY_MAP.getOrDefault(name, null);
    }
}
