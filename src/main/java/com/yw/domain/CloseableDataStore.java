package com.yw.domain;

import com.yw.domain.vo.IDataStoreVO;
import com.yw.infrastructure.exception.InternalServerException;
import lombok.Getter;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

@Getter
public class CloseableDataStore implements Closeable {

    private final DataStore dataStore;

    public CloseableDataStore(IDataStoreVO vo) {
        try {
            if (Objects.isNull(vo)) {
                throw new InternalServerException("데이터 스토어 속성 데이터 NULL");
            }

            this.dataStore = DataStoreFinder.getDataStore(vo.toMap());

            if (Objects.isNull(this.dataStore)) {
                throw new InternalServerException("데이터 스토어 생성 실패");
            }

        } catch (IOException ioe) {
            throw new InternalServerException("데이터 스토어와의 연결 에러, ", ioe);
        }
    }

    @Override
    public void close() throws IOException {
        this.dataStore.dispose();
    }
}