package com.yw.domain;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.AttributeType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.ReferenceIdentifier;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.*;

public class DataStoreUtils {

    public static CoordinateReferenceSystem getSchemaCoordinateReferenceSystem(SimpleFeatureType featureType) {
        CoordinateReferenceSystem crs = Optional.ofNullable(featureType.getCoordinateReferenceSystem()).orElse(DefaultGeographicCRS.WGS84);
        try {
            String code = getEpsgCodeWithIdentifiers(crs);
            if (Objects.isNull(code)) {

                code = CRSNameToAuthorityResolver.get(crs.getName().getCode());
                if (Objects.isNull(code)) {
                    return crs;
                } else {
                    return CRS.decode(code);
                }
            } else {
                return CRS.decode(code);
            }
        } catch (FactoryException e) {
            return crs;
        }
    }

    public static String getEpsgCodeWithIdentifiers(CoordinateReferenceSystem crs) {
        Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();

        if (!ObjectUtils.isEmpty(identifiers)) {
            for (ReferenceIdentifier identifier : identifiers) {
                if (identifier.getCodeSpace().equalsIgnoreCase("EPSG")) {
                    return identifier.getCodeSpace() + ":" + identifier.getCode();
                }
            }
        }

        return null;
    }

    public static SimpleFeatureCollection transformFeatureCollection(SimpleFeatureCollection featureCollection, CoordinateReferenceSystem crs) throws IOException {
        if (Objects.isNull(crs)) {
            if (Objects.isNull(getEpsgCodeWithIdentifiers(featureCollection.getSchema().getCoordinateReferenceSystem()))) {
                SimpleFeatureType simpleFeatureType = copyFeatureType(featureCollection, getSchemaCoordinateReferenceSystem(featureCollection.getSchema()));

                return copyFeatureCollectionByReTypeSchema(simpleFeatureType, featureCollection);
            } else {
                return featureCollection;
            }
        } else {
            return new ReprojectingFeatureCollection(featureCollection, getSchemaCoordinateReferenceSystem(featureCollection.getSchema()), crs);
        }
    }

    public static SimpleFeatureType copyFeatureType(SimpleFeatureCollection featureCollection, CoordinateReferenceSystem crs) {
        SimpleFeatureType schema = featureCollection.getSchema();
        GeometryDescriptor geom = schema.getGeometryDescriptor();

        GeometryType geometryType = null;
        List<AttributeDescriptor> attributeDescriptorList = new ArrayList<>();
        for (AttributeDescriptor attributeDescriptor : schema.getAttributeDescriptors()) {
            AttributeType type = attributeDescriptor.getType();
            if (type instanceof GeometryType) {
                geometryType = (GeometryType) type;
            } else {
                attributeDescriptorList.add(attributeDescriptor);
            }
        }

        GeometryTypeImpl geometryTypeImpl = new GeometryTypeImpl(
                geometryType.getName(),
                Objects.requireNonNull(geometryType).getBinding(),
                Optional.ofNullable(crs)
                        .orElse(schema.getCoordinateReferenceSystem()),
                geometryType.isIdentified(),
                geometryType.isAbstract(),
                geometryType.getRestrictions(),
                geometryType.getSuper(),
                geometryType.getDescription());

        GeometryDescriptor geomDesc = new GeometryDescriptorImpl(
                geometryTypeImpl,
                geometryType.getName(),
                geom.getMinOccurs(),
                geom.getMaxOccurs(),
                geom.isNillable(),
                geom.getDefaultValue());
        attributeDescriptorList.add(0, geomDesc);

        return new SimpleFeatureTypeImpl(schema.getName(), attributeDescriptorList, geomDesc, schema.isAbstract(),
                schema.getRestrictions(), schema.getSuper(), schema.getDescription());
    }

    /**
     * geometry name을 바꾸는 이유
     * -
     * shape 파일은 스키마 생성시 기본 the_geom으로 설정 하지만 받아올 featureCollection의 geometry name이 the_geom이 아닌 경우 존재
     * 이때 name을 변경하지 않고 featureCollection을 add하면 데이터가 들어가지 않음 (속성명으로 파싱)
     * 그렇기 때문에 shape 파일을 만들때는 typeName을 필수적으로 바꿔줘야함, jdbc는 필수는 아님..
     */
    public static SimpleFeatureType copyFeatureTypeByChangeName(SimpleFeatureCollection featureCollection, String geometryTypeName, CoordinateReferenceSystem crs) {
        SimpleFeatureType schema = featureCollection.getSchema();
        GeometryDescriptor geom = schema.getGeometryDescriptor();

        GeometryType geometryType = null;
        List<AttributeDescriptor> attributeDescriptorList = new ArrayList<>();
        for (AttributeDescriptor attributeDescriptor : schema.getAttributeDescriptors()) {
            AttributeType type = attributeDescriptor.getType();
            if (type instanceof GeometryType) {
                geometryType = (GeometryType) type;
            } else {
                attributeDescriptorList.add(attributeDescriptor);
            }
        }

        GeometryTypeImpl geometryTypeImpl = new GeometryTypeImpl(
                new NameImpl(geometryTypeName),
                Objects.requireNonNull(geometryType).getBinding(),
                Optional.ofNullable(crs)
                        .orElse(schema.getCoordinateReferenceSystem()),
                geometryType.isIdentified(),
                geometryType.isAbstract(),
                geometryType.getRestrictions(),
                geometryType.getSuper(),
                geometryType.getDescription());

        GeometryDescriptor geomDesc = new GeometryDescriptorImpl(
                geometryTypeImpl,
                new NameImpl(geometryTypeName),
                geom.getMinOccurs(),
                geom.getMaxOccurs(),
                geom.isNillable(),
                geom.getDefaultValue());
        attributeDescriptorList.add(0, geomDesc);

        return new SimpleFeatureTypeImpl(schema.getName(), attributeDescriptorList, geomDesc, schema.isAbstract(),
                schema.getRestrictions(), schema.getSuper(), schema.getDescription());
    }

    public static SimpleFeatureCollection copyFeatureCollectionByReTypeSchema(SimpleFeatureType featureType, SimpleFeatureCollection featureCollection) {
        List<SimpleFeature> featureList = new ArrayList<>();

        try (FeatureIterator<SimpleFeature> featureIterator = featureCollection.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                SimpleFeature newFeature = DataUtilities.reType(featureType, feature, true);

                newFeature.setAttribute(featureType.getGeometryDescriptor().getName(),
                        feature.getAttribute(featureCollection.getSchema().getGeometryDescriptor().getName()));

                featureList.add(newFeature);
            }
        }

        return new ListFeatureCollection(featureType, featureList);
    }
}
