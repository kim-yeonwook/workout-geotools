package com.yw.domain;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.AttributeType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.feature.type.GeometryType;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FeatureCollectionUtils {

    public static SimpleFeatureCollection transform(SimpleFeatureCollection featureCollection, CoordinateReferenceSystem crs) throws IOException {
        if (Objects.isNull(crs)) {
            if (Objects.isNull(CRSUtils.getEpsgCodeWithIdentifiers(featureCollection.getSchema().getCoordinateReferenceSystem()))) {
                SimpleFeatureType simpleFeatureType = copyType(featureCollection, CRSUtils.getCRS(featureCollection.getSchema()));

                return generateCollection(simpleFeatureType, featureCollection);
            } else {
                return featureCollection;
            }
        } else {
            return new ReprojectingFeatureCollection(featureCollection, CRSUtils.getCRS(featureCollection.getSchema()), crs);
        }
    }

    public static SimpleFeatureType copyType(SimpleFeatureCollection featureCollection, CoordinateReferenceSystem crs) {
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

    public static SimpleFeatureType copyTypeByChangeName(SimpleFeatureCollection featureCollection, String geometryTypeName, CoordinateReferenceSystem crs) {
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

    public static SimpleFeatureCollection generateCollection(SimpleFeatureType featureType, SimpleFeatureCollection featureCollection) {
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