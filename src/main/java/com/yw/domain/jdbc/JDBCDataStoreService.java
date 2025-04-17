package com.yw.domain.jdbc;

import com.yw.domain.DataStoreService;
import com.yw.domain.DataStoreUtils;
import com.yw.infrastructure.exception.InternalServerException;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.jdbc.JDBCDataStore;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JDBCDataStoreService extends DataStoreService {

    private static final String DEFAULT_GEOMETRY_COLUMN = "geom";

    public void createTable(JDBCDataStore dataStore, SimpleFeatureCollection featureCollection) throws IOException {
        SimpleFeatureType featureType = DataStoreUtils.copyFeatureTypeByChangeName(featureCollection, DEFAULT_GEOMETRY_COLUMN, null);
        dataStore.createSchema(featureType);

        addFeatureCollection(dataStore.getFeatureSource(featureType.getTypeName()), DataStoreUtils.copyFeatureCollectionByReTypeSchema(featureType, featureCollection));
    }

    public SimpleFeatureCollection getFeatureCollection(JDBCDataStore dataStore, String tableName, String queryString, CoordinateReferenceSystem crs) {
        try {
            SimpleFeatureCollection featureCollection = dataStore.getFeatureSource(tableName).getFeatures(createCQLQuery(queryString));

            return DataStoreUtils.transformFeatureCollection(featureCollection, crs);
        } catch (IOException ioe) {
            throw new InternalServerException("featureCollection 생성 실패. ", ioe);
        }
    }
}
