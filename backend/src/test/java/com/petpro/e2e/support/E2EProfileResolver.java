package com.petpro.e2e.support;

import org.springframework.test.context.ActiveProfilesResolver;

/**
 * E2E 테스트 프로필을 동적으로 결정하는 리졸버.
 *
 * 기본값: "test" (H2 인메모리)
 * PostgreSQL: -De2e.profiles=test,test-pg
 */
public class E2EProfileResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> testClass) {
        String profiles = System.getProperty("e2e.profiles", "test");
        return profiles.split(",");
    }
}
