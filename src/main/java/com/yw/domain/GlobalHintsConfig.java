package com.yw.domain;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.util.factory.Hints;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * geotools 내부 epsg들의 최신화 이슈 (좌표 변환시 좌표 틀어짐)
 * 최신 epsg를 properties에 추가해 factory로 생성후 저장
 * -
 * crs를 찾을때 저장된 factory중 기본 factory부터 우선순위가 높은 factory 순으로 crs를 찾음 (epsg_hsql 사용하기 때문에 우선순위에서 밀림)
 * 우선 순위가 높은 factory에 해당하는 epsg가 있다면 반환, 추가한 factory는 우선순위가 낮아 최신화 안된 데이터를 반환
 * -
 * factory에 우선순위를 부여 하는 방법을 찾지 못해 hints의 setting을 건들어 추가한 factory를 기본 factory로 지정
 */
@Slf4j
@Configuration
public class GlobalHintsConfig {

    @PostConstruct
    private void init() throws Exception {
        ClassPathResource resource = new ClassPathResource("epsg.properties");
        Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
        ReferencingFactoryContainer container = ReferencingFactoryContainer.instance(hints);
        PropertyAuthorityFactory factory = new PropertyAuthorityFactory(container, Citations.fromName("EPSG"), resource.getURL());
        ReferencingFactoryFinder.addAuthorityFactory(factory);
        Hints.putSystemDefault(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);

        log.info("GlobalHintsConfig Init Success");
    }
}
