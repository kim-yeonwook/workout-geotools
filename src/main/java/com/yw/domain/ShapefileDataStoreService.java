package com.yw.domain;

import com.yw.infrastructure.exception.InternalServerException;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ShapefileDataStoreService extends DataStoreService {

    private static final String DEFAULT_GEOMETRY_COLUMN = "the_geom";

    public void createFile(ShapefileDataStore dataStore, SimpleFeatureCollection featureCollection) throws IOException {
        SimpleFeatureType featureType = FeatureCollectionUtils.copyTypeByChangeName(featureCollection, DEFAULT_GEOMETRY_COLUMN, null);
        dataStore.createSchema(featureType);

        addFeatureCollection(dataStore.getFeatureSource(), FeatureCollectionUtils.generateCollection(featureType, featureCollection));
    }

    public SimpleFeatureCollection getFeatureCollection(ShapefileDataStore dataStore, String queryString, CoordinateReferenceSystem crs) {
        try {
            SimpleFeatureCollection featureCollection = dataStore.getFeatureSource().getFeatures(createCQLQuery(queryString));

            return FeatureCollectionUtils.transform(featureCollection, crs);
        } catch (IOException ioe) {
            throw new InternalServerException("", ioe);
        }
    }
}