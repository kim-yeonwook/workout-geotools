package com.yw.domain;

import com.yw.infrastructure.exception.InternalServerException;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.ReferenceIdentifier;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CRSUtils {

    private static final String EPSG = "EPSG";

    public static CoordinateReferenceSystem findCrsByEpsg(int srid) {
        return findCrsByEpsg(EPSG + ":" + srid);
    }

    public static CoordinateReferenceSystem findCrsByEpsg(String epsg) {
        try {
            return CRS.decode(epsg);
        } catch (FactoryException fe) {
            throw new InternalServerException("좌표계를 찾을수 없습니다. CODE : " + epsg, fe);
        }
    }

    public static CoordinateReferenceSystem getCRS(SimpleFeatureSource source) {
        return getCRS(source.getSchema());
    }

    public static CoordinateReferenceSystem getCRS(SimpleFeatureCollection collection) {
        return getCRS(collection.getSchema());
    }

    public static CoordinateReferenceSystem getCRS(SimpleFeatureType featureType) {
        CoordinateReferenceSystem crs = Optional.ofNullable(featureType.getCoordinateReferenceSystem())
                .orElse(DefaultGeographicCRS.WGS84);

        String code = getEpsgCodeWithIdentifiers(crs);
        if (Objects.isNull(code)) {
            code = AuthorityResolver.get(crs.getName().getCode());
            if (Objects.isNull(code)) {
                return crs;
            } else {
                return findCrsByEpsg(code);
            }

        } else {
            return findCrsByEpsg(code);
        }
    }

    public static String getEpsgCodeWithIdentifiers(CoordinateReferenceSystem crs) {
        Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();

        if (!ObjectUtils.isEmpty(identifiers)) {
            for (ReferenceIdentifier identifier : identifiers) {
                if (identifier.getCodeSpace().equalsIgnoreCase(EPSG)) {
                    return identifier.getCodeSpace() + ":" + identifier.getCode();
                }
            }
        }
        return null;
    }
}
