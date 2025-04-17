package com.yw.domain;

import com.yw.infrastructure.exception.InternalServerException;
import org.geotools.api.data.*;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;

import java.io.IOException;
import java.util.Objects;

public class DataStoreService {

    public void removeSchema(DataStore dataStore, String typeName) throws IOException {
        dataStore.removeSchema(typeName);
    }

    public SimpleFeatureType getSchema(DataStore dataStore, String typeName) throws IOException {
        return dataStore.getSchema(typeName);
    }

    protected Query createCQLQuery(String queryString) {
        try {
            Query query = new Query();
            if (!Objects.isNull(queryString)) {
                query.setFilter(CQL.toFilter(queryString));
            } else {
                query.setFilter(ECQL.toFilter("1=1"));
            }

            return query;
        } catch (CQLException cqle) {
            throw new InternalServerException("쿼리 생성 실패. ", cqle);
        }
    }

    protected void addFeatureCollection(SimpleFeatureSource featureSource, SimpleFeatureCollection featureCollection) throws IOException {
        if (featureSource instanceof FeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            Transaction transaction = new DefaultTransaction("create");
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(featureCollection);
                transaction.commit();

            } catch (IOException e) {
                transaction.rollback();
                throw e;
            } catch (Exception e) {
                transaction.rollback();
                throw new InternalServerException("정의 되지 않은 에러", e);
            } finally {
                transaction.close();
            }
        } else {
            throw new InternalServerException("featureSource is not FeatureStore");
        }
    }
}
