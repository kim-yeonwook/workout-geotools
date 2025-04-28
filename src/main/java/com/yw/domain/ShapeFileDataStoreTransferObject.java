package com.yw.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Builder
@Getter
public class ShapeFileDataStoreTransferObject implements IDataStoreTransferObject {

    private static final String type = "shapefile";

    private URL url;

    @Builder.Default
    private Charset encoding = Charset.forName("EUC-KR");

    @Override
    public Map<String, Object> transferData() {
        return new HashMap<>(){{
            put(ShapefileDataStoreFactory.FILE_TYPE.key, type);
            put(ShapefileDataStoreFactory.URLP.key, url);
            put(ShapefileDataStoreFactory.DBFCHARSET.key, encoding);
        }};
    }
}
