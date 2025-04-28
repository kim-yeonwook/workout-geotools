package com.yw.domain;

import org.geotools.referencing.CRS;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class INODataStoreServiceTest {

    @Spy
    private JDBCDataStoreService jdbcDataStoreService;

    @Spy
    private ShapefileDataStoreService shapefileDataStoreService;

    @InjectMocks
    private INOGeometryService inoGeometryService;

    private static final String TEST_TABLE_NAME = "충청남도_학교 현황_point_WGS84";

    private IDataStoreTransferObject postGisDataStoreVO;

    private IDataStoreTransferObject shapeFileDataStoreVO;

    private IDataStoreTransferObject outShapeFileDataStoreVO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        postGisDataStoreVO = getPostGisDataStoreVO();
        shapeFileDataStoreVO = getShapeFileDataStoreVO();
        outShapeFileDataStoreVO = getOutShapeFileDataStoreVO();

        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    IDataStoreTransferObject getPostGisDataStoreVO() {
        return PostGisDataStoreTransferObject.builder()
                .host("localhost")
                .port(15432)
                .database("pts")
                .schema("test")
                .user("dtapi")
                .password("123456789!")
                .build();
    }

    IDataStoreTransferObject getShapeFileDataStoreVO() throws Exception {
        return ShapeFileDataStoreTransferObject.builder()
                .url(new File("src/test/resources/shp/" + TEST_TABLE_NAME + ".shp").toURI().toURL())
                .build();
    }

    IDataStoreTransferObject getOutShapeFileDataStoreVO() throws Exception {
        return ShapeFileDataStoreTransferObject.builder()
                .url(new File("src/test/resources/out/" + TEST_TABLE_NAME + ".shp").toURI().toURL())
                .build();
    }

    @Test
    @Order(1)
    @Rollback(false)
    @DisplayName("SHP 파일을 데이터 베이스 저장")
    void testShp2jdbc() throws Exception {
        inoGeometryService.shp2jdbc(shapeFileDataStoreVO, postGisDataStoreVO, null);
    }

    @Test
    @Order(2)
    @DisplayName("저장된 데이터 불러와 GEOJSON 변환")
    void testJdbc2GeoJson() throws Exception {
        String expectedGeoJson = Files.readString(Path.of(new ClassPathResource("충청남도.geojson").getURI()));

        String result = inoGeometryService.jdbc2geojson(postGisDataStoreVO, TEST_TABLE_NAME, null, null);

        assertEquals(expectedGeoJson, result);
    }
//
    @Test
    @Order(3)
    @DisplayName("저장된 데이터 불러와 GEOJSON 변환 (좌표 변환)")
    void testJdbc2GeoJsonEpsg() throws Exception {
        String expectedGeoJson = Files.readString(Path.of(new ClassPathResource("충청남도_좌표.geojson").getURI()));

        String result = inoGeometryService.jdbc2geojson(postGisDataStoreVO, TEST_TABLE_NAME, null, CRS.decode("EPSG:5174"));

        assertEquals(expectedGeoJson, result);
    }

    @Test
    @Order(4)
    @DisplayName("저장된 데이터 불러와 GEOJSON 변환 (쿼리 사용)")
    void testJdbc2GeoJsonQuery() throws IOException {
        String expectedGeoJson = Files.readString(Path.of(new ClassPathResource("충청남도_쿼리.geojson").getURI()));

        String result = inoGeometryService.jdbc2geojson(postGisDataStoreVO, TEST_TABLE_NAME, "\"구분\" = '각종학교'", null);

        assertEquals(expectedGeoJson, result);
    }

    @Test
    @Order(5)
    void testJdbc2shp() throws Exception {
        inoGeometryService.jdbc2shp(postGisDataStoreVO, outShapeFileDataStoreVO, TEST_TABLE_NAME, null);
    }

    @Test
    @Order(6)
    void clear() throws Exception {
        inoGeometryService.jdbcDropTable(postGisDataStoreVO, TEST_TABLE_NAME);
        new File("src/test/resources/out/" + TEST_TABLE_NAME + ".shp").delete();
        new File("src/test/resources/out/" + TEST_TABLE_NAME + ".prj").delete();
        new File("src/test/resources/out/" + TEST_TABLE_NAME + ".dbf").delete();
        new File("src/test/resources/out/" + TEST_TABLE_NAME + ".shx").delete();
        new File("src/test/resources/out/" + TEST_TABLE_NAME + ".fix").delete();
    }
}
