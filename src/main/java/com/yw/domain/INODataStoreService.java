package com.yw.domain;

import com.yw.domain.jdbc.JDBCDataStoreService;
import com.yw.domain.shp.ShapefileDataStoreService;
import com.yw.domain.vo.IDataStoreVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.jdbc.JDBCDataStore;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class INODataStoreService {

    private final JDBCDataStoreService jdbcDataStoreService;

    private final ShapefileDataStoreService shapefileDataStoreService;

    public void jdbc2shp(IDataStoreVO jdbc, IDataStoreVO shp, String tableName, CoordinateReferenceSystem crs) throws IOException {
        jdbc2shp(jdbc, shp, tableName, null, crs);
    }

    public void jdbc2shp(IDataStoreVO jdbc, IDataStoreVO shp, String tableName, String queryString, CoordinateReferenceSystem crs) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(jdbc)) {
            JDBCDataStore jdbcDataStore = (JDBCDataStore) in.getDataStore();
            SimpleFeatureCollection simpleFeatureCollection = this.jdbcDataStoreService.getFeatureCollection(jdbcDataStore, tableName, queryString, crs);

            try (CloseableDataStore out = new CloseableDataStore(shp)) {
                ShapefileDataStore shapefileDataStore = (ShapefileDataStore) out.getDataStore();
                this.shapefileDataStoreService.createFile(shapefileDataStore, simpleFeatureCollection);
            }
        }
    }

    public String jdbc2geojson(IDataStoreVO jdbc, String tableName, CoordinateReferenceSystem crs) throws IOException {
        return jdbc2geojson(jdbc, tableName, null, crs);
    }

    public String jdbc2geojson(IDataStoreVO jdbc, String tableName, String queryString, CoordinateReferenceSystem crs) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(jdbc)) {
            JDBCDataStore jdbcDataStore = (JDBCDataStore) in.getDataStore();
            SimpleFeatureCollection featureCollection = this.jdbcDataStoreService.getFeatureCollection(jdbcDataStore, tableName, queryString, crs);
            return createGeoJson(featureCollection);
        }
    }

    public void jdbcDropTable(IDataStoreVO jdbc, String tableName) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(jdbc)) {
            this.jdbcDataStoreService.removeSchema(in.getDataStore(), tableName);
        }
    }

    public void shp2jdbc(IDataStoreVO jdbc, IDataStoreVO shp, CoordinateReferenceSystem crs) throws IOException {
        shp2jdbc(jdbc, shp, null, crs);
    }

    public void shp2jdbc(IDataStoreVO jdbc, IDataStoreVO shp, String queryString, CoordinateReferenceSystem crs) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(jdbc)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) in.getDataStore();
            SimpleFeatureCollection featureCollection = this.shapefileDataStoreService.getFeatureCollection(shapefileDataStore, queryString, crs);

            try (CloseableDataStore out = new CloseableDataStore(shp)) {
                JDBCDataStore jdbcDataStore = (JDBCDataStore) out.getDataStore();

                this.jdbcDataStoreService.createTable(jdbcDataStore, featureCollection);
            }
        }
    }

    public String shp2geojson(IDataStoreVO shp, CoordinateReferenceSystem crs) throws IOException {
        return shp2geojson(shp, null, crs);
    }

    public String shp2geojson(IDataStoreVO shp, String queryString, CoordinateReferenceSystem crs) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(shp)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) in.getDataStore();
            SimpleFeatureCollection featureCollection = this.shapefileDataStoreService.getFeatureCollection(shapefileDataStore, queryString, crs);

            return createGeoJson(featureCollection);
        }
    }

    public CoordinateReferenceSystem shp2schema(IDataStoreVO shp) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(shp)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) in.getDataStore();
            SimpleFeatureSource shapefileFeatureSource = shapefileDataStore.getFeatureSource();

            return DataStoreUtils.getSchemaCoordinateReferenceSystem(shapefileFeatureSource.getSchema());
        }
    }

    public List<String> shp2wkt(IDataStoreVO shp, CoordinateReferenceSystem crs) throws IOException {
        return shp2wkt(shp, null, crs);
    }

    public List<String> shp2wkt(IDataStoreVO shp, String queryString, CoordinateReferenceSystem crs) throws IOException {
        try (CloseableDataStore in = new CloseableDataStore(shp)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) in.getDataStore();
            SimpleFeatureCollection featureCollection = this.shapefileDataStoreService.getFeatureCollection(shapefileDataStore, queryString, crs);

            return createWKT(featureCollection);
        }
    }

    public void geojson2shp(String geoJson, IDataStoreVO shp, CoordinateReferenceSystem crs) throws IOException {
        GeometryJSON geometryJSON = new GeometryJSON(15);
        FeatureJSON featureJSON = new FeatureJSON(geometryJSON);

        SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) featureJSON.readFeatureCollection(geoJson);
        if (!Objects.isNull(crs)) {
            featureCollection = DataStoreUtils.transformFeatureCollection(featureCollection, crs);
        }

        try (CloseableDataStore out = new CloseableDataStore(shp)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) out.getDataStore();
            this.shapefileDataStoreService.createFile(shapefileDataStore, featureCollection);
        }
    }

    public void collection2shp(SimpleFeatureCollection featureCollection, IDataStoreVO shp, CoordinateReferenceSystem crs) throws IOException {
        SimpleFeatureCollection transformFeatureCollection = DataStoreUtils.transformFeatureCollection(featureCollection, crs);

        try (CloseableDataStore out = new CloseableDataStore(shp)) {
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) out.getDataStore();
            this.shapefileDataStoreService.createFile(shapefileDataStore, transformFeatureCollection);
        }
    }

    public void geojson2jdbc(String geoJson, IDataStoreVO jdbc) throws IOException {

    }

    private List<String> createWKT(SimpleFeatureCollection featureCollection) throws IOException {
        List<String> list = new ArrayList<>();
        try(FeatureIterator<SimpleFeature> features = featureCollection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();

                Geometry defaultGeometry = (Geometry) feature.getDefaultGeometry();
                list.add(defaultGeometry.toString());
            }
        }
        return list;
    }

    private String createGeoJson(SimpleFeatureCollection featureCollection) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            GeometryJSON geometryJSON = new GeometryJSON(16);
            FeatureJSON featureJSON = new FeatureJSON(geometryJSON);

            featureJSON.writeFeatureCollection(featureCollection, writer);

            return writer.toString();
        }
    }
}
